/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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
 * Variable names parser based on indexes, names, ranges or predicates. The purpose of this parser
 * is to extract a concrete list of variable names based on a compact and flexible
 * specification.
 * <p>
 * Often time, when we work with data frame, we want to specify a list of variables from that frame
 * for further processing. The most direct way to do that would be to use a list of variable names.
 * When data frames contains many variables this process is cumbersome, thus a more flexible way is
 * necessary to simplify that. This class contains tools to simplify this process.
 * <p>
 * As an examples of simpler specifications:
 * <ul>
 *     <li>first 3 variables</li>
 *     <li>last variable</li>
 *     <li>from variable named <i>a</i> to variable named <code>d</code></li>
 * </ul>
 * <p>
 * <p>
 * Variable ranges syntax uses as range separator <code>~</code>, and as column
 * range delimiter the comma <code>,</code>. For more insights we provide below some illustrative examples:
 * <ul>
 *     <li><code>a~d</code> all variables in the data frame starting with variable named <code>a</code> to variable named <code>d</code>,
 *     inclusive</li>
 *     <li><code>d</code>  only variable named `d`</li>
 *     <li><code>0~3</code> first 4 variables in the data frame</li>
 *     <li><code>1~4,a~d</code> variables found on indexes <code>1,2,3,4</code>, followed by variables starting with <code>a</code>
 *     until <code>d</code></li>
 *     <li><code>all</code> all variables, when this is the only specification</li>
 * </ul>
 * <p>
 * Another way to specify how variable names are parsed is by using predicates. A predicate is an
 * instance of {@link Predicate}. If the predicate has {@link String} as parametrized type, the
 * predicate acts on variable names. If the predicate has {@link Var} as parametrized type, the
 * predicate acts on the variable itself.
 * <p>
 * The predicates allow maximum flexibility but requires code. During parsing, the variables from
 * the data frame are passed to the predicate and if the predicate returns true, the parser allows
 * the variable in outcome.
 * <p>
 * Another way to parse variables from a data frame is by specifying a set of types. The parser
 * will retain all the variables with types in the given list of allowed types.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class VarRange {
    public static final String DELIMITER = ",";
    public static final String RANGE_SEPARATOR = "~";
    public static final String ALL = "all";

    /**
     * Predicate which specifies all the columns found in the data frame in order
     */
    public static VarRange all() {
        return new VarRangeByValues(ALL);
    }

    /**
     * String array of ranges. A range can be a variable name, a variable index, a range of indexes or variable names
     * or even a list of ranges.
     *
     * @param ranges array of ranges
     * @return parser instance
     */
    public static VarRange of(String... ranges) {
        return new VarRangeByValues(ranges);
    }

    /**
     * Collection with list of ranges
     *
     * @param ranges collection of ranges
     * @return parser instance
     */
    public static VarRange of(Collection<String> ranges) {
        String[] names = new String[ranges.size()];
        int i = 0;
        for (String varName : ranges) {
            names[i++] = varName;
        }
        return new VarRangeByValues(names);
    }

    /**
     * List of variable indices.
     *
     * @param varIndices array of variable indices
     * @return parser instance
     */
    public static VarRange of(int... varIndices) {
        return new VarRangeByValues(varIndices);
    }

    /**
     * Select all the variables which has a name which pass the given predicate.
     *
     * @param filter variable name predicate
     * @return parser instance
     */
    public static VarRange byName(Predicate<String> filter) {
        return new VarRangeByPredName(filter);
    }

    /**
     * Select all the variables which pass the given predicate.
     *
     * @param filter variable predicate
     * @return parser instance
     */
    public static VarRange byFilter(Predicate<Var> filter) {
        return new VarRangeByPred(filter);
    }

    /**
     * Select all the variables with types in the given list of types.
     *
     * @param types selected list of types
     * @return parser instance
     */
    public static VarRange onlyTypes(VarType... types) {
        Set<VarType> keep = Arrays.stream(types).collect(Collectors.toSet());
        return new VarRangeByPred(var -> keep.contains(var.type()));
    }

    /**
     * Apply {@link VarRange} over a frame and returns a list of variable indexes.
     * A variable index is the position of the variable in the given frame.
     * <p>
     * If a variable is not found, then it will be omitted.
     *
     * @param df input data frame
     * @return a list of variable indexes which corresponds to variable range
     */
    public abstract List<Integer> parseVarIndexes(Frame df);

    /**
     * Apply {@link VarRange} over a frame and returns a list of variable names.
     * <p>
     * If a variable is not found, it will be ommited.
     *
     * @param df input data frame
     * @return a list of variable names which corresponds to the variable range
     */
    public abstract List<String> parseVarNames(Frame df);

    /**
     * Apply {@link VarRange} over a frame and returns the list of complement variable names.
     * A complement variable is a variable which is not described by variable range.
     * <p>
     * This is useful if you want to select all columns which does not meet a given condition.
     * A common example is to split vertically a data frame into a data frame which contains
     * the target variable and another one which contains all other variables.
     *
     * @param df input data frame
     * @return list of variable names with corresponds to the complement of variable range
     */
    public abstract List<String> parseComplementVarNames(Frame df);
}

final class VarRangeByValues extends VarRange {

    private final String rawColumnRange;

    /**
     * Builds a var range directly from a list of var indexes.
     *
     * @param indexes list of var indexes
     */
    public VarRangeByValues(int... indexes) {
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
    public VarRangeByValues(String... ranges) {
        this.rawColumnRange = String.join(DELIMITER, ranges);
    }

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
        return parseVarIndexes(df).stream().map(i -> df.varNames()[i]).toList();
    }

    @Override
    public List<String> parseComplementVarNames(Frame df) {
        Set<Integer> indexes = new HashSet<>(parseVarIndexes(df));
        return IntStream.range(0, df.varCount()).filter(i -> !indexes.contains(i)).boxed().map(i -> df.rvar(i).name()).toList();
    }
}

class VarRangeByPredName extends VarRange {

    private final Predicate<String> predicate;

    public VarRangeByPredName(Predicate<String> predicate) {
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
    public List<String> parseComplementVarNames(Frame df) {
        return df.varStream().map(Var::name)
                .filter(name -> !predicate.test(name))
                .collect(Collectors.toList());
    }
}

class VarRangeByPred extends VarRange {

    private final Predicate<Var> predicate;

    public VarRangeByPred(Predicate<Var> predicate) {
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
    public List<String> parseComplementVarNames(Frame df) {
        return df.varStream()
                .filter(var -> !predicate.test(var))
                .map(Var::name)
                .collect(Collectors.toList());
    }
}