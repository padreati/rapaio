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

package rapaio.ml.refactor.boost.gbt;

import rapaio.core.stat.Quantiles;
import rapaio.data.Numeric;
import rapaio.data.Vector;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class L1BoostingLossFunction implements BoostingLossFunction {

    @Override
    public double findMinimum(Vector y, Vector fx) {
        Numeric values = new Numeric();
        for (int i = 0; i < y.rowCount(); i++) {
            values.addValue(y.getValue(i) - fx.getValue(i));
        }
        return new Quantiles(values, new double[]{0.5}).getValues()[0];
    }

    @Override
    public Numeric gradient(Vector y, Vector fx) {
        Numeric gradient = new Numeric();
        for (int i = 0; i < y.rowCount(); i++) {
            gradient.addValue(y.getValue(i) - fx.getValue(i) < 0 ? -1. : 1.);
        }
        return gradient;
    }
}
