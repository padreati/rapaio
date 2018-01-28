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
import rapaio.data.NumVar;
import rapaio.data.Var;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/17.
 */
@Deprecated
public class GBTRegressionLossL2 implements GBTRegressionLoss {

    private static final long serialVersionUID = 774518413127447869L;

    @Override
    public String name() {
        return "L2";
    }

    @Override
    public double findMinimum(Var y, Var fx) {
        return Mean.from(gradient(y, fx)).value();
    }

    @Override
    public NumVar gradient(Var y, Var fx) {
        NumVar delta = NumVar.empty();
        for (int i = 0; i < y.rowCount(); i++) {
            delta.addValue(y.value(i) - fx.value(i));
        }
        return delta;
    }
}