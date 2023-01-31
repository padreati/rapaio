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

package rapaio.util.param;

import java.io.Serial;
import java.util.Objects;

import rapaio.util.function.SFunction;

/**
 * This implementation models a parameter which has a single value of given generic type {@link T}.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/29/20.
 */
public class ValueParam<T, S extends ParamSet<S>> implements Param<T, S> {

    @Serial
    private static final long serialVersionUID = -199987098272932206L;
    protected final S params;
    protected final T defaultValue;
    protected final String name;
    protected T value;
    protected final SFunction<T, Boolean> validator;

    public ValueParam(S params, T defaultValue, String name) {
        this(params, defaultValue, name, Objects::nonNull);
    }

    public ValueParam(S params, T defaultValue, String name, SFunction<T, Boolean> validator) {
        this.params = params;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
        this.name = name;
        this.validator = validator;

        params.registerParameter(this);
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public S set(T value) {
        if (!validate(value)) {
            throw new IllegalArgumentException("Value " + value + " for parameter " + name() + " is invalid!");
        }
        this.value = value;
        return params;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean hasDefaultValue() {
        if (defaultValue == null) {
            return value == null;
        }
        if (defaultValue instanceof ParametricEquals dvi) {
            return dvi.equalOnParams(value);
        }
        return defaultValue().equals(value);
    }

    @Override
    public S clear() {
        value = defaultValue();
        return params;
    }

    @Override
    public T defaultValue() {
        return defaultValue;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean validate(T value) {
        return validator.apply(value);
    }
}
