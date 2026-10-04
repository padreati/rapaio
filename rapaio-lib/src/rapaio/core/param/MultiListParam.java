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
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import rapaio.util.function.SFunction;

/**
 * This implementation models a parameter which is a map of keys and values.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/3/20.
 */
public class MultiListParam<K, T, S extends ParamSet<S>> implements Param<Map<K, List<T>>, S> {

    @Serial
    private static final long serialVersionUID = -2477338925409713295L;
    private final S params;
    private final TreeMap<K, List<T>> defaultValue;
    private final TreeMap<K, List<T>> valueMap;
    private final String name;
    private final SFunction<Map<K, List<T>>, Boolean> validator;

    public MultiListParam(S params, Map<K, List<T>> defaultValue, String name,
                          SFunction<Map<K, List<T>>, Boolean> validator) {
        this.params = params;
        this.defaultValue = new TreeMap<>(defaultValue);
        this.valueMap = new TreeMap<>(defaultValue);
        this.name = name;
        this.validator = validator;

        params.registerParameter(this);
    }

    /**
     * @return an unmodifiable view of the current map; use {@link #set(Map)}, {@link #add(Map)},
     * {@link #add(Object, Object[])} or {@link #clear()} to change it so that the validator is applied
     */
    @Override
    public Map<K, List<T>> get() {
        return Collections.unmodifiableMap(valueMap);
    }

    public List<T> get(K key) {
        List<T> list = valueMap.get(key);
        return list == null ? null : Collections.unmodifiableList(list);
    }

    @Override
    public S set(Map<K, List<T>> value) {
        // a defensive copy of the map and of every list, taken before anything is discarded: the argument can be the
        // live view of this parameter, which clearing would empty, and the lists must not be shared with the
        // caller, who could change them afterwards without the validator being applied
        TreeMap<K, List<T>> copy = copyOf(value);
        checkValid(copy);
        this.valueMap.clear();
        this.valueMap.putAll(copy);
        return params;
    }

    public S add(Map<K, List<T>> value) {
        TreeMap<K, List<T>> copy = copyOf(value);
        TreeMap<K, List<T>> merged = new TreeMap<>(valueMap);
        merged.putAll(copy);
        checkValid(merged);
        this.valueMap.putAll(copy);
        return params;
    }

    @SafeVarargs
    public final S add(K key, T... values) {
        TreeMap<K, List<T>> merged = new TreeMap<>(valueMap);
        merged.put(key, new ArrayList<>(Arrays.asList(values)));
        checkValid(merged);
        this.valueMap.put(key, new ArrayList<>(Arrays.asList(values)));
        return params;
    }

    private static <K, T> TreeMap<K, List<T>> copyOf(Map<K, List<T>> value) {
        TreeMap<K, List<T>> copy = new TreeMap<>();
        for (var entry : value.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    /**
     * Applies the validator before anything is changed, so a rejected value leaves the
     * parameter as it was.
     */
    private void checkValid(Map<K, List<T>> value) {
        if (!validate(value)) {
            throw new IllegalArgumentException("Parameter value is invalid: " + value);
        }
    }

    @Override
    public Map<K, List<T>> defaultValue() {
        return defaultValue;
    }

    public K getReverseKey(T value) {
        for (Map.Entry<K, List<T>> entry : valueMap.entrySet()) {
            if (entry.getValue().contains(value)) {
                return entry.getKey();
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean hasDefaultValue() {
        if (defaultValue == null) {
            return valueMap == null;
        }
        if (valueMap == null) {
            return false;
        }
        if (defaultValue.size() != valueMap.size()) {
            return false;
        }

        for (var entry : defaultValue.entrySet()) {
            var ref = entry.getValue();
            var comp = valueMap.get(entry.getKey());
            if (comp == null || comp.size() != ref.size()) {
                return false;
            }
            for (int i = 0; i < ref.size(); i++) {
                T value = ref.get(i);
                if (value instanceof ParametricEquals) {
                    boolean eq = ((ParametricEquals<T>) value).equalOnParams(comp.get(i));
                    if (!eq) {
                        return false;
                    }
                } else {
                    if (!Objects.equals(value, comp.get(i))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public S clear() {
        valueMap.clear();
        valueMap.putAll(defaultValue);
        return params;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean validate(Map<K, List<T>> value) {
        return validator.apply(value);
    }
}
