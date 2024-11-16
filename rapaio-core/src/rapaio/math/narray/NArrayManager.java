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

package rapaio.math.narray;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import rapaio.core.distributions.Distribution;
import rapaio.math.narray.layout.StrideLayout;
import rapaio.math.narray.manager.base.BaseNArrayManager;
import rapaio.util.Hardware;

public abstract class NArrayManager {

    public static NArrayManager base() {
        return new BaseNArrayManager(Hardware.CORES);
    }

    public static NArrayManager base(int cpuThreads) {
        return new BaseNArrayManager(cpuThreads);
    }

    protected final int cpuThreads;
    protected final OfType<Byte> ofByte;
    protected final OfType<Integer> ofInt;
    protected final OfType<Float> ofFloat;
    protected final OfType<Double> ofDouble;
    protected final StorageManager storageManager;

    protected NArrayManager(int cpuThreads,
            OfType<Byte> ofByte,
            OfType<Integer> ofInt,
            OfType<Float> ofFloat,
            OfType<Double> ofDouble,
            StorageManager storageManager) {
        this.cpuThreads = cpuThreads;

        this.ofDouble = ofDouble;
        this.ofFloat = ofFloat;
        this.ofInt = ofInt;
        this.ofByte = ofByte;
        this.storageManager = storageManager;

        this.ofByte.registerParent(this, storageManager.ofType(DType.BYTE));
        this.ofInt.registerParent(this, storageManager.ofType(DType.INTEGER));
        this.ofFloat.registerParent(this, storageManager.ofType(DType.FLOAT));
        this.ofDouble.registerParent(this, storageManager.ofType(DType.DOUBLE));
    }

    public final OfType<Byte> ofByte() {
        return ofByte;
    }

    public final OfType<Integer> ofInt() {
        return ofInt;
    }

    public final OfType<Float> ofFloat() {
        return ofFloat;
    }

    public final OfType<Double> ofDouble() {
        return ofDouble;
    }

    @SuppressWarnings("unchecked")
    public final <N extends Number> OfType<N> ofType(DType<N> dtype) {
        return (OfType<N>) switch (dtype.id()) {
            case BYTE -> ofByte();
            case INTEGER -> ofInt();
            case FLOAT -> ofFloat();
            case DOUBLE -> ofDouble();
        };
    }

    public final int cpuThreads() {
        return cpuThreads;
    }

    public final <N extends Number> NArray<N> scalar(DType<N> dType, N value) {
        return ofType(dType).scalar(value);
    }

    public final <N extends Number> NArray<N> scalar(DType<N> dType, byte value) {
        return ofType(dType).scalar(value);
    }

    public final <N extends Number> NArray<N> scalar(DType<N> dType, int value) {
        return ofType(dType).scalar(value);
    }

    public final <N extends Number> NArray<N> scalar(DType<N> dType, float value) {
        return ofType(dType).scalar(value);
    }

    public final <N extends Number> NArray<N> scalar(DType<N> dType, double value) {
        return ofType(dType).scalar(value);
    }

    public final <N extends Number> NArray<N> zeros(DType<N> dType, Shape shape) {
        return ofType(dType).zeros(shape, Order.defaultOrder());
    }

    public final <N extends Number> NArray<N> zeros(DType<N> dType, Shape shape, Order order) {
        return ofType(dType).zeros(shape, order);
    }

    public final <N extends Number> NArray<N> eye(DType<N> dType, int n) {
        return ofType(dType).eye(n, Order.defaultOrder());
    }

    public final <N extends Number> NArray<N> eye(DType<N> dType, int n, Order order) {
        return ofType(dType).eye(n, order);
    }

    public final <N extends Number> NArray<N> full(DType<N> dType, Shape shape, N value) {
        return ofType(dType).full(shape, value, Order.defaultOrder());
    }

    public final <N extends Number> NArray<N> full(DType<N> dType, Shape shape, N value, Order order) {
        return ofType(dType).full(shape, value, order);
    }

    public final <N extends Number> NArray<N> seq(DType<N> dType, Shape shape) {
        return ofType(dType).seq(shape, Order.defaultOrder());
    }

