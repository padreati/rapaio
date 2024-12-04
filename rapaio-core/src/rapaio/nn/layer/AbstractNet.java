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

import java.io.IOException;
import java.util.List;

import rapaio.io.atom.AtomInputStream;
import rapaio.io.atom.AtomOutputStream;
import rapaio.narray.DType;
import rapaio.narray.NArray;
import rapaio.narray.Order;
import rapaio.narray.Shape;
import rapaio.narray.iterators.PointerIterator;
import rapaio.nn.Net;
import rapaio.nn.NetState;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public abstract class AbstractNet implements Net {

    protected final TensorManager tm;
    protected boolean train = false;

    public AbstractNet(TensorManager tm) {
        this.tm = tm;
    }

    @Override
    public TensorManager tm() {
        return tm;
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
    public void saveState(AtomOutputStream out) throws IOException {
        NetState state = state();
        List<? extends NArray<?>> values = state.tensors().stream().map(Tensor::value).toList();
        for (NArray<?> value : values) {
            out.saveString(value.dtype().id().name());
            out.saveAtom(value.shape());
            switch (value.dtype().id()) {
                case DOUBLE -> saveDoubleArray(out, value);
                case FLOAT -> saveFloatArray(out, value);
                case INTEGER -> saveIntArray(out, value);
                case BYTE -> saveByteArray(out, value);
            }
        }
    }

    private void saveByteArray(AtomOutputStream out, NArray<?> value) throws IOException {
        byte[] array = new byte[value.size()];
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            array[p++] = value.ptrGetByte(it.next());
        }
        out.saveBytes(array);
    }

    private void saveIntArray(AtomOutputStream out, NArray<?> value) throws IOException {
        int[] array = new int[value.size()];
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            array[p++] = value.ptrGetInt(it.next());
        }
        out.saveInts(array);
    }

    private void saveFloatArray(AtomOutputStream out, NArray<?> value) throws IOException {
        float[] array = new float[value.size()];
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            array[p++] = value.ptrGetFloat(it.next());
        }
        out.saveFloats(array);
    }

    private void saveDoubleArray(AtomOutputStream out, NArray<?> value) throws IOException {
        double[] array = new double[value.size()];
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            array[p++] = value.ptrGetDouble(it.next());
        }
        out.saveDoubles(array);
    }

    @Override
    public void loadState(AtomInputStream in) throws IOException {
        NetState state = state();
        List<? extends NArray<?>> values = state.tensors().stream().map(Tensor::value).toList();
        for (NArray<?> value : values) {
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

    private void readeByteArray(AtomInputStream in, NArray<?> value) throws IOException {
        byte[] array = in.readBytes();
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            value.ptrSetInt(it.next(), array[p++]);
        }
    }

    private void readeIntArray(AtomInputStream in, NArray<?> value) throws IOException {
        int[] array = in.readInts();
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            value.ptrSetInt(it.next(), array[p++]);
        }
    }

    private void readeFloatArray(AtomInputStream in, NArray<?> value) throws IOException {
        float[] array = in.readFloats();
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            value.ptrSetFloat(it.next(), array[p++]);
        }
    }

    private void readeDoubleArray(AtomInputStream in, NArray<?> value) throws IOException {
        double[] array = in.readDoubles();
        PointerIterator it = value.ptrIterator(Order.C);
        int p = 0;
        while (it.hasNext()) {
            value.ptrSetDouble(it.next(), array[p++]);
        }
    }
}
