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

package rapaio.data;

import rapaio.util.IntComparator;

import java.io.Serial;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class RowComparators implements Serializable {

    @Serial
    private static final long serialVersionUID = -3396667513004042385L;

    public static IntComparator from(final IntComparator... comparators) {
        return new AggregateComparator(comparators);
    }

    public static IntComparator labelComparator(final Var var, final boolean asc) {
        final int sign = asc ? 1 : -1;

        return (row1, row2) -> {
            boolean missing1 = var.isMissing(row1);
            boolean missing2 = var.isMissing(row2);
            if (!missing1 && !missing2) {
                return sign * var.getLabel(row1).compareTo(var.getLabel(row2));
            }
            if (missing1 && missing2) {
                return 0;
            }
            return missing1 ? -sign : sign;
        };
    }

    public static IntComparator doubleComparator(final Var var, final boolean asc) {
        final int sign = asc ? 1 : -1;
        return (i1, i2) -> sign * Double.compare(var.getDouble(i1), var.getDouble(i2));
    }

    public static IntComparator integerComparator(final Var var, final boolean asc) {
        final int sign = asc ? 1 : -1;
        return (i1, i2) -> sign * Integer.compare(var.getInt(i1), var.getInt(i2));
    }

    public static IntComparator longComparator(final Var var, final boolean asc) {
        final int sign = asc ? 1 : -1;
        return (i1, i2) -> sign * (var.getLong(i1) < var.getLong(i2) ? -1 : 1);
    }
}

class AggregateComparator implements IntComparator {

    private final IntComparator[] comparators;

    public AggregateComparator(IntComparator[] comparators) {
        this.comparators = Arrays.copyOf(comparators, comparators.length);
    }

    @Override
    public int compare(int row1, int row2) {
        for (Comparator<Integer> comparator : comparators) {
            int comp = comparator.compare(row1, row2);
            if (comp != 0) {
                return comp;
            }
        }
        return 0;
    }
}
