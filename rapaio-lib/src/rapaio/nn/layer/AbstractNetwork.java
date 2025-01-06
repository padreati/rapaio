/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.nn.layer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import rapaio.darray.DArray;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.darray.iterators.PointerIterator;
import rapaio.io.atom.AtomInputStream;
import rapaio.io.atom.AtomOutputStream;
import rapaio.io.atom.BinaryAtomProtocol;
import rapaio.nn.Network;
import rapaio.nn.NetworkState;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;
import rapaio.datasets.Batch;
import rapaio.datasets.TabularDataset;

public abstract class AbstractNetwork implements Network {

    protected final TensorManager tm;
    protected boolean train = false;

    public AbstractNetwork(TensorManager tm) {
        this.tm = tm;
    }

    @Override
    public TensorManager tm() {
        return tm;
    }

    /**
     * Default implementation which uses reflection to search recursively in class hierarchy
     * for finding field members which are layers and collects their parameters.
     * <p>
     * This default implementation might not be what you actually want, a custom implementation
     * might be needed.
     *
     * @return a collection of all parameters from all layers defined as fields in class hierarchy
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<Tensor> parameters() {
        Set<Tensor> set = new HashSet<>();
        ArrayList<Tensor> params = new ArrayList<>();
        Class<?> clazz = getClass();
        while (clazz != null) {
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (Network.class.isAssignableFrom(field.getType())) {
                    try {
                        Method method = field.getType().getMethod("parameters");
                        field.setAccessible(true);
                        List<Tensor> ps = (List<Tensor>) method.invoke(field.get(this));
                        for (Tensor p : ps) {
                            if (!set.contains(p)) {
                                set.add(p);
                                params.add(p);
                            }
                        }
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return new ArrayList<>(params);
    }

    /**
     * Default implementation for network state which uses reflection to search through the hierarchy of classes
     * for layer declared fields and collects their state.
     * <p>
     * This implementation might not cover your case, a custom implementation might be needed.
     *
     * @return a collection of network states made from all network states from all layers defined as fields
     */
    @Override
    public NetworkState state() {
        NetworkState state = new NetworkState();
        Class<?> clazz = getClass();
        while (clazz != null) {
            Field[] fields = this.getClass().getDeclaredFields();
            for (Field field : fields) {
                if (Network.class.isAssignableFrom(field.getType())) {
                    try {
                        Method method = field.getType().getMethod("state");
                        field.setAccessible(true);
                        state.merge((NetworkState) method.invoke(field.get(this)));
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }

        return state;
    }

    @Override
    public void train() {
        train = true;
    }

    @Override
    public void eval() {
        train = false;
    }

    @Override
    public List<Batch> batchForward(int batchSize, boolean shuffle, boolean skipLast, Tensor... inputs) {
        TabularDataset dataset = new TabularDataset(tm, inputs);
        List<CompletableFuture<Batch>> futures = new ArrayList<>();

        var batchIt = dataset.batchIterator(batchSize, shuffle, skipLast);

        while (batchIt.hasNext()) {
            var batch = batchIt.next();
            futures.add(CompletableFuture.supplyAsync(() -> {
                Tensor[] outputs = forward(batch.tensors());
                batch.withOutputs(outputs);
                return batch;
            }, tm.outerExecutor()));
        }
        try {
            return futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void saveState(AtomOutputStream out) throws IOException {
        NetworkState state = state();
        List<? extends DArray<?>> values = state.tensors().stream().map(Tensor::value).toList();
        for (DArray<?> value : values) {
            out.saveString(value.dt().id().name());
            out.saveAtom(value.shape());
            switch (value.dt().id()) {
                case DOUBLE -> saveDoubleArray(out, value);
                case FLOAT -> saveFloatArray(out, value);
                case INTEGER -> saveIntArray(out, value);
                case BYTE -> saveByteArray(out, value);
            }
        }
    }

    private void saveByteArray(AtomOutputStream out, DArray<?> value) throws IOException {
        byte[] array = new byte[value.size()];
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            array[p++] = value.ptrGetByte(it.next());
        }
        out.saveBytes(array);
    }

    private void saveIntArray(AtomOutputStream out, DArray<?> value) throws IOException {
        int[] array = new int[value.size()];
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            array[p++] = value.ptrGetInt(it.next());
        }
        out.saveInts(array);
    }

    private void saveFloatArray(AtomOutputStream out, DArray<?> value) throws IOException {
        float[] array = new float[value.size()];
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            array[p++] = value.ptrGetFloat(it.next());
        }
        out.saveFloats(array);
    }

    private void saveDoubleArray(AtomOutputStream out, DArray<?> value) throws IOException {
        double[] array = new double[value.size()];
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            array[p++] = value.ptrGetDouble(it.next());
        }
        out.saveDoubles(array);
    }

    @Override
    public void saveState(File file) throws IOException {
        try (AtomOutputStream out = BinaryAtomProtocol.outputToFile(file)) {
            saveState(out);
        }
    }

    @Override
    public void saveState(OutputStream out) throws IOException {
        try (AtomOutputStream outputStream = BinaryAtomProtocol.outputToStream(out)) {
            saveState(outputStream);
        }
    }

    @Override
    public void loadState(AtomInputStream in) throws IOException {
        NetworkState state = state();
        List<? extends DArray<?>> values = state.tensors().stream().map(Tensor::value).toList();
        for (DArray<?> value : values) {
            DType dt = DType.fromId(in.readString());
            Shape shape = in.loadAtom(Shape.class);
            switch (dt.id()) {
                case DOUBLE -> readeDoubleArray(in, value);
                case FLOAT -> readeFloatArray(in, value);
                case INTEGER -> readeIntArray(in, value);
                case BYTE -> readeByteArray(in, value);
            }
        }
    }

    @Override
    public void loadState(File file) throws IOException {
        try (AtomInputStream in = BinaryAtomProtocol.inputFromFile(file)) {
            loadState(in);
        }
    }

    @Override
    public void loadState(InputStream in) throws IOException {
        try (AtomInputStream inputStream = BinaryAtomProtocol.inputFromStream(in)) {
            loadState(inputStream);
        }
    }

    private void readeByteArray(AtomInputStream in, DArray<?> value) throws IOException {
        byte[] array = in.readBytes();
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            value.ptrSetInt(it.next(), array[p++]);
        }
    }

    private void readeIntArray(AtomInputStream in, DArray<?> value) throws IOException {
        int[] array = in.readInts();
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            value.ptrSetInt(it.next(), array[p++]);
        }
    }

    private void readeFloatArray(AtomInputStream in, DArray<?> value) throws IOException {
        float[] array = in.readFloats();
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            value.ptrSetFloat(it.next(), array[p++]);
        }
    }

    private void readeDoubleArray(AtomInputStream in, DArray<?> value) throws IOException {
        double[] array = in.readDoubles();
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            value.ptrSetDouble(it.next(), array[p++]);
        }
    }
}
