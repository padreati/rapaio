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

package rapaio.experiment.ml.regression.nnet;

import rapaio.core.RandomSource;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public enum TFunction {

    SIGMOID() {
        public double compute(double input) {
            return 1. / (1. + StrictMath.exp(-input));
        }

        public double differential(double value) {
            if (value == 0) return RandomSource.nextDouble() / 100;
            return value * (1. - value);
        }
    },
    TANH() {
        @Override
        public double compute(double x) {
            return 1.7159 * Math.tanh(0.66666667 * x);
//        return Math.tanh(x);
        }

        @Override
        public double differential(double x) {
//        double cosh = Math.cosh(0.66666666667 * x);
//        return 1.14393 / (cosh*cosh);
//        return 1 - x * x;
            return 0.66666667 / 1.7159 * (1.7159 + x) * (1.7159 - x);
        }
    };


    public abstract double compute(double input);

    public abstract double differential(double value);
}
