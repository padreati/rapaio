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

package rapaio.printer.opt;

import java.io.Serializable;
import java.util.function.Function;

public final class POpt<V> extends NamedParam<POpts, V> implements Serializable {

    public POpt(String name, Function<POpts, V> fun) {
        super(name, fun);
    }

    public POpt(POpt<V> p, Function<POpts, V> fun) {
        super(p.getName(), fun);
    }
}
