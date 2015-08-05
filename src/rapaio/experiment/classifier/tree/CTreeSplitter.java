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

package rapaio.experiment.classifier.tree;

import rapaio.core.RandomSource;
import rapaio.data.*;
import rapaio.data.stream.FSpot;
import rapaio.util.Pair;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
@Deprecated
public interface CTreeSplitter extends Serializable {

    String name();

    CTreeSplitter newInstance();

    public Pair<List<Frame>, List<Numeric>> performSplit(Frame df, Var weights, CTreeCandidate candidate);

    public static class RemainsIgnored implements CTreeSplitter {

        @Override
        public String name() {
            return "RemainsIgnored";
        }

        @Override
        public CTreeSplitter newInstance() {
            return new RemainsIgnored();
        }

        @Override
        public Pair<List<Frame>, List<Numeric>> performSplit(Frame df, Var weights, CTreeCandidate candidate) {
            List<Mapping> mappings = new ArrayList<>();
            List<Numeric> weightsList = new ArrayList<>();
            for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                mappings.add(Mapping.newEmpty());
                weightsList.add(Numeric.newEmpty());
            }

            df.stream().forEach(s -> {
                for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                    Predicate<FSpot> predicate = candidate.getGroupPredicates().get(i);
                    if (predicate.test(s)) {
                        mappings.get(i).add(s.row());
                        weightsList.get(i).addValue(weights.value(s.row()));
                        break;
                    }
                }
            });
            List<Frame> frames = new ArrayList<>();
            mappings.stream().forEach(mapping -> {
                frames.add(MappedFrame.newByRow(df, mapping));
            });
            return new Pair<>(frames, weightsList);
        }
    }


    public static class RemainsToMajority implements CTreeSplitter {

        @Override
        public String name() {
            return "RemainsToMajority";
        }

        @Override
        public CTreeSplitter newInstance() {
            return new RemainsToMajority();
        }

        @Override
        public Pair<List<Frame>, List<Numeric>> performSplit(Frame df, Var weights, CTreeCandidate candidate) {
            List<Mapping> mappings = new ArrayList<>();
            List<Numeric> weightsList = new ArrayList<>();
            for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                mappings.add(Mapping.newEmpty());
                weightsList.add(Numeric.newEmpty());
            }

            List<FSpot> missingSpots = new LinkedList<>();
            df.stream().forEach(s -> {
                for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                    Predicate<FSpot> predicate = candidate.getGroupPredicates().get(i);
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
            mappings.stream().forEach(mapping -> {
                frames.add(MappedFrame.newByRow(df, mapping));
            });
            return new Pair<>(frames, weightsList);
        }
    }

    public static class RemainsToAllWeighted implements CTreeSplitter {

        @Override
        public String name() {
            return "RemainsToAllWeighted";
        }

        @Override
        public CTreeSplitter newInstance() {
            return new RemainsToAllWeighted();
        }

        @Override
        public Pair<List<Frame>, List<Numeric>> performSplit(Frame df, Var weights, CTreeCandidate candidate) {
            List<Mapping> mappings = new ArrayList<>();
            List<Numeric> weightsList = new ArrayList<>();
            for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                mappings.add(Mapping.newEmpty());
                weightsList.add(Numeric.newEmpty());
            }

            final Set<Integer> missingSpots = new HashSet<>();
            df.stream().forEach(s -> {
                for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                    Predicate<FSpot> predicate = candidate.getGroupPredicates().get(i);
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
            List<Frame> frames = mappings.stream().map(mapping -> MappedFrame.newByRow(df, mapping)).collect(Collectors.toList());
            return new Pair<>(frames, weightsList);
        }
    }

    public static final class RemainsToRandom implements CTreeSplitter {

        @Override
        public String name() {
            return "RemainsToRandom";
        }

        @Override
        public CTreeSplitter newInstance() {
            return new RemainsToRandom();
        }

        @Override
        public Pair<List<Frame>, List<Numeric>> performSplit(Frame df, Var weights, CTreeCandidate candidate) {
            List<Mapping> mappings = new ArrayList<>();
            List<Numeric> weightList = new ArrayList<>();
            for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                mappings.add(Mapping.newEmpty());
                weightList.add(Numeric.newEmpty());
            }

            final Set<Integer> missingSpots = new HashSet<>();
            df.stream().forEach(s -> {
                for (int i = 0; i < candidate.getGroupPredicates().size(); i++) {
                    Predicate<FSpot> predicate = candidate.getGroupPredicates().get(i);
                    if (predicate.test(s)) {
                        mappings.get(i).add(s.row());
                        weightList.get(i).addValue(weights.value(s.row()));
                        return;
                    }
                }
                missingSpots.add(s.row());
            });
            missingSpots.forEach(rowId -> mappings.get(RandomSource.nextInt(mappings.size())).add(rowId));
            List<Frame> frames = mappings.stream().map(mapping -> MappedFrame.newByRow(df, mapping)).collect(Collectors.toList());
            return new Pair<>(frames, weightList);
        }
    }

    @Deprecated
    final class RemainsWithSurrogates implements CTreeSplitter {

        @Override
        public String name() {
            return "RemainsWithSurrogates";
        }

        @Override
        public CTreeSplitter newInstance() {
            return new RemainsWithSurrogates();
        }

        @Override
        public Pair<List<Frame>, List<Numeric>> performSplit(Frame df, Var weights, CTreeCandidate candidate) {
            // TODO partition tree classifier - remains surrogates
            throw new IllegalArgumentException("not implemented");
        }
    };
}

