/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import rapaio.util.Tag;
import rapaio.util.ValuePair;

import java.io.Serializable;
import java.util.*;

/**
 * Pruning techniques
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/20/15.
 */
public interface CTreePruning extends Serializable {

    /**
     * No pruning, default schema
     */
    Tag<CTreePruning> NONE = Tag.valueOf("None", (tree, df, all) -> tree);
    /**
     * Reduced error pruning, according with Quinlan for ID3, Described in Tom Mitchell
     */
    Tag<CTreePruning> REDUCED_ERROR = Tag.valueOf("ReducedError", ReducedErrorPruning::prune);

    default CTree prune(CTree tree, Frame df) {
        return prune(tree, df, false);
    }

    CTree prune(CTree tree, Frame df, boolean all);
}

class ReducedErrorPruning {

    public static CTree prune(CTree tree, Frame df, boolean all) {

        // collect how current fitting works

        HashMap<Integer, CTreeNode> nodes = collectNodes(tree, tree.getRoot(), new HashMap<>());

        // collect fit produced in each node, in a cumulative way

        HashMap<Integer, ValuePair> bottomUp = new HashMap<>();
        HashMap<Integer, ValuePair> topDown = new HashMap<>();
        nodes.keySet().forEach(id -> {
            bottomUp.put(id, ValuePair.empty());
            topDown.put(id, ValuePair.empty());
        });

        df.stream().forEach(s -> bottomUpCollect(s, tree, tree.getRoot(), bottomUp));
        df.stream().forEach(s -> topDownCollect(s, tree, tree.getRoot(), topDown));

        // test for pruning

        List<Integer> ids = new ArrayList<>(nodes.keySet());
        Set<Integer> pruned = new HashSet<>();
        boolean found = true;
        double rowCount = df.getRowCount();
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
                double delta = topDown.get(id)._2 / topDown.get(id).sum() - bottomUp.get(id)._2 / bottomUp.get(id).sum();
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
                updateError(maxId, bottomUp, nodes, ValuePair.of(
                        topDown.get(maxId)._1 - bottomUp.get(maxId)._1,
                        topDown.get(maxId)._2 - bottomUp.get(maxId)._2));
                addToPruned(maxId, nodes.get(maxId), pruned, topDown, bottomUp, nodes);
                nodes.get(maxId).cut();
            }

            if (tree.runningHook() != null) {
                tree.runningHook().accept(tree, nodes.size());
            }
        }

        return tree;
    }

    private static void updateError(int id, HashMap<Integer, ValuePair> bottomUp, HashMap<Integer, CTreeNode> nodes, ValuePair accDiff) {
        bottomUp.get(id).increment(accDiff);
        if (nodes.get(id).getParent() != null)
            updateError(nodes.get(id).getParent().getId(), bottomUp, nodes, accDiff);
    }

    private static void addToPruned(int id, CTreeNode node, Set<Integer> pruned,
                                    HashMap<Integer, ValuePair> topDown,
                                    HashMap<Integer, ValuePair> bottomUp,
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

    private static HashMap<Integer, CTreeNode> collectNodes(CTree tree, CTreeNode node, HashMap<Integer, CTreeNode> nodes) {
        nodes.put(node.getId(), node);
        for (CTreeNode child : node.getChildren()) {
            collectNodes(tree, child, nodes);
        }
        return nodes;
    }

    private static ValuePair bottomUpCollect(FSpot spot, CTree tree, CTreeNode node, HashMap<Integer, ValuePair> bottomUp) {

        if (node.isLeaf()) {
            ValuePair err = spot.getIndex(tree.firstTargetName()) != node.getBestIndex() ? ValuePair.of(1.0, 0.0) : ValuePair.of(0.0, 1.0);
            bottomUp.get(node.getId()).increment(err);
            return err;
        }

        for (CTreeNode child : node.getChildren()) {
            if (child.getPredicate().test(spot)) {
                ValuePair err = bottomUpCollect(spot, tree, child, bottomUp);
                bottomUp.get(node.getId()).increment(err);
                return err;
            }
        }
        return ValuePair.empty();
    }

    private static void topDownCollect(FSpot spot, CTree tree, CTreeNode node, HashMap<Integer, ValuePair> topDown) {

        ValuePair err = spot.getIndex(tree.firstTargetName()) != node.getBestIndex() ? ValuePair.of(1.0, 0.0) : ValuePair.of(0.0, 1.0);
        topDown.get(node.getId()).increment(err);

        for (CTreeNode child : node.getChildren()) {
            if (child.getPredicate().test(spot)) {
                topDownCollect(spot, tree, child, topDown);
                return;
            }
        }
    }
}
