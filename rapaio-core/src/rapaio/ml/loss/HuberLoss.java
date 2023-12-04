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

import static rapaio.printer.Format.floatFlex;

import java.io.Serial;

import rapaio.core.stat.Quantiles;
import rapaio.data.Var;
import rapaio.data.VarDouble;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/17.
 */
public class HuberLoss implements Loss {

    @Serial
    private static final long serialVersionUID = -8624877244857556563L;
    private double alpha = 0.25;

    public double getAlpha() {
        return alpha;
    }

    public HuberLoss withAlpha(double alpha) {
        if (alpha < 0 || alpha > 1) {
            throw new IllegalArgumentException("alpha quantile must be in interval [0, 1]");
        }
        this.alpha = alpha;
        return this;
    }

    @Override
    public String name() {
        return "Huber(alpha=" + floatFlex(alpha) + ")";
    }

    @Override
    public double scalarMinimizer(Var y) {

        double r_bar = Quantiles.of(y, 0.5).values()[0];
        var abs = y.copy();
        abs.dv().apply(Math::abs);

        // compute rho as an alpha-quantile of absolute residuals

        double rho = Quantiles.of(abs, alpha).values()[0];

        // compute one-iteration approximation

        double gamma = r_bar;
        double count = y.size();
        for (int i = 0; i < y.size(); i++) {
            gamma += (y.getDouble(i) - r_bar <= 0 ? -1 : 1)
                    * Math.min(rho, Math.abs(y.getDouble(i) - r_bar))
                    / count;
        }
        return gamma;
    }

    @Override
    public double scalarMinimizer(Var y, Var weight) {
        return scalarMinimizer(y);
    }

    @Override
    public double additiveScalarMinimizer(Var y, Var fx) {

        // compute residuals

        VarDouble residual = VarDouble.empty();
        for (int i = 0; i < y.size(); i++) {
            residual.addDouble(y.getDouble(i) - fx.getDouble(i));
        }

        // compute median of residuals

        double r_bar = Quantiles.of(residual, 0.5).values()[0];

        // compute absolute residuals

        VarDouble absResidual = VarDouble.empty();
        for (int i = 0; i < y.size(); i++) {
            absResidual.addDouble(Math.abs(y.getDouble(i) - fx.getDouble(i)));
        }

        // compute rho as an alpha-quantile of absolute residuals

        double rho = Quantiles.of(absResidual, alpha).values()[0];

        // compute one-iteration approximation

        double gamma = r_bar;
        double count = y.size();
        for (int i = 0; i < y.size(); i++) {
            gamma += (residual.getDouble(i) - r_bar <= 0 ? -1 : 1)
                    * Math.min(rho, Math.abs(residual.getDouble(i) - r_bar))
                    / count;
        }
        return gamma;
    }

    @Override
    public VarDouble gradient(Var y, Var y_hat) {

        // compute absolute residuals

        VarDouble absResidual = VarDouble.empty();
        for (int i = 0; i < y.size(); i++) {
            absResidual.addDouble(Math.abs(y.getDouble(i) - y_hat.getDouble(i)));
        }

        // compute rho as an alpha-quantile of absolute residuals

        double rho = Quantiles.of(absResidual, alpha).values()[0];

        // now compute gradient

        VarDouble gradient = VarDouble.empty();
        for (int i = 0; i < y.size(); i++) {
            if (absResidual.getDouble(i) <= rho) {
                gradient.addDouble(y.getDouble(i) - y_hat.getDouble(i));
            } else {
                gradient.addDouble(rho * ((y.getDouble(i) - y_hat.getDouble(i) <= 0) ? -1 : 1));
            }
        }
        return gradient;
    }

    @Override
    public VarDouble error(Var y, Var y_hat) {
        return VarDouble.from(y.size(), row -> {
            double a = Math.abs(y.getDouble(row) - y_hat.getDouble(row));
            return (a < alpha) ? (a * a / 2.0) : (alpha * a - alpha * alpha / 2);
        });
    }

    @Override
    public double errorScore(Var y, Var y_hat) {
        return error(y, y_hat).dv().nansum();
    }

    @Override
    public double residualErrorScore(Var residual) {
        double score = 0.0;
        for (int i = 0; i < residual.size(); i++) {
            double a = Math.abs(residual.getDouble(i));
            score += (a < alpha) ? (a * a / 2.0) : (alpha * a - alpha * alpha / 2);
        }
        return score;
    }

    @Override
    public boolean equalOnParams(Loss object) {
        if (!(object instanceof HuberLoss)) {
            return false;
        }
        return Double.valueOf(alpha).equals(((HuberLoss) object).alpha);
    }
}