/*
 * Copyright 2013 Aurelian Tutuianu
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

package rapaio.data.util;

import rapaio.data.Frame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ColumnRange {

    private static final String COL_DELIMITER = ",";
    private static final String COL_RANGE = "-";
    private final String rawColumnRange;

    public ColumnRange(int... colIndexes) {
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

    public ColumnRange(String rawColumnRange) {
        this.rawColumnRange = rawColumnRange;
    }

    public List<Integer> parseColumnIndexes(Frame df) {
        String[] ranges = rawColumnRange.split(COL_DELIMITER);
        List<Integer> colIndexes = new ArrayList<>();

        HashSet<String> colNames = new HashSet<>();
        for (int i = 0; i < df.getColNames().length; i++) {
            colNames.add(df.getColNames()[i]);
        }

        for (int i = 0; i < ranges.length; i++) {
            String range = ranges[i];
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
