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

package rapaio.ml.supervised.linear;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Uniform;
import rapaio.data.BoundFrame;
import rapaio.data.Frame;
import rapaio.data.VarDouble;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/21/20.
 */
public class WeightedLinearRegressionResultResultModelTest {

    private static final double TOL = 1e-20;

    @Test
    void testCoefficients() {
        RandomSource.setSeed(123);
        Normal normal = Normal.of(0, 10);
        VarDouble x = VarDouble.seq(0, 100, 1).name("x");
        VarDouble intercept = VarDouble.fill(x.size(), 1.0).name("I");
        VarDouble y1 = VarDouble.from(x, v -> v * 2 + normal.sampleNext()).name("y1");
        VarDouble y2 = VarDouble.from(x, v -> v * 3 - 10 + normal.sampleNext()).name("y2");

        Frame df = BoundFrame.byVars(x, y1, y2);

        var result1 = WeightedLinearRegression.newModel()
                .intercept.set(true).fit(df, "y1")
                .predict(df, true);

        var result2 = LinearRegressionModel.newModel()
                .intercept.set(true).fit(df, "y1")
                .predict(df, true);

        assertTrue(result1.beta_hat.deepEquals(result2.beta_hat));

        VarDouble w = VarDouble.wrapArray(df.rowCount(), Uniform.of(0, 1).sample(df.rowCount()).elements());

        var result3 = WeightedLinearRegression.newModel()
                .intercept.set(true)
                .newInstance()
                .fit(df, w, "y1")
                .predict(df, true);

        assertFalse(result1.beta_hat.deepEquals(result3.beta_hat));

        assertEquals("WeightedLinearRegression", WeightedLinearRegression.newModel().name());
    }
}
