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

package rapaio.util.param;

import java.io.Serial;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import rapaio.util.function.SFunction;

/**
 * This implementation models a parameter which is a map of keys and values.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/3/20.
 */
public class MultiParam<K, T, S extends ParamSet<S>> implements Param<Map<K, T>, S> {

    @Serial
    private static final long serialVersionUID = 6971154885891319057L;
    private final S params;
    private final TreeMap<K, T> defaultValue;
    private final TreeMap<K, T> valueMap;
    private final String name;
    private final SFunction<Map<K, T>, Boolean> validator;

    public MultiParam(S params, Map<K, T> defaultValue, String name, SFunction<Map<K, T>, Boolean> validator) {
        this.params = params;
        this.defaultValue = new TreeMap<>(defaultValue);
        this.valueMap = new TreeMap<>(defaultValue);
        this.name = name;
        this.validator = validator;

        params.registerParameter(this);
    }

    @Override
    public Map<K, T> get() {
        return valueMap;
    }

    public T get(K key) {
        return valueMap.get(key);
    }

    @Override
    public S set(Map<K, T> value) {
        this.valueMap.clear();
        this.valueMap.putAll(value);
        return params;
    }

    public S add(Map<K, T> value) {
        this.valueMap.putAll(value);
        return params;
    }

    public S add(K key, T value) {
        this.valueMap.put(key, value);
        return params;
    }

    @Override
    public Map<K, T> defaultValue() {
        return defaultValue;
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
            if (comp == null) {
                return false;
            }
            if (ref instanceof ParametricEquals) {
                boolean eq = ((ParametricEquals<T>) ref).equalOnParams(comp);
                if (!eq) {
                    return false;
                }
            } else {
                if (!Objects.equals(ref, comp)) {
                    return false;
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
    public boolean validate(Map<K, T> value) {
        return validator.apply(value);
    }
}
