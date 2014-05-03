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

import rapaio.core.ColRange;
import rapaio.core.stat.Mean;
import rapaio.core.stat.StatOnline;
import rapaio.data.*;
import rapaio.data.filters.BaseFilters;
import rapaio.data.mapping.MappedFrame;
import rapaio.data.mapping.Mapping;
import rapaio.ml.AbstractRegressor;
import rapaio.ml.Regressor;
import rapaio.ml.refactor.colselect.ColSelector;
import rapaio.ml.refactor.colselect.DefaultColSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * This works for numeric attributes only with no missing values.
 * With this restriction it works like CART or C45Classifier regression trees.
 * <p>
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class TreeRegressor extends AbstractRegressor {

    double minWeight = 1;
    TreeRegressorNode root;
    Vector fitted;
    ColSelector colSelector;
    String targetColNames;

    public double getMinWeight() {
        return minWeight;
    }

    public TreeRegressor setMinWeight(double minWeight) {
        this.minWeight = minWeight;
        return this;
    }

    public ColSelector getColSelector() {
        return colSelector;
    }

    public TreeRegressor setColSelector(ColSelector colSelector) {
        this.colSelector = colSelector;
        return this;
    }

    @Override
    public Regressor newInstance() {
        return new TreeRegressor().setColSelector(colSelector).setMinWeight(minWeight);
    }

    @Override
    public void learn(Frame df, List<Double> weights, String targetColName) {
        this.targetColNames = targetColName;
        root = new TreeRegressorNode();
        root.learn(this, df, weights, targetColName);
        if (colSelector == null) {
            colSelector = new DefaultColSelector(df, new ColRange(targetColName));
        }
    }

    @Override
    public void predict(Frame df) {
        fitted = new Numeric(new double[df.rowCount()]);
        for (int i = 0; i < df.rowCount(); i++) {
            fitted.setValue(i, root.predict(df, i));
        }
    }

    @Override
    public Numeric getFitValues() {
        return (Numeric) fitted;
    }

    @Override
    public Frame getAllFitValues() {
        return null;
    }
}

class TreeRegressorNode {
    boolean leaf;
    double pred;
    double eval = Double.MAX_VALUE;
    String splitColName;
    double splitValue;
    double totalWeight;
    TreeRegressorNode left;
    TreeRegressorNode right;

    public void learn(TreeRegressor parent, Frame df, List<Double> weights, String targetColNames) {
        totalWeight = 0;
        for (Double weight : weights) {
            totalWeight += weight;
        }

        if (totalWeight < 2 * parent.minWeight) {
            leaf = true;
            pred = new Mean(df.col(targetColNames)).getValue();
            return;
        }

        String[] colNames = parent.colSelector.nextColNames();
        for (String testColName : colNames) {
            if (df.col(testColName).type().isNumeric() && !targetColNames.equals(testColName)) {
                evaluateNumeric(parent, df, weights, targetColNames, testColName);
            }
        }

        // if we have a split
        if (splitColName != null) {
            Mapping leftMapping = new Mapping();
            Mapping rightMapping = new Mapping();
            List<Double> leftWeights = new ArrayList<>();
            List<Double> rightWeights = new ArrayList<>();

            for (int i = 0; i < df.rowCount(); i++) {
                if (df.getValue(i, splitColName) <= splitValue) {
                    leftMapping.add(df.rowId(i));
                    leftWeights.add(weights.get(i));
                } else {
                    rightMapping.add(df.rowId(i));
                    rightWeights.add(weights.get(i));
                }
            }
            left = new TreeRegressorNode();
            right = new TreeRegressorNode();
            left.learn(parent, new MappedFrame(df.source(), leftMapping), leftWeights, targetColNames);
            right.learn(parent, new MappedFrame(df.source(), rightMapping), rightWeights, targetColNames);
            return;
        }

        // else do the default
        leaf = true;
        pred = new Mean(df.col(targetColNames)).getValue();
    }

    private void evaluateNumeric(TreeRegressor parent,
                                 Frame df, List<Double> weights,
                                 String targetColName,
                                 String testColNames) {

        Vector testCol = df.col(testColNames);
        double[] var = new double[df.rowCount()];
        StatOnline so = new StatOnline();
        Vector sort = Vectors.newSeq(df.rowCount());
        sort = BaseFilters.sort(sort, RowComparators.numericComparator(testCol, true));
        double w = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            int pos = sort.rowId(i);
            so.update(testCol.getValue(pos));
            w += weights.get(pos);
            if (i > 0) {
                var[i] = so.getStandardDeviation() * w / totalWeight;
            }
        }
        so.clean();
        w = 0;
        for (int i = df.rowCount() - 1; i >= 0; i--) {
            int pos = sort.rowId(i);
            so.update(testCol.getValue(pos));
            w += weights.get(pos);
            if (i < df.rowCount() - 1) {
                var[i] += so.getStandardDeviation() * w / totalWeight;
            }
        }
        w = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            int pos = sort.rowId(i);
            w += weights.get(pos);

            if (w >= parent.minWeight && totalWeight - w >= parent.minWeight) {
                if (var[i] < eval && i > 0 && testCol.getValue(sort.rowId(i - 1)) != testCol.getValue(sort.rowId(i))) {
                    eval = var[i];
                    splitColName = testColNames;
                    splitValue = testCol.getValue(pos);
                }
            }
        }
    }

    public double predict(Frame df, int row) {
        if (leaf) {
            return pred;
        }
        if (df.getValue(row, splitColName) <= splitValue) {
            return left.predict(df, row);
        } else {
            return right.predict(df, row);
        }
    }
}