/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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
 *
 */

package rapaio.ml.regression.loss;

import rapaio.core.stat.WeightedMean;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.solid.SolidVarDouble;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/6/18.
 */
public class L2RegressionLoss implements RegressionLoss {
    @Override
    public String name() {
        return "L2";
    }

    @Override
    public double findWeightedMinimum(Var y, Var w) {
        return WeightedMean.from(y, w).value();
    }

    @Override
    public double findWeightedMinimum(Frame df, String varName, Var w) {
        return WeightedMean.from(df, w, varName).value();
    }

    @Override
    public VarDouble computeGradient(Var y, Var y_hat) {
        int len = Math.min(y.rowCount(), y_hat.rowCount());
        return SolidVarDouble.from(len, row -> 0.5 * (y.getDouble(row) - y_hat.getDouble(row)));
    }
}
