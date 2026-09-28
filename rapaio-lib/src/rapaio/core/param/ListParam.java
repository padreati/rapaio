/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.core.param;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import rapaio.util.function.SBiFunction;

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
    private final SBiFunction<List<T>, List<T>, Boolean> validator;

    public ListParam(S params, List<T> defaultValues, String name, SBiFunction<List<T>, List<T>, Boolean> validator) {
        this.params = params;
        this.defaultValues = defaultValues;
        this.name = name;
        this.validator = validator;
        set(defaultValues);
        params.registerParameter(this);
    }

    /**
     * @return an unmodifiable view of the current values; use {@link #set(List)}, {@link #add(Object[])}
     * or {@link #clear()} to change them so that the validator is applied
     */
    @Override
    public List<T> get() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public S set(List<T> values) {
        return replace(values);
    }

    @SafeVarargs
    public final S set(T... arrayValues) {
        return replace(Arrays.asList(arrayValues));
    }

    @SafeVarargs
    public final S add(T... values) {
        List<T> newValues = Arrays.asList(values);
        if (!validator.apply(this.values, newValues)) {
            throw new IllegalArgumentException("Parameter values are invalid.");
        }
        this.values.addAll(newValues);
        return params;
    }

    /**
     * Replaces the current values. The validator sees an empty list of existing values, since
     * a replacement discards them; it is applied before anything is changed, so a rejected
     * value leaves the parameter as it was.
     */
    private S replace(List<T> newValues) {
        if (!validator.apply(List.of(), newValues)) {
            throw new IllegalArgumentException("Parameter values are invalid.");
        }
        this.values.clear();
        this.values.addAll(newValues);
        return params;
    }

    @Override
    public List<T> defaultValue() {
        return defaultValues;
    }

    @Override
    @SuppressWarnings( {"unchecked", "rawtypes"})
    public boolean hasDefaultValue() {
        if (defaultValues == null) {
            return values.isEmpty();
        }
        if (defaultValues.size() != values.size()) {
            return false;
        }
        for (int i = 0; i < defaultValues.size(); i++) {
            if (defaultValues.get(i) instanceof ParametricEquals dvi) {
                if (!dvi.equalOnParams(values.get(i))) {
                    return false;
                }
            } else if (!defaultValues.get(i).equals(values.get(i))) {
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
