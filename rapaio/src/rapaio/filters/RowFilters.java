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
package rapaio.filters;

import rapaio.core.ColRange;
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
     * Shuffle the order of rows from specified vector.
     *
     * @param v source frame
     * @return shuffled frame
     */
    public static Vector shuffle(Vector v) {
        ArrayList<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < v.getRowCount(); i++) {
            mapping.add(v.getRowId(i));
        }
        for (int i = mapping.size(); i > 1; i--) {
            mapping.set(i - 1, mapping.set(RandomSource.nextInt(i), mapping.get(i - 1)));
        }
        return new MappedVector(v.getSourceVector(), new Mapping(mapping));
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
            mapping.add(df.getRowId(i));
        }
        for (int i = mapping.size(); i > 1; i--) {
            mapping.set(i - 1, mapping.set(RandomSource.nextInt(i), mapping.get(i - 1)));
        }
        return new MappedFrame(df.getSourceFrame(), new Mapping(mapping));
    }

    public static Vector sort(Vector v) {
        return sort(v, true);
    }

    public static Vector sort(Vector v, boolean asc) {
        if (v.getType().isNumeric()) {
            return sort(v, RowComparators.numericComparator(v, asc));
        }
        return sort(v, RowComparators.nominalComparator(v, asc));
    }

    @SafeVarargs
    public static Vector sort(Vector vector, Comparator<Integer>... comparators) {
        List<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < vector.getRowCount(); i++) {
            mapping.add(i);
        }
        Collections.sort(mapping, RowComparators.aggregateComparator(comparators));
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < mapping.size(); i++) {
            ids.add(vector.getRowId(mapping.get(i)));
        }
        return new MappedVector(vector.getSourceVector(), new Mapping(ids));
    }

    public static Frame sort(Frame df, Comparator<Integer>... comparators) {
        List<Integer> mapping = new ArrayList();
        for (int i = 0; i < df.getRowCount(); i++) {
            mapping.add(i);
        }
        Collections.sort(mapping, RowComparators.aggregateComparator(comparators));
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < mapping.size(); i++) {
            ids.add(df.getRowId(mapping.get(i)));
        }
        return new MappedFrame(df.getSourceFrame(), new Mapping(ids));
    }


    public static Frame delta(Frame source, Frame remove) {
        HashSet<Integer> existing = new HashSet<>();
        for (int i = 0; i < remove.getRowCount(); i++) {
            existing.add(remove.getRowId(i));
        }
        List<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < source.getRowCount(); i++) {
            int rowId = source.getRowId(i);
            if (!existing.contains(rowId)) {
                mapping.add(i);
            }
        }
        return new MappedFrame(source.getSourceFrame(), new Mapping(mapping));
    }

    /**
     * Returns a mapped frame with cases which does not contain missing values
     * in any column of the frame.
     *
     * @param source source frame
     * @return mapped frame with complete cases
     */
    public static Frame completeCases(Frame source) {
        return completeCases(source, new ColRange("all"));
    }

    public static Frame completeCases(Frame source, ColRange colRange) {
        List<Integer> selectedCols = colRange.parseColumnIndexes(source);
        List<Integer> ids = new ArrayList<>();
        for (int i = 0; i < source.getRowCount(); i++) {
            boolean complete = true;
            for (int col : selectedCols) {
                if (source.getCol(col).isMissing(i)) {
                    complete = false;
                    break;
                }
            }
            if (complete) {
                ids.add(source.getRowId(i));
            }
        }
        return new MappedFrame(source.getSourceFrame(), new Mapping(ids));
    }
}