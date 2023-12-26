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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

import java.io.Serial;

import rapaio.data.Var;
import rapaio.data.VarDouble;

/**
 * Deviance loss function. The formula for deviance loss is -sum_{k=1}^{K} y_k log(p_k(x))
 * where y_k = 1(class==k), 0 otherwise.
 *
 *
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/9/18.
 */
public class KDevianceLoss implements Loss {

    @Serial
    private static final long serialVersionUID = 3608607822562742621L;
    private final int k;

    public KDevianceLoss(int k) {
        this.k = k;
    }

    @Override
    public String name() {
        return "KDeviance(k=" + k + ")";
    }

    @Override
    public double scalarMinimizer(Var y) {
        double up = 0.0;
        double down = 0.0;

        for (int i = 0; i < y.size(); i++) {
            up += y.getDouble(i);
            down += Math.abs(y.getDouble(i)) * (1.0 - Math.abs(y.getDouble(i)));
        }
        if (down == 0 || Double.isNaN(up) || Double.isNaN(down)) {
            return 0;
        }
        return ((k - 1) * up) / (k * down);
    }

    @Override
    public double scalarMinimizer(Var y, Var weight) {
        double up = 0.0;
        double down = 0.0;

        for (int i = 0; i < y.size(); i++) {
            up += weight.getDouble(i) * y.getDouble(i);
            down += weight.getDouble(i) * Math.abs(y.getDouble(i)) * (1.0 - Math.abs(y.getDouble(i)));
        }

        if (down == 0 || Double.isNaN(up) || Double.isNaN(down)) {
            return 0;
        }

        return ((k - 1) * up) / (k * down);
    }

    @Override
    public double additiveScalarMinimizer(Var y, Var fx) {
        double up = 0.0;
        double down = 0.0;

        for (int i = 0; i < y.size(); i++) {
            double delta = y.getDouble(i) - fx.getDouble(i);
            up += delta;
            down += Math.abs(delta) * (1.0 - Math.abs(delta));
        }

        if (down == 0) {
            return 0;
        }

        if (Double.isNaN(up) || Double.isNaN(down)) {
            return 0;
        }

        return ((k - 1) * up) / (k * down);
    }

    @Override
    public VarDouble gradient(Var y, Var y_hat) {
        throw new IllegalStateException("This method is not available for KDevianceLoss");
    }

    @Override
    public VarDouble error(Var y, Var y_hat) {
        throw new IllegalStateException("This method is not available for KDevianceLoss");
    }

    @Override
    public double errorScore(Var y, Var y_hat) {
        throw new IllegalStateException("This method is not available for KDevianceLoss");
    }

    @Override
    public double residualErrorScore(Var residual) {
        throw new IllegalStateException();
    }

    @Override
    public boolean equalOnParams(Loss object) {
        if (!(object instanceof KDevianceLoss)) {
            return false;
        }
        return Integer.valueOf(k).equals(((KDevianceLoss) object).k);
    }
}
