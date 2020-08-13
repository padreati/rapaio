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

package rapaio.ml.classifier.tree.ctree;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import rapaio.data.Frame;
import rapaio.ml.classifier.tree.CTree;
import rapaio.util.DoublePair;

import java.io.Serializable;

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

            Int2ObjectOpenHashMap<Node> nodes = collectNodes(tree, tree.getRoot(), new Int2ObjectOpenHashMap<>());

            // collect predictions produced in each node in a cumulative way

            Int2ObjectOpenHashMap<DoublePair> bottomUp = new Int2ObjectOpenHashMap<>();
            Int2ObjectOpenHashMap<DoublePair> topDown = new Int2ObjectOpenHashMap<>();
            nodes.keySet().forEach((int id) -> {
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

            IntList ids = new IntArrayList(nodes.keySet());
            IntSet pruned = new IntOpenHashSet();
            boolean found = true;
            double rowCount = df.rowCount();
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

                if (tree.runningHook.get() != null) {
                    tree.runningHook.get().accept(tree, nodes.size());
                }
            }

            return tree;
        }

        private void updateError(int id, Int2ObjectOpenHashMap<DoublePair> bottomUp,
                                 Int2ObjectOpenHashMap<Node> nodes, DoublePair accDiff) {
            bottomUp.get(id).increment(accDiff);
            if (nodes.get(id).parent != null)
                updateError(nodes.get(id).parent.id, bottomUp, nodes, accDiff);
        }

        private void addToPruned(int id, Node node, IntSet pruned,
                                 Int2ObjectOpenHashMap<DoublePair> topDown,
                                 Int2ObjectOpenHashMap<DoublePair> bottomUp,
                                 Int2ObjectOpenHashMap<Node> nodes) {
            pruned.add(node.id);
            if (node.id != id) {
                topDown.remove(node.id);
                bottomUp.remove(node.id);
                nodes.remove(node.id);
            }
            for (Node child : node.children)
                addToPruned(id, child, pruned, topDown, bottomUp, nodes);
        }

        private Int2ObjectOpenHashMap<Node> collectNodes(CTree tree, Node node, Int2ObjectOpenHashMap<Node> nodes) {
            nodes.put(node.id, node);
            for (Node child : node.children) {
                collectNodes(tree, child, nodes);
            }
            return nodes;
        }

        private DoublePair bottomUpCollect(int row, Frame df, CTree tree, Node node, Int2ObjectOpenHashMap<DoublePair> bottomUp) {

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

        private void topDownCollect(int row, Frame df, CTree tree, Node node, Int2ObjectOpenHashMap<DoublePair> topDown) {

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