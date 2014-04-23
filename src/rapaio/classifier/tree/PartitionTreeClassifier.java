/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.classifier.tree;

import rapaio.classifier.AbstractClassifier;
import rapaio.classifier.Classifier;
import rapaio.classifier.tools.DensityVector;
import rapaio.cluster.util.Pair;
import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Nominal;

import static rapaio.classifier.tree.CTree.*;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class PartitionTreeClassifier extends AbstractClassifier {

    // parameters
    private int minCount = 1;
    private int maxDepth = Integer.MAX_VALUE;

    private NominalMethod nominalMethod = NominalMethods.FULL;
    private NumericMethod numericMethod = NumericMethods.BINARY;
    private Function function = Functions.INFO_GAIN;
    private Splitter splitter = Splitters.IGNORE_MISSING;
    private Predictor predictor = Predictors.STANDARD;

    // tree root node
    private CPartitionTreeNode root;
    private int rows;

    @Override
    public Classifier newInstance() {
        return new PartitionTreeClassifier()
                .withMinCount(minCount)
                .withMaxDepth(maxDepth)
                .withNominalMethod(nominalMethod)
                .withNumericMethod(numericMethod)
                .withFunction(function)
                .withSplitter(splitter)
                .withPredictor(predictor);
    }

    public int getMinCount() {
        return minCount;
    }

    public PartitionTreeClassifier withMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public PartitionTreeClassifier withMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    public NominalMethod getNominalMethod() {
        return nominalMethod;
    }

    public PartitionTreeClassifier withNominalMethod(NominalMethod methodNominal) {
        this.nominalMethod = methodNominal;
        return this;
    }

    public NumericMethod getNumericMethod() {
        return numericMethod;
    }

    public PartitionTreeClassifier withNumericMethod(NumericMethod numericMethod) {
        this.numericMethod = numericMethod;
        return this;
    }

    public Function getFunction() {
        return function;
    }

    public PartitionTreeClassifier withFunction(Function function) {
        this.function = function;
        return this;
    }

    public Splitter getSplitter() {
        return splitter;
    }

    public PartitionTreeClassifier withSplitter(Splitter splitter) {
        this.splitter = splitter;
        return this;
    }

    public Predictor getPredictor() {
        return predictor;
    }

    public PartitionTreeClassifier withPredictor(Predictor predictor) {
        this.predictor = predictor;
        return this;
    }

    @Override
    public String name() {
        return "PartitionTreeClassifier";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("PartitionTreeClassifier (");
        sb.append("minCount=").append(minCount).append(",");
        sb.append("maxDepth=").append(maxDepth).append(",");
        sb.append("numericMethod=").append(numericMethod.getMethodName()).append(",");
        sb.append("nominalMethod=").append(nominalMethod.getMethodName()).append(",");
        sb.append("function=").append(function.getFunctionName()).append(",");
        sb.append("splitter=").append(splitter.getSplitterName()).append(",");
        sb.append("predictor=").append(predictor.getPredictorName());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void learn(Frame df, String targetColName) {

        targetCol = targetColName;
        dict = df.col(targetCol).getDictionary();
        rows = df.rowCount();

        root = new CPartitionTreeNode(this, null);
        root.learn(df, maxDepth);
    }

    @Override
    public void predict(Frame df) {

        pred = new Nominal(df.rowCount(), dict);
        dist = Frames.newMatrix(df.rowCount(), dict);

        for (int i = 0; i < df.rowCount(); i++) {
            Pair<Integer, DensityVector> result = predictor.predict(df, i, root);
            pred.setIndex(i, result.first);
            for (int j = 0; j < dict.length; j++) {
                dist.setValue(i, j, result.second.get(j));
            }
        }
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append("> ").append(fullName()).append("\n");

        sb.append(String.format("n=%d\n", rows));

        sb.append("\n");
        sb.append("description:\n");
        sb.append("split, n, err, pred (dist) [* - if is leaf]\n\n");

        buildSummary(sb, root, 0);
    }

    private void buildSummary(StringBuilder sb, CPartitionTreeNode node, int level) {
        sb.append("|");
        for (int i = 0; i < level; i++) {
            sb.append("   |");
        }
        if (node.parent == null) {
            sb.append("root").append(" ");
            sb.append(node.density.sum(true)).append("/");
            sb.append(node.density.sumExcept(node.bestIndex, true)).append(" ");
            sb.append(dict[node.bestIndex]).append(" (");
            DensityVector d = node.density.solidCopy();
            d.normalize(false);
            for (int i = 1; i < dict.length; i++) {
                sb.append(String.format("%.6f", d.get(i))).append(" ");
            }
            sb.append(") ");
            if (node.leaf) sb.append("*");
            sb.append("\n");

        } else {

//            sb.append(node.parent.test.testName());
//            boolean left = node.parent.leftNode == node;
//            if (node.parent.test.splitLabel() == null) {
//                if (left) {
//                    sb.append(String.format(" <=%f  ", node.parent.test.splitValue()));
//                } else {
//                    sb.append(String.format(" >%f  ", node.parent.test.splitValue()));
//                }
//            } else {
//                if (left) {
//                    sb.append(" == '").append(node.parent.test.splitLabel()).append("'  ");
//                } else {
//                    sb.append(" != '").append(node.parent.test.splitLabel()).append("'  ");
//                }
//            }
//            sb.append(node.density.sum(true)).append("/");
//            sb.append(node.density.sumExcept(node.bestIndex, true)).append(" ");
//            sb.append(dict[node.bestIndex]).append(" (");
//            DensityVector d = node.density.solidCopy();
//            d.normalize(false);
//            for (int i = 1; i < dict.length; i++) {
//                sb.append(String.format("%.6f", d.get(i))).append(" ");
//            }
//            sb.append(") ");
//            if (node.leaf) sb.append("*");
//            sb.append("\n");
        }

        // children

        if (!node.leaf) {
//            buildSummary(sb, node.leftNode, level + 1);
//            buildSummary(sb, node.rightNode, level + 1);
        }
    }
}
