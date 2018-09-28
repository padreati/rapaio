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

package rapaio.ml.classifier.tree;

import rapaio.core.tools.DVector;
import rapaio.ml.common.predicate.RowPredicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public class CTreeNode implements Serializable {

    private static final long serialVersionUID = -5045581827808911763L;

    final int id;
    final int depth;
    final CTreeNode parent;
    final String groupName;
    final RowPredicate predicate;
    final List<CTreeNode> children = new ArrayList<>();
    boolean leaf = true;
    DVector density;
    DVector counter;
    int bestIndex;
    CTreeCandidate bestCandidate;

    public CTreeNode(int id, final CTreeNode parent, final String groupName, final RowPredicate predicate, int depth) {
        this.id = id;
        this.depth = depth;
        this.parent = parent;
        this.groupName = groupName;
        this.predicate = predicate;
    }

    public CTreeNode getParent() {
        return parent;
    }

    public int getId() {
        return id;
    }

    public int getDepth() {
        return depth;
    }

    public String getGroupName() {
        return groupName;
    }

    public RowPredicate getPredicate() {
        return predicate;
    }

    public DVector getCounter() {
        return counter;
    }

    public int getBestIndex() {
        return bestIndex;
    }

    public DVector getDensity() {
        return density;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public List<CTreeNode> getChildren() {
        return children;
    }

    public CTreeCandidate getBestCandidate() {
        return bestCandidate;
    }

    public void cut() {
        leaf = true;
        children.clear();
    }
}
