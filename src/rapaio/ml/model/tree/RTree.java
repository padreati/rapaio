/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.model.tree;

import static rapaio.printer.Format.*;

import java.io.Serial;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Sum;
import rapaio.core.stat.WeightedMean;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.VarSelector;
import rapaio.ml.common.param.MultiParam;
import rapaio.ml.common.param.ValueParam;
import rapaio.ml.loss.L2Loss;
import rapaio.ml.loss.Loss;
import rapaio.ml.model.RegressionResult;
import rapaio.ml.model.RunInfo;
import rapaio.ml.model.boost.GBTRtree;
import rapaio.ml.model.tree.rtree.Candidate;
import rapaio.ml.model.tree.rtree.Node;
import rapaio.ml.model.tree.rtree.Search;
import rapaio.ml.model.tree.rtree.Splitter;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.DoublePair;

/**
 * Implements a regression decision tree.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/24/14.
 */
public class RTree extends GBTRtree<RTree, RegressionResult, RunInfo<RTree>> {

    @Serial
    private static final long serialVersionUID = -2748764643670512376L;

    public static RTree newDecisionStump() {
        return new RTree()
                .maxDepth.set(2)
                .test.add(VarType.DOUBLE, Search.NumericBinary)
                .test.add(VarType.INT, Search.NumericBinary)
                .test.add(VarType.BINARY, Search.NumericBinary)
                .test.add(VarType.NOMINAL, Search.NominalBinary)
                .splitter.set(Splitter.Majority);
    }

    public static RTree newC45() {
        return new RTree()
                .maxDepth.set(Integer.MAX_VALUE)
                .test.add(VarType.DOUBLE, Search.NumericBinary)
                .test.add(VarType.INT, Search.NumericBinary)
                .test.add(VarType.BINARY, Search.NumericBinary)
                .test.add(VarType.NOMINAL, Search.NominalFull)
                .splitter.set(Splitter.Random)
                .minCount.set(2);
    }

    public static RTree newCART() {
        return new RTree()
                .maxDepth.set(Integer.MAX_VALUE)
                .test.add(VarType.DOUBLE, Search.NumericBinary)
                .test.add(VarType.INT, Search.NumericBinary)
                .test.add(VarType.BINARY, Search.NumericBinary)
                .test.add(VarType.NOMINAL, Search.NominalBinary)
                .splitter.set(Splitter.Random)
                .minCount.set(1);
    }

    private static final Map<VarType, Search> DEFAULT_TEST_MAP;

    static {
        DEFAULT_TEST_MAP = new HashMap<>();
        DEFAULT_TEST_MAP.put(VarType.DOUBLE, Search.NumericBinary);
        DEFAULT_TEST_MAP.put(VarType.INT, Search.NumericBinary);
        DEFAULT_TEST_MAP.put(VarType.LONG, Search.NumericBinary);
        DEFAULT_TEST_MAP.put(VarType.BINARY, Search.NumericBinary);
        DEFAULT_TEST_MAP.put(VarType.NOMINAL, Search.NominalFull);
        DEFAULT_TEST_MAP.put(VarType.STRING, Search.Ignore);
    }

    /**
     * Minimum number of observations in a leaf node
     */
    public final ValueParam<Integer, RTree> minCount = new ValueParam<>(this, 1, "minCount", x -> x != null && x >= 1);

    /**
     * Maximum depth of the tree
     */
    public final ValueParam<Integer, RTree> maxDepth = new ValueParam<>(this, Integer.MAX_VALUE, "maxDepth", x -> x != null && x > 0);

    /**
     * Maximum number of nodes a tree is grown
     */
    public final ValueParam<Integer, RTree> maxSize = new ValueParam<>(this, Integer.MAX_VALUE, "maxSize", x -> x != null && x > 0);

    /**
     * Minimum quantity a split must gain to proceed further
     */
    public final ValueParam<Double, RTree> minScore = new ValueParam<>(this, 1e-30, "minScore", Double::isFinite);

    /**
     * Method used to distribute observations with missing values on test variable on splitting a node
     */
    public final ValueParam<Splitter, RTree> splitter = new ValueParam<>(this, Splitter.Ignore, "splitter", Objects::nonNull);

    /**
     * Loss function
     */
    public final ValueParam<RTreeLoss, RTree> loss = new ValueParam<>(this, new L2Loss(), "loss", Objects::nonNull);

    /**
     * Variable selection method used to obtain test variables at each node
     */
    public final ValueParam<VarSelector, RTree> varSelector = new ValueParam<>(this, VarSelector.all(), "varSelector", Objects::nonNull);

    /**
     * Map with test method for each variable type
     */
    public final MultiParam<VarType, Search, RTree> test = new MultiParam<>(this, DEFAULT_TEST_MAP, "testMap", Objects::nonNull);

    // tree root node

    private Node root;

    private RTree() {
    }

    @Override
    public RTree newInstance() {
        return new RTree().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "RTree";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .inputs(1, 1_000_000, true, VarType.BINARY, VarType.INT, VarType.DOUBLE, VarType.NOMINAL)
                .targets(1, 1, false, VarType.DOUBLE);
    }

