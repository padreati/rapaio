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

package rapaio.ml.regression.tree;

import rapaio.core.stat.WeightedMean;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.NumVar;
import rapaio.data.Var;
import rapaio.ml.regression.boost.gbt.GBTRegressionLoss;
import rapaio.util.Pair;
import rapaio.util.func.SBiPredicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/2/17.
 */
public class RTreeNode implements Serializable {

    private static final long serialVersionUID = 385363626560575837L;
    private final RTree tree;
    private final RTreeNode parent;
    private final String groupName;
    private final SBiPredicate<Integer, Frame> predicate;

    private boolean leaf = true;
    private double value;
    private double weight;
    private List<RTreeNode> children = new ArrayList<>();
    private RTreeCandidate bestCandidate;

    public RTreeNode(final RTree tree,
                final RTreeNode parent,
                final String groupName,
                final SBiPredicate<Integer, Frame> predicate) {
        this.tree = tree;
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

    public SBiPredicate<Integer, Frame> getPredicate() {
        return predicate;
    }

    public boolean isLeaf() {
        return leaf;
    }

    public void setLeaf(boolean leaf) {
        this.leaf = leaf;
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

    public void setValue(double value) {
        this.value = value;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void learn(RTree tree, Frame df, Var weights, int depth) {
        value = WeightedMean.from(df.rvar(tree.firstTargetName()), weights).value();
        weight = weights.stream().complete().mapToDouble().sum();
        if (weight == 0) {
            value = parent != null ? parent.value : Double.NaN;
        }

        if (df.rowCount() == 0 || df.rowCount() <= tree.minCount || depth <= 1) {
            return;
        }

        List<RTreeCandidate> candidateList = new ArrayList<>();

        ConcurrentLinkedQueue<RTreeCandidate> candidates = new ConcurrentLinkedQueue<>();
        Stream<String> stream = Arrays.stream(tree.varSelector.nextVarNames());
        if (tree.poolSize() > 0) {
            stream = stream.parallel();
        }
        stream.forEach(testCol -> {
            if (testCol.equals(tree.firstTargetName())) return;

            if (df.rvar(testCol).type().isNumeric()) {
                tree.numericMethod.computeCandidate(
                        tree, df, weights, testCol, tree.firstTargetName(), tree.function)
                        .ifPresent(candidates::add);
            } else {
                tree.nominalMethod.computeCandidate(
                        tree, df, weights, testCol, tree.firstTargetName(), tree.function)
                        .ifPresent(candidates::add);
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

        Pair<List<Frame>, List<Var>> frames = tree.splitter.performSplit(
                df, weights, bestCandidate.getGroupPredicates());
        children = new ArrayList<>(frames._1.size());
        for (int i = 0; i < frames._1.size(); i++) {
            RTreeNode child = new RTreeNode(tree, this, bestCandidate.getGroupNames().get(i), bestCandidate.getGroupPredicates().get(i));
            children.add(child);
            child.learn(tree, frames._1.get(i), frames._2.get(i), depth - 1);
        }
    }

    public void boostUpdate(Frame x, Var y, Var fx, GBTRegressionLoss lossFunction) {
        if (leaf) {
            value = lossFunction.findMinimum(y, fx);
            return;
        }

        List<SBiPredicate<Integer, Frame>> groupPredicates = new ArrayList<>();
        for (RTreeNode child : children) {
            groupPredicates.add(child.getPredicate());
        }

        List<Mapping> mappings = tree.splitter.performMapping(x, NumVar.fill(x.rowCount(), 1), groupPredicates);

        for (int i = 0; i < children.size(); i++) {
            children.get(i).boostUpdate(
                    x.mapRows(mappings.get(i)),
                    y.mapRows(mappings.get(i)),
                    fx.mapRows(mappings.get(i)),
                    lossFunction);
        }
    }
}
