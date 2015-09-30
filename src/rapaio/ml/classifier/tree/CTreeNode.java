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
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.data.stream.FSpot;
import rapaio.util.Pair;
import rapaio.util.func.SPredicate;

import java.io.Serializable;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public class CTreeNode implements Serializable {

    private static final long serialVersionUID = -5045581827808911763L;

    private final CTreeNode parent;
    private final String groupName;
    private final SPredicate<FSpot> predicate;

    private boolean leaf = true;
    private final List<CTreeNode> children = new ArrayList<>();
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

    public void learn(CTree tree, Frame df, Var weights, int depth, CTreeNominalTerms terms) {
        density = DVector.newFromWeights(df.var(tree.firstTargetName()), weights);
        density.normalize(false);

        counter = DVector.newFromWeights(df.var(tree.firstTargetName()), Numeric.newFill(df.rowCount(), 1));
        bestIndex = density.findBestIndex(false);


        if (df.rowCount() == 0) {
            return;
        }

        if (df.rowCount() <= tree.getMinCount() || counter.countValues(x -> x > 0, false) == 1 || depth < 1) {
            return;
        }

        List<CTreeCandidate> candidateList = new ArrayList<>();

        LinkedList<CTreeCandidate> candidates = new LinkedList<>();
        for (String testCol : tree.varSelector().nextVarNames()) {
            if (testCol.equals(tree.firstTargetName())) break;
            if (!tree.testCounter.canUse(testCol)) break;

            if (df.var(testCol).type().isNumeric()) {
                tree.getNumericMethod().get().computeCandidates(
                        tree, df, weights, testCol, tree.firstTargetName(), tree.getFunction())
                        .forEach(candidates::add);
            } else {
                tree.getNominalMethod().get().computeCandidates(
                        tree, df, weights, testCol, tree.firstTargetName(), tree.getFunction(), terms)
                        .forEach(candidates::add);
            }
        }
        candidateList.addAll(candidates);

//         TODO test this better; parallel this can be broken here
//
//        LinkedList<CTreeCandidate> candidates = new LinkedList<>();
//        Arrays.stream(tree.varSelector().nextVarNames())
//                .parallel()
//                .filter(testCol -> !testCol.equals(tree.firstTargetName()))
//                .filter(tree.testCounter::canUse)
//                .map(testCol -> {
//                    return df.var(testCol).type().isNumeric() ?
//                            tree.getNumericMethod().computeCandidates(tree, df, weights, testCol, tree.firstTargetName(), tree.getFunction())
//                            :
//                            tree.getNominalMethod().computeCandidates(tree, df, weights, testCol, tree.firstTargetName(), tree.getFunction(), terms);
//
//                }).collect(Collectors.toList()).stream().sequential().forEach(candidateList::addAll);


        Collections.sort(candidateList);

        if (candidateList.isEmpty()) {
            return;
        }
        leaf = false;

        bestCandidate = candidateList.get(0);
        String testName = bestCandidate.getTestName();
        tree.testCounter.use(testName);

        // now that we have a best candidate, do the effective split

        if (bestCandidate.getGroupNames().isEmpty()) {
            leaf = true;
            tree.testCounter.free(testName);
            return;
        }

        Pair<List<Frame>, List<Var>> frames = tree.getSplitter().get().performSplit(df, weights, bestCandidate);

        for (int i = 0; i < frames.first.size(); i++) {
            CTreeNode child = new CTreeNode(this, bestCandidate.getGroupNames().get(i), bestCandidate.getGroupPredicates().get(i));
            children.add(child);
            child.learn(tree, frames.first.get(i), frames.second.get(i), depth - 1, terms.solidCopy());
        }
        tree.testCounter.free(testName);
    }
}
