/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.supervised.tree.rtree;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.mapping.ArrayMapping;
import rapaio.experiment.ml.common.predicate.RowPredicate;

/**
 * Regression Tree Splitter. At learning time for each node, multiple
 * candidates are evaluated. After one of the candidates is chosen, instances
 * from that node must be split between nodes, this is the function of
 * this components. The general rule is than an instance is assigned
 * to the node of the rule the instance apply. If an instance does not apply
 * with any rule of the candidate, than this components decides where to be
 * assigned those instances.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/24/14.
 */
public enum Splitter implements Serializable {

    /**
     * Do the regular split of instances and simply ignores the ones which do not
     * meet any of the predicates.
     */
    Ignore {
        @Override
        public List<Mapping> performSplitMapping(Frame df, Var weights, List<RowPredicate> groupPredicates) {
            List<Mapping> mapList = createMapList(df.rowCount(), groupPredicates);
            for (int row = 0; row < df.rowCount(); row++) {
                int group = getMatchedPredicate(df, row, groupPredicates);
                if (group != -1) {
                    mapList.get(group).add(row);
                }
            }
            return mapList;
        }
    },
    /**
     * Instances are splited as usual, all not matched instances are assigned
     * to the rule which has most matched instances.
     */
    Majority {
        @Override
        public List<Mapping> performSplitMapping(Frame df, Var weights, List<RowPredicate> groupPredicates) {
            List<Mapping> mapList = createMapList(df.rowCount(), groupPredicates);
            double[] w = new double[mapList.size()];
            Mapping missing = Mapping.empty();
            for (int row = 0; row < df.rowCount(); row++) {
                int group = getMatchedPredicate(df, row, groupPredicates);
                if (group != -1) {
                    mapList.get(group).add(row);
                    w[group] += weights.getDouble(row);
                } else {
                    missing.add(row);
                }
            }
            double maxW = -1;
            int indexW = -1;
            for (int i = 0; i < w.length; i++) {
                if (w[i] < maxW) {
                    continue;
                }
                if (w[i] == maxW) {
                    if (RandomSource.nextDouble() > 0.5) {
                        maxW = w[i];
                        indexW = i;
                    }
                    continue;
                }
                maxW = w[i];
                indexW = i;
            }
            if (indexW != -1) {
                mapList.get(indexW).addAll(missing.iterator());
            }
            return mapList;
        }
    },
    /**
     * Regular splitting and distribute remaining instances
     * randomly between regular nodes.
     */
    Random {
        @Override
        public List<Mapping> performSplitMapping(Frame df, Var weights, List<RowPredicate> groups) {
            int[] maps = new int[df.rowCount()];
            int[] counts = new int[groups.size()];
            for (int i = 0; i < df.rowCount(); i++) {
                int group = getMatchedPredicate(df, i, groups);
                if (group == -1) {
                    group = RandomSource.nextInt(groups.size());
                }
                maps[i] = group;
                counts[group]++;
            }
            int[][] mappings = new int[groups.size()][];
            for (int i = 0; i < groups.size(); i++) {
                mappings[i] = new int[counts[i]];
            }
            int[] pos = new int[groups.size()];
            for (int i = 0; i < df.rowCount(); i++) {
                int group = maps[i];
                mappings[group][pos[group]] = i;
                pos[group]++;
            }
            return Arrays.stream(mappings).map(Mapping::wrap).collect(Collectors.toList());
        }
    };

    /**
     * Perform the splitting but returns only the mappings for each branch
     *
     * @param df              source data frame
     * @param weights         source weights
     * @param groupPredicates predicates used for splitting
     * @return a list of mappings, one for each rule
     */
    public abstract List<Mapping> performSplitMapping(Frame df, Var weights, List<RowPredicate> groupPredicates);

    private static List<Mapping> createMapList(int capacity, List<RowPredicate> groupPredicates) {
        return IntStream.range(0, groupPredicates.size()).boxed().map(i -> new ArrayMapping()).collect(Collectors.toList());
    }

    private static int getMatchedPredicate(Frame df, int row, List<RowPredicate> predicates) {
        for (int i = 0; i < predicates.size(); i++) {
            RowPredicate predicate = predicates.get(i);
            if (predicate.test(row, df)) {
                return i;
            }
        }
        return -1;
    }
}