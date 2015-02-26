/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
 */

package rapaio.experiment.json.tree;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/16/15.
 */
public final class JsonObject extends AbstractJsonValue {

    public static final JsonObject NULL = new JsonObject();

    private Map<String, JsonValue> map = new HashMap<>();

    public void addValue(String key, JsonValue value) {
        map.put(key, value);
    }

    public JsonValue getValue(String key) {
        if (map.containsKey(key)) {
            return map.get(key);
        }
        return JsonValue.NULL;
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public String stringValue(String key) {
        if (map.containsKey(key))
            return map.get(key).stringValue();
        return "";
    }

    @Override
    public String stringValue() {
        return "";
    }

    public JsonObject retain(String... keys) {
        JsonObject obj = new JsonObject();
        for (String key : keys) {
            obj.addValue(key, getValue(key));
        }
        return obj;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (Map.Entry<String, JsonValue> entry : map.entrySet()) {
            if (sb.length() > 1)
                sb.append(",");
            sb.append('\"').append(entry.getKey()).append("\":").append(entry.getValue().toString());
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    protected String pretty(int level) {
        StringBuilder sb = new StringBuilder();
        sb.append(tabs(level)).append("{\n");
        boolean first = true;
        for (String key : keySet()) {
            sb.append(tabs(level + 1)).append('\"').append(key).append("\":");

            AbstractJsonValue value = (AbstractJsonValue) getValue(key);
            if (value.isObject() || value.isArray()) {
                sb.append('\n');
                sb.append(value.pretty(level + 1));
            } else {
                sb.append(value.pretty(level + 1));
            }
            if (first) {
                first = false;
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append(tabs(level)).append("}");
        return sb.toString();
    }
}
