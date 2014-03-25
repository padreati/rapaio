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
import rapaio.data.*;
import rapaio.data.stream.FSpot;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class DecisionStumpClassifier extends AbstractClassifier {

    private int minCount = 1;
    private CTreeTest.Method method = CTreeTest.Method.INFO_GAIN;
    private CTreeTest test = new CTreeTest(method, minCount);

    private String leftLabel;
    private String rightLabel;
    private String defaultLabel;
    private int leftIndex;
    private int rightIndex;
    private int defaultIndex;
    private int leftCount;
    private int rightCount;
    private int defaultCount;

    @Override
    public Classifier newInstance() {
        return new DecisionStumpClassifier().withMethod(method).withMinCount(minCount);
    }

    @Override
    public String name() {
        return "DecisionStump";
    }

    @Override
    public String fullName() {
        return String.format("DecisionStump (method:%s, minCount:%d)",
                method.name(), minCount);
    }

    public DecisionStumpClassifier withMinCount(int minCount) {
        this.minCount = minCount;
        test = new CTreeTest(method, minCount);
        return this;
    }

    public DecisionStumpClassifier withMethod(CTreeTest.Method method) {
        this.method = method;
        test = new CTreeTest(method, minCount);
        return this;
    }

    @Override
    public void learn(Frame df, Numeric weights, String targetColName) {

        dict = df.col(targetColName).getDictionary();

        // find best split test and eventually split point

        for (String colName : df.colNames()) {
            if (targetColName.equals(colName)) continue;
            if (df.col(colName).type().isNumeric()) {
                test.binaryNumericTest(df, colName, targetColName, weights);
            } else {
                for (String testLabel : df.col(colName).getDictionary()) {
                    if (testLabel.equals("?")) continue;
                    test.binaryNominalTest(df, colName, targetColName, weights, testLabel);
                }
            }
        }

        if (test.testName() != null) {

            // we have something, we evaluate both branches

            String testName = test.testName();
            DensityVector left = new DensityVector(dict);
            DensityVector right = new DensityVector(dict);
            DensityVector missing = new DensityVector(dict);

            Vector testVector = df.col(testName);

            // update density vectors in order to predict

            for (int i = 0; i < df.rowCount(); i++) {
                if (testVector.isMissing(i)) {
                    missing.update(df.col(targetColName).getIndex(i), weights.getValue(i));
                    continue;
                }
                boolean onLeft = true;
                if (testVector.type().isNominal() && !test.splitLabel().equals(testVector.getLabel(i))) {
                    onLeft = false;
                }
                if (testVector.type().isNumeric() && test.splitValue() < testVector.getValue(i)) {
                    onLeft = false;
                }
                (onLeft ? left : right).update(df.col(targetColName).getIndex(i), weights.getValue(i));
            }

            // now predict

            leftIndex = left.findBestIndex(false);
            leftLabel = dict[leftIndex];
            rightIndex = right.findBestIndex(false);
            rightLabel = dict[rightIndex];
            defaultIndex = missing.findBestIndex(false);
            defaultLabel = dict[defaultIndex];
        } else {

            // we found nothing, predict with majority

            DensityVector missing = new DensityVector(dict);
            for (int i = 0; i < df.rowCount(); i++) {
                missing.update(df.col(targetColName).getIndex(i), weights.getValue(i));
            }
            defaultIndex = missing.findBestIndex(false);
            defaultLabel = dict[defaultIndex];
        }
    }

    @Override
    public void predict(Frame df) {
        pred = new Nominal(df.rowCount(), dict);
        dist = Frames.newMatrix(df.rowCount(), dict);
        leftCount = 0;
        rightCount = 0;
        defaultCount = 0;

        df.stream().filter(FSpot::isMissing).forEach(spot -> {
            dist.setValue(spot.row(), defaultLabel, 1.0);
            pred.setLabel(spot.row(), defaultLabel);
            defaultCount++;
        });

        if (df.col(test.testName()).type().isNumeric()) {
            df.col(test.testName()).stream().complete().forEach(spot -> {
                if (spot.getValue() <= test.splitValue()) {
                    dist.setValue(spot.row(), leftIndex, 1.0);
                    pred.setLabel(spot.row(), leftLabel);
                    leftCount++;
                } else {
                    dist.setValue(spot.row(), rightIndex, 1.0);
                    pred.setLabel(spot.row(), rightLabel);
                    rightCount++;
                }
            });
        } else {
            df.col(test.testName()).stream().complete().forEach(spot -> {
                if (test.splitLabel().equals(spot.getLabel())) {
                    dist.setValue(spot.row(), leftIndex, 1.0);
                    pred.setLabel(spot.row(), leftLabel);
                    leftCount++;
                } else {
                    dist.setValue(spot.row(), rightIndex, 1.0);
                    pred.setLabel(spot.row(), rightLabel);
                    rightCount++;
                }
            });
        }
    }

    @Override
    public void buildSummary(StringBuilder sb) {

        sb.append("> ").append(fullName()).append("\n");

        sb.append("prediction:\n");
        sb.append(String.format("- left => label: %s, index: %d, count: %d\n",
                leftLabel, leftIndex, leftCount));
        sb.append(String.format("- right => label: %s, index: %d, count: %d\n",
                rightLabel, rightIndex, rightCount));
        sb.append(String.format("- default => label: %s, index: %d, count: %d\n",
                defaultLabel, defaultIndex, defaultCount));
        sb.append("\n");
    }
}
