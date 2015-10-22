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

import rapaio.data.Frame;
import rapaio.data.stream.FSpot;
import rapaio.util.Pair;
import rapaio.util.Tag;

import java.io.Serializable;
import java.util.*;

/**
 * Pruning techniques
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/20/15.
 */
public interface CTreePruning extends Serializable {

    CTree prune(CTree tree, Frame df, boolean all);

    /**
     * No pruning, default schema
     */
    Tag<CTreePruning> NONE = Tag.valueOf("None", (tree, df, all) -> tree);

    /**
     * Reduced error pruning, according with Quinlan for ID3, Described in Tom Mitchell
     */
    Tag<CTreePruning> REDUCED_ERROR = Tag.valueOf("ReducedError", ReducedError::prune);
}

class ReducedError {

    public static CTree prune(CTree tree, Frame df, boolean all) {

        // collect how current fitting works

        HashMap<Integer, CTreeNode> nodes = new HashMap<>();
        collectInfo(tree, tree.getRoot(), nodes);

        // collect fit produced in each node, in a cumulative way

        HashMap<Integer, Pair<Double, Double>> bottomUp = new HashMap<>();
        for (int i : nodes.keySet()) {
            bottomUp.put(i, Pair.valueOf(0.0, 0.0));
        }
        df.stream().forEach(s -> bottomUpCollect(s, tree, tree.getRoot(), bottomUp));

        HashMap<Integer, Pair<Double, Double>> topDown = new HashMap<>();
        for (int i : nodes.keySet()) {
            topDown.put(i, Pair.valueOf(0.0, 0.0));
        }
        df.stream().forEach(s -> topDownCollect(s, tree, tree.getRoot(), topDown));

        // test for pruning

        List<Integer> ids = new ArrayList<>(nodes.keySet());
        Set<Integer> pruned = new HashSet<>();
        boolean found = true;
        double rowCount = df.rowCount();
        while (found) {
            found = false;
            double maxAcc = -1;
            int maxId = -1;

            // find best cut point

            Iterator<Integer> it = ids.iterator();
            while (it.hasNext()) {
                int id = it.next();
                if (pruned.contains(id)) {
                    it.remove();
                    continue;
                }
                double delta = topDown.get(id).second / (topDown.get(id).first + topDown.get(id).second)
                        - bottomUp.get(id).second / (bottomUp.get(id).first + bottomUp.get(id).second);
                if (topDown.get(id).second + topDown.get(id).first != bottomUp.get(id).second + bottomUp.get(id).first) {
                    throw new RuntimeException("problem");
                }
                if (delta >= maxAcc) {
                    maxAcc = delta;
                    maxId = id;
                    found = true;
                }
                if (!all && delta < 0) {
                    pruned.add(id);
                }
            }

            // if found than prune the tree and clear info on pruned nodes

            if (found) {
                updateError(maxId, bottomUp, nodes, Pair.valueOf(
                        topDown.get(maxId).first - bottomUp.get(maxId).first,
                        topDown.get(maxId).second - bottomUp.get(maxId).second));
                addToPruned(maxId, nodes.get(maxId), pruned, topDown, bottomUp, nodes);
                nodes.get(maxId).cut();
            }

            if (tree.getRunningHook() != null) {
                tree.getRunningHook().accept(tree, nodes.size());
            }
        }

        return tree;
    }

    private static void updateError(int id, HashMap<Integer, Pair<Double, Double>> bottomUp, HashMap<Integer, CTreeNode> nodes, Pair<Double, Double> accDiff) {
        Pair<Double, Double> old = bottomUp.get(id);
        bottomUp.put(id, Pair.valueOf(old.first + accDiff.first, old.second + accDiff.second));
        if (nodes.get(id).getParent() != null)
            updateError(nodes.get(id).getParent().getId(), bottomUp, nodes, accDiff);
    }

    private static void addToPruned(int id, CTreeNode node, Set<Integer> pruned,
                                    HashMap<Integer, Pair<Double, Double>> topDown,
                                    HashMap<Integer, Pair<Double, Double>> bottomUp,
                                    HashMap<Integer, CTreeNode> nodes) {
        pruned.add(node.getId());
        if (node.getId() != id) {
            topDown.remove(node.getId());
            bottomUp.remove(node.getId());
            nodes.remove(node.getId());
        }
        for (CTreeNode child : node.getChildren())
            addToPruned(id, child, pruned, topDown, bottomUp, nodes);
    }

    private static void collectInfo(CTree tree, CTreeNode node, HashMap<Integer, CTreeNode> bestIndexes) {
        bestIndexes.put(node.getId(), node);
        for (CTreeNode child : node.getChildren()) {
            collectInfo(tree, child, bestIndexes);
        }
    }

    private static Pair<Double, Double> bottomUpCollect(FSpot spot, CTree tree, CTreeNode node, HashMap<Integer, Pair<Double, Double>> bottomUp) {

        if (node.isLeaf()) {
            Pair<Double, Double> err = spot.index(tree.firstTargetName()) != node.getBestIndex() ? Pair.valueOf(1.0, 0.0) : Pair.valueOf(0.0, 1.0);
            Pair<Double, Double> old = bottomUp.get(node.getId());
            bottomUp.put(node.getId(), Pair.valueOf(old.first + err.first, old.second + err.second));
            return err;
        }

        for (CTreeNode child : node.getChildren()) {
            if (child.getPredicate().test(spot)) {
                Pair<Double, Double> err = bottomUpCollect(spot, tree, child, bottomUp);
                Pair<Double, Double> old = bottomUp.get(node.getId());
                bottomUp.put(node.getId(), Pair.valueOf(old.first + err.first, old.second + err.second));
                return err;
            }
        }
        return Pair.valueOf(0.0, 0.0);
    }

    private static void topDownCollect(FSpot spot, CTree tree, CTreeNode node, HashMap<Integer, Pair<Double, Double>> topDown) {

        Pair<Double, Double> err = spot.index(tree.firstTargetName()) != node.getBestIndex() ? Pair.valueOf(1.0, 0.0) : Pair.valueOf(0.0, 1.0);
        Pair<Double, Double> old = topDown.get(node.getId());
        topDown.put(node.getId(), Pair.valueOf(old.first + err.first, old.second + err.second));

        for (CTreeNode child : node.getChildren()) {
            if (child.getPredicate().test(spot)) {
                topDownCollect(spot, tree, child, topDown);
                return;
            }
        }
    }
}
