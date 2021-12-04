/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.model.tree;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.MultiParam;
import rapaio.ml.common.ParametricEquals;
import rapaio.ml.common.ValueParam;
import rapaio.ml.common.VarSelector;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;
import rapaio.ml.model.RunInfo;
import rapaio.ml.model.tree.ctree.Candidate;
import rapaio.ml.model.tree.ctree.Node;
import rapaio.ml.model.tree.ctree.Pruning;
import rapaio.ml.model.tree.ctree.Purity;
import rapaio.ml.model.tree.ctree.Search;
import rapaio.ml.model.tree.ctree.Splitter;
import rapaio.printer.Format;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.Pair;

/**
 * Tree classifier.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CTree extends ClassifierModel<CTree, ClassifierResult, RunInfo<CTree>> implements Printable, ParametricEquals<CTree> {

    public static CTree newID3() {
        return new CTree()
                .maxDepth.set(-1)
                .minCount.set(1)
                .varSelector.set(VarSelector.all())
                .splitter.set(Splitter.Ignore)
                .searchMap.add(VarType.NOMINAL, Search.NominalFull)
                .searchMap.add(VarType.DOUBLE, Search.Ignore)
                .purity.set(Purity.InfoGain)
                .pruning.set(Pruning.None);
    }

    public static CTree newC45() {
        return new CTree()
                .maxDepth.set(-1)
                .minCount.set(1)
                .varSelector.set(VarSelector.all())
                .splitter.set(Splitter.Weighted)
                .searchMap.add(VarType.NOMINAL, Search.NominalFull)
                .searchMap.add(VarType.DOUBLE, Search.NumericBinary)
                .purity.set(Purity.GainRatio);
    }

    public static CTree newDecisionStump() {
        return new CTree()
                .maxDepth.set(1)
                .minCount.set(1)
                .varSelector.set(VarSelector.all())
                .splitter.set(Splitter.Weighted)
                .purity.set(Purity.GainRatio)
                .searchMap.add(VarType.NOMINAL, Search.NominalBinary)
                .searchMap.add(VarType.DOUBLE, Search.NumericBinary);
    }

    public static CTree newCART() {
        return new CTree()
                .maxDepth.set(-1)
                .minCount.set(1)
                .varSelector.set(VarSelector.all())
                .splitter.set(Splitter.Random)
                .searchMap.add(VarType.NOMINAL, Search.NominalBinary)
                .searchMap.add(VarType.DOUBLE, Search.NumericBinary)
                .searchMap.add(VarType.INT, Search.NumericBinary)
                .purity.set(Purity.GiniGain);
    }

    @Serial
    private static final long serialVersionUID = 1203926824359387358L;

    /**
     * Minimum number of instances from a leaf node.
     */
    public final ValueParam<Integer, CTree> minCount = new ValueParam<>(this, 1, "minCount", x -> x != null && x >= 1);

    /**
     * Tree maximum depth.
     */
    public final ValueParam<Integer, CTree> maxDepth = new ValueParam<>(this, -1, "maxDepth");

    /**
     * Minimum gain required to proceed with split instances into child nodes.
     */
    public final ValueParam<Double, CTree> minGain = new ValueParam<>(this, -1000.0, "minGain");

    /**
     * Method used to select variable candidates tested during the search for a split condition.
     */
    public final ValueParam<VarSelector, CTree> varSelector = new ValueParam<>(this, VarSelector.all(), "varSelector");

    /**
     * Test criteria used to select best splits.
     */
    public final MultiParam<VarType, Search, CTree> searchMap = new MultiParam<>(this,
            Map.of(
                    VarType.BINARY, Search.Binary,
                    VarType.INT, Search.NumericBinary,
                    VarType.DOUBLE, Search.NumericBinary,
                    VarType.NOMINAL, Search.NominalBinary),
            "testMap", Objects::nonNull);

    /**
     * If is true the score of the purity function for some given test is multiplied with the
     * ratio between the weight sum of non-missing test values and the weight sum of all test values.
     * <p>
     * Thus, for example if the test score for some split on some test variable have a given value,
     * if the test variable has missing values the intuition is that it's score is not reliable.
     * Thus enabling this configuration the computed score will be diminished linearly with the proportion
     * of non-missing values.
     * <p>
     * Since this is a non-standard penalty in literature, the default value is @{code false}, but
     * it is worth a try to be turned true.
     */
    public final ValueParam<Boolean, CTree> missingPenalty = new ValueParam<>(this, false, "missingPenalty");

    /**
     * Purity function used to compute a performance measure for a split.
     */
    public final ValueParam<Purity, CTree> purity = new ValueParam<>(this, Purity.InfoGain, "purity");

    /**
     * Method used to split instances into child nodes.
     */
    public final ValueParam<Splitter, CTree> splitter = new ValueParam<>(this, Splitter.Ignore, "splitter");

    /**
     * Pruning method.
     */
    public final ValueParam<Pruning, CTree> pruning = new ValueParam<>(this, Pruning.None, "prunning");

    /**
     * Data frame used by pruning method as a test set.
     */
    public final ValueParam<Frame, CTree> pruningDf = new ValueParam<>(this, null, "pruningDf", x -> true);

    private Node root;

    public Node getRoot() {
        return root;
    }

    @Override
    public CTree newInstance() {
        return new CTree().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "CTree";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities(1, 1_000_000, Arrays.asList(VarType.NOMINAL, VarType.INT, VarType.DOUBLE, VarType.BINARY), true,
                1, 1, List.of(VarType.NOMINAL), false);
    }

    record Triple(Node node, Frame df, Var weight) {
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        additionalValidation(df);
        this.varSelector.get().withVarNames(inputNames());

        int rows = df.rowCount();

        // create the root node
        AtomicInteger idGenerator = new AtomicInteger();
        idGenerator.set(0);
        root = new Node(null, idGenerator.get(), 0, "root", RowPredicate.all());

        Queue<Triple> queue = new ConcurrentLinkedQueue<>();
        queue.add(new Triple(root, df, weights));

        while (!queue.isEmpty()) {
            var t = queue.poll();

            Node node = t.node;
            Frame nodeDf = t.df;
            Var weightsDf = t.weight;

            learnNode(node, nodeDf, weightsDf);

            if (node.leaf) {
                continue;
            }
            Candidate bestCandidate = node.bestCandidate;
            String testName = bestCandidate.testName();

            // now that we have a best candidate, do the effective split
            Pair<List<Frame>, List<Var>> frames = splitter.get().performSplit(nodeDf, weightsDf,
                    bestCandidate.groupPredicates());

            for (RowPredicate predicate : bestCandidate.groupPredicates()) {
                var child = new Node(node,
                        idGenerator.incrementAndGet(), node.depth + 1, predicate.toString(), predicate);
                node.children.add(child);
            }
            for (int i = 0; i < node.children.size(); i++) {
                var child = node.children.get(i);
                queue.add(new Triple(child, frames.v1.get(i), frames.v2.get(i)));
            }
        }

        pruning.get().prune(this, (pruningDf.get() == null) ? df : pruningDf.get(), false);
        return true;
    }

    private void learnNode(Node node, Frame df, Var weights) {
        node.density = DensityVector.fromLevelWeights(false, df.rvar(firstTargetName()), weights);
        node.counter = DensityVector.fromLevelCounts(false, df.rvar(firstTargetName()));
        node.bestLabel = node.density.findBestLabel();

        if (df.rowCount() == 0) {
            node.bestLabel = node.parent.bestLabel;
            return;
        }
        if (node.counter.countValues(x -> x > 0) == 1 || (maxDepth.get() > 0 && node.depth > maxDepth.get())
                || df.rowCount() <= minCount.get()) {
            return;
        }

        String[] nextVarNames = varSelector.get().nextVarNames();
        List<Candidate> candidateList = new ArrayList<>();
        Queue<String> exhaustList = new ConcurrentLinkedQueue<>();

        int m = varSelector.get().mCount();
        for (String testCol : nextVarNames) {
            if (m <= 0) {
                continue;
            }
            if (testCol.equals(firstTargetName())) {
                continue;
            }
            if (!(searchMap.get().containsKey(df.type(testCol)))) {
                throw new IllegalArgumentException("No test for given variable type: " + testCol + " [" + df.type(testCol).name() + "]");
            }
            var test = searchMap.get().get(df.type(testCol));
            var candidate = test.computeCandidate(
                    this, df, weights, testCol, firstTargetName(), purity.get());
            if (candidate != null) {
                candidateList.add(candidate);
                m--;
            } else {
                exhaustList.add(testCol);
            }
        }
        candidateList.sort((o1, o2) -> -(Double.compare(o1.score(), o2.score())));
        if (candidateList.isEmpty() || candidateList.get(0).groupPredicates().isEmpty()) {
            return;
        }
        // leave as leaf if the gain is not bigger than minimum gain
        if (candidateList.get(0).score() <= minGain.get()) {
            return;
        }

        node.leaf = false;
        node.bestCandidate = candidateList.get(0);
        varSelector.get().removeVarNames(exhaustList);
    }

    public void prune(Frame df) {
        prune(df, false);
    }

    public void prune(Frame df, boolean all) {
        pruning.get().prune(this, df, all);
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDensities) {
        ClassifierResult prediction = ClassifierResult.build(this, df, withClasses, withDensities);
        for (int i = 0; i < df.rowCount(); i++) {
            Pair<String, DensityVector<String>> res = predictPoint(this, root, i, df);
            String label = res.v1;
            var dv = res.v2;
            if (withClasses) {
                prediction.firstClasses().setLabel(i, label);
            }
            if (withDensities) {
                for (int j = 1; j < firstTargetLevels().size(); j++) {
                    prediction.firstDensity().setDouble(i, j, dv.get(firstTargetLevel(j)));
                }
            }
        }
        return prediction;
    }

    protected Pair<String, DensityVector<String>> predictPoint(CTree tree, Node node, int row, Frame df) {
        if (node.leaf) {
            return Pair.from(node.bestLabel, node.density.copy().normalize());
        }

        for (Node child : node.children) {
            if (child.predicate.test(row, df)) {
                return this.predictPoint(tree, child, row, df);
            }
        }

        List<String> dict = tree.firstTargetLevels();
        var dv = DensityVector.emptyByLabels(false, dict);
        double w = 0.0;
        for (Node child : node.children) {
            var d = this.predictPoint(tree, child, row, df).v2;
            double wc = child.density.sum();
            dv.plus(d, wc);
            w += wc;
        }
        for (int i = 1; i < dict.size(); i++) {
            dv.set(tree.firstTargetLevel(i), dv.get(tree.firstTargetLevel(i)) / w);
        }
        return Pair.from(dv.findBestLabel(), dv);
    }

    private void additionalValidation(Frame df) {
        df.varStream().forEach(var -> {
            if (searchMap.get().containsKey(var.type())) {
                return;
            }
            throw new IllegalArgumentException("can't predict ctree with no " +
                    "tests for given variable: " + var.name() +
                    " [" + var.type().name() + "]");
        });
    }

    public int countNodes(boolean onlyLeaves) {
        int count = 0;
        LinkedList<Node> nodes = new LinkedList<>();
        nodes.addLast(root);
        while (!nodes.isEmpty()) {
            var node = nodes.pollFirst();
            count += onlyLeaves ? (node.leaf ? 1 : 0) : 1;
            node.children.forEach(nodes::addLast);
        }
        return count;
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("CTree model\n");
        sb.append("================\n\n");

        sb.append("Description:\n");
        sb.append(fullName().replaceAll(";", ";\n")).append("\n\n");

        sb.append("Capabilities:\n");
        sb.append(capabilities().toString()).append("\n");

        sb.append("Learned model:\n");

        if (!hasLearned()) {
            sb.append("Learning phase not called\n\n");
            return sb.toString();
        }

        sb.append("\n");

        int nodeCount = 0;
        int leaveCount = 0;
        LinkedList<Node> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Node node = queue.pollFirst();
            nodeCount++;
            if (node.leaf) {
                leaveCount++;
            }
            node.children.forEach(queue::addLast);
        }

        sb.append("total number of nodes: ").append(nodeCount).append("\n");
        sb.append("total number of leaves: ").append(leaveCount).append("\n");
        sb.append("description:\n");
        sb.append("split, n/err, classes (densities) [* if is leaf / purity if not]\n\n");

        buildSummary(sb, root, 0);

        return sb.toString();

    }

    private void buildSummary(StringBuilder sb, Node node, int level) {

        sb.append(level == 0 ? "|- " : "|");
        for (int i = 0; i < level; i++) {
            sb.append((i == level - 1) ? "   |- " : "   |");
        }
        sb.append(node.id).append(". ").append(node.groupName).append("    ");
        sb.append(Format.floatFlexShort(node.counter.sum())).append("/");
        sb.append(Format.floatFlexShort(node.counter.sumExcept(node.bestLabel))).append(" ");
        sb.append(node.bestLabel).append(" (");
        var d = node.density.copy().normalize();
        for (int i = 1; i < firstTargetLevels().size(); i++) {
            sb.append(Format.floatFlexShort(d.get(firstTargetLevel(i)))).append(" ");
        }
        sb.append(") ");
        if (node.leaf) {
            sb.append("*");
        } else {
            sb.append("[").append(Format.floatFlex(node.bestCandidate.score())).append("]");
        }
        sb.append("\n");

        node.children.forEach(child -> buildSummary(sb, child, level + 1));
    }

    @Override
    public String toContent(POption<?>... options) {
        return toSummary();
    }

    @Override
    public String toFullContent(POption<?>... options) {
        return toSummary();
    }

    @Override
    public String toString() {
        return fullName();
    }

    @Override
    public boolean equalOnParams(CTree tree) {
        return fullName().equals(tree.fullName());
    }
}

