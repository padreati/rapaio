/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

package rapaio.filters;

import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.data.Vector;

import java.util.*;

/**
 * Provides filters which manipulates rows from a frame.
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class RowFilters {

    private RowFilters() {
    }

    /**
     * Shuffle the order of rows from specified frame.
     *
     * @param df source frame
     * @return shuffled frame
     */
    public static Frame shuffle(Frame df) {
        ArrayList<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < df.getRowCount(); i++) {
            mapping.add(i);
        }
        for (int i = mapping.size(); i > 1; i--) {
            mapping.set(i - 1, mapping.set(RandomSource.nextInt(i), mapping.get(i - 1)));
        }
        return new MappedFrame(df, new Mapping(mapping));
    }

    public static Vector sort(Vector v) {
        return sort(v, true);
    }

    public static Vector sort(Vector v, boolean asc) {
        if (v.isNumeric()) {
            return sort(v.getName(), v, RowComparators.numericComparator(v, asc));
        }
        return sort(v.getName(), v, RowComparators.nominalComparator(v, asc));
    }

    public static Vector sort(Vector vector, Comparator<Integer>... comparators) {
        return sort(vector.getName(), vector, comparators);
    }

    public static Vector sort(String name, Vector vector, Comparator<Integer>... comparators) {
        List<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < vector.getRowCount(); i++) {
            mapping.add(i);
        }
        Collections.sort(mapping, RowComparators.aggregateComparator(comparators));
        return new MappedVector(name, vector, new Mapping(mapping));
    }

    public static Frame sort(Frame df, Comparator<Integer>... comparators) {
        return sort(df.getName(), df, comparators);
    }

    public static Frame sort(String name, Frame df, Comparator<Integer>... comparators) {
        List<Integer> mapping = new ArrayList();
        for (int i = 0; i < df.getRowCount(); i++) {
            mapping.add(i);
        }
        Collections.sort(mapping, RowComparators.aggregateComparator(comparators));
        return new MappedFrame(name, df, new Mapping(mapping));
    }
}