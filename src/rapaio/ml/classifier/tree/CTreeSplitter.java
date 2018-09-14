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

package rapaio.ml.classifier.tree;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.ml.common.predicate.RowPredicate;
import rapaio.util.Pair;
import rapaio.util.Tagged;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface CTreeSplitter extends Tagged, Serializable {

    /**
     * Splits the initial data set into pairs of frame and weights according with the
     * policy for missing values implemented splitter.
     *
     * @param df initial data set
     * @param weights initial weights
     * @param predicates rules/criteria used to perform the splitting
     *
     * @return a pair with a list of frames and a list of weights
     */
    Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, List<RowPredicate> predicates);

    /**
     * Simply ignores the missing values, it will propagate only the instances which are accepted by a rule
     */
    CTreeSplitter Ignored = new CTreeSplitter() {
        private static final long serialVersionUID = -9017265383541294518L;

        @Override
        public String name() {
            return "Ignored";
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, List<RowPredicate> p) {
            List<IntArrayList> mappings = new ArrayList<>(p.size());
            for (int i = 0; i < p.size(); i++) {
                mappings.add(new IntArrayList());
            }

            for (int row = 0; row < df.rowCount(); row++) {
                for (int i = 0; i < p.size(); i++) {
                    if (p.get(i).test(row, df)) {
                        mappings.get(i).add(row);
                        break;
                    }
                }
            }
            return Pair.from(
                    mappings.stream().map(Mapping::wrap).map(df::mapRows).collect(toList()),
                    mappings.stream().map(Mapping::wrap).map(weights::mapRows).collect(toList())
            );
        }

    };

    /**
     * Put all missing values to the node with the highest weight
     */
    CTreeSplitter ToMajority = new CTreeSplitter() {
        private static final long serialVersionUID = -5858151664805703831L;

        @Override
        public String name() {
            return "ToMajority";
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, List<RowPredicate> p) {
            List<IntArrayList> mappings = new ArrayList<>(p.size());
            for (int i = 0; i < p.size(); i++) {
                mappings.add(new IntArrayList());
            }

            IntList missingSpots = new IntArrayList();
            for (int row = 0; row < df.rowCount(); row++) {
                boolean consumed = false;
                for (int i = 0; i < p.size(); i++) {
                    if (p.get(i).test(row, df)) {
                        mappings.get(i).add(row);
                        consumed = true;
                        break;
                    }
                }
                if (!consumed)
                    missingSpots.add(row);
            }
            List<Integer> lens = mappings.stream().map(Mapping::wrap).map(Mapping::size).collect(toList());
            Collections.shuffle(lens);
            int majorityGroup = 0;
            int majoritySize = 0;
            for (int i = 0; i < mappings.size(); i++) {
                if (mappings.get(i).size() > majoritySize) {
                    majorityGroup = i;
                    majoritySize = mappings.get(i).size();
                }
            }
            final int index = majorityGroup;

            mappings.get(index).addAll(missingSpots);

            return Pair.from(
                    mappings.stream().map(Mapping::wrap).map(df::mapRows).collect(toList()),
                    mappings.stream().map(Mapping::wrap).map(weights::mapRows).collect(toList())
            );
        }
    };

    /**
     * Put instances with missing value on test variable to all branches, with diminished weights
     */
    CTreeSplitter ToAllWeighted = new CTreeSplitter() {
        private static final long serialVersionUID = 5936044048099571710L;

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, List<RowPredicate> pred) {

            List<Mapping> mappings = new ArrayList<>();
            List<Var> weighting = new ArrayList<>();
            for (int i = 0; i < pred.size(); i++) {
                mappings.add(Mapping.empty());
                weighting.add(VarDouble.empty());
            }

            final double[] p = new double[mappings.size()];
            double psum = 0;
            List<Integer> missingRows = new ArrayList<>();
            for (int row = 0; row < df.rowCount(); row++) {
                boolean consumed = false;
                for (int i = 0; i < pred.size(); i++) {
                    if (pred.get(i).test(row, df)) {
                        mappings.get(i).add(row);
                        weighting.get(i).addDouble(weights.getDouble(row));
                        p[i] += weights.getDouble(row);
                        psum += weights.getDouble(row);
                        consumed = true;
                        break;
                    }
                }
                if (!consumed)
                    missingRows.add(row);
            }
            for (int i = 0; i < p.length; i++) {
                p[i] /= psum;
            }
            for (int i = 0; i < mappings.size(); i++) {
                for (int row : missingRows) {
                    // we distribute something to a node only if it has
                    // already something
                    if (p[i] > 0) {
                        mappings.get(i).add(row);
                        weighting.get(i).addDouble(weights.getDouble(row) * p[i]);
                    }
                }
            }
            List<Frame> frames = mappings.stream().map(df::mapRows).collect(toList());
            return Pair.from(frames, weighting);
        }

        @Override
        public String name() {
            return "ToAllWeighted";
        }
    };

    /**
     * Assign randomly to any child the instances with missing value on test variable
     */
    CTreeSplitter ToRandom = new CTreeSplitter() {
        private static final long serialVersionUID = -4762758695801141929L;

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, List<RowPredicate> pred) {
            List<Mapping> mappings = IntStream.range(0, pred.size()).boxed().map(i -> Mapping.empty()).collect(toList());

            final Set<Integer> missingSpots = new HashSet<>();
            for (int row = 0; row < df.rowCount(); row++) {
                boolean consumed = false;
                for (int i = 0; i < pred.size(); i++) {
                    if (pred.get(i).test(row, df)) {
                        mappings.get(i).add(row);
                        consumed = true;
                        break;
                    }
                }
                if (!consumed)
                    missingSpots.add(row);
            }
            missingSpots.forEach(rowId -> mappings.get(RandomSource.nextInt(mappings.size())).add(rowId));
            List<Frame> frameList = mappings.stream().map(df::mapRows).collect(toList());
            List<Var> weightList = mappings.stream().map(weights::mapRows).collect(toList());
            return Pair.from(frameList, weightList);
        }

        @Override
        public String name() {
            return "ToRandom";
        }
    };
}
