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

package rapaio.util;

import java.io.Serializable;
import java.util.Optional;

/**
 * Utility class for a tagged object. A tagged object is an object which has a name.
 * Tag objects has a name and a value.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/25/15.
 */
public final class Tag<T extends Serializable> implements Serializable {

    private static final long serialVersionUID = -1158395816197889041L;

    private final String name;
    private T value;

    public static <U extends Serializable> Tag<U> valueOf(String name, U value) {
        return new Tag<>(name, value);
    }

    private Tag(String name, T value) {
        if (name == null)
            throw new IllegalArgumentException("tagged objects must have a name");
        this.name = name;
        this.value = value;
    }

    public String name() {
        return name;
    }

    public T get() {
        return value;
    }

    public T orElse(T other) {
        return Optional.of(value).orElse(other);
    }

    public Tag<T> with(T value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return "Tag{" + name + ':' + value.getClass().getName() + '}';
    }
}
