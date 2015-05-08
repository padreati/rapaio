/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.core.distributions.empirical;

/**
 * Tri-cubic kernel function
 *
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KFuncTricube implements KFunc {

    private static final long serialVersionUID = 3788981779668937261L;

    @Override
    public double pdf(double x, double x0, double bandwidth) {
        double value = Math.abs(x - x0) / bandwidth;
        if (value <= 1) {
            double weight = 1 - value * value * value;
            return 70. * weight * weight * weight / 81.;
        }
        return 0;
    }

    @Override
    public double minValue(double x0, double bandwidth) {
        return x0 - bandwidth;
    }

    @Override
    public double getMaxValue(double x0, double bandwidth) {
        return x0 + bandwidth;
    }
}
