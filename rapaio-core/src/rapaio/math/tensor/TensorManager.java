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

import java.util.Collection;
import java.util.Random;
import java.util.Vector;

import rapaio.math.tensor.layout.StrideLayout;
import rapaio.math.tensor.manager.base.BaseTensorManager;

public interface TensorManager {

    static TensorManager base() {
        return new BaseTensorManager();
    }

    static TensorManager base(int cpuThreads) {
        return new BaseTensorManager(cpuThreads);
    }

    OfType<Double> ofDouble();

    OfType<Float> ofFloat();

    OfType<Integer> ofInt();

    OfType<Byte> ofByte();

    @SuppressWarnings("unchecked")
    default <N extends Number> OfType<N> ofType(DType<N> dType) {
        return (OfType<N>) switch (dType.id()) {
            case DOUBLE -> ofDouble();
            case FLOAT -> ofFloat();
            case INTEGER -> ofInt();
            case BYTE -> ofByte();
        };
    }

    StorageFactory storage();

    int cpuThreads();

    default <N extends Number> Tensor<N> scalar(DType<N> dType, N value) {
        return ofType(dType).scalar(value);
    }

    default <N extends Number> Tensor<N> zeros(DType<N> dType, Shape shape) {
        return ofType(dType).zeros(shape, Order.defaultOrder());
    }

    default <N extends Number> Tensor<N> zeros(DType<N> dType, Shape shape, Order order) {
        return ofType(dType).zeros(shape, order);
    }

    default <N extends Number> Tensor<N> eye(DType<N> dType, int n) {
        return ofType(dType).eye(n, Order.defaultOrder());
    }

    default <N extends Number> Tensor<N> eye(DType<N> dType, int n, Order order) {
        return ofType(dType).eye(n, order);
    }

    default <N extends Number> Tensor<N> full(DType<N> dType, Shape shape, N value) {
        return ofType(dType).full(shape, value, Order.defaultOrder());
    }

    default <N extends Number> Tensor<N> full(DType<N> dType, Shape shape, N value, Order order) {
        return ofType(dType).full(shape, value, order);
    }

    default <N extends Number> Tensor<N> seq(DType<N> dType, Shape shape) {
        return ofType(dType).seq(shape, Order.defaultOrder());
    }

    default <N extends Number> Tensor<N> seq(DType<N> dType, Shape shape, Order order) {
        return ofType(dType).seq(shape, order);
    }

    default <N extends Number> Tensor<N> random(DType<N> dType, Shape shape, Random random) {
        return ofType(dType).random(shape, random, Order.defaultOrder());
    }

    default <N extends Number> Tensor<N> random(DType<N> dType, Shape shape, Random random, Order order) {
        return ofType(dType).random(shape, random, order);
    }


    default <N extends Number, M extends Number> Tensor<N> strideCast(DType<N> dType, Shape shape, Order order, Storage<M> storage) {
        return ofType(dType).strideCast(StrideLayout.ofDense(shape, 0, order), storage);
    }

    default <N extends Number, M extends Number> Tensor<N> strideCast(DType<N> dType, StrideLayout layout, Storage<M> storage) {
        return ofType(dType).strideCast(layout, storage);
    }


    default <N extends Number, V extends Vector<N>> Tensor<N> stride(DType<N> dType, Shape shape, Order order, Storage<N> storage) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), storage);
    }

    default <N extends Number> Tensor<N> stride(DType<N> dType, Shape shape, Order order, byte[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), storage().ofType(dType).from(array));
    }

    default <N extends Number> Tensor<N> stride(DType<N> dType, Shape shape, Order order, int[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), storage().ofType(dType).from(array));
    }

    default <N extends Number> Tensor<N> stride(DType<N> dType, Shape shape, Order order, float[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), storage().ofType(dType).from(array));
    }

    default <N extends Number> Tensor<N> stride(DType<N> dType, Shape shape, Order order, double[] array) {
        return ofType(dType).stride(StrideLayout.ofDense(shape, 0, order), storage().ofType(dType).from(array));
    }

    default <N extends Number> Tensor<N> stride(DType<N> dType, StrideLayout layout, Storage<N> storage) {
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
    default <N extends Number> Tensor<N> stack(int axis, Collection<? extends Tensor<N>> tensors) {
        return stack(Order.defaultOrder(), axis, tensors);
    }

    /**
     * Concatenates multiple tensors along a new axis. All tensors must have the same shape.
     * The position of the new axis is between 0 (inclusive) and the number of dimensions (inclusive).
     * <p>
     * The new tensor will have an additional dimension, which is the dimension created during stacking and
     * will have size equal with the number of the stacked tensors.
     * <p>
     * The new tensor will be stored with specified order.
     *
     * @param order   storage order of the result
     * @param axis    index of the new dimension
     * @param tensors tensors to concatenate
     * @return new tensor with concatenated data
     */
    <N extends Number> Tensor<N> stack(Order order, int axis, Collection<? extends Tensor<N>> tensors);

    default <N extends Number> Tensor<N> concat(int axis, Collection<? extends Tensor<N>> tensors) {
        return concat(Order.defaultOrder(), axis, tensors);
    }

    /**
     * Concatenates multiple tensors along a given axis.
     * Tensors must have compatible size, all other dimensions must be equal.
     *
     * @param order   storage order or the result
     * @param axis    axis to concatenate along
     * @param tensors tensors to concatenate
     * @return new tensor with concatenated data
     */
    <N extends Number> Tensor<N> concat(Order order, int axis, Collection<? extends Tensor<N>> tensors);

    abstract class OfType<N extends Number> {

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
            return zeros(shape, Order.autoFC(order)).apply_(Order.C, (i, p) -> dType.castValue(i));
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
