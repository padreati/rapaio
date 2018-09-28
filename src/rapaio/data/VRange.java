/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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
public interface VRange {

    static VRange all() {
        return new VRangeByName(VRangeByName.ALL);
    }

    static VRange of(String... varNames) {
        return new VRangeByName(varNames);
    }

    static VRange of(Collection<String> varNames) {
        String[] names = new String[varNames.size()];
        int i = 0;
        for (String varName : varNames) {
            names[i++] = varName;
        }
        return new VRangeByName(names);
    }

    static VRange of(int... varIndexes) {
        return new VRangeByName(varIndexes);
    }

    static VRange byName(Predicate<String> filter) {
        return new VRangeByPredName(filter);
    }

    static VRange byFilter(Predicate<Var> filter) {
        return new VRangeByPred(filter);
    }

    static VRange onlyTypes(VType... types) {
        Set<VType> keep = Arrays.stream(types).collect(Collectors.toSet());
        return new VRangeByPred(var -> keep.contains(var.type()));
    }

    List<Integer> parseVarIndexes(Frame df);

    List<String> parseVarNames(Frame df);

    List<String> parseInverseVarNames(Frame df);
}

class VRangeByName implements VRange {

    static final String DELIMITER = ",";
    static final String RANGE = "~";
    static final String ALL = "all";
    private final String rawColumnRange;

    /**
     * Builds a var range directly from a list of var indexes.
     *
     * @param indexes list of var indexes
     */
    public VRangeByName(int... indexes) {
        if (indexes == null || indexes.length == 0) {
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
    public VRangeByName(String... ranges) {
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
                if(range.trim().isEmpty()) {
                    continue;
                }
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
        return colIndexes;
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

class VRangeByPredName implements VRange {

    private final Predicate<String> predicate;

    VRangeByPredName(Predicate<String> predicate) {
        this.predicate = predicate;
    }

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

class VRangeByPred implements VRange {

    private final Predicate<Var> predicate;

    VRangeByPred(Predicate<Var> predicate) {
        this.predicate = predicate;
    }

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