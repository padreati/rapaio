/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.preprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarRange;
import rapaio.ml.model.simple.L2Regression;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/28/19.
 */
public class ImputeRegressionTest {

    private static final double TOL = 1e-20;

    private Normal normal;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
        normal = Normal.std();
    }

    @Test
    void testBasic() {

        VarDouble x = VarDouble.from(100, row -> row % 7 == 0 ? Double.NaN : normal.sampleNext()).name("x");
        VarDouble y = VarDouble.from(100, row -> row % 9 == 0 ? Double.NaN : normal.sampleNext()).name("y");

        double xm = Mean.of(x).value();
        double ym = Mean.of(y).value();

        var model = L2Regression.newModel();

        ImputeRegression xfilter = ImputeRegression.of(model, VarRange.of("x"), "x").newInstance();
        ImputeRegression yfilter = ImputeRegression.of(model, VarRange.of("y"), "y");

        Frame df = SolidFrame.byVars(x, y);
        Frame copy = df.copy().fapply(xfilter, yfilter);

        for (int i = 0; i < 100; i++) {
            if (x.isMissing(i)) {
                assertEquals(xm, copy.getDouble(i, "x"), TOL);
            } else {
                assertEquals(x.getDouble(i), copy.getDouble(i, "x"), TOL);
            }
            if (y.isMissing(i)) {
                assertEquals(ym, copy.getDouble(i, "y"), TOL);
            } else {
                assertEquals(y.getDouble(i), copy.getDouble(i, "y"), TOL);
            }
        }
    }
}
