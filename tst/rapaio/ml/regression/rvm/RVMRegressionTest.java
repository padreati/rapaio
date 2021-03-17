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
import rapaio.ml.classifier.svm.kernel.LinearKernel;
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
        assertEquals(new LinearKernel().name(), model.kernel.get().name());
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

        RVMRegression rvm = RVMRegression.newModel();

        assertEquals("RVMRegression{}; fitted=false", rvm.toString());

        RegressionResult rvmResult = rvm.fit(df, target).predict(df, true);

        assertEquals("RVMRegression{}; fitted=true, rvm count=3", rvm.toString());

        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "Model class: RVMRegression\n" +
                "Model instance: RVMRegression{}\n" +
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
                "    Sales [dbl]          Sales [dbl]         Mean : 13.5922670    Mean :  0.4302330 \n" +
                "   Min. :  1.3986109    Min. : -7.5969764 2nd Qu. : 17.9819851 2nd Qu. :  1.5054285 \n" +
                "1st Qu. :  9.0039990 1st Qu. : -0.5430484    Max. : 26.1414107    Max. :  4.8419865 \n" +
                " Median : 13.6087185  Median :  0.4649531                                           \n" +
                "Total sum of squares     (TSS) : 5417.149\n" +
                "Explained sum of squares (ESS) : 6704.446\n" +
                "Residual sum of squares  (RSS) :  738.984\n" +
                "\n" +
                "Coeff. of determination  (R^2) :    0.864\n" +
                "\n", rvmResult.toSummary());

        assertEquals(rvm.toContent(), rvm.toSummary());
        assertEquals(rvm.toFullContent(), rvm.toSummary());
    }
}
