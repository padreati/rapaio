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

package rapaio.ml.regression.tree;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import rapaio.core.stat.WeightedMean;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.VarSelector;
import rapaio.ml.common.predicate.RowPredicate;
import rapaio.ml.regression.AbstractRegression;
import rapaio.ml.regression.RPrediction;
import rapaio.ml.regression.boost.gbt.BTRegression;
import rapaio.ml.regression.boost.gbt.GBTRegressionLoss;
import rapaio.util.DoublePair;
import rapaio.util.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static rapaio.sys.WS.formatFlex;

/**
 * Implements a regression decision tree.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/24/14.
 */
public class RTree extends AbstractRegression implements BTRegression {

    private static final long serialVersionUID = -2748764643670512376L;

    public static RTree newDecisionStump() {
        return new RTree()
                .withMaxDepth(2)
                .withNominalMethod(RTreeNominalTest.BINARY)
                .withNumericMethod(RTreeNumericTest.BINARY)
                .withSplitter(RTreeSplitter.REMAINS_TO_MAJORITY)
                .withFunction(RTreePurityFunction.WEIGHTED_VAR_GAIN);
    }

    public static RTree newC45() {
        return new RTree()
                .withMaxDepth(-1)
                .withNominalMethod(RTreeNominalTest.FULL)
                .withNumericMethod(RTreeNumericTest.BINARY)
                .withSplitter(RTreeSplitter.REMAINS_TO_RANDOM)
                .withFunction(RTreePurityFunction.WEIGHTED_VAR_GAIN)
                .withMinCount(2);
    }

    public static RTree newCART() {
        return new RTree()
                .withMaxDepth(-1)
                .withNominalMethod(RTreeNominalTest.BINARY)
                .withNumericMethod(RTreeNumericTest.BINARY)
                .withSplitter(RTreeSplitter.REMAINS_TO_ALL_WEIGHTED)
                .withFunction(RTreePurityFunction.WEIGHTED_VAR_GAIN)
                .withMinCount(1);
    }

    private int minCount = 1;
    private int maxDepth = -1;

    private RTreeNominalTest nominalMethod = RTreeNominalTest.BINARY;
    private RTreeNumericTest numericMethod = RTreeNumericTest.BINARY;
    private RTreePurityFunction function = RTreePurityFunction.WEIGHTED_VAR_GAIN;
    private RTreeSplitter splitter = RTreeSplitter.REMAINS_IGNORED;
    private RTreePredictor predictor = RTreePredictor.STANDARD;
    private VarSelector varSelector = VarSelector.all();

    // tree root node

    private RTreeNode root;

    private RTree() {
    }

