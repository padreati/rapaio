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

package rapaio.ml.model.rvm;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;
import rapaio.math.linear.DMatrix;
import rapaio.ml.common.kernel.LinearKernel;
import rapaio.ml.model.RegressionResult;
import rapaio.ml.model.linear.LinearRegressionModel;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/16/21.
 */
public class RVMRegressionTest {

    @BeforeEach
    public void beforeEach() {
        RandomSource.setSeed(42);
    }

    @Test
    void testSales() {
        Frame df = Datasets.loadISLAdvertising();
        final String target = "Sales";

        LinearRegressionModel lm = LinearRegressionModel.newModel();
        RegressionResult lmResult = lm.fit(df, target).predict(df, true);

        RVMRegression rvm = RVMRegression.newModel()
                .method.set(RVMRegression.Method.EVIDENCE_APPROXIMATION)
                .providers.clear()
                .providers.add(new RVMRegression.InterceptProvider())
                .providers.add(new RVMRegression.KernelProvider(new LinearKernel(1), 1));
        RegressionResult rvmResult = rvm.fit(df, target).predict(df, true);

        // linear model should give similar results with RVM with linear kernel
        assertTrue(Math.abs(lmResult.firstRSquare() - rvmResult.firstRSquare()) < 0.05);

        rvm = RVMRegression.newModel()
                .method.set(RVMRegression.Method.FAST_TIPPING)
                .providers.clear()
                .providers.add(new RVMRegression.InterceptProvider())
                .providers.add(new RVMRegression.KernelProvider(new LinearKernel(1)));
        rvmResult = rvm.fit(df, target).predict(df, true);

        // linear model should give similar results with RVM with linear kernel
        assertTrue(Math.abs(lmResult.firstRSquare() - rvmResult.firstRSquare()) < 0.05);

        rvm = RVMRegression.newModel()
                .method.set(RVMRegression.Method.ONLINE_PRUNING)
                .providers.clear()
                .providers.add(new RVMRegression.InterceptProvider())
                .providers.add(new RVMRegression.KernelProvider(new LinearKernel(1), 1));
        rvmResult = rvm.fit(df, target).predict(df, true);

        // linear model should give similar results with RVM with linear kernel
        assertTrue(Math.abs(lmResult.firstRSquare() - rvmResult.firstRSquare()) < 0.05);
    }

    @Test
    void testPrinting() {
        Frame df = Datasets.loadISLAdvertising();
        final String target = "Sales";

        RVMRegression rvm = RVMRegression.newModel()
                .providers.clear()
                .providers.add(new RVMRegression.RBFProvider(VarDouble.wrap(0.00001)));

        assertEquals("RVMRegression{providers=[RBFProvider{gammas=[0.00001],p=1}]}; fitted=false", rvm.toString());

        RegressionResult rvmResult = rvm.fit(df, target).predict(df, true);

        assertEquals("RVMRegression{providers=[RBFProvider{gammas=[0.00001],p=1}]}; fitted=true, rvm count=21", rvm.toString());

        assertEquals("""
                Regression predict summary
                =======================
                Model class: RVMRegression
                Model instance: RVMRegression{providers=[RBFProvider{gammas=[0.00001],p=1}]}
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
                    Sales [dbl]          Sales [dbl]         Mean : 14.0225403    Mean : -0.0000403\s
                   Min. :  4.6940359    Min. : -3.6318551 2nd Qu. : 17.7113725 2nd Qu. :  0.2350278\s
                1st Qu. : 10.2685110 1st Qu. : -0.2421982    Max. : 27.0347093    Max. :  1.4899671\s
                 Median : 12.8623793  Median :  0.0012826                                          \s
                Total sum of squares     (TSS) : 5417.149
                Explained sum of squares (ESS) : 5367.601
                Residual sum of squares  (RSS) :   44.613
                                
                Coeff. of determination  (R^2) :    0.992
                                
                """, rvmResult.toSummary());

        assertEquals(rvm.toContent(), rvm.toSummary());
    }

    @Test
    void testInterceptProvider() {

        RVMRegression.InterceptProvider provider = new RVMRegression.InterceptProvider();
        assertTrue(provider.equalOnParams(new RVMRegression.InterceptProvider()));
        assertFalse(provider.equalOnParams(new RVMRegression.RBFProvider(VarDouble.wrap(1))));

        DMatrix x = DMatrix.eye(10);
        RVMRegression.Feature[] features = provider.generateFeatures(x);
        assertEquals(1, features.length);
        assertEquals("intercept", features[0].name());
    }

