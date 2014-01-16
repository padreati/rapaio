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

import java.util.Comparator;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class RowComparators {

    public static Comparator<Integer> aggregateComparator(final Comparator<Integer>... comparators) {
        return new Comparator<Integer>() {
            @Override
            public int compare(Integer row1, Integer row2) {
                for (Comparator<Integer> comparator : comparators) {
                    int comp = comparator.compare(row1, row2);
                    if (comp != 0) {
                        return comp;
                    }
                }
                return 0;
            }
        };
    }

    public static Comparator<Integer> nominalComparator(final Vector vector, final boolean asc) {
        final int sign = asc ? 1 : -1;

        return new Comparator<Integer>() {
            @Override
            public int compare(Integer row1, Integer row2) {
                if (vector.isMissing(row1) && vector.isMissing(row2)) {
                    return 0;
                }
                if (vector.isMissing(row1)) {
                    return -sign;
                }
                if (vector.isMissing(row2)) {
                    return sign;
                }
                return sign * vector.label(row1).compareTo(vector.label(row2));
            }
        };
    }

    public static Comparator<Integer> numericComparator(final Vector vector, final boolean asc) {
        final int sign = asc ? 1 : -1;
        return new Comparator<Integer>() {
            @Override
            public int compare(Integer row1, Integer row2) {
                if (vector.isMissing(row1) && vector.isMissing(row2)) {
                    return 0;
                }
                if (vector.isMissing(row1)) {
                    return -sign;
                }
                if (vector.isMissing(row2)) {
                    return sign;
                }
                if (vector.value(row1) == vector.value(row2)) {
                    return 0;
                }
                return sign * (vector.value(row1) < vector.value(row2) ? -1 : 1);
            }
        };
    }

    public static Comparator<Integer> indexComparator(final Vector vector, final boolean asc) {
        final int sign = asc ? 1 : -1;

        return new Comparator<Integer>() {
            @Override
            public int compare(Integer row1, Integer row2) {
                if (vector.isMissing(row1) && vector.isMissing(row2)) {
                    return 0;
                }
                if (vector.isMissing(row1)) {
                    return -1 * sign;
                }
                if (vector.isMissing(row2)) {
                    return sign;
                }
                if (vector.index(row1) == vector.index(row2)) {
                    return 0;
                }
                return sign * (vector.index(row1) < vector.index(row2) ? -1 : 1);
            }
        };
    }
}
