/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
 *
 */

package rapaio.data;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility tool to ease the specification of selection of variable,
 * based on lists or ranges of variable names.
 * Variable ranges can be specified directly as a list of variable indexes
 * or as a list of variable ranges.
 * <p>
 * Variable ranges syntax uses as range separator "~", and as column
 * range delimiter the comma ",". Thus "a~d" means all the variables, starting
 * with variable a and ending with variable d, inclusive. A single variable
 * name is also a range.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class VarRange {

    private static final String DELIMITER = ",";
    private static final String RANGE = "~";
    private static final String ALL = "all";
    private final String rawColumnRange;

    /**
     * Builds a var range directly from a list of var indexes.
     *
     * @param indexes list of var indexes
     */
    public VarRange(int... indexes) {
        if (indexes.length == 0) {
            throw new IllegalArgumentException("No column indexes specified.");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indexes.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(String.valueOf(indexes[i]));
        }
        this.rawColumnRange = sb.toString();
    }

    /**
     * Builds a var range from var ranges formatted as strings with the required syntax.
     *
     * @param ranges var ranges specified in string format
     */
    public VarRange(String... ranges) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(ranges).forEach(s -> {
            if (sb.length() > 0)
                sb.append(DELIMITER);
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
        if (ALL.equals(rawColumnRange)) {
            for (int i = 0; i < df.varCount(); i++) {
                colIndexes.add(i);
            }
            return colIndexes;
        }
        String[] ranges = rawColumnRange.split(DELIMITER);

        HashSet<String> colNames = new HashSet<>();
        for (int i = 0; i < df.varNames().length; i++) {
            colNames.add(df.varNames()[i]);
        }

        for (String range : ranges) {
            int start, end;

            if (range.contains(RANGE)) {
                String[] parts = range.split(RANGE);
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

    public List<String> parseInverseVarNames(Frame df) {
        Set<Integer> indexes = new HashSet(parseVarIndexes(df));
        return IntStream.range(0, df.varCount()).filter(i -> !indexes.contains(i)).boxed().map(i -> df.var(i).name()).collect(Collectors.toList());
    }
}
