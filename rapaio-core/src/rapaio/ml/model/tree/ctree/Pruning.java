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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.ml.model.tree.ctree;

import java.io.Serializable;
import java.util.HashMap;

import rapaio.data.Frame;
import rapaio.ml.model.RunInfo;
import rapaio.ml.model.tree.CTree;
import rapaio.util.DoublePair;
import rapaio.util.collection.IntArrayList;
import rapaio.util.collection.IntOpenHashSet;

/**
 * Pruning techniques
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/20/15.
 */
public enum Pruning implements Serializable {

    /**
     * No pruning, default schema
     */
    None {
        @Override
        public CTree prune(CTree tree, Frame df, boolean all) {
            return tree;
        }
    },
    /**
     * Reduced error pruning, according with Quinlan for ID3, Described in Tom Mitchell
     */
    ReducedError {
        @Override
        public CTree prune(CTree tree, Frame df, boolean all) {
            // collect how current fitting works

            HashMap<Integer, Node> nodes = collectNodes(tree.getRoot(), new HashMap<>());

            // collect predictions produced in each node in a cumulative way

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

            IntArrayList ids = new IntArrayList(nodes.keySet());
            IntOpenHashSet pruned = new IntOpenHashSet();
            boolean found = true;
            while (found) {
                found = false;
                double maxAcc = -1;
                int maxId = -1;

                // find best cut point

                var it = ids.iterator();
                while (it.hasNext()) {
                    int id = it.nextInt();
                    if (pruned.contains(id)) {
                        it.remove();
                        continue;
                    }
                    double delta = topDown.get(id).v2 / topDown.get(id).sum() - bottomUp.get(id).v2 / bottomUp.get(id).sum();
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
                            topDown.get(maxId).v1 - bottomUp.get(maxId).v1,
                            topDown.get(maxId).v2 - bottomUp.get(maxId).v2));
                    addToPruned(maxId, nodes.get(maxId), pruned, topDown, bottomUp, nodes);
                    nodes.get(maxId).cut();
                }

                if (tree.runningHook.get() != null) {
                    tree.runningHook.get().accept(RunInfo.forClassifier(tree, nodes.size()));
                }
            }

            return tree;
        }

        private void updateError(int id, HashMap<Integer, DoublePair> bottomUp,
                                 HashMap<Integer, Node> nodes, DoublePair accDiff) {
            bottomUp.get(id).increment(accDiff);
            if (nodes.get(id).parent != null)
                updateError(nodes.get(id).parent.id, bottomUp, nodes, accDiff);
        }

        private void addToPruned(int id, Node node, IntOpenHashSet pruned,
                                 HashMap<Integer, DoublePair> topDown,
                                 HashMap<Integer, DoublePair> bottomUp,
                                 HashMap<Integer, Node> nodes) {
            pruned.add(node.id);
            if (node.id != id) {
                topDown.remove(node.id);
                bottomUp.remove(node.id);
                nodes.remove(node.id);
            }
            for (Node child : node.children)
                addToPruned(id, child, pruned, topDown, bottomUp, nodes);
        }

        private HashMap<Integer, Node> collectNodes(Node node, HashMap<Integer, Node> nodes) {
            nodes.put(node.id, node);
            for (Node child : node.children) {
                collectNodes(child, nodes);
            }
            return nodes;
        }

        private DoublePair bottomUpCollect(int row, Frame df, CTree tree, Node node, HashMap<Integer, DoublePair> bottomUp) {

            if (node.leaf) {
                DoublePair err = df.getLabel(row, tree.firstTargetName()).equals(node.bestLabel)
                        ? DoublePair.of(0.0, 1.0) : DoublePair.of(1.0, 0.0);
                bottomUp.get(node.id).increment(err);
                return err;
            }

            for (Node child : node.children) {
                if (child.predicate.test(row, df)) {
                    DoublePair err = bottomUpCollect(row, df, tree, child, bottomUp);
                    bottomUp.get(node.id).increment(err);
                    return err;
                }
            }
            return DoublePair.zeros();
        }

        private void topDownCollect(int row, Frame df, CTree tree, Node node, HashMap<Integer, DoublePair> topDown) {

            DoublePair err = df.getLabel(row, tree.firstTargetName()).equals(node.bestLabel)
                    ? DoublePair.of(0.0, 1.0) : DoublePair.of(1.0, 0.0);
            topDown.get(node.id).increment(err);

            for (Node child : node.children) {
                if (child.predicate.test(row, df)) {
                    topDownCollect(row, df, tree, child, topDown);
                    return;
                }
            }
        }
    };

    public CTree prune(CTree tree, Frame df) {
        return prune(tree, df, true);
    }

    public abstract CTree prune(CTree tree, Frame df, boolean all);
}