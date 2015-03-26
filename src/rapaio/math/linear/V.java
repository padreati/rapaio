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

package rapaio.math.linear;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/6/15.
 */
public interface V extends M {

    default double get(int i) {
        if (rowCount() == 1)
            return get(0, i);
        if (colCount() == 1)
            return get(i, 0);
        throw new IllegalArgumentException("This shortcut method can be called only for vectors or special matrices");
    }

    default void set(int i, double value) {
        if (rowCount() == 1) {
            set(0, i, value);
            return;
        }
        if (colCount() == 1) {
            set(i, 0, value);
            return;
        }
        throw new IllegalArgumentException("This shortcut method can be called only for vectors");
    }

    default double dotProd(V b) {
        int max = Math.min(Math.max(rowCount(), colCount()), Math.max(b.rowCount(), b.colCount()));
        double s = 0;
        for (int i = 0; i < max; i++) {
            s += get(i) * b.get(i);
        }
        return s;
    }
}
