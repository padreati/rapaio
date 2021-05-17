/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import rapaio.core.stat.Quantiles;
import rapaio.data.Var;
import rapaio.data.VarDouble;

import java.io.Serial;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/17.
 */
public class L1Loss implements Loss {

    @Serial
    private static final long serialVersionUID = 2596472667917498236L;

    @Override
    public String name() {
        return "L1";
    }

    @Override
    public double scalarMinimizer(Var y) {
        return Quantiles.of(y, 0.5).values()[0];
    }

    @Override
    public double scalarMinimizer(Var y, Var weight) {
        return Quantiles.of(y, 0.5).values()[0];
    }

    @Override
    public double additiveScalarMinimizer(Var y, Var fx) {
        return Quantiles.of(y.copy().op().minus(fx), 0.5).values()[0];
    }

    @Override
    public VarDouble gradient(Var y, Var y_hat) {
        return VarDouble.from(y.size(), row -> y.getDouble(row) - y_hat.getDouble(row) < 0 ? -1.0 : 1.0);
    }

    @Override
    public VarDouble error(Var y, Var y_hat) {
        return y.copy().op().minus(y_hat).op().capply(Math::abs);
    }

    @Override
    public double errorScore(Var y, Var y_hat) {
        return error(y, y_hat).op().nansum();
    }

    @Override
    public double residualErrorScore(Var residual) {
        return residual.op().capply(Math::abs).op().nansum();
    }

    @Override
    public boolean equalOnParams(Loss object) {
        return true;
    }
}
