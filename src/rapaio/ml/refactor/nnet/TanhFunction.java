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

package rapaio.ml.refactor.nnet;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class TanhFunction implements TFunction {

    @Override
    public double compute(double x) {
//        return 1.7159 * Math.tanh(0.66666667 * x);
        return Math.tanh(x);
    }

    @Override
    public double differential(double x) {
//        double cosh = Math.cosh(0.66666666667 * x);
//        return 1.14393 / (cosh*cosh);
        return 1 - x * x;
//        return 0.66666667 / 1.7159 * (1.7159 + x) * (1.7159 - x);
    }
}
