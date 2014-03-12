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

import rapaio.core.stat.Mean;
import rapaio.core.stat.StatOnline;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Vector;
import rapaio.data.mapping.MappedVector;
import rapaio.data.mapping.Mapping;
import rapaio.ml.AbstractRegressor;
import rapaio.ml.boost.gbt.BTRegressor;
import rapaio.ml.boost.gbt.BoostingLossFunction;

import java.util.List;

import static rapaio.data.filters.BaseFilters.completeCases;
import static rapaio.data.filters.BaseFilters.sort;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class DecisionStumpRegressor extends AbstractRegressor implements BTRegressor {

    // parameters
    int minCount = 2;
    // prediction results
    Numeric fitValues;
    String targetColName;
    //
    double criterion;
    String testColName;
    double testValue;
    String testLabel;
    //
    double leftFit;
    double rightFit;
    double defaultFit;

    @Override
    public BTRegressor newInstance() {
        return new DecisionStumpRegressor().setMinCount(minCount);
    }

    public int getMinCount() {
        return minCount;
    }

    public DecisionStumpRegressor setMinCount(int minCount) {
        this.minCount = minCount;
        return this;
    }

    @Override
    public void learn(Frame df, List<Double> weights, String targetColName) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void learn(final Frame df, String targetColName) {

        this.targetColName = targetColName;
        //
        defaultFit = new Mean(df.getCol(targetColName)).getValue();
        //
        criterion = Double.MAX_VALUE;
        for (String colName : df.getColNames()) {
//			if (RandomSource.nextDouble() > 0.3) continue;
            if (colName.equals(targetColName)) continue;
            switch (df.getCol(colName).getType()) {
                case INDEX:
                case NUMERIC:
                    evalNumeric(df, colName);
                    break;
                case NOMINAL:
                    // TODO implement for nominal
                    break;
            }
        }
        predict(df);
    }

    private void evalNumeric(Frame df, String colName) {

        Vector sort = sort(completeCases(df.getCol(colName)));
        double[] var = new double[sort.getRowCount()];
        StatOnline so = new StatOnline();
        for (int i = 0; i < sort.getRowCount(); i++) {
            so.update(df.getSourceFrame().getValue(sort.getRowId(i), targetColName));
            var[i] = so.getVariance() * so.getN();
        }
        so = new StatOnline();
        for (int i = sort.getRowCount() - 1; i >= 0; i--) {
            so.update(df.getSourceFrame().getValue(sort.getRowId(i), targetColName));
            var[i] += so.getVariance() * so.getN();
        }
        for (int i = minCount + 1; i < sort.getRowCount() - minCount; i++) {
            if (var[i - 1] < criterion && sort.getValue(i - 1) != sort.getValue(i)
                    && sort.getValue(i - 1) != sort.getValue(sort.getRowCount() - 1)) {
                criterion = var[i - 1];
                testColName = colName;
                testValue = sort.getValue(i - 1);
            }
        }
    }

    @Override
    public void boostFit(Frame x, Vector y, Vector fx, BoostingLossFunction lossFunction) {

        if (testColName == null) {
            fitValues = new Numeric(x.rowCount(), x.rowCount(), defaultFit);
            return;
        }

        Mapping dfLeft = new Mapping();
        Mapping dfRight = new Mapping();


        Vector test = x.getCol(testColName);
        for (int i = 0; i < test.getRowCount(); i++) {
            if (test.isMissing(i)) continue;
            if (test.getType().isNominal()) continue;
            if (test.getType().isNumeric() && (testValue >= test.getValue(i))) {
                dfLeft.add(test.getRowId(i));
            } else {
                dfRight.add(test.getRowId(i));
            }
        }

        defaultFit = lossFunction.findMinimum(y, fx);
        leftFit = lossFunction.findMinimum(
                new MappedVector(y.getSourceVector(), dfLeft),
                new MappedVector(fx.getSourceVector(), dfLeft));
        rightFit = lossFunction.findMinimum(
                new MappedVector(y.getSourceVector(), dfRight),
                new MappedVector(fx.getSourceVector(), dfRight));
    }

    @Override
    public void predict(Frame df) {
        fitValues = new Numeric(df.rowCount());
        if (testColName == null) {
            fitValues = new Numeric(df.rowCount(), df.rowCount(), defaultFit);
            return;
        }
        Vector test = df.getCol(testColName);

        for (int i = 0; i < df.rowCount(); i++) {
            if (test.isMissing(i)) {
                fitValues.setValue(i, defaultFit);
                continue;
            }
            if ((test.getType().isNominal() && testLabel.equals(test.getLabel(i)))
                    || (test.getType().isNumeric() && (testValue >= test.getValue(i)))) {
                fitValues.setValue(i, leftFit);
            } else {
                fitValues.setValue(i, rightFit);
            }
        }
    }

    @Override
    public Vector getFitValues() {
        return fitValues;
    }

    @Override
    public Frame getAllFitValues() {
        throw new RuntimeException("Not implemented");
    }
}
