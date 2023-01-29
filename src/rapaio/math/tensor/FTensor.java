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

import rapaio.math.tensor.storage.FStorage;

public abstract class FTensor extends AbstractTensor<Float, FStorage, FTensor> {

    public static FTensor zeros(Shape shape) {
        return zeros(shape, Type.defaultType());
    }

    public static FTensor zeros(Shape shape, Type type) {
        return new FTensorDense(shape, type, FStorage.zeros(shape.size()));
    }

    public static FTensor seq(Shape shape) {
        return seq(shape, Type.defaultType());
    }

    public static FTensor seq(Shape shape, Type type) {
        return new FTensorDense(shape, type, FStorage.seq(0, shape.size()));
    }

    public static FTensor random(Shape shape, Random random) {
        return random(shape, Type.defaultType(), random);
    }

    public static FTensor random(Shape shape, Type type, Random random) {
        return new FTensorDense(shape, type, FStorage.random(shape.size(), random));
    }

    public static FTensor wrap(Shape shape, Type type, float[] array) {
        return new FTensorDense(shape, type, FStorage.wrap(array));
    }

    protected FTensor(Shape shape, Type type) {
        super(shape, type);
    }

    @Override
    public abstract FStorage storage();

    @Override
    public final Float get(int... idxs) {
        return getFloat(idxs);
    }

    public abstract float getFloat(int... idxs);

    @Override
    public final void set(Float value, int... idxs) {
        setFloat(value, idxs);
    }

    public abstract void setFloat(float value, int... idxs);

    @Override
    public final FTensor reshape(Shape shape) {
        return reshape(shape, Type.defaultType());
    }

    @Override
    public abstract FTensor reshape(Shape shape, Type type);

    @Override
    public abstract FTensor t();
}
