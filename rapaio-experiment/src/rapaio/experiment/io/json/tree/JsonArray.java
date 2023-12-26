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

package rapaio.experiment.io.json.tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/17/15.
 */
public final class JsonArray extends JsonValue {

    final List<JsonValue> array = new ArrayList<>();

    public void addValue(JsonValue value) {
        array.add(value);
    }

    @Override
    public Set<JsonValue> valueSet() {
        return new HashSet<>(array);
    }

    @Override
    public List<JsonValue> valueList() {
        return array;
    }

    public Stream<JsonValue> valueStream() {
        return array.stream();
    }

    @Override
    public Optional<String> asString() {
        return Optional.of(toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (JsonValue js : array) {
            if (sb.length() > 1) {
                sb.append(',');
            }
            sb.append(js.toString());
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    protected String pretty(int level) {
        StringBuilder sb = new StringBuilder();
        sb.append(tabs(level)).append("[\n");
        for (int i = 0; i < array.size(); i++) {
            JsonValue value = array.get(i);
            if (!(value.isArray() || value.isObject()))
                sb.append(tabs(level + 1));
            sb.append(value.pretty(level + 1));
            if (i != array.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append(tabs(level)).append("]");
        return sb.toString();
    }

    @Override
    protected Stream<String> stringPathStream(String path) {
        return array.stream().flatMap(js -> js.stringPathStream(path));
    }
}
