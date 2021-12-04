/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.eval.metric;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.data.VarType;
import rapaio.datasets.Datasets;
import rapaio.ml.model.linear.LinearRegressionModel;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/4/17.
 */
public class RMSETest {

    private static final double TOL = 1e-15;

    @Test
    void basicTest() {

        RandomSource.setSeed(123);
        Normal normal = Normal.of(0, 10);
        VarDouble x = normal.sample(100).name("x");
        VarDouble y = VarDouble.from(x, val -> val + 1).name("y");
        VarDouble z = VarDouble.from(x, val -> val - 2).name("z");

        RegressionScore rmse1 = RMSE.newMetric().compute(x, y);
        RegressionScore rmse2 = RMSE.newMetric().compute(x, z);

        assertEquals("RMSE: 1\n", rmse1.toSummary());
        assertEquals("RMSE: 2\n", rmse2.toSummary());
    }

    @Test
    void irisTest() {

        Frame df = Datasets.loadIrisDataset().mapVars(VarRange.onlyTypes(VarType.DOUBLE));
        String[] targets = new String[]{"sepal-length", "sepal-width", "petal-length"};

        LinearRegressionModel lm = LinearRegressionModel.newModel().intercept.set(true);
        lm.fit(df, targets);

        var prediction = lm.predict(df, true);
        for (String target : targets) {
            RegressionScore rmse = RMSE.newMetric().compute(df.rvar(target), prediction.prediction(target));
            assertEquals(prediction.rss(target) / df.rowCount(), Math.pow(rmse.value(), 2), TOL);
        }
    }
}
