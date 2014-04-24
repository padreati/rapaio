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
import rapaio.data.Numeric;
import rapaio.data.stream.FSpot;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.function.Predicate;

/**
 * Models node of a classification decision tree.
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class CPartitionTreeNode {
    final PartitionTreeClassifier c;
    final CPartitionTreeNode parent;
    final String groupName;
    final Predicate<FSpot> predicate;

    boolean leaf = true;
    List<CPartitionTreeNode> children;
    DensityVector density;
    DensityVector counter;
    int bestIndex;
    CTreeCandidate bestCandidate;
    TreeSet<CTreeCandidate> candidates;

    public CPartitionTreeNode(final PartitionTreeClassifier c, final CPartitionTreeNode parent,
                              final String groupName, final Predicate<FSpot> predicate) {
        this.parent = parent;
        this.c = c;
        this.groupName = groupName;
        this.predicate = predicate;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public List<CPartitionTreeNode> getChildren() {
        return children;
    }

    public void learn(Frame df, int depth) {
        density = new DensityVector(df.col(c.getTargetCol()), df.getWeights());
        counter = new DensityVector(df.col(c.getTargetCol()), new Numeric(df.rowCount(), df.rowCount(), 1));
        bestIndex = density.findBestIndex();

        if (df.rowCount() == 0) {
            return;
        }

        if (df.rowCount() <= c.getMinCount() || counter.countValues(x -> x > 0) == 1 || depth < 1) {
            return;
        }

        candidates = new TreeSet<>();

        // here we have to implement some form of column selector for RF, ID3 and C4.5
        for (String testCol : df.colNames()) {
            if (testCol.equals(c.getTargetCol())) continue;
            if (df.col(testCol).type().isNumeric()) {
                candidates.addAll(c.getNumericMethod().computeCandidates(c, df, testCol, c.getTargetCol(), c.getFunction()));
            } else {
                candidates.addAll(c.getNominalMethod().computeCandidates(c, df, testCol, c.getTargetCol(), c.getFunction()));
            }
        }

        if (candidates.isEmpty()) {
            return;
        }
        leaf = false;

        bestCandidate = candidates.first();

        // now that we have a best candidate, do the effective split

        if (bestCandidate.getGroupNames().isEmpty()) {
            leaf = true;
            return;
        }

        List<Frame> frames = c.getSplitter().performSplit(df, bestCandidate);
        children = new ArrayList<>(frames.size());
        for (int i = 0; i < frames.size(); i++) {
            Frame f = frames.get(i);
            CPartitionTreeNode child = new CPartitionTreeNode(c, this, bestCandidate.getGroupNames().get(i), bestCandidate.getGroupPredicates().get(i));
            children.add(child);
            child.learn(f, depth - 1);
        }
    }
}
