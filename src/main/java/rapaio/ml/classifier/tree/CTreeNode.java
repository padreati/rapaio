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
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.stream.FSpot;
import rapaio.ml.common.VarSelector;
import rapaio.util.Pair;
import rapaio.util.func.SPredicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public class CTreeNode implements Serializable {

    private static final long serialVersionUID = -5045581827808911763L;
    private final CTreeNode parent;
    private final String groupName;
    private final SPredicate<FSpot> predicate;
    private final List<CTreeNode> children = new ArrayList<>();
    private int id;
    private boolean leaf = true;
    private DVector density;
    private DVector counter;
    private int bestIndex;
    private CTreeCandidate bestCandidate;

    public CTreeNode(final CTreeNode parent, final String groupName, final SPredicate<FSpot> predicate) {
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

    public String getGroupName() {
        return groupName;
    }

    public Predicate<FSpot> getPredicate() {
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

    public int fillId(int index) {
        id = index;
        int next = index;
        for (CTreeNode child : getChildren()) {
            next = child.fillId(next + 1);
        }
        return next;
    }

    public void cut() {
        leaf = true;
        children.clear();
    }

    public void learn(CTree tree, Frame df, Var weights, int depth) {
        density = DVector.newFromWeights(false, df.var(tree.firstTargetName()), weights);
        counter = DVector.fromCount(false, df.var(tree.firstTargetName()));
        bestIndex = density.findBestIndex();

        if (df.rowCount() == 0) {
            bestIndex = parent.bestIndex;
            return;
        }
        if (counter.countValues(x -> x > 0) == 1 || depth < 1 || df.rowCount() <= tree.minCount()) {
            return;
        }

        VarSelector varSel = tree.varSelector();
        String[] nextVarNames = varSel.nextAllVarNames();
        List<CTreeCandidate> candidateList = new ArrayList<>();
        Queue<String> exhaustList = new ConcurrentLinkedQueue<>();

        if (tree.runPoolSize() == 0) {
            int m = varSel.mCount();
            for (String testCol : nextVarNames) {
                if (m <= 0) {
                    continue;
                }
                if (testCol.equals(tree.firstTargetName())) {
                    continue;
                }

                CTreePurityTest test = null;
                if (tree.customTestMap().containsKey(testCol)) {
                    test = tree.customTestMap().get(testCol);
                }
                if (tree.testMap().containsKey(df.var(testCol).type())) {
                    test = tree.testMap().get(df.var(testCol).type());
                }
                if (test == null) {
                    throw new IllegalArgumentException("can't train ctree with no " +
                            "tests for given variable: " + df.var(testCol).name() +
                            " [" + df.var(testCol).type().name() + "]");
                }
                CTreeCandidate candidate = test.computeCandidate(
                        tree, df, weights, testCol, tree.firstTargetName(), tree.getFunction());
                if (candidate != null) {
                    candidateList.add(candidate);
                    m--;
                } else {
                    exhaustList.add(testCol);
                }
            }
        } else {
            int m = varSel.mCount();
            int start = 0;

            while (m > 0 && start < nextVarNames.length) {
                List<CTreeCandidate> next = IntStream.range(start, Math.min(nextVarNames.length, start + m))
                        .parallel()
                        .mapToObj(i -> nextVarNames[i])
                        .filter(testCol -> !testCol.equals(tree.firstTargetName()))
                        .map(testCol -> {
                            CTreePurityTest test = null;
                            if (tree.customTestMap().containsKey(testCol)) {
                                test = tree.customTestMap().get(testCol);
                            }
                            if (tree.testMap().containsKey(df.var(testCol).type())) {
                                test = tree.testMap().get(df.var(testCol).type());
                            }
                            if (test == null) {
                                throw new IllegalArgumentException("can't train ctree with no " +
                                        "tests for given variable: " + df.var(testCol).name() +
                                        " [" + df.var(testCol).type().name() + "]");
                            }
                            CTreeCandidate candidate = test.computeCandidate(
                                    tree, df, weights, testCol, tree.firstTargetName(), tree.getFunction());
                            if (candidate == null) {
                                exhaustList.add(testCol);
                            }
                            return candidate;
                        })
                        .filter(c -> c != null)
                        .collect(Collectors.toList());
                candidateList.addAll(next);
                start += m;
                m -= next.size();
            }
        }
        Collections.sort(candidateList);
        if (candidateList.isEmpty() || candidateList.get(0).getGroupNames().isEmpty()) {
            return;
        }
        // leave as leaf if the gain is not bigger than minimum gain
        if(candidateList.get(0).getScore()<= tree.minGain()) {
            return;
        }

        leaf = false;
        bestCandidate = candidateList.get(0);
        String testName = bestCandidate.getTestName();

        // now that we have a best candidate, do the effective split
        Pair<List<Frame>, List<Var>> frames = tree.getMissingHandler().performSplit(df, weights, bestCandidate);

        for (int i = 0; i < bestCandidate.getGroupNames().size(); i++) {
            CTreeNode child = new CTreeNode(this, bestCandidate.getGroupNames().get(i), bestCandidate.getGroupPredicates().get(i));
            children.add(child);
        }
        tree.varSelector().removeVarNames(exhaustList);
        for (int i = 0; i < children.size(); i++) {
            children.get(i).learn(tree, frames._1.get(i), frames._2.get(i), depth - 1);
        }
        tree.varSelector().addVarNames(exhaustList);
    }
}
