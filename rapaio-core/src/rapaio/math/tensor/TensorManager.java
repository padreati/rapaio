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

package rapaio.math.tensor;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.manager.base.BaseTensorManager;
import rapaio.util.Hardware;

public abstract class TensorManager {

    public static TensorManager base() {
        return new BaseTensorManager(Hardware.CORES);
    }

    public static TensorManager base(int cpuThreads) {
        return new BaseTensorManager(cpuThreads);
    }

    protected final int cpuThreads;
    protected final OfType<Double> ofDouble;
    protected final OfType<Float> ofFloat;
    protected final OfType<Integer> ofInt;
    protected final OfType<Byte> ofByte;
    protected final StorageFactory storageFactory;

    protected TensorManager(int cpuThreads, OfType<Double> ofDouble, OfType<Float> ofFloat, OfType<Integer> ofInt, OfType<Byte> ofByte,
            StorageFactory storageFactory) {
        this.cpuThreads = cpuThreads;

        this.ofDouble = ofDouble;
        this.ofFloat = ofFloat;
        this.ofInt = ofInt;
        this.ofByte = ofByte;
        this.storageFactory = storageFactory;

        this.ofDouble.registerParent(this, storageFactory.ofType(DType.DOUBLE));
        this.ofFloat.registerParent(this, storageFactory.ofType(DType.FLOAT));
        this.ofInt.registerParent(this, storageFactory.ofType(DType.INTEGER));
        this.ofByte.registerParent(this, storageFactory.ofType(DType.BYTE));
    }

    public final OfType<Double> ofDouble() {
        return ofDouble;
    }

    public final OfType<Float> ofFloat() {
        return ofFloat;
    }

    public final OfType<Integer> ofInt() {
        return ofInt;
    }

    public final OfType<Byte> ofByte() {
        return ofByte;
    }

    @SuppressWarnings("unchecked")
    public final <N extends Number> OfType<N> ofType(DType<N> dtype) {
        return (OfType<N>) switch (dtype.id()) {
            case DOUBLE -> ofDouble();
            case FLOAT -> ofFloat();
            case INTEGER -> ofInt();
            case BYTE -> ofByte();
        };
    }

    public final StorageFactory storage() {
        return storageFactory;
    }

    public final int cpuThreads() {
        return cpuThreads;
    }

    public final <N extends Number> Tensor<N> scalar(DType<N> dType, N value) {
        return ofType(dType).scalar(value);
    }

    public final <N extends Number> Tensor<N> scalar(DType<N> dType, byte value) {
        return ofType(dType).scalar(value);
    }

    public final <N extends Number> Tensor<N> scalar(DType<N> dType, int value) {
        return ofType(dType).scalar(value);
    }

    public final <N extends Number> Tensor<N> scalar(DType<N> dType, float value) {
        return ofType(dType).scalar(value);
    }

    public final <N extends Number> Tensor<N> scalar(DType<N> dType, double value) {
        return ofType(dType).scalar(value);
    }

    public final <N extends Number> Tensor<N> zeros(DType<N> dType, Shape shape) {
        return ofType(dType).zeros(shape, Order.defaultOrder());
    }

    public final <N extends Number> Tensor<N> zeros(DType<N> dType, Shape shape, Order order) {
        return ofType(dType).zeros(shape, order);
    }

    public final <N extends Number> Tensor<N> eye(DType<N> dType, int n) {
        return ofType(dType).eye(n, Order.defaultOrder());
    }

    public final <N extends Number> Tensor<N> eye(DType<N> dType, int n, Order order) {
        return ofType(dType).eye(n, order);
    }

    public final <N extends Number> Tensor<N> full(DType<N> dType, Shape shape, N value) {
        return ofType(dType).full(shape, value, Order.defaultOrder());
    }

    public final <N extends Number> Tensor<N> full(DType<N> dType, Shape shape, N value, Order order) {
        return ofType(dType).full(shape, value, order);
    }

    public final <N extends Number> Tensor<N> seq(DType<N> dType, Shape shape) {
        return ofType(dType).seq(shape, Order.defaultOrder());
    }

    public final <N extends Number> Tensor<N> seq(DType<N> dType, Shape shape, Order order) {
        return ofType(dType).seq(shape, order);
    }

    public final <N extends Number> Tensor<N> random(DType<N> dType, Shape shape, Random random) {
        return ofType(dType).random(shape, random, Order.defaultOrder());
    }

    public final <N extends Number> Tensor<N> random(DType<N> dType, Shape shape, Random random, Order order) {
        return ofType(dType).random(shape, random, order);
    }


    public final <N extends Number, M extends Number> Tensor<N> strideCast(DType<N> dType, Shape shape, Order order, Storage<M> storage) {
        return ofType(dType).strideCast(StrideLayout.ofDense(shape, 0, order), storage);
    }

    public final <N extends Number, M extends Number> Tensor<N> strideCast(DType<N> dType, StrideLayout layout, Storage<M> storage) {
        return ofType(dType).strideCast(layout, storage);
    }


