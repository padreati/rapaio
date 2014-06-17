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

package rapaio.ml.refactor.tree;

import rapaio.core.stat.Mean;
import rapaio.core.stat.StatOnline;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.data.mapping.MappedVar;
import rapaio.data.mapping.Mapping;
import rapaio.ml.refactor.boost.gbt.BTRegressor;
import rapaio.ml.refactor.boost.gbt.BoostingLossFunction;
import rapaio.ml.regressor.Regressor;

import java.util.List;

import static rapaio.data.filters.BaseFilters.completeCases;
import static rapaio.data.filters.BaseFilters.sort;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class DecisionStumpRegressor implements Regressor, BTRegressor {

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
    public void learn(final Frame df, String targetCols) {

        this.targetColName = targetCols;
        //
        defaultFit = new Mean(df.col(targetCols)).getValue();
        //
        criterion = Double.MAX_VALUE;
        for (String colName : df.colNames()) {
//			if (RandomSource.nextDouble() > 0.3) continue;
            if (colName.equals(targetCols)) continue;
            switch (df.col(colName).type()) {
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

        Var sort = sort(completeCases(df.col(colName)));
        double[] var = new double[sort.rowCount()];
        StatOnline so = new StatOnline();
        for (int i = 0; i < sort.rowCount(); i++) {
            so.update(df.sourceFrame().value(sort.rowId(i), targetColName));
            var[i] = so.getVariance() * so.getN();
        }
        so = new StatOnline();
        for (int i = sort.rowCount() - 1; i >= 0; i--) {
            so.update(df.sourceFrame().value(sort.rowId(i), targetColName));
            var[i] += so.getVariance() * so.getN();
        }
        for (int i = minCount + 1; i < sort.rowCount() - minCount; i++) {
            if (var[i - 1] < criterion && sort.value(i - 1) != sort.value(i)
                    && sort.value(i - 1) != sort.value(sort.rowCount() - 1)) {
                criterion = var[i - 1];
                testColName = colName;
                testValue = sort.value(i - 1);
            }
        }
    }

    @Override
    public void boostFit(Frame x, Var y, Var fx, BoostingLossFunction lossFunction) {

        if (testColName == null) {
            fitValues = Numeric.newFill(x.rowCount(), defaultFit);
            return;
        }

        Mapping dfLeft = new Mapping();
        Mapping dfRight = new Mapping();


        Var test = x.col(testColName);
        for (int i = 0; i < test.rowCount(); i++) {
            if (test.missing(i)) continue;
            if (test.type().isNominal()) continue;
            if (test.type().isNumeric() && (testValue >= test.value(i))) {
                dfLeft.add(test.rowId(i));
            } else {
                dfRight.add(test.rowId(i));
            }
        }

        defaultFit = lossFunction.findMinimum(y, fx);
        leftFit = lossFunction.findMinimum(
                new MappedVar(y.source(), dfLeft),
                new MappedVar(fx.source(), dfLeft));
        rightFit = lossFunction.findMinimum(
                new MappedVar(y.source(), dfRight),
                new MappedVar(fx.source(), dfRight));
    }

    @Override
    public void predict(Frame df) {
        fitValues = Numeric.newFill(df.rowCount());
        if (testColName == null) {
            fitValues = Numeric.newFill(df.rowCount(), defaultFit);
            return;
        }
        Var test = df.col(testColName);

        for (int i = 0; i < df.rowCount(); i++) {
            if (test.missing(i)) {
                fitValues.setValue(i, defaultFit);
                continue;
            }
            if ((test.type().isNominal() && testLabel.equals(test.label(i)))
                    || (test.type().isNumeric() && (testValue >= test.value(i)))) {
                fitValues.setValue(i, leftFit);
            } else {
                fitValues.setValue(i, rightFit);
            }
        }
    }

    @Override
    public Var getFitValues() {
        return fitValues;
    }

    @Override
    public Frame getAllFitValues() {
        throw new RuntimeException("Not implemented");
    }
}
