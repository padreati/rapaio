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

package rapaio.core.distributions;

import static java.lang.StrictMath.exp;
import static java.lang.StrictMath.log;

import static rapaio.math.MathTools.*;

import java.io.Serial;

import rapaio.printer.Format;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/19/20.
 */
public record Exponential(double lambda) implements Distribution {

    public static Exponential of(double lambda) {
        return new Exponential(lambda);
    }

    @Serial
    private static final long serialVersionUID = 5064238118800143270L;

    @Override
    public String name() {
        return "Exponential(lambda=" + Format.floatFlex(lambda) + ")";
    }

    @Override
    public boolean discrete() {
        return false;
    }

    @Override
    public double pdf(double x) {
        if (x < 0) {
            return 0;
        }
        return lambda * exp(-lambda * x);
    }

    @Override
    public double cdf(double x) {
        if (x < 0) {
            return 0;
        }
        return 1 - exp(-lambda * x);
    }

    @Override
    public double quantile(double p) {
        if (p < 0) {
            return 0;
        }
        if (p == 1) {
            return Double.POSITIVE_INFINITY;
        }
        return -log(1 - p) / lambda;
    }

    @Override
    public double minValue() {
        return 0;
    }

    @Override
    public double maxValue() {
        return Double.POSITIVE_INFINITY;
    }

    @Override
    public double mean() {
        return 1 / lambda;
    }

    @Override
    public double mode() {
        return 0;
    }

    @Override
    public double median() {
        return LN_2 / lambda;
    }

    @Override
    public double var() {
        return 1 / (lambda * lambda);
    }

    @Override
    public double skewness() {
        return 2;
    }

    @Override
    public double kurtosis() {
        return 6;
    }

    @Override
    public double entropy() {
        return 1 - log(lambda);
    }
}
