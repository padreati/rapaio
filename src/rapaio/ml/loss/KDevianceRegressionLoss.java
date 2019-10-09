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

import rapaio.data.*;

/**
 * No weighting implemented for now.
 *
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/9/18.
 */
@Deprecated
public class KDevianceRegressionLoss implements RegressionLoss {

    private final int k;

    public KDevianceRegressionLoss(int k) {
        this.k = k;
    }

    @Override
    public String name() {
        return "KDeviance(k=" + k + ")";
    }

    @Override
    public double computeConstantMinimizer(Var y, Var weight) {
        double up = 0.0;
        double down = 0.0;

        for (int i = 0; i < y.rowCount(); i++) {
            up += weight.getDouble(i) * y.getDouble(i);
            down += weight.getDouble(i) * Math.abs(y.getDouble(i)) * (1.0 - Math.abs(y.getDouble(i)));
        }

        if (down == 0 || Double.isNaN(up) || Double.isNaN(down)) {
            return 0;
        }

        return ((k - 1) * up) / (k * down);
    }

    @Override
    public double computeConstantMinimizer(Frame df, String varName, Var weight) {
        int varNameIndex = df.varIndex(varName);
        double up = 0.0;
        double down = 0.0;

        for (int i = 0; i < df.rowCount(); i++) {
            double y = df.getDouble(i, varNameIndex);
            up += y;
            down += Math.abs(y) * (1.0 - Math.abs(y));
        }
        if (down == 0 || Double.isNaN(up) || Double.isNaN(down)) {
            throw new RuntimeException("Numerical problem");
        }
        return ((k - 1) * up) / (k * down);
    }

    @Override
    public VarDouble computeGradient(Var y, Var y_hat) {
        throw new IllegalStateException("This method is not available for KDevianceLoss");
    }

    @Override
    public VarDouble computeError(Var y, Var y_hat) {
        throw new IllegalStateException("This method is not available for KDevianceLoss");
    }

    @Override
    public double computeErrorScore(Var y, Var y_hat) {
        throw new IllegalStateException("This method is not available for KDevianceLoss");
    }

    @Override
    public double computeResidualErrorScore(Var residual) {
        throw new IllegalStateException();
    }
}
