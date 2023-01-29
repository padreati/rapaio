/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.tensor;

import java.util.Random;

import rapaio.math.tensor.storage.DStorage;
import rapaio.math.tensor.storage.FStorage;
import rapaio.math.tensor.storage.Storage;
import rapaio.util.collection.IntArrays;

interface NTensorDataGenerator<N extends Number, S extends Storage<N>, V extends Tensor<N, S, V>> {

    String className();

    N value(int x);

    N inc(N x);

    N mul(N x, double y);

    Tensor<N, S, V> sequence(Shape shape);

    Tensor<N, S, V> zeros(Shape shape);

    abstract class DoubleDense implements NTensorDataGenerator<Double, DStorage, DTensor> {

        @Override
        public final Double value(int x) {
            return (double) x;
        }

        @Override
        public final Double inc(Double x) {
            return x + 1;
        }

        @Override
        public final Double mul(Double x, double y) {
            return x * y;
        }

    }

    final class DoubleDenseCol extends DoubleDense {

        @Override
        public String className() {
            return DTensorDense.class.getSimpleName();
        }

        @Override
        public DTensor sequence(Shape shape) {
            DTensor t = (Tensor.Type.defaultType() == Tensor.Type.DenseCol) ?
                    DTensor.zeros(shape) :
                    DTensor.zeros(shape, Tensor.Type.DenseCol);
            var it = t.pointerIterator(Tensor.Order.RowMajor);
            double i = 0;
            while (it.hasNext()) {
                t.storage().setDouble(it.nextInt(), i++);
            }
            return t;
        }

        @Override
        public DTensor zeros(Shape shape) {
            return (Tensor.Type.defaultType() == Tensor.Type.DenseCol) ?
                    DTensor.zeros(shape) :
                    DTensor.zeros(shape, Tensor.Type.DenseCol);
        }
    }

    final class DoubleDenseRow extends DoubleDense {

        @Override
        public String className() {
            return DTensorDense.class.getSimpleName();
        }

        @Override
        public DTensor sequence(Shape shape) {
            return Tensor.Type.defaultType() == Tensor.Type.DenseRow ?
                    DTensor.seq(shape) :
                    DTensor.seq(shape, Tensor.Type.DenseRow);
        }

        @Override
        public DTensor zeros(Shape shape) {
            return Tensor.Type.defaultType() == Tensor.Type.DenseRow ?
                    DTensor.zeros(shape) :
                    DTensor.zeros(shape, Tensor.Type.DenseRow);
        }
    }

    final class DoubleDenseStride extends DoubleDense {

        @Override
        public String className() {
            return "DTensorStride";
        }

        @Override
        public DTensor sequence(Shape shape) {
            DTensorStride t = zeros(shape);
            var it = t.pointerIterator(Tensor.Order.RowMajor);
            int p = 0;
            while (it.hasNext()) {
                t.storage().setDouble(it.nextInt(), p++);
            }
            return t;
        }

        @Override
        public DTensorStride zeros(Shape shape) {
            int offset = 10;
            int[] strides = IntArrays.newFill(shape.rank(), 1);
            int[] ordering = IntArrays.newSeq(0, shape.rank());
            IntArrays.shuffle(ordering, new Random(42));

            for (int i = 1; i < shape.rank(); i++) {
                strides[ordering[i]] = strides[ordering[i - 1]] * shape.dim(ordering[i - 1]);
            }

            return new DTensorStride(shape, offset, strides, DStorage.zeros(offset + shape.size()));
        }
    }

    abstract class FloatDense implements NTensorDataGenerator<Float, FStorage, FTensor> {

        @Override
        public final Float value(int x) {
            return (float) x;
        }

        @Override
        public final Float inc(Float x) {
            return x + 1;
        }

        @Override
        public final Float mul(Float x, double y) {
            return (float) (x * y);
        }
    }

    final class FloatDenseCol extends FloatDense {

        @Override
        public String className() {
            return FTensorDense.class.getSimpleName();
        }

        @Override
        public FTensor sequence(Shape shape) {
            FTensor t = (Tensor.Type.defaultType() == Tensor.Type.DenseCol) ?
                    FTensor.zeros(shape) :
                    FTensor.zeros(shape, Tensor.Type.DenseCol);
            var it = t.pointerIterator(Tensor.Order.RowMajor);
            float i = 0;
            while (it.hasNext()) {
                t.storage().setFloat(it.nextInt(), i++);
            }
            return t;
        }

        @Override
        public FTensor zeros(Shape shape) {
            return (Tensor.Type.defaultType() == Tensor.Type.DenseCol) ?
                    FTensor.zeros(shape) :
                    FTensor.zeros(shape, Tensor.Type.DenseCol);
        }
    }

    final class FloatDenseRow extends FloatDense {

        @Override
        public String className() {
            return FTensorDense.class.getSimpleName();
        }

        @Override
        public FTensor sequence(Shape shape) {
            return (Tensor.Type.defaultType() == Tensor.Type.DenseRow) ?
                    FTensor.seq(shape) :
                    FTensor.seq(shape, Tensor.Type.DenseRow);
        }

        @Override
        public FTensor zeros(Shape shape) {
            return (Tensor.Type.defaultType() == Tensor.Type.DenseRow) ?
                    FTensor.zeros(shape) :
                    FTensor.zeros(shape, Tensor.Type.DenseRow);
        }
    }

    final class FloatDenseStride extends FloatDense {

        @Override
        public String className() {
            return "FTensorStride";
        }

        @Override
        public FTensor sequence(Shape shape) {
            FTensor t = zeros(shape);
            var it = t.pointerIterator(Tensor.Order.RowMajor);
            int p = 0;
            while (it.hasNext()) {
                t.storage().setFloat(it.nextInt(), p++);
            }
            return t;
        }

        @Override
        public FTensor zeros(Shape shape) {
            int offset = 10;
            int[] strides = IntArrays.newFill(shape.rank(), 1);
            int[] ordering = IntArrays.newSeq(0, shape.rank());
            IntArrays.shuffle(ordering, new Random(42));

            for (int i = 1; i < shape.rank(); i++) {
                strides[ordering[i]] = strides[ordering[i - 1]] * shape.dim(ordering[i - 1]);
            }

            return new FTensorStride(shape, offset, strides, FStorage.zeros(offset + shape.size()));
        }

    }
}
