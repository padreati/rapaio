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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/17/15.
 */
public class JsonArray extends JsonValue {

    List<JsonValue> array = new ArrayList<>();

    @Override
    public JsonType type() {
        return JsonType.ARRAY;
    }

    public void addValue(JsonValue value) {
        array.add(value);
    }

    public List<JsonValue> values() {
        return array;
    }

    @Override
    public String stringValue(String key) {
        return "";
    }

    @Override
    public String stringValue() {
        return "";
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
}
