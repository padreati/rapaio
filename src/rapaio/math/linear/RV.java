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

package rapaio.math.linear;

import rapaio.data.Var;
import rapaio.math.linear.impl.SolidRV;

/**
 * Real valued vector interface
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/6/15.
 */
public interface RV extends RM {

    static RV empty(int len) {
        return new SolidRV(len);
    }

    static RV copyOf(Var x) {
        RV copy = empty(x.rowCount());
        for (int i = 0; i < x.rowCount(); i++) {
            copy.set(i, x.value(i));
        }
        return copy;
    }

    static RV copyOf(RV x) {
        RV copy = empty(Math.max(x.rowCount(), x.colCount()));
        for (int i = 0; i < x.rowCount(); i++) {
            copy.set(i, x.get(i));
        }
        return copy;
    }

    static RV fill(int len, double value) {
        RV result = new SolidRV(len);
        for (int i = 0; i < len; i++) {
            result.set(i, value);
        }
        return result;
    }

    /**
     * Additional single index accessor for vector elements
     *
     * @param i position of the element
     * @return value at the given position
     */
    default double get(int i) {
        if (rowCount() == 1)
            return get(0, i);
        if (colCount() == 1)
            return get(i, 0);
        throw new IllegalArgumentException("This shortcut method can be called only for vectors or special matrices");
    }

    /**
     * Additional single index setter for vector elements
     *
     * @param i     position of the elements
     * @param value new value for the given position
     */
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

    default void increment(int i, double increment) {
        set(i, get(i) + increment);
    }

    /**
     * Dot product between two vectors is equal to the sum of the
     * product of elements from each given position.
     * <p>
     * sum_{i=1}^{n}a_i*b_i
     *
     * @param b
     * @return
     */
    default double dotProd(RV b) {
        int max = Math.max(Math.max(rowCount(), colCount()), Math.max(b.rowCount(), b.colCount()));
        double s = 0;
        for (int i = 0; i < max; i++) {
            s += get(i) * b.get(i);
        }
        return s;
    }

    /**
     * Makes a solid copy of the matrix
     *
     * @return new solid copy of the matrix
     */
    default RV copy() {
        if (rowCount() > colCount()) {
            RV rv = new SolidRV(rowCount());
            for (int i = 0; i < rowCount(); i++) {
                rv.set(i, get(i));
            }
            return rv;
        } else {
            RV rv = new SolidRV(colCount());
            for (int i = 0; i < colCount(); i++) {
                rv.set(i, get(i));
            }
            return rv;
        }
    }

    default double norm(double p) {
        double s = 0.0;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                s += Math.pow(get(i, j), p);
            }
        }
        return Math.pow(s, 1 / p);
    }

    default RV normalize(double p) {
        double norm = norm(p);
        if (norm != 0.0)
            dot(1.0 / norm);
        return this;
    }
}
