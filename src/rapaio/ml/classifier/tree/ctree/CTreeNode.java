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

import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.stream.FSpot;
import rapaio.ml.classifier.tools.DensityVector;
import rapaio.util.Pair;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public class CTreeNode implements Serializable {
    private final CTree tree;
    private final CTreeNode parent;
    private final String groupName;
    private final Predicate<FSpot> predicate;

    private boolean leaf = true;
    private List<CTreeNode> children;
    private DensityVector density;
    private DensityVector counter;
    private int bestIndex;
    private CTreeCandidate bestCandidate;
    private List<CTreeCandidate> candidates;

    public CTreeNode(final CTree tree, final CTreeNode parent,
                     final String groupName, final Predicate<FSpot> predicate) {
        this.parent = parent;
        this.tree = tree;
        this.groupName = groupName;
        this.predicate = predicate;
    }

    public CTree getTree() {
        return tree;
    }

    public CTreeNode getParent() {
        return parent;
    }

    public String getGroupName() {
        return groupName;
    }

    public Predicate<FSpot> getPredicate() {
        return predicate;
    }

    public DensityVector getCounter() {
        return counter;
    }

    public int getBestIndex() {
        return bestIndex;
    }

    public DensityVector getDensity() {
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

    public List<CTreeCandidate> getCandidates() {
        return candidates;
    }

    public void learn(Frame df, Numeric weights, int depth) {
        density = new DensityVector(df.var(tree.firstTargetVar()), weights);
        counter = new DensityVector(df.var(tree.firstTargetVar()), Numeric.newFill(df.rowCount(), 1));
        bestIndex = density.findBestIndex();

        if (df.rowCount() == 0) {
            return;
        }

        if (df.rowCount() <= tree.getMinCount() || counter.countValues(x -> x > 0) == 1 || depth < 1) {
            return;
        }

        candidates = new ArrayList<>();
        tree.getVarSelector().initialize(df, null);

        ConcurrentLinkedQueue<CTreeCandidate> candidates = new ConcurrentLinkedQueue<>();
        Arrays.stream(tree.getVarSelector().nextVarNames()).parallel().forEach(testCol -> {
            if (testCol.equals(tree.firstTargetVar())) return;
            if (!tree.testCounter.canUse(testCol)) return;

            if (df.var(testCol).type().isNumeric()) {
                tree.getNumericMethod().computeCandidates(
                        tree, df, weights, testCol, tree.firstTargetVar(), tree.getFunction())
                        .forEach(candidate -> candidates.add(candidate));
            } else {
                tree.getNominalMethod().computeCandidates(
                        tree, df, weights, testCol, tree.firstTargetVar(), tree.getFunction())
                        .forEach(candidate -> candidates.add(candidate));
            }
        });
        this.candidates.addAll(candidates);
        Collections.sort(this.candidates);

        if (this.candidates.isEmpty()) {
            return;
        }
        leaf = false;

        bestCandidate = this.candidates.get(0);
        tree.testCounter.markUse(this.candidates.get(0).getTestName());

        // now that we have a best candidate, do the effective split

        if (bestCandidate.getGroupNames().isEmpty()) {
            leaf = true;
            return;
        }

        Pair<List<Frame>, List<Numeric>> frames = tree.getSplitter().performSplit(df, weights, bestCandidate);
        children = new ArrayList<>(frames.first.size());
        for (int i = 0; i < frames.first.size(); i++) {
            CTreeNode child = new CTreeNode(tree, this, bestCandidate.getGroupNames().get(i), bestCandidate.getGroupPredicates().get(i));
            children.add(child);
            child.learn(frames.first.get(i), frames.second.get(i), depth - 1);
        }
    }
}
