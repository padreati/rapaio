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

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.core.ColumnRange;

import java.util.List;

/**
 * Provides filters which manipulate columns from a frame.
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class ColFilters {

    private ColFilters() {
    }

    /**
     * Remove columns specified in a column range from a frame.
     *
     * @param df       frame
     * @param colRange column range
     * @return original frame without columns specified in {@param colRange}
     */
    public static Frame removeCols(Frame df, String colRange) {
        ColumnRange range = new ColumnRange(colRange);
        final List<Integer> indexes = range.parseColumnIndexes(df);
        Vector[] vectors = new Vector[df.getColCount() - indexes.size()];
        int posIndexes = 0;
        int posFinal = 0;
        for (int i = 0; i < df.getColCount(); i++) {
            if (posIndexes < indexes.size() && i == indexes.get(posIndexes)) {
                posIndexes++;
                continue;
            }
            vectors[posFinal] = df.getCol(i);
            posFinal++;
        }
        return new SolidFrame(df.getName(), df.getRowCount(), vectors);
    }

    /**
     * Remove columns from a frame by specifying which columns to keep.
     *
     * @param df       frame
     * @param colRange column range
     * @return original frame which has only columns specified in {@param colRange}
     */
    public static Frame retainCols(Frame df, String colRange) {
        ColumnRange range = new ColumnRange(colRange);
        final List<Integer> indexes = range.parseColumnIndexes(df);
        Vector[] vectors = new Vector[indexes.size()];
        int posIndexes = 0;
        for (int i = 0; i < df.getColCount(); i++) {
            if (posIndexes < indexes.size() && i == indexes.get(posIndexes)) {
                vectors[posIndexes] = df.getCol(i);
                posIndexes++;
            }
        }
        return new SolidFrame(df.getName(), df.getRowCount(), vectors);
    }

}
