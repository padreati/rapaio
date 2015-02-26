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

import java.util.Set;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/16/15.
 */
public interface JsonValue {

    JsonNull NULL = new JsonNull();

    default JsonObject object() {
        return (JsonObject) this;
    }

    default JsonArray array() {
        return (JsonArray) this;
    }

    default boolean isObject() {
        return this instanceof JsonObject;
    }

    default boolean isArray() {
        return this instanceof JsonArray;
    }

    default JsonValue getValue(String key) {
        return JsonObject.NULL;
    }

    default Set<String> keySet() {
        return object().keySet();
    }

    default String singleKey() {
        if (object().keySet().size() == 1)
            return object().keySet().iterator().next();
        return "";
    }

    default String stringValue(String key) {
        return "";
    }

    default double doubleValue() {
        return Double.NaN;
    }

    default boolean boolValue() {
        return false;
    }

    String stringValue();

    String pretty();
}
