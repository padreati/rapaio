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

package rapaio.experiment.ml.eval;

import rapaio.data.*;
import rapaio.math.*;

/**
 * Regression metric used by REvaluation to do regression model selection.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 5/10/19.
 */
public interface RMetric {

    /**
     * @return metric name
     */
    String name();

    /**
     * Compute overall metric score.
     *
     * @param y_true true value
     * @param y_pred predicted value
     * @return computed overall metric score
     */
    double compute(Var y_true, Var y_pred);

    RMetric RMS = new RMS();
}

/**
 * Root Mean Square.
 */
class RMS implements RMetric {

    @Override
    public String name() {
        return "RMS";
    }

    @Override
    public double compute(Var y_true, Var y_pred) {
        double sum = 0;
        double count = 0;
        for (int i = 0; i < y_true.rowCount(); i++) {
            if (y_true.isMissing(i) || y_pred.isMissing(i)) {
                continue;
            }
            sum += MTools.pow(y_true.getDouble(i) - y_pred.getDouble(i), 2);
            count++;
        }
        return Math.sqrt(sum / count);
    }
}