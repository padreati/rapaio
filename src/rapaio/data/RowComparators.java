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

package rapaio.data;

import it.unimi.dsi.fastutil.ints.IntComparator;

import java.io.Serializable;
import java.util.Comparator;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class RowComparators implements Serializable {

    private static final long serialVersionUID = -3396667513004042385L;

    public static IntComparator from(final IntComparator... comparators) {
        return (row1, row2) -> {
            for (Comparator<Integer> comparator : comparators) {
                int comp = comparator.compare(row1, row2);
                if (comp != 0) {
                    return comp;
                }
            }
            return 0;
        };
    }

    public static IntComparator nominal(final Var var, final boolean asc) {
        final int sign = asc ? 1 : -1;

        return (row1, row2) -> {
            if (var.isMissing(row1) && var.isMissing(row2)) {
                return 0;
            }
            if (var.isMissing(row1)) {
                return -sign;
            }
            if (var.isMissing(row2)) {
                return sign;
            }
            return sign * var.getLabel(row1).compareTo(var.getLabel(row2));
        };
    }

    public static IntComparator numeric(final Var var, final boolean asc) {
        final int sign = asc ? 1 : -1;
        return (row1, row2) -> {
            double d1 = var.getDouble(row1);
            double d2 = var.getDouble(row2);
            if (d1 < d2)
                return -sign;           // Neither val is NaN, thisVal is smaller
            if (d1 > d2)
                return sign;            // Neither val is NaN, thisVal is larger

            // Cannot use doubleToRawLongBits because of possibility of NaNs.
            long thisBits = Double.doubleToLongBits(d1);
            long anotherBits = Double.doubleToLongBits(d2);

            return (thisBits == anotherBits ? 0 : // Values are equal
                    (thisBits < anotherBits ? sign : // (-0.0, 0.0) or (!NaN, NaN)
                            -sign));                          // (0.0, -0.0) or (NaN, !NaN)
        };
    }

    public static IntComparator index(final Var var, final boolean asc) {
        final int sign = asc ? 1 : -1;
        return (row1, row2) -> sign * Integer.compare(var.getInt(row1), var.getInt(row2));
    }

    public static IntComparator stamp(final Var var, final boolean asc) {
        final int sign = asc ? 1 : -1;

        return (row1, row2) -> {
            if (var.isMissing(row1) && var.isMissing(row2)) {
                return 0;
            }
            if (var.isMissing(row1)) {
                return -1 * sign;
            }
            if (var.isMissing(row2)) {
                return sign;
            }
            if (var.getLong(row1) == var.getLong(row2)) {
                return 0;
            }
            return sign * (var.getLong(row1) < var.getLong(row2) ? -1 : 1);
        };
    }
}
