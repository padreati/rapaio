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

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
@Deprecated
public interface RTreeSplitter extends Serializable {

    RTreeSplitter REMAINS_IGNORED = new RTreeSplitter() {
        @Override
        public String name() {
            return "REMAINS_IGNORED";
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, RTreeCandidate candidate) {
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
            mappings.stream().forEach(mapping -> frames.add(MappedFrame.newByRow(df, mapping)));
            return Pair.from(frames, weightsList);
        }
    };
    RTreeSplitter REMAINS_TO_MAJORITY = new RTreeSplitter() {

        @Override
        public String name() {
            return "REMAINS_TO_MAJORITY";
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, RTreeCandidate candidate) {
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

        @Override
        public String name() {
            return "REMAINS_TO_ALL_WEIGHTED";
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, RTreeCandidate candidate) {
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
        @Override
        public String name() {
            return "REMAINS_TO_RANDOM";
        }

        @Override
        public Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, RTreeCandidate candidate) {
            List<Mapping> mappings = new ArrayList<>();
            for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                mappings.add(Mapping.empty());
            }

            final Set<Integer> missingSpots = new HashSet<>();
            df.stream().forEach(s -> {
                for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                    SPredicate<FSpot> predicate = candidate.getGroupPredicates().get(i);
                    if (predicate.test(s)) {
                        mappings.get(i).add(s.row());
                        return;
                    }
                }
                missingSpots.add(s.row());
            });
            missingSpots.forEach(rowId -> mappings.get(RandomSource.nextInt(mappings.size())).add(rowId));
            List<Frame> frameList = mappings.stream()
                    .map(mapping -> MappedFrame.newByRow(df, mapping)).collect(Collectors.toList());
            List<Var> weightList = mappings.stream()
                    .map(mapping -> weights.mapRows(mapping)).collect(Collectors.toList());
            return Pair.from(frameList, weightList);
        }
    };

    String name();

    Pair<List<Frame>, List<Var>> performSplit(Frame df, Var weights, RTreeCandidate candidate);
}
