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

package rapaio.ml.regressor.linear;

import rapaio.ml.regressor.RegressionFit;
import rapaio.sys.WS;
import rapaio.data.Frame;

import java.util.Arrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 12/1/14.
 */
@Deprecated
public class OLSRegressionFit extends RegressionFit {

    private final OLSRegression regressor;

    public OLSRegressionFit(OLSRegression model, Frame df) {
        super(model, df, true);
        this.regressor = model;
    }

    @Override
    public OLSRegressionFit addTarget(String targetName) {
        return (OLSRegressionFit) super.addTarget(targetName);
    }

    @Override
    public void buildComplete() {
    }

    @Override
    public void printSummary() {

        StringBuilder sb = new StringBuilder();
        sb.append("OLS regressor fit printSummary\n");
        sb.append("======================\n");
        sb.append("\n");

        sb.append("Model type: ").append(regressor.name()).append("\n");
        sb.append("Model instance: ").append(regressor.fullName()).append("\n");
        sb.append("\n");

        sb.append("Predicted frame printSummary:\n");
        sb.append(" - rows: ").append(getFrame().rowCount()).append("\n");
        sb.append(" - cols: ").append(getFrame().varCount()).append("\n");
        sb.append(" - inputs: ").append(Arrays.toString(regressor.inputNames())).append("\n");
        sb.append(" - targets: ").append(Arrays.toString(regressor.targetNames())).append("\n");
        sb.append("\n");

        WS.code(sb.toString());
    }
}
