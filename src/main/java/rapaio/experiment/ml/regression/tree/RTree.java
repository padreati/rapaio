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

package rapaio.experiment.ml.regression.tree;

import rapaio.core.stat.WeightedMean;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.stream.FSpot;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.VarSelector;
import rapaio.ml.regression.AbstractRegression;
import rapaio.ml.regression.RFit;
import rapaio.experiment.ml.regression.boost.gbt.BTRegression;
import rapaio.experiment.ml.regression.boost.gbt.GBTLossFunction;
import rapaio.util.Pair;
import rapaio.util.func.SPredicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

import static rapaio.sys.WS.formatFlex;

/**
 * Implements a regression tree.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
@Deprecated
public class RTree extends AbstractRegression implements BTRegression {

    private static final long serialVersionUID = -2748764643670512376L;

    int minCount = 1;
    int maxDepth = -1;

    RTreeNominalMethod nominalMethod = RTreeNominalMethod.BINARY;
    RTreeNumericMethod numericMethod = RTreeNumericMethod.BINARY;
    RTreeTestFunction function = RTreeTestFunction.WeightedVarGain;
    RTreeSplitter splitter = RTreeSplitter.REMAINS_IGNORED;
    RTreePredictor predictor = RTreePredictor.STANDARD;
    VarSelector varSelector = VarSelector.ALL;

    // tree root node
    private RTreeNode root;
    private int rows;

    private RTree() {
    }

    public static RTree buildDecisionStump() {
        return new RTree()
                .withMaxDepth(2)
                .withNominalMethod(RTreeNominalMethod.BINARY)
                .withNumericMethod(RTreeNumericMethod.BINARY)
                .withSplitter(RTreeSplitter.REMAINS_TO_MAJORITY)
                ;
    }

    public static RTree buildC45() {
        return new RTree()
                .withMaxDepth(-1)
                .withNominalMethod(RTreeNominalMethod.FULL)
                .withNumericMethod(RTreeNumericMethod.BINARY)
                .withSplitter(RTreeSplitter.REMAINS_TO_RANDOM)
                .withMinCount(2)
                ;
    }

    public static RTree buildCART() {
        return new RTree()
                .withMaxDepth(-1)
                .withNominalMethod(RTreeNominalMethod.BINARY)
                .withNumericMethod(RTreeNumericMethod.BINARY)
                .withSplitter(RTreeSplitter.REMAINS_TO_RANDOM)
                .withFunction(RTreeTestFunction.WeightedSdGain)
                .withMinCount(1);
    }

    @Override
    public BTRegression newInstance() {
        return new RTree()
                .withMinCount(minCount)
                .withNumericMethod(numericMethod)
                .withNominalMethod(nominalMethod)
                .withMaxDepth(maxDepth)
                .withSplitter(splitter)
                .withFunction(function)
                .withVarSelector(varSelector);
    }

    @Override
    public String name() {
        return "RTree";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("TreeClassifier {");
        sb.append("  varSelector=").append(varSelector.name()).append(",\n");
        sb.append("  minCount=").append(minCount).append(",\n");
        sb.append("  maxDepth=").append(maxDepth).append(",\n");
        sb.append("  numericMethod=").append(numericMethod.name()).append(",\n");
        sb.append("  nominalMethod=").append(nominalMethod.name()).append(",\n");
        sb.append("  function=").append(function.name()).append(",\n");
        sb.append("  splitter=").append(splitter.name()).append(",\n");
        sb.append("  predictor=").append(predictor.name()).append("\n");
        sb.append("}");
        return sb.toString();
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .withInputCount(1, 1_000_000)
                .withTargetCount(1, 1)
                .withInputTypes(VarType.BINARY, VarType.INDEX, VarType.NUMERIC, VarType.ORDINAL, VarType.NOMINAL)
                .withTargetTypes(VarType.NUMERIC)
                .withAllowMissingInputValues(true)
                .withAllowMissingTargetValues(false);
    }

    @Override
    public void boostFit(Frame x, Var y, Var fx, GBTLossFunction lossFunction) {
        root.boostFit(x, y, fx, lossFunction);
    }

    public RTree withVarSelector(VarSelector varSelector) {
        this.varSelector = varSelector;
        return this;
    }

    public RTree withMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    public RTree withMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public RTree withNumericMethod(RTreeNumericMethod numericMethod) {
        this.numericMethod = numericMethod;
        return this;
    }

    public RTree withNominalMethod(RTreeNominalMethod nominalMethod) {
        this.nominalMethod = nominalMethod;
        return this;
    }

    public RTree withFunction(RTreeTestFunction function) {
        this.function = function;
        return this;
    }

    public RTree withSplitter(RTreeSplitter splitter) {
        this.splitter = splitter;
        return this;
    }

    @Override
    protected boolean coreTrain(Frame df, Var weights) {

        if (targetNames().length == 0) {
            throw new IllegalArgumentException("tree classifier must specify a target variable");
        }
        if (targetNames().length > 1) {
            throw new IllegalArgumentException("tree classifier can't fit more than one target variable");
        }

        rows = df.rowCount();

        root = new RTreeNode(null, "root", spot -> true);
        this.varSelector.withVarNames(inputNames());
        root.learn(this, df, weights, maxDepth < 0 ? Integer.MAX_VALUE : maxDepth);
        return true;
    }

    @Override
    protected RFit coreFit(Frame df, boolean withResiduals) {
        RFit pred = RFit.build(this, df, withResiduals);

        df.stream().forEach(spot -> {
            Pair<Double, Double> result = predictor.predict(this, spot, root);
            pred.fit(firstTargetName()).setValue(spot.row(), result._1);
        });
        pred.buildComplete();
        return pred;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n > ").append(fullName()).append("\n");

        sb.append(String.format("n=%d\n", rows));

        sb.append("\n");
        sb.append("description:\n");
        sb.append("split, mean (total weight) [* if is leaf]\n\n");

        buildSummary(sb, root, 0);
        return sb.toString();
    }

    private void buildSummary(StringBuilder sb, RTreeNode node, int level) {
        sb.append("|");
        for (int i = 0; i < level; i++) {
            sb.append("   |");
        }
        sb.append(node.getGroupName()).append("  ");

        sb.append(formatFlex(node.getValue()));
        sb.append(" (").append(formatFlex(node.getWeight())).append(") ");
        if (node.isLeaf()) sb.append(" *");
        sb.append("\n");

//        children

        if (!node.isLeaf()) {
            node.getChildren().stream().forEach(child -> buildSummary(sb, child, level + 1));
        }
    }

    /**
     * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
     */
    @Deprecated
    public static class RTreeNode implements Serializable {

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
            if (weight == 0) {
//                WS.println("ERROR");
                value = parent!=null ? parent.value : Double.NaN;
            }

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

            Mapping[] mapping = IntStream
                    .range(0, children.size()).boxed()
                    .map(i -> Mapping.empty()).toArray(Mapping[]::new);
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

    /**
     * RTree split candidate.
     * <p>
     * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
     */
    public static class RTreeCandidate implements Comparable<RTreeCandidate>, Serializable {

        private static final long serialVersionUID = 6698766675237089849L;
        private final double score;
        private final String testName;
        private final List<String> groupNames = new ArrayList<>();
        private final List<SPredicate<FSpot>> groupPredicates = new ArrayList<>();

        public RTreeCandidate(double score, String testName) {
            this.score = score;
            this.testName = testName;
        }

        public void addGroup(String name, SPredicate<FSpot> predicate) {
            if (groupNames.contains(name)) {
                throw new IllegalArgumentException("group name already defined");
            }
            groupNames.add(name);
            groupPredicates.add(predicate);
        }

        public List<String> getGroupNames() {
            return groupNames;
        }

        public List<SPredicate<FSpot>> getGroupPredicates() {
            return groupPredicates;
        }

        public double getScore() {
            return score;
        }

        public String getTestName() {
            return testName;
        }

        @Override
        public int compareTo(RTreeCandidate o) {
            if (o == null) return 1;
            return -Double.compare(score, o.score);
        }
    }
}
