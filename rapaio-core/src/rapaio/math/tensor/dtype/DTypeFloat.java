/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.tensor.dtype;

import java.util.function.Function;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.FTensor;

public final class DTypeFloat extends DType<Float, FTensor> {

    private static final String ID = "FLOAT";

    public DTypeFloat() {
        super(ID, false);
    }

    @Override
    public <M extends Number> Float castValue(M value) {
        return value.floatValue();
    }

    @Override
    public Float castValue(int value) {
        return (float) value;
    }

    @Override
    public Float castValue(float value) {
        return value;
    }

    @Override
    public Float castValue(double value) {
        return (float) value;
    }

    @Override
    public <M extends Number> Function<Float, M> castFunction(DType<M, ?> dType) {
        return dType::castValue;
    }
}
