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

package rapaio.ml.classifier.tree;

import rapaio.core.tools.DVector;
import rapaio.data.stream.FSpot;
import rapaio.util.Pair;
import rapaio.util.Tag;

import java.io.Serializable;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public interface CTreePredictor extends Serializable {

    Pair<Integer, DVector> predict(CTree tree, FSpot spot, CTreeNode node);

    Tag<CTreePredictor> Standard = Tag.valueOf("Standard", (CTree tree, FSpot spot, CTreeNode node) -> new CTreePredictor() {

        @Override
        public Pair<Integer, DVector> predict(CTree tree, FSpot spot, CTreeNode node) {
            try {
                node.getCounter().sum(false);
            } catch (Throwable throwable) {
                System.out.println();
            }
            if (node.getCounter().sum(false) == 0)
                if (node.getParent() == null) {
                    throw new RuntimeException("Something bad happened at learning time");
                } else {
                    return new Pair<>(node.getParent().getBestIndex(), node.getParent().getDensity());
                }
            if (node.isLeaf())
                return new Pair<>(node.getBestIndex(), node.getDensity());

            for (CTreeNode child : node.getChildren()) {
                if (child.getPredicate().test(spot)) {
                    return this.predict(tree, spot, child);
                }
            }

            String[] dict = tree.firstDict();
            DVector dv = DVector.newEmpty(dict);
            for (CTreeNode child : node.getChildren()) {
                DVector d = this.predict(tree, spot, child).second;
                for (int i = 0; i < dict.length; i++) {
                    dv.increment(i, d.get(i) * child.getDensity().sum(false));
                }
            }
            dv.normalize(false);
            return new Pair<>(dv.findBestIndex(false), dv);
        }
    }.predict(tree, spot, node));

}