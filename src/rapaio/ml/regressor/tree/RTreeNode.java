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

package rapaio.ml.regressor.tree;

import rapaio.core.stat.WeightedMean;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.stream.FSpot;
import rapaio.ml.regressor.boost.gbt.GBTLossFunction;
import rapaio.util.Pair;
import rapaio.util.func.SPredicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
@Deprecated
public class RTreeNode implements Serializable {

    private static final long serialVersionUID = 385363626560575837L;
    private final RTreeNode parent;
    private final String groupName;
    private final SPredicate<FSpot> predicate;

    private boolean leaf = true;
    private double value;
    private double weight;
    private List<RTreeNode> children;
    private RTreeCandidate bestCandidate;

    public RTreeNode(final RTreeNode parent,
                     final String groupName,
                     final SPredicate<FSpot> predicate) {
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

    public SPredicate<FSpot> getPredicate() {
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
        value = new WeightedMean(df.var(tree.firstTargetName()), weights).value();
        weight = weights.stream().complete().mapToDouble().sum();

        if (df.rowCount() == 0 || df.rowCount() <= tree.minCount || depth <= 1) {
            return;
        }

        List<RTreeCandidate> candidateList = new ArrayList<>();

        ConcurrentLinkedQueue<RTreeCandidate> candidates = new ConcurrentLinkedQueue<>();
        Arrays.stream(tree.varSelector.nextVarNames()).parallel().forEach(testCol -> {
            if (testCol.equals(tree.firstTargetName())) return;

            if (df.var(testCol).type().isNumeric()) {
                tree.numericMethod.computeCandidates(
                        tree, df, weights, testCol, tree.firstTargetName(), tree.function)
                        .forEach(candidates::add);
            } else {
                tree.nominalMethod.computeCandidates(
                        tree, df, weights, testCol, tree.firstTargetName(), tree.function)
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
        children = new ArrayList<>(frames._1.size());
        for (int i = 0; i < frames._1.size(); i++) {
            RTreeNode child = new RTreeNode(this, bestCandidate.getGroupNames().get(i), bestCandidate.getGroupPredicates().get(i));
            children.add(child);
            child.learn(tree, frames._1.get(i), frames._2.get(i), depth - 1);
        }
    }

    public void boostFit(Frame x, Var y, Var fx, GBTLossFunction lossFunction) {
        if (leaf) {
            value = lossFunction.findMinimum(y, fx);
            return;
        }

        Mapping[] mapping = new Mapping[children.size()];
        for (int i = 0; i < children.size(); i++) {
            mapping[i] = Mapping.newEmpty();
        }
        x.stream().forEach(spot -> {
            for (int i = 0; i < children.size(); i++) {
                RTreeNode child = children.get(i);
                if (child.predicate.test(spot)) {
                    mapping[i].add(spot.row());
                    return;
                }
            }
        });

        for (int i = 0; i < children.size(); i++) {
            children.get(i).boostFit(x.mapRows(mapping[i]), y.mapRows(mapping[i]), fx.mapRows(mapping[i]), lossFunction);
        }
    }
}
