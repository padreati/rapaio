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

package rapaio.ml.regression.tree.rtree;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import rapaio.core.*;
import rapaio.data.*;
import rapaio.data.mapping.*;
import rapaio.ml.common.predicate.*;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
public interface RTreeSplitter extends Serializable {

    /**
     * @return name of the splitter class
     */
    String name();

    /**
     * Perform the splitting but returns only the mappings for each branch
     *
     * @param df              source data frame
     * @param weights         source weights
     * @param groupPredicates predicates used for splitting
     * @return a list of mappings, one for each rule
     */
    List<Mapping> performSplitMapping(Frame df, Var weights, List<RowPredicate> groupPredicates);

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
        public List<Mapping> performSplitMapping(Frame df, Var weights, List<RowPredicate> groupPredicates) {
            List<Mapping> mapList = Util.createMapList(df.rowCount(), groupPredicates);
            for (int row = 0; row < df.rowCount(); row++) {
                int group = Util.getMatchedPredicate(df, row, groupPredicates);
                if (group != -1) {
                    mapList.get(group).add(row);
                }
            }
            return mapList;
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
        public List<Mapping> performSplitMapping(Frame df, Var weights, List<RowPredicate> groupPredicates) {
            List<Mapping> mapList = Util.createMapList(df.rowCount(), groupPredicates);
            double[] w = new double[mapList.size()];
            IntArrayList missing = new IntArrayList();
            for (int row = 0; row < df.rowCount(); row++) {
                int group = Util.getMatchedPredicate(df, row, groupPredicates);
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
                mapList.get(indexW).addAll(missing);
            }
            return mapList;
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
        public List<Mapping> performSplitMapping(Frame df, Var weights, List<RowPredicate> groupPredicates) {
            List<Mapping> mapList = Util.createMapList(df.rowCount(), groupPredicates);
            for (int row = 0; row < df.rowCount(); row++) {
                int group = Util.getMatchedPredicate(df, row, groupPredicates);
                if (group != -1) {
                    mapList.get(group).add(row);
                } else {
                    int next = RandomSource.nextInt(groupPredicates.size());
                    mapList.get(next).add(row);
                }
            }
            return mapList;
        }
    };
}

/*
Regular splitting performs a split for each candidate rule and keep the missing rows into a separate list.
 */
final class Util {

    private Util() {
    }

    public static List<Mapping> createMapList(int capacity, List<RowPredicate> groupPredicates) {
        return IntStream.range(0, groupPredicates.size()).boxed().map(i -> new ListMapping()).collect(Collectors.toList());
    }

    public static int getMatchedPredicate(Frame df, int row, List<RowPredicate> predicates) {
        for (int i = 0; i < predicates.size(); i++) {
            RowPredicate predicate = predicates.get(i);
            if (predicate.test(row, df)) {
                return i;
            }
        }
        return -1;
    }
}