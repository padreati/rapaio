/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.linear.option;

import java.io.Serializable;
import java.util.Arrays;

public class AlgebraOptions implements Serializable {

    public static AlgebraOptions from(AlgebraOption<?>... opts) {
        return new AlgebraOptions(opts);
    }

    private static final AlgebraOptions defaults;

    static {
        defaults = new AlgebraOptions(null, new AlgebraOption[0]);
        defaults.copy = new AlgebraOptionCopy(false);
    }

    private final AlgebraOptions parent;

    private AlgebraOptionCopy copy;

    public AlgebraOptions(AlgebraOption<?>... options) {
        this(defaults, options);
    }

    public AlgebraOptions(AlgebraOptions parent, AlgebraOption<?>... options) {
        this.parent = parent;
        this.bind(options);
    }

    public AlgebraOptions bind(AlgebraOption<?>... options) {
        Arrays.stream(options).forEach(o -> o.bind(this));
        return this;
    }

    public void setCopy(AlgebraOptionCopy copy) {
        this.copy = copy;
    }

    public boolean isCopy() {
        if (copy == null) {
            return parent.isCopy();
        }
        return copy.apply(this);
    }

    public AlgebraOption<?>[] toArray() {
        return new AlgebraOption[] {
                copy
        };
    }
}
