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

package rapaio.experiment.ml.classifier.tree;

import rapaio.data.Frame;
import rapaio.util.DoublePair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Pruning techniques
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/20/15.
 */
public interface CTreePruning extends Serializable {

    String name();

    default CTree prune(CTree tree, Frame df) {
        return prune(tree, df, false);
    }

    CTree prune(CTree tree, Frame df, boolean all);

    /**
     * No pruning, default schema
     */
    CTreePruning None = new CTreePruning() {
        private static final long serialVersionUID = 5560247560756643826L;

        @Override
        public String name() {
            return "none";
        }

        @Override
        public CTree prune(CTree tree, Frame df, boolean all) {
            return tree;
        }
    };

    /**
     * Reduced error pruning, according with Quinlan for ID3, Described in Tom Mitchell
     */
    CTreePruning ReducedError = new CTreePruning() {
        private static final long serialVersionUID = 6935342081721699245L;

        @Override
        public String name() {
            return "ReducedError";
        }

        @Override
        public CTree prune(CTree tree, Frame df, boolean all) {
            // collect how current fitting works

            HashMap<Integer, CTreeNode> nodes = collectNodes(tree, tree.getRoot(), new HashMap<>());

            // collect predict produced in each node, in a cumulative way

            HashMap<Integer, DoublePair> bottomUp = new HashMap<>();
            HashMap<Integer, DoublePair> topDown = new HashMap<>();
            nodes.keySet().forEach(id -> {
                bottomUp.put(id, DoublePair.zeros());
                topDown.put(id, DoublePair.zeros());
            });

            for (int i = 0; i < df.rowCount(); i++) {
                bottomUpCollect(i, df, tree, tree.getRoot(), bottomUp);
            }
            for (int i = 0; i < df.rowCount(); i++) {
                topDownCollect(i, df, tree, tree.getRoot(), topDown);
            }

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
                    updateError(maxId, bottomUp, nodes, DoublePair.of(
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

        private void updateError(int id, HashMap<Integer, DoublePair> bottomUp, HashMap<Integer, CTreeNode> nodes, DoublePair accDiff) {
            bottomUp.get(id).increment(accDiff);
            if (nodes.get(id).getParent() != null)
                updateError(nodes.get(id).getParent().getId(), bottomUp, nodes, accDiff);
        }

        private void addToPruned(int id, CTreeNode node, Set<Integer> pruned,
                                 HashMap<Integer, DoublePair> topDown,
                                 HashMap<Integer, DoublePair> bottomUp,
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

        private HashMap<Integer, CTreeNode> collectNodes(CTree tree, CTreeNode node, HashMap<Integer, CTreeNode> nodes) {
            nodes.put(node.getId(), node);
            for (CTreeNode child : node.getChildren()) {
                collectNodes(tree, child, nodes);
            }
            return nodes;
        }

        private DoublePair bottomUpCollect(int row, Frame df, CTree tree, CTreeNode node, HashMap<Integer, DoublePair> bottomUp) {

            if (node.isLeaf()) {
                DoublePair err = !df.getLabel(row, tree.firstTargetName()).equals(node.getBestLabel()) ? DoublePair.of(1.0, 0.0) : DoublePair.of(0.0, 1.0);
                bottomUp.get(node.getId()).increment(err);
                return err;
            }

            for (CTreeNode child : node.getChildren()) {
                if (child.getPredicate().test(row, df)) {
                    DoublePair err = bottomUpCollect(row, df, tree, child, bottomUp);
                    bottomUp.get(node.getId()).increment(err);
                    return err;
                }
            }
            return DoublePair.zeros();
        }

        private void topDownCollect(int row, Frame df, CTree tree, CTreeNode node, HashMap<Integer, DoublePair> topDown) {

            DoublePair err = !df.getLabel(row, tree.firstTargetName()).equals(node.getBestLabel()) ? DoublePair.of(1.0, 0.0) : DoublePair.of(0.0, 1.0);
            topDown.get(node.getId()).increment(err);

            for (CTreeNode child : node.getChildren()) {
                if (child.getPredicate().test(row, df)) {
                    topDownCollect(row, df, tree, child, topDown);
                    return;
                }
            }
        }
    };
}