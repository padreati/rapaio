/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility tool to ease the specification of selection of variables
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
public interface VarRange {

    static VarRange all() {
        return new VarRangeByName(VarRangeByName.ALL);
    }

    static VarRange of(String... varNames) {
        return new VarRangeByName(varNames);
    }

    static VarRange of(Collection<String> varNames) {
        String[] names = new String[varNames.size()];
        int i = 0;
        for (String varName : varNames) {
            names[i++] = varName;
        }
        return new VarRangeByName(names);
    }

    static VarRange of(int... varIndexes) {
        return new VarRangeByName(varIndexes);
    }

    static VarRange byName(Predicate<String> filter) {
        return new VarRangeByPredName(filter);
    }

    static VarRange byFilter(Predicate<Var> filter) {
        return new VarRangeByPred(filter);
    }

    static VarRange onlyTypes(VarType... types) {
        Set<VarType> keep = Arrays.stream(types).collect(Collectors.toSet());
        return new VarRangeByPred(var -> keep.contains(var.type()));
    }

    List<Integer> parseVarIndexes(Frame df);

    List<String> parseVarNames(Frame df);

    List<String> parseInverseVarNames(Frame df);
}

class VarRangeByName implements VarRange {

    public static final String DELIMITER = ",";
    public static final String RANGE_SEPARATOR = "~";
    public static final String ALL = "all";
    private final String rawColumnRange;

    /**
     * Builds a var range directly from a list of var indexes.
     *
     * @param indexes list of var indexes
     */
    public VarRangeByName(int... indexes) {
        if (indexes == null || indexes.length == 0) {
            throw new IllegalArgumentException("No column indexes specified.");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indexes.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(indexes[i]);
        }
        this.rawColumnRange = sb.toString();
    }

    /**
     * Builds a var range from var ranges formatted as strings with the required syntax.
     *
     * @param ranges var ranges specified in string format
     */
    public VarRangeByName(String... ranges) {
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
     * If a variable is not found, than it will be omitted.
     *
     * @param df target frame
     * @return a list of column indexes which corresponds to column range
     */
    @Override
    public List<Integer> parseVarIndexes(Frame df) {
        List<Integer> colIndexes = new ArrayList<>();
        if (ALL.equals(rawColumnRange)) {
            for (int i = 0; i < df.varCount(); i++) {
                colIndexes.add(i);
            }
            return colIndexes;
        }
        String[] ranges = rawColumnRange.split(DELIMITER);
        Set<String> colNames = df.varStream().map(Var::name).collect(Collectors.toSet());

        for (String range : ranges) {
            int start, end;

            if (range.contains(RANGE_SEPARATOR)) {
                String[] parts = range.split(RANGE_SEPARATOR);
                if (!colNames.contains(parts[0])) {
                    start = parseInt(parts[0]);
                } else {
                    start = df.varIndex(parts[0]);
                }
                if (!colNames.contains(parts[1])) {
                    end = parseInt(parts[1]);
                } else {
                    end = df.varIndex(parts[1]);
                }
            } else {
                if (range.trim().isEmpty()) {
                    continue;
                }
                if (!colNames.contains(range)) {
                    start = parseInt(range);
                } else {
                    start = df.varIndex(range);
                }
                end = start;
            }
            if (start == -1 || end == -1) {
                continue;
            }
            for (int j = start; j <= end; j++) {
                colIndexes.add(j);
            }
        }
        return colIndexes;
    }

    private int parseInt(String token) {
        try {
            return Integer.parseInt(token);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    @Override
    public List<String> parseVarNames(Frame df) {
        return parseVarIndexes(df).stream().map(i -> df.varNames()[i]).collect(Collectors.toList());
    }

    @Override
    public List<String> parseInverseVarNames(Frame df) {
        Set<Integer> indexes = new HashSet<>(parseVarIndexes(df));
        return IntStream.range(0, df.varCount()).filter(i -> !indexes.contains(i)).boxed().map(i -> df.rvar(i).name()).collect(Collectors.toList());
    }
}

record VarRangeByPredName(Predicate<String> predicate) implements VarRange {

    @Override
    public List<Integer> parseVarIndexes(Frame df) {
        return IntStream.range(0, df.varCount())
                .filter(i -> predicate.test(df.rvar(i).name()))
                .boxed()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> parseVarNames(Frame df) {
        return df.varStream().map(Var::name)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> parseInverseVarNames(Frame df) {
        return df.varStream().map(Var::name)
                .filter(name -> !predicate.test(name))
                .collect(Collectors.toList());
    }
}

record VarRangeByPred(Predicate<Var> predicate) implements VarRange {

    @Override
    public List<Integer> parseVarIndexes(Frame df) {
        return IntStream.range(0, df.varCount())
                .filter(i -> predicate.test(df.rvar(i)))
                .boxed()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> parseVarNames(Frame df) {
        return df.varStream()
                .filter(predicate)
                .map(Var::name)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> parseInverseVarNames(Frame df) {
        return df.varStream()
                .filter(var -> !predicate.test(var))
                .map(Var::name)
                .collect(Collectors.toList());
    }
}