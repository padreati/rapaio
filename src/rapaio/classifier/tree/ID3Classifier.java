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

import java.util.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ID3Classifier extends AbstractClassifier {

    // parameters

    int minCount = 1;
    int maxSize = Integer.MAX_VALUE;
    CTreeTest.Method method = CTreeTest.Method.INFO_GAIN;

    // model

    Node root;

    @Override
    public String name() {
        return "ID3";
    }

    @Override
    public String fullName() {
        return String.format("ID3 (method: %s, minCount: %d)", method.name(), minCount);
    }

    public ID3Classifier withMethod(CTreeTest.Method method) {
        this.method = method;
        return this;
    }

    public ID3Classifier withMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    public ID3Classifier withMaxSize(int maxSize) {
        this.maxSize = maxSize;
        return this;
    }

    public ID3Classifier newInstance() {
        return new ID3Classifier()
                .withMethod(method)
                .withMinCount(minCount)
                .withMaxSize(maxSize);
    }

    @Override
    public void learn(Frame df, Numeric weights, String targetCol) {
        validate(df, targetCol);
        this.dict = df.col(targetCol).getDictionary();
        this.targetCol = targetCol;
        this.root = new Node();
        this.root.df = df;
        this.root.weights = weights;

        LinkedList<Node> queue = new LinkedList<>();
        queue.add(this.root);

        int remain = maxSize - 1;
        Set<String> usedCols = new HashSet<>();

        while (!queue.isEmpty()) {
            Node node = queue.pollFirst();
            if (remain > 0) {
                node.learn(this, targetCol, usedCols, false);
                if (!node.leaf) {
                    remain -= node.splitMap.size();
                    usedCols.add(node.test.testName());
                    queue.addAll(node.splitMap.values());
                }
            } else {
                node.learn(this, targetCol, usedCols, true);
            }
        }

        // clean pointers
        queue.add(root);
        while (!queue.isEmpty()) {
            Node node = queue.pollFirst();
            node.df = null;
            node.weights = null;
            node.parent = null;
            if (!node.leaf)
                queue.addAll(node.splitMap.values());
        }
    }

    @Override
    public void buildSummary(StringBuilder sb) {
        sb.append("> ").append(fullName()).append("\n");
        summary(root, sb, 0);
    }

    private void summary(Node root, StringBuilder sb, int level) {
        if (root.leaf) {
            for (int i = 0; i < level; i++) {
                sb.append("   ");
            }
            sb.append("-> ");
            sb.append("predict:").append(root.predictedIndex);
            sb.append("\n");
            return;
        }
        for (String label : root.splitMap.keySet()) {
            for (int i = 0; i < level; i++) {
                sb.append("   ");
            }
            sb.append("-> ");
            sb.append(root.test.testName()).append(":").append(label);
            sb.append(" (").append(method.name()).append("=");
            sb.append(String.format("%.6f", root.test.bestValue())).append(")");
            sb.append("\n");
            summary(root.splitMap.get(label), sb, level + 1);
        }
    }

    @Override
    public void predict(final Frame df) {
        pred = new Nominal(df.rowCount(), dict);
        dist = Frames.newMatrix(df.rowCount(), dict);
        df.stream().forEach(spot -> predict(df, spot.row(), root));
    }

    private void predict(Frame df, int row, Node root) {
        if (root.leaf) {
            pred.setIndex(row, root.predictedIndex);
            for (int i = 0; i < dict.length; i++) {
                dist.setValue(row, i, root.density.get(i));
            }
            return;
        }
        String label = df.getLabel(row, root.test.testName());
        Map<String, Node> map = root.splitMap;
        if (!map.containsKey(label)) {
            pred.setIndex(row, root.predictedIndex);
            for (int i = 0; i < dict.length; i++) {
                dist.setValue(row, i, root.density.get(i));
            }
            return;
        }
        predict(df, row, map.get(label));
    }

    private void validate(Frame df, String classColName) {
        for (int i = 0; i < df.colCount(); i++) {
            if (!df.col(i).type().isNominal()) {
                throw new IllegalArgumentException("ID3 can handle only nominal attributes.");
            }
        }
        if (df.stream().complete().count() == 0)
            throw new IllegalArgumentException("ID3 can't handle missing values.");
    }
}

class Node {

    boolean leaf = false;
    HashMap<String, Node> splitMap;
    Frame df;
    Numeric weights;
    DensityVector density;
    int predictedIndex;
    CTreeTest test;
    Node parent;

    void learn(final ID3Classifier id3,
               final String targetCol,
               final Set<String> used,
               final boolean toLeaf) {

        density = new DensityVector(df.col(targetCol), weights);
        density.normalize();
        predictedIndex = density.findBestIndex();

        // leaf on empty set
        if (df.rowCount() == 0) {
            if (parent == null) {
                throw new IllegalArgumentException("Can't train from an empty frame");
            }
            density = new DensityVector(parent.df.col(targetCol), parent.weights);
            density.normalize();
            predictedIndex = density.findBestIndex();

            leaf = true;
            return;
        }

        // leaf on all classes of same getValue
        boolean same = true;
        for (int i = 1; i < df.rowCount(); i++) {
            if (df.getIndex(i - 1, df.colIndex(targetCol)) != df.getIndex(i, df.colIndex(targetCol))) {
                same = false;
                break;
            }
        }
        if (same || toLeaf) {
            leaf = true;
            return;
        }

        // find best split
        test = new CTreeTest(id3.method, id3.minCount);
        for (String testCol : df.colNames()) {
            if (testCol.equals(targetCol) || used.contains(testCol)) {
                continue;
            }
            test.fullNominalTest(df, testCol, targetCol, weights, id3.minCount);
        }

        // if none were selected then there are no columns to select
        if (test.testName() == null) {
            leaf = true;
            return;
        }

        // usual case, a split node

        String[] dict = df.col(test.testName()).getDictionary();
        Mapping[] splitMappings = new Mapping[dict.length];
        Numeric[] splitWeights = new Numeric[dict.length];

        for (int i = 0; i < dict.length; i++) {
            splitMappings[i] = new Mapping();
            splitWeights[i] = new Numeric();
        }

        for (int i = 0; i < df.rowCount(); i++) {
            int index = df.getIndex(i, df.colIndex(test.testName()));
            splitMappings[index].add(df.rowId(i));
            splitWeights[index].addValue(weights.getValue(i));
        }
        Frame[] frames = new Frame[dict.length];
        for (int i = 0; i < dict.length; i++) {
            frames[i] = new MappedFrame(df.source(), splitMappings[i]);
        }

        splitMap = new HashMap<>();
        for (int i = 0; i < dict.length; i++) {
            Node node = new Node();
            node.df = frames[i];
            node.weights = splitWeights[i];
            node.parent = this;
            splitMap.put(dict[i], node);
        }
    }
}
