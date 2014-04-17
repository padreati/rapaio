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

package rapaio.classifier.tree;

import rapaio.classifier.tools.DensityVector;
import rapaio.data.Frame;

import java.util.ArrayList;
import java.util.List;

/**
 * Models node of a classification decision tree.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class CTreeNode {
    private final TreeClassifier c;
    private final CTreeNode parent;

    private boolean leaf = false;
    private List<CTreeNode> children = new ArrayList<>();
    private DensityVector density;
    private DensityVector counter;

    public CTreeNode(final TreeClassifier c, final CTreeNode parent) {
        this.parent = parent;
        this.c = c;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public List<CTreeNode> getChildren() {
        return children;
    }

    public void learn(TreeClassifier c, Frame df, CTreeNode parent) {
        if (df.rowCount() == 0) {
            return;
        }


    }
}
