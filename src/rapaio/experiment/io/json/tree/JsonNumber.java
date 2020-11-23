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

import java.util.Objects;
import java.util.Optional;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/26/15.
 */
public class JsonNumber extends JsonValue {

    private final String original;

    public JsonNumber(String original) {
        this.original = original;
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
        return Optional.ofNullable(Double.parseDouble(original));
    }

    @Override
    public Optional<Boolean> asBool(String key) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> asBool() {
        return Optional.empty();
    }

    @Override
    protected String pretty(int level) {
        return asString().get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonNumber that = (JsonNumber) o;
        return Objects.equals(original, that.original);
    }

    @Override
    public int hashCode() {
        return original != null ? original.hashCode() : 0;
    }

    @Override
    public String toString() {
        return original;
    }
}
