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

package rapaio.ml.regressor.tree.rtree;

import rapaio.core.stat.WeightedMean;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.stream.FSpot;
import rapaio.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
public class RTreeNode {

    private final RTreeNode parent;
    private final String groupName;
    private final Predicate<FSpot> predicate;

    private boolean leaf = true;
    private double value;
    private double weight;
    private List<RTreeNode> children;
    private RTreeCandidate bestCandidate;

    public RTreeNode(final RTreeNode parent,
                     final String groupName, final Predicate<FSpot> predicate) {
        this.parent = parent;
        this.groupName = groupName;
        this.predicate = predicate;
    }

    public RTreeNode getParent() {
        return parent;
    }

    public String getGroupName() {
        return groupName;
    }

    public Predicate<FSpot> getPredicate() {
        return predicate;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public List<RTreeNode> getChildren() {
        return children;
    }

    public RTreeCandidate getBestCandidate() {
        return bestCandidate;
    }

    public double getValue() {
        return value;
    }

    public double getWeight() {
        return weight;
    }

    public void learn(RTree tree, Frame df, Var weights, int depth) {
        value = new WeightedMean(df.var(tree.firstTargetVar()), weights).value();
        weight = weights.stream().parallel().complete().mapToDouble().sum();

        if (df.rowCount() == 0 || df.rowCount() <= tree.minCount || depth <= 1) {
            return;
        }

        List<RTreeCandidate> candidateList = new ArrayList<>();
        tree.getVarSelector().initialize(df, null);

        ConcurrentLinkedQueue<RTreeCandidate> candidates = new ConcurrentLinkedQueue<>();
        Arrays.stream(tree.getVarSelector().nextVarNames()).parallel().forEach(testCol -> {
            if (testCol.equals(tree.firstTargetVar())) return;

            if (df.var(testCol).type().isNumeric()) {
                tree.numericMethod.computeCandidates(
                        tree, df, weights, testCol, tree.firstTargetVar(), tree.function)
                        .forEach(candidates::add);
            } else {
                tree.nominalMethod.computeCandidates(
                        tree, df, weights, testCol, tree.firstTargetVar(), tree.function)
                        .forEach(candidates::add);
            }
        });
        candidateList.addAll(candidates);
        Collections.sort(candidateList);

        if (candidateList.isEmpty()) {
            return;
        }
        leaf = false;
        bestCandidate = candidateList.get(0);

        // now that we have a best candidate,do the effective split

        if (bestCandidate.getGroupNames().isEmpty()) {
            leaf = true;
            return;
        }

        Pair<List<Frame>, List<Var>> frames = tree.splitter.performSplit(df, weights, bestCandidate);
        children = new ArrayList<>(frames.first.size());
        for (int i = 0; i < frames.first.size(); i++) {
            RTreeNode child = new RTreeNode(this, bestCandidate.getGroupNames().get(i), bestCandidate.getGroupPredicates().get(i));
            children.add(child);
            child.learn(tree, frames.first.get(i), frames.second.get(i), depth - 1);
        }
    }
}
