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
import rapaio.classifier.tools.CTreeTest;
import rapaio.classifier.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Nominal;
import rapaio.data.Numeric;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class C45Classifier extends AbstractClassifier {

    int minCount = 2;
    int maxDepth = Integer.MAX_VALUE;
    CTreeTest.Method method = CTreeTest.Method.GAIN_RATIO;
    C45ClassifierNode root;

    @Override
    public String name() {
        return "C45";
    }

    @Override
    public String fullName() {
        StringBuilder sb = new StringBuilder();
        sb.append("C45(");
        sb.append(String.format("method=%s,", method.name()));
        sb.append(String.format("minCount=%d,", minCount));
        sb.append(String.format("maxDepth=%d", maxDepth));
        sb.append(")");
        return sb.toString();
    }

    public C45Classifier withMethod(CTreeTest.Method method) {
        this.method = method;
        return this;
    }

    public C45Classifier withMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    public C45Classifier withMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }

    @Override
    public Classifier newInstance() {
        return new C45Classifier()
                .withMinCount(minCount)
                .withMethod(method)
                .withMaxDepth(maxDepth);
    }

    @Override
    public void learn(Frame df, String targetColName) {
        dict = df.col(targetColName).getDictionary();
        targetCol = targetColName;
        Set<String> testColNames = new HashSet<>();
        for (String colName : df.colNames()) {
            if (colName.equals(targetColName))
                continue;
            testColNames.add(colName);
        }
        root = new C45ClassifierNode(this);
        root.learn(df, testColNames, targetColName, maxDepth);
    }

    @Override
    public void predict(Frame df) {
        pred = new Nominal(df.rowCount(), dict);
        dist = Frames.newMatrix(df.rowCount(), dict);

        for (int i = 0; i < df.rowCount(); i++) {
            DensityVector d = root.computeDistribution(df, i);
            for (int j = 0; j < dict.length; j++) {
                dist.setValue(i, j, d.get(j));
            }
            pred.setIndex(i, d.findBestIndex());
        }
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append("> ").append(fullName()).append(")\n");
        summary(root, sb, 0);
    }

    private void summary(C45ClassifierNode root, StringBuilder sb, int level) {
        if (root.leaf) {
            for (int i = 0; i < level; i++) {
                sb.append("   ");
            }
            sb.append("-> ");
            sb.append("predict:{");
            for (int i = 1; i < dict.length; i++) {
                sb.append(String.format(" %s=%.2f", dict[i], root.counts.get(i)));
            }
            sb.append("}\n");
            return;
        }
        for (String label : root.children.keySet()) {
            for (int i = 0; i < level; i++) {
                sb.append("   ");
            }
            sb.append("-> ");
            sb.append(root.test.testName());
            if (root.test.splitLabel() != null) {
                sb.append(":").append(label);
            } else {
                if ("left".equals(label)) {
                    sb.append(String.format("<=%.4f", root.test.splitValue()));
                } else {
                    sb.append(String.format(">%.4f", root.test.splitValue()));
                }
            }
            sb.append("\n");
            summary(root.children.get(label), sb, level + 1);
        }
    }

    String[] getDict() {
        return dict;
    }
}

class C45ClassifierNode {

    final C45Classifier parent;
    boolean leaf = false;
    DensityVector density;
    DensityVector counts;
    Map<String, C45ClassifierNode> children;
    final CTreeTest test;

    public C45ClassifierNode(C45Classifier parent) {
        this.parent = parent;
        this.test = new CTreeTest(parent.method, parent.minCount);
    }

