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
import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.SortedVector;
import rapaio.data.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        return new MappedFrame(df, mapping);
    }

    /**
     * Sort ascending the values according to the type comparator
     *
     * @param v input values
     * @return sorted values
     */
    public static Vector sort(Vector v) {
        return sort(v, true);
    }

    /**
     * Sort the values according to the type comparator
     *
     * @param vector input vector
     * @param asc    true if ascending, false if descending
     * @return sorted values
     */
    public static Vector sort(Vector vector, boolean asc) {
        List<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < vector.getRowCount(); i++) {
            mapping.add(i);
        }
        Collections.sort(mapping, vector.getComparator(asc));
        return new SortedVector(vector, mapping);
    }


}
