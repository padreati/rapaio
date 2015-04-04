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

package rapaio.io.json.tree;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Base class for a json value. Json values are linked as top-down trees.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/16/15.
 */
public abstract class JsonValue {

    public static JsonNull NULL = new JsonNull();

    /**
     * @return true if the current instance is of type {@link JsonObject}, false otherwise
     */
    public boolean isObject() {
        return this instanceof JsonObject;
    }

    /**
     * @return true if the current instance is of type {@link JsonArray}, false otherwise
     */
    public boolean isArray() {
        return this instanceof JsonArray;
    }

    /**
     * @return true if the current instance is of type {@link JsonString}, false otherwise
     */
    public boolean isString() {
        return this instanceof JsonString;
    }

    /**
     * @return true if the current instance is of type {@link JsonBool}, false otherwise
     */
    public boolean isBool() {
        return this instanceof JsonBool;
    }

    /**
     * @return true if the current instance is of type {@link JsonNumber}, false otherwise
     */
    public boolean isNumber() {
        return this instanceof JsonNumber;
    }

    /**
     * @return true if the current instance is of type {@link JsonNull}, false otherwise
     */
    public boolean isNull() {
        return this instanceof JsonNull;
    }

    /**
     * @return the same instance casted to {@link JsonObject} class
     */
    public final JsonObject getObject() {
        return (JsonObject) this;
    }

    /**
     * @return the same instance casted to {@link JsonArray} class
     */
    public JsonArray getArray() {
        return (JsonArray) this;
    }

    /**
     * Get from object the value from specified property
     *
     * @param key property name
     * @return value instance, if property not defined returns {@link JsonValue#NULL}
     */
    public abstract JsonValue get(String key);

    public Set<String> keySet() {
        return getObject().keySet();
    }

    public Optional<String> findAnyKey() {
        return getObject().keySet().stream().findAny();
    }

    public abstract Optional<String> asString(String key);

    public abstract Optional<String> asString();

    public abstract Optional<Double> asDouble(String key);

    public abstract Optional<Double> asDouble();

    public abstract Optional<Boolean> asBool(String key);

    public abstract Optional<Boolean> asBool();

    public String pretty() {
        return pretty(0);
    }

    protected abstract String pretty(int level);

    protected String tabs(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("\t");
        }
        return sb.toString();
    }

    public final Stream<String> stringKeyValuePairs() {
        return stringKeyValuePairs("");
    }

    protected Stream<String> stringKeyValuePairs(String path) {
        return Stream.empty();
    }
}
