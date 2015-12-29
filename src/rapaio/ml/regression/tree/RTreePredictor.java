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

import rapaio.core.stat.Sum;
import rapaio.core.stat.WeightedMean;
import rapaio.data.Numeric;
import rapaio.data.stream.FSpot;
import rapaio.util.Pair;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
@Deprecated
public interface RTreePredictor extends Serializable {

    RTreePredictor STANDARD = new RTreePredictor() {

        @Override
        public String name() {
            return "standard";
        }

        @Override
        public Pair<Double, Double> predict(RTree tree, FSpot spot, RTree.RTreeNode node) {

            // if we are at a leaf node we simply return what we found there
            if (node.isLeaf())
                return Pair.from(node.getValue(), node.getWeight());

            // if is an interior node, we check to see if there is a child
            // which can handle the instance
            for (RTree.RTreeNode child : node.getChildren()) {
                if (child.getPredicate().test(spot)) {
                    return predict(tree, spot, child);
                }
            }

            // so is a missing value for the current test feature

            Numeric values = Numeric.empty();
            Numeric weights = Numeric.empty();
            for (RTree.RTreeNode child : node.getChildren()) {
                Pair<Double, Double> prediction = predict(tree, spot, child);
                prediction = predict(tree, spot, child);
                values.addValue(prediction._1);
                weights.addValue(prediction._2);
            }
            return Pair.from(new WeightedMean(values, weights).value(), new Sum(weights).value());
        }
    };

    String name();

    Pair<Double, Double> predict(RTree tree, FSpot spot, RTree.RTreeNode root);
}
