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

/**
 * Tuple of length 2.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class Pair<T1, T2> implements Serializable {

    private static final long serialVersionUID = -1594916059995575867L;

    public final T1 a;
    public final T2 b;

    public Pair(T1 a, T2 b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public String toString() {
        return "Pair{" + a + ", " + b + "}";
    }

    public static <U, V> Pair<U, V> valueOf(U u, V v) {
        return new Pair<>(u, v);
    }
}
