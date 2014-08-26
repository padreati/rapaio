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
import rapaio.core.stat.Mean;
import rapaio.data.*;
import rapaio.ml.regressor.Regressor;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public class L2ConstantRegressor implements Regressor {

    private List<String> targets;
    private List<Double> means;
    private List<Var> fitValues;

    @Override
    public Regressor newInstance() {
        return new L2ConstantRegressor();
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

        means = new ArrayList<>();
        fitValues = new ArrayList<>();
        for (String target : targets) {
            double mean = new Mean(df.var(target)).value();
            means.add(mean);
            fitValues.add(Numeric.newFill(df.var(target).rowCount(), mean));
        }
    }

    @Override
    public void predict(Frame df) {
        fitValues = new ArrayList<>();
        for (int i = 0; i < targets.size(); i++) {
            fitValues.add(Numeric.newFill(df.rowCount(),means.get(i)));
        }
    }

    @Override
    public Numeric getFitValues() {
        return (Numeric) fitValues.get(0);
    }

    @Override
    public Frame getAllFitValues() {
        return SolidFrame.newWrapOf(fitValues.get(0).rowCount(), fitValues, targets);
    }
}
