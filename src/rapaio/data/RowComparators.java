/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

package rapaio.data;

import java.io.Serializable;
import java.util.Comparator;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class RowComparators implements Serializable {

    @SafeVarargs
    public static Comparator<Integer> aggregateComparator(final Comparator<Integer>... comparators) {
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

    public static Comparator<Integer> nominal(final Var var, final boolean asc) {
        final int sign = asc ? 1 : -1;

        return (row1, row2) -> {
            if (var.missing(row1) && var.missing(row2)) {
                return 0;
            }
            if (var.missing(row1)) {
                return -sign;
            }
            if (var.missing(row2)) {
                return sign;
            }
            return sign * var.label(row1).compareTo(var.label(row2));
        };
    }

    public static Comparator<Integer> numeric(final Var var, final boolean asc) {
        final int sign = asc ? 1 : -1;
        return (row1, row2) -> {
            if (var.missing(row1) && var.missing(row2)) {
                return 0;
            }
            if (var.missing(row1)) {
                return -sign;
            }
            if (var.missing(row2)) {
                return sign;
            }
            if (var.value(row1) == var.value(row2)) {
                return 0;
            }
            return sign * (var.value(row1) < var.value(row2) ? -1 : 1);
        };
    }

    public static Comparator<Integer> index(final Var var, final boolean asc) {
        final int sign = asc ? 1 : -1;

        return (row1, row2) -> {
            if (var.missing(row1) && var.missing(row2)) {
                return 0;
            }
            if (var.missing(row1)) {
                return -1 * sign;
            }
            if (var.missing(row2)) {
                return sign;
            }
            if (var.index(row1) == var.index(row2)) {
                return 0;
            }
            return sign * (var.index(row1) < var.index(row2) ? -1 : 1);
        };
    }
}
