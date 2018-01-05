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

package rapaio.ml.regression.tree;

import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.ml.common.predicate.RowPredicate;
import rapaio.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Regression Tree Splitter. At learning time for each node, multiple
 * candidates are evaluated. After one of the candidates is chosen, instances
 * from that node must be split between nodes, this is the function of
 * this components. The general rule is than an instance is assigned
 * to the node of the rule the instance apply. If an instance does not apply
 * with any rule of the candidate, than this components decides where to be
 * assigned those instances.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
public interface RTreeSplitter extends Serializable {

    /**
     * @return name of the splitter class
     */
    String name();

    List<Mapping> performMapping(Frame df, Var weights, List<RowPredicate> groupPredicates);

    /**
     * Split the instances and produces two lists, one with the frames which
     * maps selected instances to predicates, and one which contains weights
     * corresponding to the same predicates. The predicates are identified
     * by position is {@link RTreeCandidate#groupPredicates}
     *
     * @param df              initial set of instances
     * @param weights         weights corresponding to each instance
     * @param groupPredicates the node candidate which contains the rules
     * @return a pair of lists, one with mapped instances for each rule and
     * one with corresponding weights
     */
    Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights,
                                              List<RowPredicate> groupPredicates);

    /**
     * Do the regular split of instances and simply ignores the ones which do not
     * meet any of the predicates.
     */
    RTreeSplitter REMAINS_IGNORED = new RTreeSplitter() {
        private static final long serialVersionUID = -3841482294679686355L;

        @Override
        public String name() {
            return "REMAINS_IGNORED";
        }

        @Override
        public List<Mapping> performMapping(Frame df, Var weights, List<RowPredicate> groupPredicates) {
            RegularSplit s = new RegularSplit(df, weights, groupPredicates);
            return s.toMappings();
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, List<RowPredicate> groupPredicates) {
            RegularSplit s = new RegularSplit(df, weights, groupPredicates);
            return Pair.from(s.toFrames(), s.toWeights());
        }
    };

    /**
     * Instances are splited as usual, all not matched instances are assigned
     * to the rule which has most matched instances.
     */
    RTreeSplitter REMAINS_TO_MAJORITY = new RTreeSplitter() {

        private static final long serialVersionUID = 5206066415613740170L;

        @Override
        public String name() {
            return "REMAINS_TO_MAJORITY";
        }

        @Override
        public List<Mapping> performMapping(Frame df, Var weights, List<RowPredicate> groupPredicates) {
            RegularSplit s = new RegularSplit(df, weights, groupPredicates);
            int majorityGroup = 0;
            int majoritySize = 0;

            List<Mapping> mappings = s.toMappings();
            for (int i = 0; i < mappings.size(); i++) {
                if (mappings.get(i).size() > majoritySize) {
                    majorityGroup = i;
                    majoritySize = mappings.get(i).size();
                }
            }
            mappings.get(majorityGroup).addAll(s.missingRows);
            return mappings;
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, List<RowPredicate> groupPredicates) {
            RegularSplit s = new RegularSplit(df, weights, groupPredicates);

            List<Mapping> mappings = s.toMappings();
            List<Var> ws = s.toWeights();

            int majorityGroup = 0;
            int majoritySize = 0;
            for (int i = 0; i < mappings.size(); i++) {
                if (mappings.get(i).size() > majoritySize) {
                    majorityGroup = i;
                    majoritySize = mappings.get(i).size();
                }
            }
            for (int row : s.missingRows) {
                mappings.get(majorityGroup).add(row);
                ws.get(majorityGroup).addValue(weights.value(row));
            }
            List<Frame> frames = new ArrayList<>();
            mappings.forEach(mapping -> frames.add(MappedFrame.byRow(df, mapping)));
            return Pair.from(frames, ws);
        }
    };

    /**
     * Regular splitting and the remaining instances are distributed
     * to all nodes but with diminished weights, proportional to
     * each node's assigned instance's weight sum.
     */
    RTreeSplitter REMAINS_TO_ALL_WEIGHTED = new RTreeSplitter() {

        private static final long serialVersionUID = -7751464101852319794L;

        @Override
        public String name() {
            return "REMAINS_TO_ALL_WEIGHTED";
        }

        @Override
        public List<Mapping> performMapping(
                Frame df, Var weights, List<RowPredicate> groupPredicates) {
            RegularSplit s = new RegularSplit(df, weights, groupPredicates);

            List<Mapping> mappings = s.toMappings();
            for (Mapping mapping : mappings) {
                mapping.addAll(s.missingRows);
            }
            return mappings;
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(
                Frame df, Var weights, List<RowPredicate> groupPredicates) {
            RegularSplit s = new RegularSplit(df, weights, groupPredicates);

            List<Mapping> mappings = s.toMappings();
            List<Var> ws = s.toWeights();

            final double[] p = new double[mappings.size()];
            double sum = 0;
            for (int i = 0; i < mappings.size(); i++) {
                p[i] = ws.get(i).stream().mapToDouble().sum();
                sum += p[i];
            }
            for (int i = 0; i < p.length; i++) {
                p[i] /= sum;
            }
            for (int i = 0; i < mappings.size(); i++) {
                for (int row : s.missingRows) {
                    mappings.get(i).add(row);
                    ws.get(i).addValue(weights.value(row) * p[i]);
                }
            }
            List<Frame> frames = new ArrayList<>();
            for (Mapping mapping : mappings) {
                frames.add(MappedFrame.byRow(df, mapping));
            }
            return Pair.from(frames, ws);
        }
    };

    /**
     * Regular splitting and distribute remaining instances
     * randomly between regular nodes.
     */
    RTreeSplitter REMAINS_TO_RANDOM = new RTreeSplitter() {
        private static final long serialVersionUID = -592529235216896819L;
        
        @Override
        public String name() {
            return "REMAINS_TO_RANDOM";
        }
        
        @Override
        public List<Mapping> performMapping(
                Frame df, Var weights, List<RowPredicate> groupPredicates) {
            RegularSplit s = new RegularSplit(df, weights, groupPredicates);

            List<Mapping> mappings = s.toMappings();
            for (int row : s.missingRows) {
                int next = RandomSource.nextInt(mappings.size());
                mappings.get(next).add(row);
            }
            return mappings;
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, List<RowPredicate> groupPredicates) {
            RegularSplit s = new RegularSplit(df, weights, groupPredicates);

            List<Mapping> mappings = s.toMappings();
            List<Var> ws = s.toWeights();

            for (int row : s.missingRows) {
                int next = RandomSource.nextInt(mappings.size());
                mappings.get(next).add(row);
                ws.get(next).addValue(weights.value(row));
            }
            List<Frame> frameList = mappings.stream().map(df::mapRows).collect(Collectors.toList());
            return Pair.from(frameList, ws);
        }
    };
}

