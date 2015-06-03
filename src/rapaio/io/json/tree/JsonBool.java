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
 *
 */

package rapaio.io.json.tree;

import java.util.Optional;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/26/15.
 */
public final class JsonBool extends JsonValue {

    private final Boolean value;
    private final String original;

    public JsonBool(String original) {
        this.original = original;
        if ("true".equals(original))
            this.value = true;
        else if ("false".equals(original)) {
            this.value = false;
        } else
            this.value = null;
    }

    @Override
    public JsonValue get(String key) {
        return JsonValue.NULL;
    }

    @Override
    public Optional<String> asString(String key) {
        return Optional.of(original);
    }

    @Override
    public Optional<String> asString() {
        return Optional.of(original);
    }

    @Override
    public Optional<Double> asDouble(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<Double> asDouble() {
        return asBool().flatMap(value -> value ? Optional.of(1d) : Optional.of(0d));
    }

    @Override
    public Optional<Boolean> asBool(String key) {
        return Optional.empty();
    }

    public Optional<Boolean> asBool() {
        return Optional.of(value);
    }

    @Override
    protected String pretty(int level) {
        return asString().get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonBool jsonBool = (JsonBool) o;
        return value == jsonBool.value;
    }

    @Override
    public int hashCode() {
        return (value ? 1 : 0);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