    @Test
    void testRBFProvider() {

        RVMRegression.RBFProvider provider = new RVMRegression.RBFProvider(VarDouble.wrap(1, 2), 0.5);

        assertTrue(provider.equalOnParams(new RVMRegression.RBFProvider(VarDouble.wrap(1, 2), 0.5)));
        assertFalse(provider.equalOnParams(new RVMRegression.InterceptProvider()));
        assertFalse(provider.equalOnParams(new RVMRegression.RBFProvider(VarDouble.wrap(2, 3), 0.5)));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new RVMRegression.RBFProvider(null, 0));
        assertEquals("Sigma vector cannot be empty.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> new RVMRegression.RBFProvider(VarDouble.wrap(), 1));
        assertEquals("Sigma vector cannot be empty.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> new RVMRegression.RBFProvider(VarDouble.wrap(1), -1));
        assertEquals("Percentage value p=-1 is not in interval [0,1].", ex.getMessage());

        DMatrix x = DMatrix.eye(10);
        RVMRegression.Feature[] features = provider.generateFeatures(x);
        assertEquals(10, features.length);

        for (RVMRegression.Feature feature : features) {
            assertTrue(feature.name().startsWith("RBF"));
        }
    }

    @Test
    void testKernelProvider() {
        RVMRegression.KernelProvider provider = new RVMRegression.KernelProvider(new LinearKernel(1), 0.5);

        assertFalse(provider.equalOnParams(new RVMRegression.InterceptProvider()));
        assertFalse(provider.equalOnParams(new RVMRegression.KernelProvider(new LinearKernel(2))));
        assertFalse(provider.equalOnParams(new RVMRegression.KernelProvider(new LinearKernel(1))));
        assertTrue(provider.equalOnParams(new RVMRegression.KernelProvider(new LinearKernel(1), 0.5)));

        RVMRegression.Feature[] features = provider.generateFeatures(DMatrix.eye(10));
        assertEquals(5, features.length);
        for (RVMRegression.Feature feature : features) {
            assertTrue(feature.name().startsWith("LinearKernel"));
        }
    }

    @Test
    void testRandomRBFProvider() {
        RVMRegression.RandomRBFProvider provider = new RVMRegression.RandomRBFProvider(VarDouble.wrap(1), 1.5, Normal.std());

        assertTrue(provider.equalOnParams(new RVMRegression.RandomRBFProvider(VarDouble.wrap(1), 1.5, Normal.std())));
        assertFalse(provider.equalOnParams(new RVMRegression.RandomRBFProvider(VarDouble.wrap(1), 1.5, Normal.of(1, 2))));
        assertFalse(provider.equalOnParams(new RVMRegression.RandomRBFProvider(VarDouble.wrap(1), 0.5, Normal.std())));
        assertFalse(provider.equalOnParams(new RVMRegression.RandomRBFProvider(VarDouble.wrap(2), 1.5, Normal.std())));
        assertFalse(provider.equalOnParams(new RVMRegression.InterceptProvider()));

        DMatrix x = DMatrix.eye(10);
        RVMRegression.Feature[] features = provider.generateFeatures(x);
        assertEquals(15, features.length);
        for (RVMRegression.Feature feature : features) {
            assertTrue(feature.name().startsWith("RBF"));
        }
    }

    @Test
    void testQuantiles() {
        Frame df = Datasets.loadISLAdvertising();
        final String target = "Sales";
        var rvm = RVMRegression.newModel()
                .method.set(RVMRegression.Method.ONLINE_PRUNING)
                .providers.clear()
                .providers.add(new RVMRegression.InterceptProvider())
                .providers.add(new RVMRegression.KernelProvider(new LinearKernel(1), 1));
        var rvmResult = rvm.fit(df, target).predict(df, true, 0.05, 0.25, 0.75, 0.95);

        VarDouble[] quantiles = rvmResult.firstQuantiles();
        VarDouble prediction = rvmResult.firstPrediction();

        for(VarDouble quantile : quantiles) {
            assertEquals(prediction.size(), quantile.size());
        }

        int len = prediction.size();
        for (int i = 0; i < len; i++) {
            assertTrue(quantiles[0].getDouble(i) <= prediction.getDouble(i));
            assertTrue(quantiles[1].getDouble(i) <= prediction.getDouble(i));
            assertTrue(quantiles[2].getDouble(i) >= prediction.getDouble(i));
            assertTrue(quantiles[3].getDouble(i) >= prediction.getDouble(i));
        }
    }
}
