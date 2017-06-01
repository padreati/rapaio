/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import rapaio.data.stream.FSpot;
import rapaio.util.Pair;
import rapaio.util.func.SPredicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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

    /**
     * Split the instances and produces two lists, one with the frames which
     * maps selected instances to predicates, and one which contains weights
     * corresponding to the same predicates. The predicates are identified
     * by position is {@link RTree.Candidate#groupPredicates}
     *
     * @param df        initial set of instances
     * @param weights   weights corresponding to each instance
     * @param candidate the node candidate which contains the rules
     * @return a pair of lists, one with mapped instances for each rule and
     * one with corresponding weights
     */
    Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights,
                                              RTree.Candidate candidate);

    /**
     * Do the regular split of instances and simply ingores the ones which do not
     * meet any of the predicates.
     */
    RTreeSplitter REMAINS_IGNORED = new RTreeSplitter() {
        private static final long serialVersionUID = -3841482294679686355L;

        @Override
        public String name() {
            return "REMAINS_IGNORED";
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, RTree.Candidate candidate) {
            RegularSplitting s = new RegularSplitting(df, weights, candidate);
            List<Frame> frames = new ArrayList<>();
            s.mappings.forEach(mapping -> frames.add(df.mapRows(mapping)));
            return Pair.from(frames, s.weightsList);
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
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, RTree.Candidate candidate) {
            RegularSplitting s = new RegularSplitting(df, weights, candidate);
            int majorityGroup = 0;
            int majoritySize = 0;
            for (int i = 0; i < s.mappings.size(); i++) {
                if (s.mappings.get(i).size() > majoritySize) {
                    majorityGroup = i;
                    majoritySize = s.mappings.get(i).size();
                }
            }
            final int index = majorityGroup;
            for (FSpot spot : s.missingSpots) {
                s.mappings.get(index).add(spot.getRow());
                s.weightsList.get(index).addValue(weights.getValue(spot.getRow()));
            }
            List<Frame> frames = new ArrayList<>();
            s.mappings.forEach(mapping -> frames.add(MappedFrame.byRow(df, mapping)));
            return Pair.from(frames, s.weightsList);
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
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, RTree.Candidate candidate) {
            RegularSplitting s = new RegularSplitting(df, weights, candidate);

            final double[] p = new double[s.mappings.size()];
            double sum = 0;
            for (int i = 0; i < s.mappings.size(); i++) {
                p[i] = s.weightsList.get(i).stream().mapToDouble().sum();
                sum += p[i];
            }
            for (int i = 0; i < p.length; i++) {
                p[i] /= sum;
            }
            for (int i = 0; i < s.mappings.size(); i++) {
                for (FSpot spot : s.missingSpots) {
                    s.mappings.get(i).add(spot.getRow());
                    s.weightsList.get(i).addValue(weights.getValue(spot.getRow()) * p[i]);
                }
            }
            List<Frame> frames = new ArrayList<>();
            for (Mapping mapping : s.mappings) {
                frames.add(MappedFrame.byRow(df, mapping));
            }
            return Pair.from(frames, s.weightsList);
        }
    };

    /**
     * Regular splitting and distribute remaining instances
     * randomly between regular nodes.
     */
    RTreeSplitter REMAINS_TO_RANDOM = new RTreeSplitter() {
        private static final long serialVersionUID = -592529235216896819L;
    RandomSource randomSource = RandomSource.createRandom();

        @Override
        public String name() {
            return "REMAINS_TO_RANDOM";
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, RTree.Candidate candidate) {
            RegularSplitting s = new RegularSplitting(df, weights, candidate);
            for (FSpot spot : s.missingSpots) {
                int next = randomSource.nextInt(s.mappings.size());
                s.mappings.get(next).add(spot.getRow());
                s.weightsList.get(next).addValue(weights.getValue(spot.getRow()));
            }
            ;
            List<Frame> frameList = s.mappings.stream().map(df::mapRows).collect(Collectors.toList());
            return Pair.from(frameList, s.weightsList);
        }
    };
}

class RegularSplitting {

    final public List<Mapping> mappings = new ArrayList<>();
    final public List<Var> weightsList = new ArrayList<>();
    final public List<FSpot> missingSpots = new ArrayList<>();

    public RegularSplitting(Frame df, Var weights, RTree.Candidate candidate) {
        // initialize the lists with one element in each list for each candidate's rule
        for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
            mappings.add(Mapping.empty());
            weightsList.add(NumericVar.empty());
        }
        // each instance is distributed to one rule
        for (FSpot s : df.spotList()) {
            boolean matched = false;
            for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                SPredicate<FSpot> predicate = candidate.getGroupPredicates().get(i);
                if (predicate.test(s)) {
                    mappings.get(i).add(s.getRow());
                    weightsList.get(i).addValue(weights.getValue(s.getRow()));
                    matched = true;
                    // first rule has priority
                    break;
                }
            }
            // if there is no matching rule, than assign to missing
            if (!matched)
                missingSpots.add(s);
        }
    }
}