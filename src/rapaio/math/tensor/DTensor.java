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

public abstract class DTensor extends AbstractTensor<Double, DStorage, DTensor> {

    public static DTensor zeros(Shape shape) {
        return zeros(shape, Type.defaultType());
    }

    public static DTensor zeros(Shape shape, Type type) {
        return new DTensorDense(shape, type, DStorage.zeros(shape.size()));
    }

    public static DTensor seq(Shape shape) {
        return seq(shape, Type.defaultType());
    }

    public static DTensor seq(Shape shape, Type type) {
        return new DTensorDense(shape, type, DStorage.seq(0, shape.size()));
    }

    public static DTensor random(Shape shape, Random random) {
        return random(shape, Type.defaultType(), random);
    }

    public static DTensor random(Shape shape, Type type, Random random) {
        return new DTensorDense(shape, type, DStorage.random(shape.size(), random));
    }

    public static DTensor wrap(Shape shape, Type type, double[] array) {
        return new DTensorDense(shape, type, DStorage.wrap(array));
    }

    protected DTensor(Shape shape, Type type) {
        super(shape, type);
    }

    @Override
    public abstract DStorage storage();

    @Override
    public final Double get(int... idxs) {
        return getDouble(idxs);
    }

    public abstract double getDouble(int... idxs);

    @Override
    public final void set(Double value, int... idxs) {
        setDouble(value, idxs);
    }

    public abstract void setDouble(double value, int... idxs);

    @Override
    public final DTensor reshape(Shape shape) {
        return reshape(shape, Type.defaultType());
    }

    @Override
    public abstract DTensor reshape(Shape shape, Type type);

    @Override
    public abstract DTensor t();
}
