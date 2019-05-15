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

package rapaio.experiment.ml.regression.tree.nbrtree;

import rapaio.data.*;
import rapaio.math.linear.*;
import rapaio.ml.regression.linear.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/23/19.
 */
public interface NBRFunction {

    NBRFunction newInstance();

    double eval(Frame df, int row);

    VarDouble findBestFit(Frame df, Var weights, Var y, String testVarName, String targetVarName);

    static NBRFunction LINEAR = new LinearFunction();
}

class LinearFunction implements NBRFunction {

    private String testVarName;
    private RV beta;

    public LinearFunction newInstance() {
        return new LinearFunction();
    }

    @Override
    public double eval(Frame df, int row) {
        return 0;
    }

    @Override
    public VarDouble findBestFit(Frame df, Var weights, Var y, String testVarName, String targetVarName) {
        LinearRegression lm = LinearRegression.newLm();

        Frame map = df.mapVars(testVarName, targetVarName);
        lm.fit(map, weights, targetVarName);
        LinearRPrediction pred = lm.predict(map, false);
        this.beta = pred.getBetaHat().mapCol(0);
        this.testVarName = testVarName;
        return pred.firstFit();
    }
}