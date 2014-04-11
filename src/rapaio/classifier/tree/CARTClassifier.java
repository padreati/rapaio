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
import rapaio.classifier.tools.CTreeTest;
import rapaio.classifier.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Nominal;
import rapaio.data.Numeric;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;
import rapaio.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class CARTClassifier extends AbstractClassifier {

    // parameters
    CTreeTest.Method method = CTreeTest.Method.GINI;
    int minCount = 1;
    int maxDepth = Integer.MAX_VALUE;
    // tree
    private CARTNode root;
    // information
    private int rows;

    @Override
    public CARTClassifier newInstance() {
        return new CARTClassifier();
    }

    public CARTClassifier withMethod(CTreeTest.Method method) {
        this.method = method;
        return this;
    }

    public CARTClassifier withMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    public CARTClassifier withMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    @Override
    public String name() {
        return "CART";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder("CART(");
        sb.append("method=").append(method.name()).append(",");
        sb.append("minCount=").append(minCount).append(",");
        sb.append("maxDepth=").append(maxDepth);
        sb.append(")");
        return sb.toString();
    }

    @Override
    public void learn(Frame df, String targetColName) {
        this.dict = df.col(targetColName).getDictionary();
        this.targetCol = targetColName;
        this.rows = df.rowCount();

        this.root = new CARTNode(this, null);
        root.learn(df, maxDepth);
    }

    @Override
    public void predict(Frame df) {
        pred = new Nominal(df.rowCount(), dict);
        dist = Frames.newMatrix(df.rowCount(), dict);

        for (int i = 0; i < df.rowCount(); i++) {
            Pair<Integer, DensityVector> pr = root.computeDist(df, i);
            pred.setIndex(i, pr.getV1());
            for (int j = 0; j < dict.length; j++) {
                dist.setValue(i, j, pr.getV2().get(j));
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

    private void buildSummary(StringBuilder sb, CARTNode node, int level) {
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

            sb.append(node.parent.test.testName());
            boolean left = node.parent.leftNode == node;
            if (node.parent.test.splitLabel() == null) {
                if (left) {
                    sb.append(String.format(" <=%f  ", node.parent.test.splitValue()));
                } else {
                    sb.append(String.format(" >%f  ", node.parent.test.splitValue()));
                }
            } else {
                if (left) {
                    sb.append(" == '").append(node.parent.test.splitLabel()).append("'  ");
                } else {
                    sb.append(" != '").append(node.parent.test.splitLabel()).append("'  ");
                }
            }
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
        }

        // children

        if (!node.leaf) {
            buildSummary(sb, node.leftNode, level + 1);
            buildSummary(sb, node.rightNode, level + 1);
        }
    }
}

class CARTNode {

    final CARTClassifier c;
    final CARTNode parent;
    final CTreeTest test;
    //
    boolean leaf;
    DensityVector density;
    DensityVector count;
    int bestIndex;
    CARTNode leftNode;
    CARTNode rightNode;

    CARTNode(CARTClassifier c, CARTNode parent) {
        this.c = c;
        this.parent = parent;
        this.test = new CTreeTest(c.method, c.minCount);
    }

    public void learn(Frame df, int depth) {
        leaf = true;
        density = new DensityVector(df.col(c.getTargetCol()), df.getWeights());
        count = new DensityVector(df.col(c.getTargetCol()), new Numeric(df.rowCount(), df.rowCount(), 1));
        bestIndex = density.findBestIndex();

        if (df.rowCount() <= c.minCount || count.countValues(x -> x > 0) == 1 || depth < 1) {
            return;
        }

        for (String testCol : df.colNames()) {
            if (testCol.equals(c.getTargetCol())) continue;
            if (df.col(testCol).type().isNumeric()) {
                test.binaryNumericTest(df, testCol, c.getTargetCol());
            } else {
                for (String testLabel : df.col(testCol).getDictionary()) {
                    if ("?".equals(testLabel)) continue;
                    test.binaryNominalTest(df, testCol, c.getTargetCol(), testLabel);
                }
            }
        }

        if (test.testName() == null) {
            return;
        }
        leaf = false;

        List<CTreeTest> surrogateCols = new ArrayList<>();
        if (hasMissing(df, test)) {
            surrogateCols.addAll(buildSurrogates(df, test));
        }

        Mapping leftMapping = new Mapping();
        Mapping rightMapping = new Mapping();

        for (int i = 0; i < df.rowCount(); i++) {
            if (df.isMissing(i, test.testName())) {
                // use surrogates
                splitOnSurrogates(df, i, test, surrogateCols, leftMapping, rightMapping);
                continue;
            }
            if (df.col(test.testName()).type().isNumeric()) {
                if (df.getValue(i, test.testName()) <= test.splitValue()) {
                    leftMapping.add(df.rowId(i));
                } else {
                    rightMapping.add(df.rowId(i));
                }
            } else {
                if (df.col(test.testName()).getLabel(i).equals(test.splitLabel())) {
                    leftMapping.add(df.rowId(i));
                } else {
                    rightMapping.add(df.rowId(i));
                }
            }
        }

        leftNode = new CARTNode(c, this);
        rightNode = new CARTNode(c, this);

        leftNode.learn(new MappedFrame(df.source(), leftMapping), depth - 1);
        rightNode.learn(new MappedFrame(df.source(), rightMapping), depth - 1);
    }

    private void splitOnSurrogates(Frame df, int i, CTreeTest test, List<CTreeTest> surrogateCols, Mapping leftMapping, Mapping rightMapping) {
        for (CTreeTest surrogateTest : surrogateCols) {
            if (df.isMissing(i, surrogateTest.testName()))
                continue;
            if (surrogateTest.splitLabel() == null) {
                if (df.getValue(i, surrogateTest.testName()) <= surrogateTest.splitValue()) {
                    leftMapping.add(df.rowId(i));
                } else {
                    rightMapping.add(df.rowId(i));
                }
            } else {
                if (df.getLabel(i, surrogateTest.testName()).equals(surrogateTest.splitLabel())) {
                    leftMapping.add(df.rowId(i));
                } else {
                    rightMapping.add(df.rowId(i));
                }
            }
            return;
        }
        if (leftMapping.size() > rightMapping.size()) {
            leftMapping.add(df.rowId(i));
        } else {
            rightMapping.add(df.rowId(i));
        }
    }

    private Collection<CTreeTest> buildSurrogates(Frame df, CTreeTest test) {
        return new ArrayList<>();
    }

    private boolean hasMissing(Frame df, CTreeTest test) {
        return df.stream().anyMatch(spot -> spot.isMissing(test.testName()));
    }

    public Pair<Integer, DensityVector> computeDist(Frame df, int i) {
        if (leaf) {
            if (count.countValues(x -> x > 0) == 0) {
                return new Pair<>((Integer) parent.bestIndex, parent.density);
            }
            return new Pair<>(bestIndex, density);
        }
        if (df.isMissing(i, test.testName())) {
            DensityVector leftDensity = leftNode.computeDist(df, i).getV2();
            DensityVector rightDensity = rightNode.computeDist(df, i).getV2();

            double leftP = leftDensity.sum(true) / (leftDensity.sum(true) + rightDensity.sum(true));
            double rightP = rightDensity.sum(true) / (leftDensity.sum(true) + rightDensity.sum(true));

            DensityVector dv = new DensityVector(c.getDict());
            for (int j = 0; j < c.getDict().length; j++) {
                dv.update(j, leftDensity.get(j) * leftP);
                dv.update(j, rightDensity.get(j) * rightP);
            }
            dv.normalize(true);
            return new Pair<>(dv.findBestIndex(), dv);
        }
        if (df.col(test.testName()).type().isNominal()) {
            if (df.getLabel(i, test.testName()).equals(test.splitLabel())) {
                return leftNode.computeDist(df, i);
            } else {
                return rightNode.computeDist(df, i);
            }
        } else {
            if (df.getValue(i, test.testName()) <= test.splitValue()) {
                return leftNode.computeDist(df, i);
            } else {
                return rightNode.computeDist(df, i);
            }
        }
    }
}