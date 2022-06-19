/*
 *
 *  * Apache License
 *  * Version 2.0, January 2004
 *  * http://www.apache.org/licenses/
 *  *
 *  * Copyright 2013 - 2022 Aurelian Tutuianu
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package rapaio.ml.model.svm;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.logging.Level;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.experiment.ml.svm.libsvm.svm_model;
import rapaio.experiment.ml.svm.libsvm.svm_predict;
import rapaio.experiment.ml.svm.libsvm.svm_train;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.sys.WS;

public class SvmRegressionTest {

    private static final double TOL = 1e-16;

    private Frame df;
    private DMatrix xs;
    private DVector ys;

    @BeforeEach
    void beforeEach() {
        WS.initLog(Level.SEVERE);
        df = Datasets.loadISLAdvertising();

        xs = DMatrix.copy(df.mapVars("TV,Radio"));
        ys = df.rvar("Sales").dv();
    }

    @Test
    void testRegression() throws IOException {

        svm_train t = new svm_train();

        // epsilon regression without probabilities

        RandomSource.setSeed(42);
        svm_model model = t.run(xs, ys, new String[] {"-s", "3",
                "-t", "2",
                "-g", "0.001",
                "-c", "10",
                "-b", "0"});
        svm_predict.Prediction pred = svm_predict.predict(model, xs, 0);

        SvmRegression svm = new SvmRegression()
                .type.set(SvmRegression.Penalty.C)
                .c.set(10.0)
                .probability.set(false)
                .kernel.set(new RBFKernel(0.001));

        RandomSource.setSeed(42);
        DVector pr = svm.fit(df.mapVars("TV,Radio,Sales"), "Sales").predict(df).firstPrediction().dv();

        assertTrue(DVector.wrap(pred.classes()).deepEquals(pr, TOL));


        // epsilon regression with probabilities

        RandomSource.setSeed(42);
        model = t.run(xs, ys, new String[] {"-s", "3",
                "-t", "2",
                "-g", "0.001",
                "-c", "10",
                "-b", "1"});
        pred = svm_predict.predict(model, xs, 0);

        svm = new SvmRegression()
                .type.set(SvmRegression.Penalty.C)
                .c.set(10.0)
                .probability.set(true)
                .kernel.set(new RBFKernel(0.001));

        RandomSource.setSeed(42);
        pr = svm.fit(df.mapVars("TV,Radio,Sales"), "Sales").predict(df).firstPrediction().dv();

        assertTrue(DVector.wrap(pred.classes()).deepEquals(pr, TOL));

        // nu regression without probabilities

        RandomSource.setSeed(42);
        model = t.run(xs, ys, new String[] {"-s", "4",
                "-t", "2",
                "-g", "0.001",
                "-n", "0.1",
                "-b", "0"});
        pred = svm_predict.predict(model, xs, 0);

        svm = new SvmRegression()
                .type.set(SvmRegression.Penalty.NU)
                .nu.set(0.1)
                .probability.set(false)
                .kernel.set(new RBFKernel(0.001));

        RandomSource.setSeed(42);
        pr = svm.fit(df.mapVars("TV,Radio,Sales"), "Sales").predict(df).firstPrediction().dv();

        assertTrue(DVector.wrap(pred.classes()).deepEquals(pr, TOL));


        // nu regression with probabilities

        RandomSource.setSeed(42);
        model = t.run(xs, ys, new String[] {"-s", "4",
                "-t", "2",
                "-g", "0.001",
                "-n", "0.1",
                "-b", "1"});
        pred = svm_predict.predict(model, xs, 0);

        svm = new SvmRegression()
                .type.set(SvmRegression.Penalty.NU)
                .nu.set(0.1)
                .probability.set(true)
                .kernel.set(new RBFKernel(0.001));

        RandomSource.setSeed(42);
        pr = svm.fit(df.mapVars("TV,Radio,Sales"), "Sales").predict(df).firstPrediction().dv();

        assertTrue(DVector.wrap(pred.classes()).deepEquals(pr, TOL));
    }
}
