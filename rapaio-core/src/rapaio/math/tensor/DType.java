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

package rapaio.math.tensor;

import java.util.Objects;
import java.util.function.Function;

import rapaio.math.tensor.dtype.DTypeDouble;
import rapaio.math.tensor.dtype.DTypeFloat;
import rapaio.math.tensor.dtype.DTypeInteger;

public abstract class DType<N extends Number, T extends Tensor<N, T>> {

    public static DTypeFloat FLOAT = new DTypeFloat();
    public static DTypeDouble DOUBLE = new DTypeDouble();
    public static DTypeInteger INTEGER = new DTypeInteger();

    private final String id;
    private final byte bytes;
    private final boolean isInteger;

    protected DType(String id, byte bytes, boolean isInteger) {
        this.id = id;
        this.bytes = bytes;
        this.isInteger = isInteger;
    }

    public String id() {
        return id;
    }

    public boolean isInteger() {
        return isInteger;
    }

    public boolean isFloat() {
        return !isInteger;
    }

    public int bytes() {
        return bytes;
    }

    public abstract <M extends Number> N castValue(M value);

    public abstract N castValue(int value);

    public abstract N castValue(float value);

    public abstract N castValue(double value);

    public abstract <M extends Number> Function<N, M> castFunction(DType<M, ?> dType);

    public abstract boolean isNaN(N value);

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DType<?, ?> dType)) {
            return false;
        }
        return Objects.equals(id, dType.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
