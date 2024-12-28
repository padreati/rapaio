/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.ml.model.svm;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.logging.Level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.sys.WS;

public class SvmRegressionTest {

    private Frame df;
    private DArray<Double> xs;
    private DArray<Double> ys;

    @BeforeEach
    void beforeEach() {
        WS.initLog(Level.SEVERE);
        df = Datasets.loadISLAdvertising();

        xs = df.mapVars("TV,Radio").darray();
        ys = df.rvar("Sales").darray_();
    }

    @Test
    void testRegression() {

        SvmRegression m = new SvmRegression()
                .type.set(SvmRegression.Penalty.C)
                .c.set(10.0)
                .probability.set(false)
                .kernel.set(new RBFKernel(0.1))
                .seed.set(42L);

        m.fit(df.mapVars("TV,Radio,Sales"), "Sales");
        var pred = m.predict(df.mapVars("TV,Radio,Sales"), true);
//        pred.printSummary();
        assertTrue(pred.firstRSquare() > 0.95);


        // epsilon regression with probabilities

        m = new SvmRegression()
                .type.set(SvmRegression.Penalty.C)
                .c.set(10.0)
                .probability.set(true)
                .kernel.set(new RBFKernel(0.001))
                .seed.set(42L);

        pred = m.fit(df.mapVars("TV,Radio,Sales"), "Sales").predict(df, true);
//        pred.printSummary();
        assertTrue(pred.firstRSquare() > 0.95);

        // nu regression without probabilities

        m = new SvmRegression()
                .type.set(SvmRegression.Penalty.NU)
                .nu.set(0.5)
                .probability.set(false)
                .kernel.set(new RBFKernel(0.001))
                .seed.set(42L);

        pred = m.fit(df.mapVars("TV,Radio,Sales"), "Sales").predict(df, true);
//        pred.printSummary();
        assertTrue(pred.firstRSquare() > 0.95);


        // nu regression with probabilities

        m = new SvmRegression()
                .type.set(SvmRegression.Penalty.NU)
                .nu.set(0.5)
                .probability.set(true)
                .kernel.set(new RBFKernel(0.001))
                .seed.set(42L);

        pred = m.fit(df.mapVars("TV,Radio,Sales"), "Sales").predict(df, true);
//        pred.printSummary();
        assertTrue(pred.firstRSquare() > 0.95);
    }
}
