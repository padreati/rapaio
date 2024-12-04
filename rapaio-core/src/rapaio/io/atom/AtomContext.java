/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.io.atom;

import java.util.HashMap;
import java.util.Map;

public final class AtomContext {

    public static final String ARRAY_MANAGER = "AM";
    public static final String TENSOR_MANAGER = "TM";

    private final Map<String, Object> values = new HashMap<>();

    public AtomContext(Map<String, Object> values) {
        if(values != null) {
            this.values.putAll(values);
        }
    }

    public AtomContext with(String key, Object value) {
        this.values.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(Class<T> type, String key) {
        Object value = values.get(key);
        if(value == null) {
            return null;
        }
        if(type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        throw new IllegalArgumentException("Cannot cast " + value + " to " + type);
    }
}
