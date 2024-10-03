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

package rapaio.experiment.io.json.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/16/15.
 */
public final class JsonObject extends JsonValue {

    private final Map<String, JsonValue> map = new HashMap<>();

    public void addValue(String key, JsonValue value) {
        map.put(key, value);
    }

    public JsonValue get(String key) {
        JsonValue value = map.get(key);
        return (value != null) ? value : NULL;
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    @Override
    public List<String> keyList() {
        return new ArrayList<>(map.keySet());
    }

    @Override
    public Stream<String> keyStream() {
        return map.keySet().stream();
    }

    @Override
    public Set<JsonValue> valueSet() {
        return new HashSet<>(map.values());
    }

    @Override
    public List<JsonValue> valueList() {
        return new ArrayList<>(map.values());
    }

    @Override
    public Stream<JsonValue> valueStream() {
        return map.values().stream();
    }

    @Override
    public Set<Map.Entry<String, JsonValue>> entrySey() {
        return map.entrySet();
    }

    @Override
    public List<Map.Entry<String, JsonValue>> entryList() {
        return new ArrayList<>(map.entrySet());
    }

    @Override
    public Stream<Map.Entry<String, JsonValue>> entryStream() {
        return map.entrySet().stream();
    }

    @Override
    public Optional<String> asString(String key) {
        return Optional.ofNullable(map.get(key)).orElse(NULL).asString();
    }

    @Override
    public Optional<String> asString() {
        return Optional.of(toString());
    }

    public Optional<Double> asDouble(String key) {
        return Optional.ofNullable(map.get(key)).orElse(NULL).asDouble();
    }

    @Override
    public Optional<Double> asDouble() {
        return Optional.empty();
    }

    public Optional<Boolean> asBool(String key) {
        return Optional.ofNullable(map.get(key)).orElse(NULL).asBool();
    }

    @Override
    public Optional<Boolean> asBool() {
        return Optional.empty();
    }

    public JsonObject retain(String... keys) {
        JsonObject obj = new JsonObject();
        for (String key : keys) {
            obj.addValue(key, get(key));
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
            sb.append('\"').append(entry.getKey()).append("\":");
            if (entry.getValue() != null) {
                sb.append(entry.getValue());
            } else {
                sb.append("<null>");
            }
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    protected String pretty(int level) {
        StringBuilder sb = new StringBuilder();
        sb.append(tabs(level)).append("{\n");
        List<String> keyList = new ArrayList<>(keySet());
        Collections.sort(keyList);
        for (int i = 0; i < keyList.size(); i++) {
            String key = keyList.get(i);
            sb.append(tabs(level + 1)).append('\"').append(key).append("\":");

            JsonValue value = get(key);
            if (value.isObject() || value.isArray()) {
                sb.append('\n');
                sb.append(value.pretty(level + 1));
            } else {
                sb.append(value.pretty(level + 1));
            }
            if (i != keyList.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append(tabs(level)).append("}");
        return sb.toString();
    }

    @Override
    protected Stream<String> stringPathStream(String path) {
        Stream<String> s = Stream.empty();
        for (String key : keySet()) {
            JsonValue js = get(key);
            if (js instanceof JsonBool || js instanceof JsonNull || js instanceof JsonString || js instanceof JsonNumber) {
                s = Stream.concat(s, Stream.of(path + "." + key + ":" + js.asString().orElse("null")));
            } else {
                s = Stream.concat(s, js.stringPathStream(path + "." + key));
            }
        }
        return s;
    }
}