    public Node root() {
        return root;
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        Random random = getRandom();
        capabilities().checkAtLearnPhase(df, weights, targetNames);

        int id = 1;

        HashMap<Integer, Frame> frameMap = new HashMap<>();
        HashMap<Integer, Var> weightsMap = new HashMap<>();

        this.varSelector.get().withVarNames(inputNames());
        root = new Node(null, id++, "root", (row, frame) -> true, 1);

        // prepare data for root
        frameMap.put(root.id, df);
        weightsMap.put(root.id, weights);

        // make queue and initialize it

        Queue<Node> queue = new ConcurrentLinkedQueue<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node last = queue.poll();
            int lastId = last.id;
            Frame lastDf = frameMap.get(lastId);
            Var lastWeights = weightsMap.get(lastId);
            learnNode(last, lastDf, lastWeights, random);

            if (last.leaf) {
                continue;
            }
            // now that we have a best candidate,do the effective split

            List<RowPredicate> predicates = last.bestCandidate.getGroupPredicates();
            List<Mapping> mappings = splitter.get().performSplitMapping(lastDf, lastWeights, predicates, random);

            for (int i = 0; i < predicates.size(); i++) {
                RowPredicate predicate = predicates.get(i);
                Node child = new Node(last, id++, predicate.toString(), predicate, last.depth + 1);
                last.children.add(child);

                frameMap.put(child.id, lastDf.mapRows(mappings.get(i)));
                weightsMap.put(child.id, lastWeights.mapRows(mappings.get(i)).copy());

                queue.add(child);
            }

            frameMap.remove(last.id);
            weightsMap.remove(last.id);
        }
        return true;
    }

    private void learnNode(Node node, Frame df, Var weights, Random random) {

        node.leaf = true;
        node.value = loss.get().scalarMinimizer(df.rvar(firstTargetName()), weights);
        node.weight = Sum.of(weights).value();

        if (node.weight == 0) {
            node.value = node.parent != null ? node.parent.value : Double.NaN;
            node.weight = node.parent != null ? node.parent.value : Double.NaN;
            return;
        }
        if (df.rowCount() <= minCount.get() || node.depth >= (maxDepth.get() == -1 ? Integer.MAX_VALUE : maxDepth.get())) {
            return;
        }

        Stream<String> stream = Arrays.stream(varSelector.get().nextVarNames(random));
        if (runs.get() > 1) {
            stream = stream.parallel();
        }

        List<Candidate> candidates = stream
                .map(testCol -> test.get(df.type(testCol))
                        .computeCandidate(this, df, weights, testCol, firstTargetName(), random)
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();

        Candidate bestCandidate = null;
        for (Candidate candidate : candidates) {
            if (bestCandidate == null || candidate.getScore() >= bestCandidate.getScore()) {
                bestCandidate = candidate;
            }
        }

        if (bestCandidate == null
                || bestCandidate.getGroupPredicates().isEmpty()
                || bestCandidate.getScore() <= minScore.get()) {
            return;
        }
        node.bestCandidate = bestCandidate;
        node.leaf = false;
    }

    @Override
    protected RegressionResult corePredict(Frame df, boolean withResiduals, final double... quantiles) {
        RegressionResult prediction = RegressionResult.build(this, df, withResiduals, quantiles);

        for (int i = 0; i < df.rowCount(); i++) {
            DoublePair result = predict(i, df, root);
            prediction.prediction(firstTargetName()).setDouble(i, result.v1);
        }
        prediction.buildComplete();
        return prediction;
    }

    private DoublePair predict(int row, Frame df, Node node) {

        // if we are at a leaf node we simply return what we found there
        if (node.leaf) {
            return DoublePair.of(node.value, node.weight);
        }

        // if is an interior node, we check to see if there is a child
        // which can handle the instance
        for (Node child : node.children) {
            if (child.predicate.test(row, df)) {
                return predict(row, df, child);
            }
        }

        // so is a missing value for the current test feature

        VarDouble values = VarDouble.empty();
        VarDouble weights = VarDouble.empty();
        for (Node child : node.children) {
            DoublePair prediction = predict(row, df, child);
            values.addDouble(prediction.v1);
            weights.addDouble(prediction.v2);
        }
        return DoublePair.of(WeightedMean.of(values, weights).value(), Mean.of(weights).value());
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n > ").append(fullName());
        sb.append("\n model fitted: ").append(hasLearned).append("\n");

        if (!hasLearned) {
            return sb.toString();
        }

        sb.append("\n");
        sb.append("description:\n");
        sb.append("split, mean (total weight) [* if is leaf]\n\n");

        buildSummary(sb, root, 0);
        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }

    private void buildSummary(StringBuilder sb, Node node, int level) {
        sb.append("|");
        sb.append("   |".repeat(Math.max(0, level)));
        sb.append(node.groupName).append("  ");

        sb.append(floatFlex(node.value));
        sb.append(" (").append(floatFlex(node.weight)).append(") ");
        if (node.leaf) {
            sb.append(" *");
        }
        sb.append("\n");

//        children

        if (!node.leaf) {
            node.children.forEach(child -> buildSummary(sb, child, level + 1));
        }
    }

    /**
     * Implements boosting additive update.
     * <p>
     * The procedure consists of fitting in each terminal node the additive optimizer of the loos function
     * where the target function is y and current fitted value is fx.
     * <p>
     * loss(y, fx + c)
     *
     * @param x            input features
     * @param y            target features
     * @param fx           current fitted function
     * @param lossFunction loss function used to compute additive gradient
     */
    public void boostUpdate(Frame x, Var y, Var fx, Loss lossFunction) {
        root.boostUpdate(x, y, fx, lossFunction, splitter.get(), getRandom());
    }
}