    @Override
    public RTree newInstance() {
        return (RTree) new RTree()
                .withMinCount(minCount)
                .withMaxDepth(maxDepth)
                .withNominalMethod(nominalMethod)
                .withNumericMethod(numericMethod)
                .withFunction(function)
                .withSplitter(splitter)
                .withPredictor(predictor)
                .withVarSelector(varSelector)
                .withRuns(runs)
                .withPoolSize(poolSize)
                .withRunningHook(runningHook)
                .withSampler(sampler);
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
    public void boostUpdate(Frame x, Var y, Var fx, GBTRegressionLoss lossFunction) {
        root.boostUpdate(x, y, fx, lossFunction, splitter);
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

    public RTree withNumericMethod(RTreeNumericTest numericMethod) {
        this.numericMethod = numericMethod;
        return this;
    }

    public RTree withNominalMethod(RTreeNominalTest nominalMethod) {
        this.nominalMethod = nominalMethod;
        return this;
    }

    public RTree withFunction(RTreePurityFunction function) {
        this.function = function;
        return this;
    }

    public RTree withSplitter(RTreeSplitter splitter) {
        this.splitter = splitter;
        return this;
    }

    public RTree withPredictor(RTreePredictor predictor) {
        this.predictor = predictor;
        return this;
    }

    public int minCount() {
        return minCount;
    }

    public int maxDepth() {
        return maxDepth;
    }

    public RTreeNominalTest nominalMethod() {
        return nominalMethod;
    }

    public RTreeNumericTest numericMethod() {
        return numericMethod;
    }

    public RTreeSplitter splitter() {
        return splitter;
    }

    public RTreePredictor predictor() {
        return predictor;
    }

    public RTreePurityFunction testFunction() {
        return function;
    }

    public VarSelector varSelector() {
        return varSelector;
    }

    public RTreeNode root() {
        return root;
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        if (targetNames().length == 0) {
            throw new IllegalArgumentException("tree classifier must specify a target variable");
        }
        if (targetNames().length > 1) {
            throw new IllegalArgumentException("tree classifier can't predict more than one target variable");
        }

        int id = 1;

        Int2ObjectOpenHashMap<Frame> frameMap = new Int2ObjectOpenHashMap<>();
        Int2ObjectOpenHashMap<Var> weightsMap = new Int2ObjectOpenHashMap<>();

        this.varSelector.withVarNames(inputNames());
        root = new RTreeNode(id++, null, "root", (row, frame) -> true, 1);

        // prepare data for root
        frameMap.put(root.getId(), df);
        weightsMap.put(root.getId(), weights);

        // make queue and initialize it

        Queue<RTreeNode> queue = new ConcurrentLinkedQueue<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            RTreeNode last = queue.poll();
            int lastId = last.getId();
            Frame lastDf = frameMap.get(lastId);
            Var lastWeights = weightsMap.get(lastId);
            learnNode(last, lastDf, lastWeights);

            if(last.isLeaf()) {
                continue;
            }
            // now that we have a best candidate,do the effective split

            List<RowPredicate> predicates = last.getBestCandidate().getGroupPredicates();
            Pair<List<Frame>, List<Var>> frames = splitter.performSplit(lastDf, lastWeights, predicates);

            for (int i = 0; i < predicates.size(); i++) {
                RowPredicate predicate = predicates.get(i);
                RTreeNode child = new RTreeNode(id++, last, predicate.toString(), predicate, last.getDepth() + 1);
                last.getChildren().add(child);
                frameMap.put(child.getId(), frames._1.get(i));
                weightsMap.put(child.getId(), frames._2.get(i));

                queue.add(child);
            }
        }
        return true;
    }

    private void learnNode(RTreeNode node, Frame df, Var weights) {

        node.setValue(WeightedMean.from(df, weights, firstTargetName()).value());
        node.setWeight(weights.stream().mapToDouble().sum());
        if (node.getWeight() == 0) {
            node.setValue(node.getParent() != null ? node.getParent().getValue() : Double.NaN);
        }
        if (df.rowCount() <= minCount() || node.getDepth() >= (maxDepth == -1 ? Integer.MAX_VALUE : maxDepth)) {
            return;
        }

        Stream<String> stream = Arrays.stream(varSelector.nextVarNames());
        if(runs>1) {
            stream = stream.parallel();
        }

        List<RTreeCandidate> candidates = stream.map(testCol -> {
            if (testCol.equals(firstTargetName())) return null;

            if (df.type(testCol).isNumeric()) {
                return numericMethod.computeCandidate(this, df, weights, testCol, firstTargetName(), testFunction()).orElse(null);
            } else {
                return nominalMethod.computeCandidate(this, df, weights, testCol, firstTargetName(), testFunction()).orElse(null);
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());

        RTreeCandidate bestCandidate = null;
        for (RTreeCandidate candidate : candidates) {
            if (bestCandidate == null || candidate.getScore() >= bestCandidate.getScore()) {
                bestCandidate = candidate;
            }
        }

        if (bestCandidate == null || bestCandidate.getGroupNames().isEmpty()) {
            return;
        }
        node.setBestCandidate(bestCandidate);
        node.setLeaf(false);
    }

    @Override
    protected RPrediction corePredict(Frame df, boolean withResiduals) {
        RPrediction pred = RPrediction.build(this, df, withResiduals);

        for (int i = 0; i < df.rowCount(); i++) {
            DoublePair result = predictor.predict(i, df, root);
            pred.fit(firstTargetName()).setValue(i, result._1);
        }
        pred.buildComplete();
        return pred;
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n > ").append(fullName()).append("\n");

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
            node.getChildren().forEach(child -> buildSummary(sb, child, level + 1));
        }
    }
}