/*
Regular splitting performs a split for each candidate rule and keep the missing rows into a separate list.
 */
final class RegularSplit {

    final public List<List<Integer>> rows = new ArrayList<>();
    final public List<Integer> missingRows = new ArrayList<>();

    private final Frame df;
    private final Var weights;

    public RegularSplit(Frame df, Var weights, List<RowPredicate> groupPredicates) {

        this.df = df;
        this.weights = weights;

        // initialize the lists with one element in each list for each candidate's rule
        for (int i = 0; i < groupPredicates.size(); i++) {
            rows.add(new ArrayList<>());
        }

        // each instance is distributed to one rule
        for (int row = 0; row < df.rowCount(); row++) {
            boolean matched = false;
            for (int i = 0; i < groupPredicates.size(); i++) {
                RowPredicate predicate = groupPredicates.get(i);
                if (predicate.test(row, df)) {
                    rows.get(i).add(row);
                    matched = true;
                    // first rule has priority
                    break;
                }
            }

            // if there is no matching rule, than assign to missing
            if (!matched)
                missingRows.add(row);
        }
    }

    public List<Mapping> toMappings() {
        List<Mapping> mappings = new ArrayList<>();
        for (List<Integer> list : rows) {
            mappings.add(Mapping.wrap(list));
        }
        return mappings;
    }

    public List<Frame> toFrames() {
        List<Frame> frames = new ArrayList<>();
        for (List<Integer> list : rows) {
            frames.add(df.mapRows(Mapping.wrap(list)));
        }
        return frames;
    }

    public List<Var> toWeights() {
        List<Var> vars = new ArrayList<>();
        for (List<Integer> list : rows) {
            NumVar w = NumVar.empty(list.size());
            for (int i = 0; i < list.size(); i++) {
                w.setValue(i, weights.value(list.get(i)));
            }
            vars.add(w);
        }
        return vars;
    }
}