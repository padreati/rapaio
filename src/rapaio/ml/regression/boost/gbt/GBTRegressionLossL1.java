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

import rapaio.core.stat.Quantiles;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.solid.SolidVarDouble;
import rapaio.sys.WS;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/17.
 */
@Deprecated
public class GBTRegressionLossL1 implements GBTRegressionLoss {

    private static final long serialVersionUID = 2596472667917498236L;

    @Override
    public String name() {
        return "L1";
    }

    @Override
    public double findMinimum(Var y, Var fx) {
        VarDouble values = SolidVarDouble.empty();
        for (int i = 0; i < y.rowCount(); i++) {
            values.addDouble(y.getDouble(i) - fx.getDouble(i));
        }
        double result = Quantiles.from(values, new double[]{0.5}).values()[0];
        if (Double.isNaN(result)) {
            WS.println();
        }
        return result;
    }

    @Override
    public VarDouble gradient(Var y, Var fx) {
        VarDouble gradient = SolidVarDouble.empty();
        for (int i = 0; i < y.rowCount(); i++) {
            gradient.addDouble(y.getDouble(i) - fx.getDouble(i) < 0 ? -1. : 1.);
        }
        return gradient;
    }
}
