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

package rapaio.ml.supervised.rvm;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/16/21.
 */
public class RVMRegressionTest {

    /*
    @BeforeEach
    public void beforeEach() {
        RandomSource.setSeed(42);
    }

    @Test
    void testBuilders() {
        RVMRegression model = RVMRegression.newModel();
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

        RVMRegression rvm = RVMRegression.newModel().kernel.set(new RBFKernel(20));

        assertEquals("RVMRegression{kernel=RBF(sigma=20)}; fitted=false", rvm.toString());

        RegressionResult rvmResult = rvm.fit(df, target).predict(df, true);

        assertEquals("RVMRegression{kernel=RBF(sigma=20)}; fitted=true, rvm count=44", rvm.toString());

        assertEquals("""
                Regression predict summary
                =======================
                Model class: RVMRegression
                Model instance: RVMRegression{kernel=RBF(sigma=20)}
                > model is trained.
                > input variables:\s
                1. TV        dbl\s
                2. Radio     dbl\s
                3. Newspaper dbl\s
                > target variables:\s
                1. Sales dbl\s

                Fit and residuals for Sales
                ===========================
                * summary:\s
                    Sales [dbl]          Sales [dbl]         Mean : 13.9191270    Mean :  0.1033730\s
                   Min. :  2.5357814    Min. : -2.6614187 2nd Qu. : 17.4096608 2nd Qu. :  0.5255609\s
                1st Qu. : 10.2752223 1st Qu. : -0.3540864    Max. : 27.0614187    Max. :  3.3858013\s
                 Median : 12.9077941  Median :  0.1139818                                          \s
                Total sum of squares     (TSS) : 5417.149
                Explained sum of squares (ESS) : 5519.327
                Residual sum of squares  (RSS) :  138.486

                Coeff. of determination  (R^2) :    0.974

                """, rvmResult.toSummary());

        assertEquals(rvm.toContent(), rvm.toSummary());
    }

    @Test
    void testMethods() {
        Frame df = Datasets.loadISLAdvertising();
        final String target = "Sales";

        RVMRegression rvm1 = RVMRegression.newModel()
                .kernel.set(new RBFKernel(40))
                .method.set(RVMRegression.Method.EVIDENCE_APPROXIMATION);
        RVMRegression rvm2 = RVMRegression.newModel()
                .kernel.set(new RBFKernel(40))
                .method.set(RVMRegression.Method.FAST_TIPPING);

        RegressionResult result1 = rvm1.fit(df, target).predict(df, true);
        RegressionResult result2 = rvm2.fit(df, target).predict(df, true);

        assertTrue(Math.abs(result1.firstRSquare() - result2.firstRSquare()) < 0.1);
    }
    */
}
