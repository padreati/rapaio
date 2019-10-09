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

package rapaio.experiment.ml.regression.boost.gbt;

import rapaio.data.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/17.
 */
public class GBTLossDeviance implements GBTRegressionLoss {

    private static final long serialVersionUID = -2622054975826334290L;
    private final double K;

    public GBTLossDeviance(int K) {
        this.K = K;
    }

    @Override
    public String name() {
        return "ClassifierLossFunction";
    }

    @Override
    public double findMinimum(Var y, Var fx) {
        double up = 0.0;
        double down = 0.0;

        for (int i = 0; i < y.rowCount(); i++) {
            up += y.getDouble(i);
            down += Math.abs(y.getDouble(i)) * (1.0 - Math.abs(y.getDouble(i)));
        }

        if (down == 0) {
            return 0;
        }

        if (Double.isNaN(up) || Double.isNaN(down)) {
            return 0;
        }

        return ((K - 1) * up) / (K * down);
    }

    @Override
    public VarDouble gradient(Var y, Var fx) {
        return null;
    }
}
