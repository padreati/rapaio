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

package rapaio.ml.regression.tree;

import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.data.stream.FSpot;
import rapaio.util.Pair;
import rapaio.util.func.SPredicate;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
public interface RTreeSplitter extends Serializable {

    RTreeSplitter REMAINS_IGNORED = new RTreeSplitter() {
        private static final long serialVersionUID = -3841482294679686355L;

        @Override
        public String name() {
            return "REMAINS_IGNORED";
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, RTree.RTreeCandidate candidate) {
            List<Mapping> mappings = new ArrayList<>();
            List<Var> weightsList = new ArrayList<>();
            for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                mappings.add(Mapping.empty());
                weightsList.add(Numeric.empty());
            }

            df.stream().forEach(s -> {
                for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                    SPredicate<FSpot> predicate = candidate.getGroupPredicates().get(i);
                    if (predicate.test(s)) {
                        mappings.get(i).add(s.row());
                        weightsList.get(i).addValue(weights.value(s.row()));
                        break;
                    }
                }
            });
            List<Frame> frames = new ArrayList<>();
            mappings.stream().forEach(mapping -> frames.add(df.mapRows(mapping)));
            return Pair.from(frames, weightsList);
        }
    };
    RTreeSplitter REMAINS_TO_MAJORITY = new RTreeSplitter() {

        private static final long serialVersionUID = 5206066415613740170L;

        @Override
        public String name() {
            return "REMAINS_TO_MAJORITY";
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, RTree.RTreeCandidate candidate) {
            List<Mapping> mappings = new ArrayList<>();
            List<Var> weightsList = new ArrayList<>();
            for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                mappings.add(Mapping.empty());
                weightsList.add(Numeric.empty());
            }

            List<FSpot> missingSpots = new LinkedList<>();
            df.stream().forEach(s -> {
                for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                    SPredicate<FSpot> predicate = candidate.getGroupPredicates().get(i);
                    if (predicate.test(s)) {
                        mappings.get(i).add(s.row());
                        weightsList.get(i).addValue(weights.value(s.row()));
                        return;
                    }
                }
                missingSpots.add(s);
            });
            int majorityGroup = 0;
            int majoritySize = 0;
            for (int i = 0; i < mappings.size(); i++) {
                if (mappings.get(i).size() > majoritySize) {
                    majorityGroup = i;
                    majoritySize = mappings.get(i).size();
                }
            }
            final int index = majorityGroup;

            missingSpots.stream().forEach(spot -> {
                mappings.get(index).add(spot.row());
                weightsList.get(index).addValue(weights.value(spot.row()));
            });
            List<Frame> frames = new ArrayList<>();
            mappings.stream().forEach(mapping -> frames.add(MappedFrame.newByRow(df, mapping)));
            return Pair.from(frames, weightsList);
        }
    };
    RTreeSplitter REMAINS_TO_ALL_WEIGHTED = new RTreeSplitter() {

        private static final long serialVersionUID = -7751464101852319794L;

        @Override
        public String name() {
            return "REMAINS_TO_ALL_WEIGHTED";
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, RTree.RTreeCandidate candidate) {
            List<Mapping> mappings = new ArrayList<>();
            List<Var> weightsList = new ArrayList<>();
            for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                mappings.add(Mapping.empty());
                weightsList.add(Numeric.empty());
            }

            final Set<Integer> missingSpots = new HashSet<>();
            df.stream().forEach(s -> {
                for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                    SPredicate<FSpot> predicate = candidate.getGroupPredicates().get(i);
                    if (predicate.test(s)) {
                        mappings.get(i).add(s.row());
                        weightsList.get(i).addValue(weights.value(s.row()));
                        return;
                    }
                }
                missingSpots.add(s.row());
            });
            final double[] p = new double[mappings.size()];
            double n = 0;
            for (int i = 0; i < mappings.size(); i++) {
                p[i] = mappings.get(i).size();
                n += p[i];
            }
            for (int i = 0; i < p.length; i++) {
                p[i] /= n;
            }
            for (int i = 0; i < mappings.size(); i++) {
                final int ii = i;
                missingSpots.forEach(missingRow -> {
                    mappings.get(ii).add(missingRow);
                    weightsList.get(ii).addValue(weights.value(missingRow) * p[ii]);
                });
            }
            List<Frame> frames = new ArrayList<>();
            for (Mapping mapping : mappings) {
                frames.add(MappedFrame.newByRow(df, mapping));
            }
            return Pair.from(frames, weightsList);
        }
    };
    RTreeSplitter REMAINS_TO_RANDOM = new RTreeSplitter() {
        private static final long serialVersionUID = -592529235216896819L;

        @Override
        public String name() {
            return "REMAINS_TO_RANDOM";
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, RTree.RTreeCandidate candidate) {
            List<Mapping> mappings = IntStream.range(0, candidate.getGroupPredicates().size())
                    .boxed().map(i -> Mapping.empty()).collect(Collectors.toList());

            df.stream().forEach(s -> {
                for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                    SPredicate<FSpot> predicate = candidate.getGroupPredicates().get(i);
                    if (predicate.test(s)) {
                        mappings.get(i).add(s.row());
                        return;
                    }
                }
                mappings.get(RandomSource.nextInt(mappings.size())).add(s.row());
            });
            List<Frame> frameList = mappings.stream().map(df::mapRows).collect(Collectors.toList());
            List<Var> weightList = mappings.stream().map(weights::mapRows).collect(Collectors.toList());
            return Pair.from(frameList, weightList);
        }
    };

    String name();

    Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, RTree.RTreeCandidate candidate);
}
