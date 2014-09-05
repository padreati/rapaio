/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.ml.classifier.tree.ctree;

import rapaio.data.stream.FSpot;
import rapaio.ml.classifier.tools.DensityVector;
import rapaio.util.Pair;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface CTreePredictor extends Serializable {

    String name();

    Pair<Integer, DensityVector> predict(FSpot spot, CTreeNode node);

    CTreePredictor STANDARD = new CTreePredictor() {
        @Override
        public String name() {
            return "STANDARD";
        }

        @Override
        public Pair<Integer, DensityVector> predict(FSpot spot, CTreeNode node) {
            if (node.getCounter().sum(false) == 0)
                return new Pair<>(node.getParent().getBestIndex(), node.getParent().getDensity());
            if (node.isLeaf())
                return new Pair<>(node.getBestIndex(), node.getDensity());

            for (CTreeNode child : node.getChildren()) {
                if (child.getPredicate().test(spot)) {
                    return predict(spot, child);
                }
            }

            String[] dict = node.getTree().firstDictionary();
            DensityVector dv = new DensityVector(dict);
            for (CTreeNode child : node.getChildren()) {
                DensityVector d = predict(spot, child).second;
                for (int i = 0; i < dict.length; i++) {
                    dv.update(i, d.get(i) * child.getDensity().sum(false));
                }
            }
            dv.normalize(false);
            return new Pair<>(dv.findBestIndex(), dv);
        }
    };
}