    public void learn(Frame df, Set<String> testNames, String targetName, int level) {

        density = new DensityVector(df.col(targetName), df.getWeights());
        density.findBestIndex();
        counts = new DensityVector(df.col(targetName), new Numeric(df.rowCount(), df.rowCount(), 1.0));
        leaf = true;

        if (df.rowCount() <= parent.minCount || level == 1 || testNames.size() <= 1) {
            return;
        }

        // if there is only one getLabel
        for (int i = 1; i < df.rowCount(); i++) {
            if (df.getIndex(0, targetName) != df.getIndex(i, targetName)) {
                leaf = false;
                break;
            }
        }
        if (leaf) {
            return;
        }

        // try to find a good split
        for (String testName : testNames) {
            if (df.col(testName).type().isNominal()) {
                test.fullNominalTest(df, testName, targetName, df.getWeights(), parent.minCount);
            } else {
                test.binaryNumericTest(df, testName, targetName);
            }
        }

        // we have some split

        String testName = test.testName();
        if (testName != null) {

            // for nominal columns

            if (df.col(test.testName()).type().isNominal()) {

                String[] testDict = df.col(testName).getDictionary();
                Map<String, Double> groupWeight = new HashMap<>();
                for (String label : testDict) {
                    groupWeight.put(label, 0.0);
                }

                double totalNonMissing = 0.0;
                for (int i = 0; i < df.rowCount(); i++) {
                    if (!df.isMissing(i, testName)) {
                        double group = groupWeight.get(df.getLabel(i, testName));
                        groupWeight.put(df.getLabel(i, testName), group + df.getWeight(i));
                        totalNonMissing += df.getWeight(i);
                    }
                }

                Map<String, Mapping> mappings = new HashMap<>();
                for (String label : testDict) {
                    mappings.put(label, new Mapping());
                }
                df.stream().forEach(spot -> {
                    if (spot.isMissing(testName)) {
                        for (String label : testDict) {
                            mappings.get(label).add(spot.rowId());
                        }
                    } else {
                        String label = spot.getLabel(testName);
                        mappings.get(label).add(spot.rowId());
                    }
                });
                Map<String, Frame> frames = new HashMap<>();
                for (String label : testDict) {
                    Frame f = new MappedFrame(df.source(), mappings.get(label));
                    // adjust weights for missing values
                    for (int i = 0; i < f.rowCount(); i++) {
                        if (f.isMissing(i, testName)) {
                            double w = f.getWeight(i);
                            w *= groupWeight.get(label);
                            if (totalNonMissing != 0)
                                w /= totalNonMissing;
                            f.setWeight(i, w);
                        }
                    }
                    frames.put(label, f);
                }

                children = new HashMap<>();
                HashSet<String> newTestColNames = new HashSet<>(testNames);
                newTestColNames.remove(testName);
                for (String label : testDict) {
                    C45ClassifierNode node = new C45ClassifierNode(parent);
                    children.put(label, node);
                    node.learn(frames.get(label), testNames, targetName, level - 1);
                }
                return;
            }

            // for numeric columns

            if (df.col(testName).type().isNumeric()) {

                double leftWeight = 0.0;
                double rightWeight = 0.0;
                for (int i = 0; i < df.rowCount(); i++) {
                    if (!df.isMissing(i, testName)) {
                        if (df.getValue(i, testName) <= test.splitValue()) {
                            leftWeight += df.getWeight(i);
                        } else {
                            rightWeight += df.getWeight(i);
                        }
                    }
                }

                Mapping leftMapping = new Mapping();
                Mapping rightMapping = new Mapping();

                for (int i = 0; i < df.rowCount(); i++) {
                    if (df.isMissing(i, testName)) {
                        leftMapping.add(df.rowId(i));
                        rightMapping.add(df.rowId(i));
                    } else {
                        if (df.getValue(i, testName) <= test.splitValue()) {
                            leftMapping.add(df.rowId(i));
                        } else {
                            rightMapping.add(df.rowId(i));
                        }
                    }
                }

                Frame leftFrame = new MappedFrame(df.source(), leftMapping);
                Frame rightFrame = new MappedFrame(df.source(), rightMapping);

                // adjust weights for missing values

                for (int i = 0; i < leftFrame.rowCount(); i++) {
                    if (leftFrame.isMissing(i, testName)) {
                        double w = leftFrame.getWeight(i);
                        w *= leftWeight;
                        if (leftWeight + rightWeight != 0)
                            w /= (leftWeight + rightWeight);
                        leftFrame.setWeight(i, w);
                    }
                }
                for (int i = 0; i < rightFrame.rowCount(); i++) {
                    if (rightFrame.isMissing(i, testName)) {
                        double w = rightFrame.getWeight(i);
                        w *= rightWeight;
                        if (leftWeight + rightWeight != 0)
                            w /= (leftWeight + rightWeight);
                        rightFrame.setWeight(i, w);
                    }
                }

                // build children nodes

                children = new HashMap<>();
                HashSet<String> newTestColNames = new HashSet<>(testNames);
                newTestColNames.remove(testName);

                C45ClassifierNode left = new C45ClassifierNode(parent);
                children.put("left", left);
                C45ClassifierNode right = new C45ClassifierNode(parent);
                children.put("right", right);
                left.learn(leftFrame, newTestColNames, targetName, level - 1);
                right.learn(rightFrame, newTestColNames, targetName, level - 1);

                return;
            }
        }

        // we could not find a split, so we degenerate to default
        leaf = true;
    }

    public DensityVector computeDistribution(Frame df, int row) {
        if (leaf) {
            return density;
        }

        // if missing aggregate all child nodes

        String testName = test.testName();

        if (df.col(testName).isMissing(row)) {
            DensityVector dv = new DensityVector(parent.getDict());
            for (Map.Entry<String, C45ClassifierNode> entry : children.entrySet()) {
                DensityVector d = entry.getValue().computeDistribution(df, row);
                double sum = d.sum(true);
                for (int i = 0; i < parent.getDict().length; i++) {
                    dv.update(i, d.get(i) * sum);
                }
            }
            dv.normalize(true);
            return dv;
        }

        // we have a value, get the distribution

        if (df.col(testName).type().isNominal()) {
            String label = df.getLabel(row, testName);
            for (Map.Entry<String, C45ClassifierNode> entry : children.entrySet()) {
                if (entry.getKey().equals(label)) {
                    return entry.getValue().computeDistribution(df, row);
                }
            }
            throw new RuntimeException("label value not found in classification tree");
        }
        if (df.getValue(row, testName) <= test.splitValue()) {
            return children.get("left").computeDistribution(df, row);
        } else {
            return children.get("right").computeDistribution(df, row);
        }
    }
}
