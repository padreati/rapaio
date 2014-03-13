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

package rapaio.ml.boost.gbt;

import rapaio.core.MathBase;
import rapaio.core.stat.Quantiles;
import rapaio.data.Numeric;
import rapaio.data.Vector;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class HuberBoostingLossFunction implements BoostingLossFunction {

    double alpha = 0.25;

    public double getAlpha() {
        return alpha;
    }

    public HuberBoostingLossFunction setAlpha(double alpha) {
        if (alpha < 0 || alpha > 1)
            throw new IllegalArgumentException("alpha quantile must be in interval [0, 1]");
        this.alpha = alpha;
        return this;
    }

    @Override
    public double findMinimum(Vector y, Vector fx) {

        // compute residuals

        Numeric residual = new Numeric();
        for (int i = 0; i < y.rowCount(); i++) {
            residual.addValue(y.value(i) - fx.value(i));
        }

        // compute median of residuals

        double r_bar = new Quantiles(residual, new double[]{0.5}).getValues()[0];

        // compute absolute residuals

        Numeric absResidual = new Numeric();
        for (int i = 0; i < y.rowCount(); i++) {
            absResidual.addValue(MathBase.abs(y.value(i) - fx.value(i)));
        }

        // compute rho as an alpha-quantile of absolute residuals

        double rho = new Quantiles(absResidual, new double[]{alpha}).getValues()[0];

        // compute one-iteration approximation

        double gamma = r_bar;
        double count = y.rowCount();
        for (int i = 0; i < y.rowCount(); i++) {
            gamma += (residual.value(i) - r_bar <= 0 ? -1 : 1)
                    * MathBase.min(rho, MathBase.abs(residual.value(i) - r_bar))
                    / count;
        }
        return gamma;
    }

    @Override
    public Numeric gradient(Vector y, Vector fx) {

        // compute absolute residuals

        Numeric absResidual = new Numeric();
        for (int i = 0; i < y.rowCount(); i++) {
            absResidual.addValue(MathBase.abs(y.value(i) - fx.value(i)));
        }

        // compute rho as an alpha-quantile of absolute residuals

        double rho = new Quantiles(absResidual, new double[]{alpha}).getValues()[0];

        // now compute gradient

        Numeric gradient = new Numeric();
        for (int i = 0; i < y.rowCount(); i++) {
            if (absResidual.value(i) <= rho) {
                gradient.addValue(y.value(i) - fx.value(i));
            } else {
                gradient.addValue(rho * ((y.value(i) - fx.value(i) <= 0) ? -1 : 1));
            }
        }

        // return gradient

        return gradient;
    }
}
