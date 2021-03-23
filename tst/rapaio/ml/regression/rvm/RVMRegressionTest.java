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

package rapaio.ml.regression.rvm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.ml.classifier.svm.kernel.RBFKernel;
import rapaio.ml.regression.RegressionResult;
import rapaio.ml.regression.linear.LinearRegressionModel;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/16/21.
 */
public class RVMRegressionTest {

    @BeforeEach
    public void beforeEach() {
        RandomSource.setSeed(42);
    }

    @Test
    void testBuilders() {
        RVMRegression model = RVMRegression.newModel();
        assertEquals(true, model.intercept.get());
        assertEquals(new RBFKernel(1).name(), model.kernel.get().name());
        assertEquals("RVMRegression", model.name());

        model.kernel.set(new RBFKernel(10));

        RVMRegression copy = model.newInstance();
        assertEquals(model.fullName(), copy.fullName());
    }

    @Test
    void testSales() {
        Frame df = Datasets.loadISLAdvertising();
        final String target = "Sales";

        LinearRegressionModel lm = LinearRegressionModel.newModel();
        RegressionResult lmResult = lm.fit(df, target).predict(df, true);

        RVMRegression rvm = RVMRegression.newModel();
        RegressionResult rvmResult = rvm.fit(df, target).predict(df, true);

        // models should provide similar results
        assertTrue(Math.abs(lmResult.firstRSquare() - rvmResult.firstRSquare()) < 0.05);
    }

    @Test
    void testPrinting() {
        Frame df = Datasets.loadISLAdvertising();
        final String target = "Sales";

        RVMRegression rvm = RVMRegression.newModel().kernel.set(new RBFKernel(400));

        assertEquals("RVMRegression{kernel=RBF(sigma=400)}; fitted=false", rvm.toString());

        RegressionResult rvmResult = rvm.fit(df, target).predict(df, true);

        assertEquals("RVMRegression{kernel=RBF(sigma=400)}; fitted=true, rvm count=57", rvm.toString());

        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: RVMRegression\n" +
                "Model instance: RVMRegression{kernel=RBF(sigma=400)}\n" +
                "> model is trained.\n" +
                "> input variables: \n" +
                "1. TV        dbl \n" +
                "2. Radio     dbl \n" +
                "3. Newspaper dbl \n" +
                "> target variables: \n" +
                "1. Sales dbl \n" +
                "\n" +
                "Fit and residuals for Sales\n" +
                "===========================\n" +
                "* summary: \n" +
                "    Sales [dbl]          Sales [dbl]         Mean : 13.6143239    Mean :  0.4081761 \n" +
                "   Min. :  1.0205857    Min. : -4.0513838 2nd Qu. : 17.1194427 2nd Qu. :  1.2808171 \n" +
                "1st Qu. :  9.9663868 1st Qu. : -0.4797786    Max. : 28.4687762    Max. :  6.5281341 \n" +
                " Median : 12.9114577  Median :  0.2398231                                           \n" +
                "Total sum of squares     (TSS) : 5417.149\n" +
                "Explained sum of squares (ESS) : 5749.542\n" +
                "Residual sum of squares  (RSS) :  444.871\n" +
                "\n" +
                "Coeff. of determination  (R^2) :    0.918\n" +
                "\n", rvmResult.toSummary());

        assertEquals(rvm.toContent(), rvm.toSummary());
    }

    @Test
    void testMethods() {
        Frame df = Datasets.loadISLAdvertising();
        final String target = "Sales";

        RVMRegression rvm1 = RVMRegression.newModel()
                .kernel.set(new RBFKernel(1000))
                .method.set(RVMRegression.Method.EVIDENCE_APPROXIMATION);
        RVMRegression rvm2 = RVMRegression.newModel()
                .kernel.set(new RBFKernel(1000))
                .method.set(RVMRegression.Method.FAST_TIPPING);

        RegressionResult result1 = rvm1.fit(df, target).predict(df, true);
        RegressionResult result2 = rvm2.fit(df, target).predict(df, true);

        assertTrue(Math.abs(result1.firstRSquare() - result2.firstRSquare()) < 0.1);
    }
}
