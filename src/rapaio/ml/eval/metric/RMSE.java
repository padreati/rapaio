/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.ml.eval.metric;

import rapaio.data.Var;
import rapaio.ml.regression.RegressionResult;

import java.io.Serial;

/**
 * Regression evaluation tool which enables one to compute
 * Root Mean Squared Error, which is the sum of the squared
 * values of the residuals for all pairs of actual and
 * prediction variables.
 * <p>
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class RMSE extends AbstractRegressionMetric {

    @Serial
    private static final long serialVersionUID = -4538721622397952296L;

    public static RMSE newMetric() {
        return new RMSE();
    }

    // artifacts

    private Var actual;
    private Var prediction;

    private RMSE() {
        super("RMSE");
    }

    @Override
    public RegressionScore compute(Var actual, RegressionResult result) {
        return compute(actual, result.firstPrediction());
    }

    @Override
    public RegressionScore compute(Var actual, Var prediction) {
        this.actual = actual;
        this.prediction = prediction;

        double totalSum = 0;
        double totalCount = 0;

        double sum = 0.0;
        double count = 0.0;
        for (int j = 0; j < actual.size(); j++) {
            count++;
            sum += Math.pow(actual.getDouble(j) - prediction.getDouble(j), 2);
        }
        return new RegressionScore(this, Math.sqrt(sum / count));
    }
}
