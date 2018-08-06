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

package rapaio.ml.regression.tree.rtree;

import rapaio.core.stat.Mean;
import rapaio.core.stat.WeightedMean;
import rapaio.data.Frame;
import rapaio.data.NumVar;
import rapaio.util.DoublePair;

import java.io.Serializable;

/**
 * Component which fits an FSpot o the tree using tree information and a starting node.
 */
public interface RTreePredictor extends Serializable {

    /**
     * @return name of the tree predictor
     */
    String name();

    /**
     * Fits a given instance to the regression tree, by following nodes
     * and predicates until a fitting decision is found.
     *
     * @param row  from frame which contains the values to be fitted
     * @param df   frame which contains the row to be fitted
     * @param root tree node where the fitting starts from, recursively
     * @return a pair of values: first is the regression predict, second is the weight
     * of the result
     */
    DoublePair predict(int row, Frame df, RTreeNode root);

    /**
     * Standard tree predictor.
     */
    RTreePredictor STANDARD = new RTreePredictor() {

        private static final long serialVersionUID = 6223796672194285760L;

        @Override
        public String name() {
            return "STANDARD";
        }

        @Override
        public DoublePair predict(int row, Frame df, RTreeNode node) {

            // if we are at a leaf node we simply return what we found there
            if (node.isLeaf())
                return DoublePair.from(node.value(), node.weight());

            // if is an interior node, we check to see if there is a child
            // which can handle the instance
            for (RTreeNode child : node.children())
                if (child.predicate().test(row, df)) {
                    return predict(row, df, child);
                }

            // so is a missing value for the current test feature

            NumVar values = NumVar.empty();
            NumVar weights = NumVar.empty();
            for (RTreeNode child : node.children()) {
                DoublePair prediction = predict(row, df, child);
                values.addValue(prediction._1);
                weights.addValue(prediction._2);
            }
            return DoublePair.from(WeightedMean.from(values, weights).value(), Mean.from(weights).value());
        }
    };
}
