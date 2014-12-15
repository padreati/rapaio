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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility tool to easy the specification of column specification by column ranges.
 * Column ranges can be specified directly as a list of column indexes or as a list of column ranges.
 * <p>
 * Column ranges syntax uses as range separator "-", and as column
 * range delimiter the comma ",".
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class VarRange {

    private static final String COL_DELIMITER = ",";
    private static final String COL_RANGE = "~";
    private static final String COL_ALL = "all";
    private final String rawColumnRange;

    /**
     * Builds a var range directly from a list of var indexes.
     *
     * @param varIndexes list of var indexes
     */
    public VarRange(int... varIndexes) {
        if (varIndexes.length == 0) {
            throw new IllegalArgumentException("No column indexes specified.");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < varIndexes.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(String.valueOf(varIndexes[i]));
        }
        this.rawColumnRange = sb.toString();
    }

    /**
     * Builds a var range from var ranges formatted as strings with the required syntax.
     *
     * @param varRanges var ranges specified in string format
     */
    public VarRange(String... varRanges) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(varRanges).forEach(s -> {
            if (sb.length() > 0)
                sb.append(COL_DELIMITER);
            sb.append(s);
        });
        this.rawColumnRange = sb.toString();
    }

    /**
     * Apply a var range over a frame, obtaining the list of var indexes for that frame.
     *
     * @param df target frame
     * @return a list of column indexes which corresponds to column range
     */
    public List<Integer> parseVarIndexes(Frame df) {
        List<Integer> colIndexes = new ArrayList<>();
        if (COL_ALL.equals(rawColumnRange)) {
            for (int i = 0; i < df.varCount(); i++) {
                colIndexes.add(i);
            }
            return colIndexes;
        }
        String[] ranges = rawColumnRange.split(COL_DELIMITER);

        HashSet<String> colNames = new HashSet<>();
        for (int i = 0; i < df.varNames().length; i++) {
            colNames.add(df.varNames()[i]);
        }

        for (String range : ranges) {
            int start, end;

            if (range.contains(COL_RANGE)) {
                String[] parts = range.split(COL_RANGE);
                if (!colNames.contains(parts[0])) {
                    start = Integer.parseInt(parts[0]);
                } else {
                    start = df.varIndex(parts[0]);
                }
                if (!colNames.contains(parts[1])) {
                    end = Integer.parseInt(parts[1]);
                } else {
                    end = df.varIndex(parts[1]);
                }
            } else {
                if (!colNames.contains(range)) {
                    start = Integer.parseInt(range);
                } else {
                    start = df.varIndex(range);
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

    public List<String> parseVarNames(Frame df) {
        return parseVarIndexes(df).stream().map(i -> df.varNames()[i]).collect(Collectors.toList());
    }
}
