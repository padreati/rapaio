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

package rapaio.codegen.param;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/7/20.
 */
public class ListParam<T, S extends ParamSet<S>> implements Param<List<T>, S> {

    @Serial
    private static final long serialVersionUID = 4085743402578672837L;
    private final S params;
    private final List<T> defaultValues;
    private final List<T> values = new ArrayList<>();
    private final String name;
    private final BiFunction<List<T>, List<T>, Boolean> validator;

    public ListParam(S params, List<T> defaultValues, String name, BiFunction<List<T>, List<T>, Boolean> validator) {
        this.params = params;
        this.defaultValues = defaultValues;
        this.name = name;
        this.validator = validator;
        set(defaultValues);
        params.registerParameter(this);
    }

    @Override
    public List<T> get() {
        return values;
    }

    @Override
    public S set(List<T> values) {
        clear();
        if (!validate(values)) {
            throw new IllegalArgumentException("Parameter values are invalid.");
        }
        this.values.addAll(values);
        return params;
    }

    @SafeVarargs
    public final S set(T... arrayValues) {
        List<T> values = Arrays.asList(arrayValues);
        clear();
        if (!validate(values)) {
            throw new IllegalArgumentException("Parameter values are invalid.");
        }
        this.values.addAll(values);
        return params;
    }

    @SafeVarargs
    public final S add(T... values) {
        if (!validate(Arrays.asList(values))) {
            throw new IllegalArgumentException("Parameter values are invalid.");
        }
        this.values.addAll(Arrays.asList(values));
        return params;
    }

    @Override
    public List<T> defaultValue() {
        return defaultValues;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean hasDefaultValue() {
        if (defaultValues == null) {
            return true;
        }
        if (defaultValues.size() != values.size()) {
            return false;
        }
        for (int i = 0; i < defaultValues.size(); i++) {
            if (defaultValues.get(i) instanceof ParametricEquals dvi) {
                boolean eq = dvi.equalOnParams(values.get(i));
                if (!eq) {
                    return false;
                }
            }
            if (!defaultValues.get(i).equals(values.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public S clear() {
        values.clear();
        return params;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean validate(List<T> newValues) {
        return validator.apply(this.values, newValues);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void copyFrom(Param<?, ?> param) {
        set((List<T>) ((ListParam<?, ?>) param).values);
    }
}
