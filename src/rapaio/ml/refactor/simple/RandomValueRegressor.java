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

package rapaio.ml.refactor.simple;

import rapaio.core.VarRange;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.ml.regressor.Regressor;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class RandomValueRegressor implements Regressor {
    List<String> targets;
    double startValue;
    double stopValue;
    List<Var> fitValues;

    @Override
    public Regressor newInstance() {
        return new L2ConstantRegressor();
    }

    public double getStartValue() {
        return startValue;
    }

    public RandomValueRegressor setStartValue(double startValue) {
        this.startValue = startValue;
        return this;
    }

    public double getStopValue() {
        return stopValue;
    }

    public RandomValueRegressor setStopValue(double stopValue) {
        this.stopValue = stopValue;
        return this;
    }

    private double getRandomValue() {
        return RandomSource.nextDouble() * (stopValue - startValue) + startValue;
    }

    @Override
    public void learn(Frame df, List<Double> weights, String targetColName) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void learn(Frame df, String targetCols) {
        VarRange varRange = new VarRange(targetCols);
        List<Integer> colIndexes = varRange.parseColumnIndexes(df);

        targets = new ArrayList<>();
        for (Integer colIndexe : colIndexes) {
            targets.add(df.varNames()[colIndexe]);
        }

        fitValues = new ArrayList<>();
        for (String target : targets) {
            double customValue = getRandomValue();
            fitValues.add(Numeric.newFill(df.var(target).rowCount(), customValue));
        }
    }

    @Override
    public void predict(Frame df) {
        fitValues = new ArrayList<>();
        for (int i = 0; i < targets.size(); i++) {
            fitValues.add(Numeric.newFill(df.rowCount()));
            for (int j = 0; j < df.rowCount(); j++) {
                fitValues.get(i).setValue(j, getRandomValue());
            }
        }
    }

    @Override
    public Numeric getFitValues() {
        return (Numeric) fitValues.get(0);
    }

    @Override
    public Frame getAllFitValues() {
        return new SolidFrame(fitValues.get(0).rowCount(), fitValues, targets);
    }
}
