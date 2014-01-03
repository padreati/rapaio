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
package rapaio.core;

import rapaio.data.Frame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Builds a list of column indexes from column ranges in string
 * format when applied to a data frame. Used as utility tool to easy
 * the specification of column indexes.
 * <p/>
 * Column ranges can be specified directly as a list of column indexes.
 * <p/>
 * Column ranges syntax uses as range separator "-", and as column
 * range delimiter the comma ",".
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ColRange {

    private static final String COL_DELIMITER = ",";
    private static final String COL_RANGE = "-";
    private static final String COL_ALL = "all";
    private final String rawColumnRange;

    /**
     * Builds a column range directly from a list of column indexes.
     *
     * @param colIndexes list of column indexes
     */
    public ColRange(int... colIndexes) {
        if (colIndexes.length == 0) {
            throw new IllegalArgumentException("No column indexes specified.");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < colIndexes.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(String.valueOf(colIndexes[i]));
        }
        this.rawColumnRange = sb.toString();
    }

    /**
     * Builds a column range from column ranges formatted as string
     * with required syntax.
     *
     * @param rawColumnRange column ranges specified in string format
     */
    public ColRange(String rawColumnRange) {
        this.rawColumnRange = rawColumnRange;
    }

    /**
     * Apply a column range over a frame, obtaining the list of
     * column indexes for that frame.
     *
     * @param df target frame
     * @return a list of column indexes which corresponds to column range
     */
    public List<Integer> parseColumnIndexes(Frame df) {
        List<Integer> colIndexes = new ArrayList<>();
        if ("all".equals(rawColumnRange)) {
            for (int i = 0; i < df.getColCount(); i++) {
                colIndexes.add(i);
            }
            return colIndexes;
        }
        String[] ranges = rawColumnRange.split(COL_DELIMITER);

        HashSet<String> colNames = new HashSet<>();
        for (int i = 0; i < df.getColNames().length; i++) {
            colNames.add(df.getColNames()[i]);
        }

        for (String range : ranges) {
            int start, end;

            if (range.contains(COL_RANGE)) {
                String[] parts = range.split(COL_RANGE);
                if (!colNames.contains(parts[0])) {
                    start = Integer.parseInt(parts[0]);
                } else {
                    start = df.getColIndex(parts[0]);
                }
                if (!colNames.contains(parts[1])) {
                    end = Integer.parseInt(parts[1]);
                } else {
                    end = df.getColIndex(parts[1]);
                }
            } else {
                if (!colNames.contains(range)) {
                    start = Integer.parseInt(range);
                } else {
                    start = df.getColIndex(range);
                }
                end = start;
            }

            for (int j = start; j <= end; j++) {
                colIndexes.add(j);
            }
        }
        Collections.sort(colIndexes);
        return colIndexes;
    }
}