    public final <N extends Number> NArray<N> seq(DType<N> dType, Shape shape, Order order) {
        return ofType(dType).seq(shape, order);
    }

    public final <N extends Number> NArray<N> random(DType<N> dType, Shape shape, Random random) {
        return ofType(dType).random(shape, random, Order.defaultOrder());
    }

    public final <N extends Number> NArray<N> random(DType<N> dType, Shape shape, Random random, Order order) {
        return ofType(dType).random(shape, random, order);
    }


    public final <N extends Number> NArray<N> stride(DType<N> dType, Shape shape, Order order, Storage<N> storage) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), storage);
    }

    public final <N extends Number> NArray<N> stride(DType<N> dType, Shape shape, Order order, byte[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), storageManager.ofType(dType).from(array));
    }

    public final <N extends Number> NArray<N> stride(DType<N> dType, Shape shape, Order order, int[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), storageManager.ofType(dType).from(array));
    }

    public final <N extends Number> NArray<N> stride(DType<N> dType, Shape shape, Order order, float[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), storageManager.ofType(dType).from(array));
    }

    public final <N extends Number> NArray<N> stride(DType<N> dType, Shape shape, Order order, double[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), storageManager.ofType(dType).from(array));
    }

    public final <N extends Number> NArray<N> stride(DType<N> dType, StrideLayout layout, Storage<N> storage) {
        return ofType(dType).stride(layout, storage);
    }

    /**
     * Concatenates multiple NArrays along a new axis.
     * All NArrays must have the same shape. The position of the new axis is between 0 (inclusive)
     * and the number of dimensions (inclusive).
     * <p>
     * The new NArray will have an additional dimension, which is the dimension created during stacking and
     * will have size equal with the number of the stacked arrays.
     * <p>
     * The new NArray with be stored with default order.
     *
     * @param axis    index of the new dimension
     * @param nArrays NArrays to concatenate
     * @return new NArray with concatenated data
     */
    public final <N extends Number> NArray<N> stack(int axis, Collection<? extends NArray<N>> nArrays) {
        return stack(Order.defaultOrder(), axis, nArrays);
    }

    public final <N extends Number> NArray<N> stack(Order order, int axis, Collection<? extends NArray<N>> nArrays) {
        var nArrayList = nArrays.stream().toList();
        for (int i = 1; i < nArrayList.size(); i++) {
            if (!nArrayList.get(i - 1).shape().equals(nArrayList.get(i).shape())) {
                throw new IllegalArgumentException("NArrays are not valid for stack, they have to have the same dimensions.");
            }
        }
        int[] newDims = new int[nArrayList.getFirst().rank() + 1];
        int i = 0;
        for (; i < axis; i++) {
            newDims[i] = nArrayList.getFirst().shape().dim(i);
        }
        for (; i < nArrayList.getFirst().rank(); i++) {
            newDims[i + 1] = nArrayList.getFirst().shape().dim(i);
        }
        newDims[axis] = nArrayList.size();
        var result = ofType(nArrayList.getFirst().dtype()).zeros(Shape.of(newDims), order);
        var slices = result.chunk(axis, true, 1);
        i = 0;
        for (; i < nArrayList.size(); i++) {
            var it1 = slices.get(i).squeeze(axis).ptrIterator(Order.defaultOrder());
            var it2 = nArrayList.get(i).ptrIterator(Order.defaultOrder());
            while (it1.hasNext() && it2.hasNext()) {
                slices.get(i).ptrSet(it1.nextInt(), nArrayList.get(i).ptrGet(it2.nextInt()));
            }
        }
        return result;
    }

    public final <N extends Number> NArray<N> concat(int axis, Collection<? extends NArray<N>> nArrays) {
        return concat(Order.defaultOrder(), axis, nArrays);
    }

    public final <N extends Number> NArray<N> concat(Order order, int axis, Collection<? extends NArray<N>> nArrays) {
        var nArrayList = nArrays.stream().toList();
        NArrayManager.validateForConcatenation(axis, nArrayList.stream().map(t -> t.shape().dims()).collect(Collectors.toList()));

        int newDim = nArrayList.stream().mapToInt(nArray -> nArray.layout().shape().dim(axis)).sum();
        NArray<N> first = nArrayList.getFirst();
        int[] newDims = Arrays.copyOf(first.shape().dims(), first.rank());
        newDims[axis] = newDim;
        var result = ofType(first.dtype()).zeros(Shape.of(newDims), order);

        int start = 0;
        for (NArray<N> array : nArrays) {
            int end = start + array.shape().dim(axis);
            var dst = result.narrow(axis, true, start, end);

            var it1 = array.ptrIterator(Order.defaultOrder());
            var it2 = dst.ptrIterator(Order.defaultOrder());

            while (it1.hasNext() && it2.hasNext()) {
                dst.ptrSet(it2.nextInt(), array.ptrGet(it1.nextInt()));
            }
            start = end;
        }
        return result;
    }

    protected static void validateForConcatenation(int axis, List<int[]> dims) {
        for (int i = 1; i < dims.size(); i++) {
            int[] dimsPrev = dims.get(i - 1);
            int[] dimsNext = dims.get(i);
            for (int j = 0; j < dimsPrev.length; j++) {
                if (j != axis && dimsNext[j] != dimsPrev[j]) {
                    throw new IllegalArgumentException("NArrays are not valid for concatenation");
                }
            }
        }
    }

    public static abstract class OfType<N extends Number> {

        protected final DType<N> dType;
        protected NArrayManager parent;
        protected StorageManager.OfType<N> storageOfType;

        public OfType(DType<N> dType) {
            this.dType = dType;
        }

        public final void registerParent(NArrayManager parent, StorageManager.OfType<N> storageOfType) {
            if (this.parent != null) {
                throw new IllegalArgumentException("AbstractEngineOfType has already a registered parent.");
            }
            this.parent = parent;
            this.storageOfType = storageOfType;
        }

        public final DType<N> dtype() {
            return dType;
        }

        public final StorageManager.OfType<N> storage() {
            return storageOfType;
        }

        public final NArray<N> scalar(N value) {
            return stride(StrideLayout.of(Shape.of(), 0, new int[0]), storage().scalar(value));
        }

        public final NArray<N> scalar(byte value) {
            return stride(StrideLayout.of(Shape.of(), 0, new int[0]), storage().scalar(value));
        }

        public final NArray<N> scalar(int value) {
            return stride(StrideLayout.of(Shape.of(), 0, new int[0]), storage().scalar(value));
        }

        public final NArray<N> scalar(float value) {
            return stride(StrideLayout.of(Shape.of(), 0, new int[0]), storage().scalar(value));
        }

        public final NArray<N> scalar(double value) {
            return stride(StrideLayout.of(Shape.of(), 0, new int[0]), storage().scalar(value));
        }

        public final NArray<N> zeros(Shape shape) {
            return zeros(shape, Order.defaultOrder());
        }

        public final NArray<N> zeros(Shape shape, Order order) {
            return stride(shape, Order.autoFC(order), storage().zeros(shape.size()));
        }

        public final NArray<N> eye(int n) {
            return eye(n, Order.defaultOrder());
        }

        public final NArray<N> eye(int n, Order order) {
            var eye = zeros(Shape.of(n, n), order);
            var v = dType.castValue(1);
            for (int i = 0; i < n; i++) {
                eye.set(v, i, i);
            }
            return eye;
        }

        public final NArray<N> full(Shape shape, N value) {
            return full(shape, value, Order.defaultOrder());
        }

        public final NArray<N> full(Shape shape, byte value) {
            return full(shape, value, Order.defaultOrder());
        }

        public final NArray<N> full(Shape shape, int value) {
            return full(shape, value, Order.defaultOrder());
        }

        public final NArray<N> full(Shape shape, float value) {
            return full(shape, value, Order.defaultOrder());
        }

        public final NArray<N> full(Shape shape, double value) {
            return full(shape, value, Order.defaultOrder());
        }

        public final NArray<N> full(Shape shape, N value, Order order) {
            var storage = storage().zeros(shape.size());
            storage.fill(value, 0, shape.size());
            return stride(shape, Order.autoFC(order), storage);
        }

        public final NArray<N> full(Shape shape, byte value, Order order) {
            var storage = storage().zeros(shape.size());
            storage.fill(dtype().castValue(value), 0, shape.size());
            return stride(shape, Order.autoFC(order), storage);
        }

        public final NArray<N> full(Shape shape, int value, Order order) {
            var storage = storage().zeros(shape.size());
            storage.fill(dtype().castValue(value), 0, shape.size());
            return stride(shape, Order.autoFC(order), storage);
        }

        public final NArray<N> full(Shape shape, float value, Order order) {
            var storage = storage().zeros(shape.size());
            storage.fill(dtype().castValue(value), 0, shape.size());
            return stride(shape, Order.autoFC(order), storage);
        }

        public final NArray<N> full(Shape shape, double value, Order order) {
            var storage = storage().zeros(shape.size());
            storage.fill(dtype().castValue(value), 0, shape.size());
            return stride(shape, Order.autoFC(order), storage);
        }

        public final NArray<N> seq(Shape shape) {
            return seq(shape, Order.defaultOrder());
        }

        public final NArray<N> seq(Shape shape, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(Order.C, (i, _) -> dType.castValue(i));
        }

        public final NArray<N> random(Shape shape, Distribution dist, Random random) {
            return random(shape, dist, random, Order.defaultOrder());
        }

        public abstract NArray<N> random(Shape shape, Distribution dist, Random random, Order order);

        public final NArray<N> random(Shape shape, Random random) {
            return random(shape, random, Order.defaultOrder());
        }

        public abstract NArray<N> random(Shape shape, Random random, Order order);

        public final NArray<N> stride(Shape shape, Order order, Storage<N> storage) {
            return stride(StrideLayout.ofDense(shape, 0, order), storage);
        }

        public final NArray<N> stride(byte... array) {
            return stride(Shape.of(array.length), Order.defaultOrder(), storage().from(array));
        }

        public final NArray<N> stride(int... array) {
            return stride(Shape.of(array.length), Order.defaultOrder(), storage().from(array));
        }

        public final NArray<N> stride(float... array) {
            return stride(Shape.of(array.length), Order.defaultOrder(), storage().from(array));
        }

        public final NArray<N> stride(double... array) {
            return stride(Shape.of(array.length), Order.defaultOrder(), storage().from(array));
        }

        public final NArray<N> stride(Shape shape, byte... array) {
            return stride(shape, Order.defaultOrder(), storage().from(array));
        }

        public final NArray<N> stride(Shape shape, int... array) {
            return stride(shape, Order.defaultOrder(), storage().from(array));
        }

        public final NArray<N> stride(Shape shape, float... array) {
            return stride(shape, Order.defaultOrder(), storage().from(array));
        }

        public final NArray<N> stride(Shape shape, double... array) {
            return stride(shape, Order.defaultOrder(), storage().from(array));
        }

        public final NArray<N> stride(Shape shape, Order order, byte... array) {
            return stride(shape, order, storage().from(array));
        }

        public final NArray<N> stride(Shape shape, Order order, int... array) {
            return stride(shape, order, storage().from(array));
        }

        public final NArray<N> stride(Shape shape, Order order, float... array) {
            return stride(shape, order, storage().from(array));
        }

        public final NArray<N> stride(Shape shape, Order order, double... array) {
            return stride(shape, order, storage().from(array));
        }

        public abstract NArray<N> stride(StrideLayout layout, Storage<N> storage);

        public final NArray<N> stride(StrideLayout layout, byte... array) {
            return stride(layout, storage().from(array));
        }

        public final NArray<N> stride(StrideLayout layout, int... array) {
            return stride(layout, storage().from(array));
        }

        public final NArray<N> stride(StrideLayout layout, float... array) {
            return stride(layout, storage().from(array));
        }

        public final NArray<N> stride(StrideLayout layout, double... array) {
            return stride(layout, storage().from(array));
        }
    }
}
