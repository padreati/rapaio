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

package rapaio.ml.regression.boost.gbt;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Quantiles;
import rapaio.data.NumericVar;
import rapaio.data.Var;
import rapaio.sys.WS;

import java.io.Serializable;

import static rapaio.sys.WS.formatFlex;

/**
 * Loss function used by gradient boosting algorithm.
 * <p>
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
@Deprecated
public interface GBTLossFunction extends Serializable {

    String name();

    double findMinimum(Var y, Var fx);

    NumericVar gradient(Var y, Var fx);

    // standard implementations

    class L1 implements GBTLossFunction {

        @Override
        public String name() {
            return "L1";
        }

        @Override
        public double findMinimum(Var y, Var fx) {
            NumericVar values = NumericVar.empty();
            for (int i = 0; i < y.rowCount(); i++) {
                values.addValue(y.value(i) - fx.value(i));
            }
            double result = Quantiles.from(values, new double[]{0.5}).values()[0];
            if (Double.isNaN(result)) {
                WS.println();
            }
            return result;
        }

        @Override
        public NumericVar gradient(Var y, Var fx) {
            NumericVar gradient = NumericVar.empty();
            for (int i = 0; i < y.rowCount(); i++) {
                gradient.addValue(y.value(i) - fx.value(i) < 0 ? -1. : 1.);
            }
            return gradient;
        }
    }

    class L2 implements GBTLossFunction {

        @Override
        public String name() {
            return "L2";
        }

        @Override
        public double findMinimum(Var y, Var fx) {
            return Mean.from(gradient(y, fx)).value();
        }

        @Override
        public NumericVar gradient(Var y, Var fx) {
            NumericVar delta = NumericVar.empty();
            for (int i = 0; i < y.rowCount(); i++) {
                delta.addValue(y.value(i) - fx.value(i));
            }
            return delta;
        }
    }

    class Huber implements GBTLossFunction {

        private static final long serialVersionUID = -8624877244857556563L;
        private double alpha = 0.25;

        @Override
        public String name() {
            return "Huber(alpha=" + formatFlex(alpha) + ")";
        }

        public double getAlpha() {
            return alpha;
        }

        public GBTLossFunction withAlpha(double alpha) {
            if (alpha < 0 || alpha > 1)
                throw new IllegalArgumentException("alpha quantile must be in interval [0, 1]");
            this.alpha = alpha;
            return this;
        }

        @Override
        public double findMinimum(Var y, Var fx) {

            // compute residuals

            NumericVar residual = NumericVar.empty();
            for (int i = 0; i < y.rowCount(); i++) {
                residual.addValue(y.value(i) - fx.value(i));
            }

            // compute median of residuals

            double r_bar = Quantiles.from(residual, new double[]{0.5}).values()[0];

            // compute absolute residuals

            NumericVar absResidual = NumericVar.empty();
            for (int i = 0; i < y.rowCount(); i++) {
                absResidual.addValue(Math.abs(y.value(i) - fx.value(i)));
            }

            // compute rho as an alpha-quantile of absolute residuals

            double rho = Quantiles.from(absResidual, new double[]{alpha}).values()[0];

            // compute one-iteration approximation

            double gamma = r_bar;
            double count = y.rowCount();
            for (int i = 0; i < y.rowCount(); i++) {
                gamma += (residual.value(i) - r_bar <= 0 ? -1 : 1)
                        * Math.min(rho, Math.abs(residual.value(i) - r_bar))
                        / count;
            }
            return gamma;
        }

        @Override
        public NumericVar gradient(Var y, Var fx) {

            // compute absolute residuals

            NumericVar absResidual = NumericVar.empty();
            for (int i = 0; i < y.rowCount(); i++) {
                absResidual.addValue(Math.abs(y.value(i) - fx.value(i)));
            }

            // compute rho as an alpha-quantile of absolute residuals

            double rho = Quantiles.from(absResidual, new double[]{alpha}).values()[0];

            // now compute gradient

            NumericVar gradient = NumericVar.empty();
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
}
