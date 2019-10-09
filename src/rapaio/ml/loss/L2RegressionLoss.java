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
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.ml.loss;

import rapaio.core.stat.WeightedMean;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/6/18.
 */
public class L2RegressionLoss implements RegressionLoss {

    @Override
    public String name() {
        return "L2";
    }

    @Override
    public double computeConstantMinimizer(Var y, Var weight) {
        return WeightedMean.of(y, weight).value();
    }

    @Override
    public double computeConstantMinimizer(Frame df, String varName, Var weight) {
        return WeightedMean.of(df, weight, varName).value();
    }

    @Override
    public VarDouble computeGradient(Var y, Var y_hat) {
        if (y.rowCount() != y_hat.rowCount()) {
            throw new IllegalArgumentException("Row count of variables does not match.");
        }
        return VarDouble.from(y.rowCount(), row -> 0.5 * (y.getDouble(row) - y_hat.getDouble(row)));
    }

    @Override
    public VarDouble computeError(Var y, Var y_hat) {
        int len = Math.min(y.rowCount(), y_hat.rowCount());
        return VarDouble.from(len, row -> Math.pow(y.getDouble(row) - y_hat.getDouble(row), 2));
    }

    @Override
    public double computeErrorScore(Var y, Var y_hat) {

        double len = Math.min(y.rowCount(), y_hat.rowCount());
        double sum = 0.0;
        for (int i = 0; i < len; i++) {
            sum += Math.pow(y.getDouble(i) - y_hat.getDouble(i), 2);
        }
        return Math.sqrt(sum / len);
    }

    @Override
    public double computeResidualErrorScore(Var residual) {
        double len = residual.rowCount();
        double sum = 0.0;
        for (int i = 0; i < len; i++) {
            sum += Math.pow(residual.getDouble(i), 2);
        }
        return Math.sqrt(sum / len);
    }
}
