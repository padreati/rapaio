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

package rapaio.ml.tree;

import rapaio.data.Frame;
import rapaio.data.Frames;
import rapaio.data.Nominal;
import rapaio.data.Vector;
import rapaio.data.collect.FIterator;
import rapaio.ml.AbstractClassifier;
import rapaio.ml.Classifier;
import rapaio.ml.tools.DensityVector;
import rapaio.ml.tools.TreeClassificationTest;

import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class DecisionStumpClassifier extends AbstractClassifier<DecisionStumpClassifier> {

    private int minCount = 1;
    private TreeClassificationTest.Method method = TreeClassificationTest.Method.INFO_GAIN;

    private String[] dict;

    private TreeClassificationTest test = new TreeClassificationTest(method, minCount);

    private String leftLabel;
    private String rightLabel;
    private String defaultLabel;
    private int leftIndex;
    private int rightIndex;
    private int defaultIndex;

    private Nominal pred;
    private Frame distr;

    @Override
    public Classifier newInstance() {
        return new DecisionStumpClassifier().withMethod(method).withMinCount(minCount);
    }

    public DecisionStumpClassifier withMinCount(int minCount) {
        this.minCount = minCount;
        test = new TreeClassificationTest(method, minCount);
        return this;
    }

    public DecisionStumpClassifier withMethod(TreeClassificationTest.Method method) {
        this.method = method;
        test = new TreeClassificationTest(method, minCount);
        return this;
    }

    @Override
    public void learn(Frame df, List<Double> weights, String targetColName) {

        dict = df.getCol(targetColName).getDictionary();

        // find best split test and eventually split point

        for (String colName : df.getColNames()) {
            if (targetColName.equals(colName)) continue;
            if (df.getCol(colName).type().isNumeric()) {
                test.binaryNumericTest(df, colName, targetColName, weights);
            } else {
                for (String testLabel : df.getCol(colName).getDictionary()) {
                    test.binaryNominalTest(df, colName, targetColName, weights, testLabel);
                }
            }
        }

        if (test.getTestName() != null) {

            // we have something, we evaluate both branches

            String testName = test.getTestName();
            DensityVector left = new DensityVector(dict);
            DensityVector right = new DensityVector(dict);
            DensityVector missing = new DensityVector(dict);

            Vector testVector = df.getCol(testName);

            // update density vectors in order to predict

            for (int i = 0; i < df.rowCount(); i++) {
                if (testVector.isMissing(i)) {
                    missing.update(df.getCol(targetColName).getIndex(i), weights.get(i));
                    continue;
                }
                boolean onLeft = true;
                if (testVector.type().isNominal() && !test.getBinarySplitLabel().equals(testVector.getLabel(i))) {
                    onLeft = false;
                }
                if (testVector.type().isNumeric() && test.getBinarySplitValue() < testVector.getValue(i)) {
                    onLeft = false;
                }
                if (onLeft) {
                    left.update(df.getCol(targetColName).getIndex(i), weights.get(i));
                } else {
                    right.update(df.getCol(targetColName).getIndex(i), weights.get(i));
                }
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
                missing.update(df.getCol(targetColName).getIndex(i), weights.get(i));
            }
            defaultIndex = missing.findBestIndex(false);
            defaultLabel = dict[defaultIndex];
        }
    }

    @Override
    public void predict(Frame df) {
        pred = new Nominal(df.rowCount(), dict);
        distr = Frames.newMatrixFrame(df.rowCount(), dict);

        FIterator it = df.getIterator();
        while (it.next()) {
            if (test.getTestName() == null || it.isMissing(test.getTestName())) {
                distr.setValue(it.getRow(), defaultIndex, 1.0);
                pred.setLabel(it.getRow(), defaultLabel);
                continue;
            }
            if (df.getCol(test.getTestName()).type().isNumeric()) {
                if (it.getValue(test.getTestName()) <= test.getBinarySplitValue()) {
                    distr.setValue(it.getRow(), leftIndex, 1.0);
                    pred.setLabel(it.getRow(), leftLabel);
                } else {
                    distr.setValue(it.getRow(), rightIndex, 1.0);
                    pred.setLabel(it.getRow(), rightLabel);
                }
            } else {
                if (test.getBinarySplitLabel().equals(it.getLabel(test.getTestName()))) {
                    distr.setValue(it.getRow(), leftIndex, 1.0);
                    pred.setLabel(it.getRow(), leftLabel);
                } else {
                    distr.setValue(it.getRow(), rightIndex, 1.0);
                    pred.setLabel(it.getRow(), rightLabel);
                }
            }
        }
    }

    @Override
    public Nominal getPrediction() {
        return pred;
    }

    @Override
    public Frame getDistribution() {
        return distr;
    }

    @Override
    public void summary() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
