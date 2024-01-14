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

package rapaio.ml.model.svm;

import java.io.IOException;
import java.util.logging.Level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.tensor.Tensor;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.sys.WS;

public class SvmRegressionTest {

    private static final double TOL = 1e-16;

    private Frame df;
    private DMatrix xs;
    private Tensor<Double> ys;

    @BeforeEach
    void beforeEach() {
        WS.initLog(Level.SEVERE);
        df = Datasets.loadISLAdvertising();

        xs = DMatrix.copy(df.mapVars("TV,Radio"));
        ys = df.rvar("Sales").dt();
    }

    @Test
    void testRegression() throws IOException {

        SvmRegression m = new SvmRegression()
                .type.set(SvmRegression.Penalty.C)
                .c.set(10.0)
                .probability.set(false)
                .kernel.set(new RBFKernel(0.1))
                .seed.set(42L);

        m.fit(df.mapVars("TV,Radio,Sales"), "Sales");
        var pred = m.predict(df.mapVars("TV,Radio,Sales"));
        DVector pr = pred.firstPrediction().dv();
        pred.printSummary();

//        assertTrue(DVector.wrap(pred.classes()).deepEquals(pr, TOL));


        // epsilon regression with probabilities

        m = new SvmRegression()
                .type.set(SvmRegression.Penalty.C)
                .c.set(10.0)
                .probability.set(true)
                .kernel.set(new RBFKernel(0.001))
                .seed.set(42L);

        pr = m.fit(df.mapVars("TV,Radio,Sales"), "Sales").predict(df).firstPrediction().dv();

//        assertTrue(DVector.wrap(pred.classes()).deepEquals(pr, TOL));

        // nu regression without probabilities

        m = new SvmRegression()
                .type.set(SvmRegression.Penalty.NU)
                .nu.set(0.1)
                .probability.set(false)
                .kernel.set(new RBFKernel(0.001))
                .seed.set(42L);

        pr = m.fit(df.mapVars("TV,Radio,Sales"), "Sales").predict(df).firstPrediction().dv();

//        assertTrue(DVector.wrap(pred.classes()).deepEquals(pr, TOL));


        // nu regression with probabilities

        m = new SvmRegression()
                .type.set(SvmRegression.Penalty.NU)
                .nu.set(0.1)
                .probability.set(true)
                .kernel.set(new RBFKernel(0.001))
                .seed.set(42L);

        pr = m.fit(df.mapVars("TV,Radio,Sales"), "Sales").predict(df).firstPrediction().dv();

//        assertTrue(DVector.wrap(pred.classes()).deepEquals(pr, TOL));
    }
}
