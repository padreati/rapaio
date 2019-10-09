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

package rapaio.experiment.ml.regression.boost.gbt;

import rapaio.core.stat.Quantiles;
import rapaio.data.Var;
import rapaio.data.VarDouble;

import static rapaio.printer.format.Format.floatFlex;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/17.
 */
@Deprecated
public class GBTRegressionLossHuber implements GBTRegressionLoss {

    private static final long serialVersionUID = -8624877244857556563L;
    private double alpha = 0.25;

    @Override
    public String name() {
        return "Huber(alpha=" + floatFlex(alpha) + ")";
    }

    public double getAlpha() {
        return alpha;
    }

    public GBTRegressionLoss withAlpha(double alpha) {
        if (alpha < 0 || alpha > 1)
            throw new IllegalArgumentException("alpha quantile must be in interval [0, 1]");
        this.alpha = alpha;
        return this;
    }

    @Override
    public double findMinimum(Var y, Var fx) {

        // compute residuals

        VarDouble residual = VarDouble.empty();
        for (int i = 0; i < y.rowCount(); i++) {
            residual.addDouble(y.getDouble(i) - fx.getDouble(i));
        }

        // compute median of residuals

        double r_bar = Quantiles.of(residual, new double[]{0.5}).values()[0];

        // compute absolute residuals

        VarDouble absResidual = VarDouble.empty();
        for (int i = 0; i < y.rowCount(); i++) {
            absResidual.addDouble(Math.abs(y.getDouble(i) - fx.getDouble(i)));
        }

        // compute rho as an alpha-quantile of absolute residuals

        double rho = Quantiles.of(absResidual, new double[]{alpha}).values()[0];

        // compute one-iteration approximation

        double gamma = r_bar;
        double count = y.rowCount();
        for (int i = 0; i < y.rowCount(); i++) {
            gamma += (residual.getDouble(i) - r_bar <= 0 ? -1 : 1)
                    * Math.min(rho, Math.abs(residual.getDouble(i) - r_bar))
                    / count;
        }
        return gamma;
    }

    @Override
    public VarDouble gradient(Var y, Var fx) {

        // compute absolute residuals

        VarDouble absResidual = VarDouble.empty();
        for (int i = 0; i < y.rowCount(); i++) {
            absResidual.addDouble(Math.abs(y.getDouble(i) - fx.getDouble(i)));
        }

        // compute rho as an alpha-quantile of absolute residuals

        double rho = Quantiles.of(absResidual, new double[]{alpha}).values()[0];

        // now compute gradient

        VarDouble gradient = VarDouble.empty();
        for (int i = 0; i < y.rowCount(); i++) {
            if (absResidual.getDouble(i) <= rho) {
                gradient.addDouble(y.getDouble(i) - fx.getDouble(i));
            } else {
                gradient.addDouble(rho * ((y.getDouble(i) - fx.getDouble(i) <= 0) ? -1 : 1));
            }
        }

        // return gradient

        return gradient;
    }
}