    public final <N extends Number> Tensor<N> stride(DType<N> dType, Shape shape, Order order, Storage<N> storage) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), storage);
    }

    public final <N extends Number> Tensor<N> stride(DType<N> dType, Shape shape, Order order, byte[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), storage().ofType(dType).from(array));
    }

    public final <N extends Number> Tensor<N> stride(DType<N> dType, Shape shape, Order order, int[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), storage().ofType(dType).from(array));
    }

    public final <N extends Number> Tensor<N> stride(DType<N> dType, Shape shape, Order order, float[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), storage().ofType(dType).from(array));
    }

    public final <N extends Number> Tensor<N> stride(DType<N> dType, Shape shape, Order order, double[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), storage().ofType(dType).from(array));
    }

    public final <N extends Number> Tensor<N> stride(DType<N> dType, StrideLayout layout, Storage<N> storage) {
        return ofType(dType).stride(layout, storage);
    }

    /**
     * Concatenates multiple tensors along a new axis.
     * All tensors must have the same shape. The position of the new axis is between 0 (inclusive)
     * and the number of dimensions (inclusive).
     * <p>
     * The new tensor will have an additional dimension, which is the dimension created during stacking and
     * will have size equal with the number of the stacked tensors.
     * <p>
     * The new tensor with be stored with default order.
     *
     * @param axis    index of the new dimension
     * @param tensors tensors to concatenate
     * @return new tensor with concatenated data
     */
    public final <N extends Number> Tensor<N> stack(int axis, Collection<? extends Tensor<N>> tensors) {
        return stack(Order.defaultOrder(), axis, tensors);
    }

    public final <N extends Number> Tensor<N> stack(Order order, int axis, Collection<? extends Tensor<N>> tensors) {
        var tensorList = tensors.stream().toList();
        for (int i = 1; i < tensorList.size(); i++) {
            if (!tensorList.get(i - 1).shape().equals(tensorList.get(i).shape())) {
                throw new IllegalArgumentException("Tensors are not valid for stack, they have to have the same dimensions.");
            }
        }
        int[] newDims = new int[tensorList.getFirst().rank() + 1];
        int i = 0;
        for (; i < axis; i++) {
            newDims[i] = tensorList.getFirst().shape().dim(i);
        }
        for (; i < tensorList.getFirst().rank(); i++) {
            newDims[i + 1] = tensorList.getFirst().shape().dim(i);
        }
        newDims[axis] = tensorList.size();
        var result = ofType(tensorList.getFirst().dtype()).zeros(Shape.of(newDims), order);
        var slices = result.chunk(axis, true, 1);
        i = 0;
        for (; i < tensorList.size(); i++) {
            var it1 = slices.get(i).squeeze(axis).ptrIterator(Order.defaultOrder());
            var it2 = tensorList.get(i).ptrIterator(Order.defaultOrder());
            while (it1.hasNext() && it2.hasNext()) {
                slices.get(i).ptrSet(it1.nextInt(), tensorList.get(i).ptrGet(it2.nextInt()));
            }
        }
        return result;
    }

    public final <N extends Number> Tensor<N> concat(int axis, Collection<? extends Tensor<N>> tensors) {
        return concat(Order.defaultOrder(), axis, tensors);
    }

    public final <N extends Number> Tensor<N> concat(Order order, int axis, Collection<? extends Tensor<N>> tensors) {
        var tensorList = tensors.stream().toList();
        TensorManager.validateForConcatenation(axis, tensorList.stream().map(t -> t.shape().dims()).collect(Collectors.toList()));

        int newDim = tensorList.stream().mapToInt(tensor -> tensor.layout().shape().dim(axis)).sum();
        Tensor<N> first = tensorList.getFirst();
        int[] newDims = Arrays.copyOf(first.shape().dims(), first.rank());
        newDims[axis] = newDim;
        var result = ofType(first.dtype()).zeros(Shape.of(newDims), order);

        int start = 0;
        for (Tensor<N> tensor : tensors) {
            int end = start + tensor.shape().dim(axis);
            var dst = result.narrow(axis, true, start, end);

            var it1 = tensor.ptrIterator(Order.defaultOrder());
            var it2 = dst.ptrIterator(Order.defaultOrder());

            while (it1.hasNext() && it2.hasNext()) {
                dst.ptrSet(it2.nextInt(), tensor.ptrGet(it1.nextInt()));
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
                    throw new IllegalArgumentException("Tensors are not valid for concatenation");
                }
            }
        }
    }

    public static abstract class OfType<N extends Number> {

        protected final DType<N> dType;
        protected TensorManager parent;
        protected StorageFactory.OfType<N> storageOfType;

        public OfType(DType<N> dType) {
            this.dType = dType;
        }

        public final void registerParent(TensorManager parent, StorageFactory.OfType<N> storageOfType) {
            if (this.parent != null) {
                throw new IllegalArgumentException("AbstractEngineOfType has already a registered parent.");
            }
            this.parent = parent;
            this.storageOfType = storageOfType;
        }

        public final DType<N> dtype() {
            return dType;
        }

        public final StorageFactory.OfType<N> storage() {
            return storageOfType;
        }

        public final Tensor<N> scalar(N value) {
            return stride(StrideLayout.of(Shape.of(), 0, new int[0]), storage().scalar(value));
        }

        public final Tensor<N> scalar(byte value) {
            return stride(StrideLayout.of(Shape.of(), 0, new int[0]), storage().scalar(value));
        }

        public final Tensor<N> scalar(int value) {
            return stride(StrideLayout.of(Shape.of(), 0, new int[0]), storage().scalar(value));
        }

        public final Tensor<N> scalar(float value) {
            return stride(StrideLayout.of(Shape.of(), 0, new int[0]), storage().scalar(value));
        }

        public final Tensor<N> scalar(double value) {
            return stride(StrideLayout.of(Shape.of(), 0, new int[0]), storage().scalar(value));
        }

        public final Tensor<N> zeros(Shape shape) {
            return zeros(shape, Order.defaultOrder());
        }

        public final Tensor<N> zeros(Shape shape, Order order) {
            return stride(shape, Order.autoFC(order), storage().zeros(shape.size()));
        }

        public final Tensor<N> eye(int n) {
            return eye(n, Order.defaultOrder());
        }

        public final Tensor<N> eye(int n, Order order) {
            var eye = zeros(Shape.of(n, n), order);
            for (int i = 0; i < n; i++) {
                eye.set(dType.castValue(1), i, i);
            }
            return eye;
        }

        public final Tensor<N> full(Shape shape, N value) {
            return full(shape, value, Order.defaultOrder());
        }

        public final Tensor<N> full(Shape shape, N value, Order order) {
            var storage = storage().zeros(shape.size());
            storage.fill(value, 0, shape.size());
            return stride(shape, Order.autoFC(order), storage);
        }

        public final Tensor<N> seq(Shape shape) {
            return seq(shape, Order.defaultOrder());
        }

        public final Tensor<N> seq(Shape shape, Order order) {
            return zeros(shape, Order.autoFC(order)).apply_(Order.C, (i, _) -> dType.castValue(i));
        }

        public final Tensor<N> random(Shape shape, Random random) {
            return random(shape, random, Order.defaultOrder());
        }

        public abstract Tensor<N> random(Shape shape, Random random, Order order);


        public <M extends Number> Tensor<N> strideCast(Shape shape, Order order, Storage<M> storage) {
            return strideCast(StrideLayout.ofDense(shape, 0, order), storage);
        }

        public final <M extends Number> Tensor<N> strideCast(StrideLayout layout, Storage<M> storage) {
            return stride(layout, storage().from(storage));
        }

        public final Tensor<N> stride(Shape shape, Order order, Storage<N> storage) {
            return stride(StrideLayout.ofDense(shape, 0, order), storage);
        }

        public final Tensor<N> stride(byte... array) {
            return stride(Shape.of(array.length), Order.defaultOrder(), storage().from(array));
        }

        public final Tensor<N> stride(int... array) {
            return stride(Shape.of(array.length), Order.defaultOrder(), storage().from(array));
        }

        public final Tensor<N> stride(float... array) {
            return stride(Shape.of(array.length), Order.defaultOrder(), storage().from(array));
        }

        public final Tensor<N> stride(double... array) {
            return stride(Shape.of(array.length), Order.defaultOrder(), storage().from(array));
        }

        public final Tensor<N> stride(Shape shape, byte... array) {
            return stride(shape, Order.defaultOrder(), storage().from(array));
        }

        public final Tensor<N> stride(Shape shape, int... array) {
            return stride(shape, Order.defaultOrder(), storage().from(array));
        }

        public final Tensor<N> stride(Shape shape, float... array) {
            return stride(shape, Order.defaultOrder(), storage().from(array));
        }

        public final Tensor<N> stride(Shape shape, double... array) {
            return stride(shape, Order.defaultOrder(), storage().from(array));
        }

        public final Tensor<N> stride(Shape shape, Order order, byte... array) {
            return stride(shape, order, storage().from(array));
        }

        public final Tensor<N> stride(Shape shape, Order order, int... array) {
            return stride(shape, order, storage().from(array));
        }

        public final Tensor<N> stride(Shape shape, Order order, float... array) {
            return stride(shape, order, storage().from(array));
        }

        public final Tensor<N> stride(Shape shape, Order order, double... array) {
            return stride(shape, order, storage().from(array));
        }

        public abstract Tensor<N> stride(StrideLayout layout, Storage<N> storage);

        public final Tensor<N> stride(StrideLayout layout, byte... array) {
            return stride(layout, storage().from(array));
        }

        public final Tensor<N> stride(StrideLayout layout, int... array) {
            return stride(layout, storage().from(array));
        }

        public final Tensor<N> stride(StrideLayout layout, float... array) {
            return stride(layout, storage().from(array));
        }

        public final Tensor<N> stride(StrideLayout layout, double... array) {
            return stride(layout, storage().from(array));
        }
    }
}
