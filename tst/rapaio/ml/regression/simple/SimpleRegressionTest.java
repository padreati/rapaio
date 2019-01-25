/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.ml.regression.simple;

import org.junit.Before;
import org.junit.Test;
import rapaio.core.*;
import rapaio.core.distributions.*;
import rapaio.core.stat.*;
import rapaio.core.tests.*;
import rapaio.data.*;
import rapaio.data.filter.var.*;
import rapaio.datasets.*;
import rapaio.ml.regression.*;

import static org.junit.Assert.*;

/**
 * Test for simple regression algorithms
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/7/16.
 */
public class SimpleRegressionTest {

    String father = "Father";
    String son = "Son";
    Frame df;

    @Before
    public void setUp() throws Exception {
        df = Datasets.loadPearsonHeightDataset();
    }

    @Test
    public void testConstantRegression() {

        ConstantRegression r1 = ConstantRegression.with(66).newInstance();
        r1.fit(df, father);
        RPrediction fit1 = r1.predict(df);
        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "\n" +
                "Model class: ConstantRegression\n" +
                "Model instance: ConstantRegression{constant=66}\n" +
                "\n" +
                "> input variables: \n" +
                "1. Son double \n" +
                "> target variables: \n" +
                "1. Father double \n" +
                "\n" +
                "Fitted values:\n" +
                "\n" +
                "Target Estimate \n" +
                "Father    66    \n" +
                "\n", r1.summary());

        ConstantRegression r2 = ConstantRegression.with(1);
        r2.fit(df, father);
        RPrediction fit2 = r2.predict(df, true);

        assertTrue(VarDouble.fill(df.rowCount(), 66).withName("Father")
                .deepEquals(fit1.firstFit()));
        assertTrue(df.rvar(father).solidCopy().fapply(VToDouble.byValue(x -> x - 66)).withName("Father-residual")
                .deepEquals(fit1.firstResidual()));

        assertTrue(VarDouble.fill(df.rowCount(), 1).withName("Father")
                .deepEquals(fit2.firstFit()));
        assertTrue(df.rvar(father).solidCopy().fapply(VToDouble.byValue(x -> x - 1)).withName("Father-residual")
                .deepEquals(fit2.firstResidual()));

        assertEquals("ConstantRegression{constant=66}", r1.toString());
        assertEquals("ConstantRegression{constant=66}", r1.content());
        assertEquals("ConstantRegression{constant=66}", r1.fullContent());
        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "\n" +
                "Model class: ConstantRegression\n" +
                "Model instance: ConstantRegression{constant=66}\n" +
                "\n" +
                "> input variables: \n" +
                "1. Son double \n" +
                "> target variables: \n" +
                "1. Father double \n" +
                "\n" +
                "Fitted values:\n" +
                "\n" +
                "Target Estimate \n" +
                "Father    66    \n" +
                "\n", r1.summary());
    }

    @Test
    public void testL1Regression() {

        L1Regression r1 = L1Regression.create();
        r1.fit(df, father);
        RPrediction fit1 = r1.predict(df);

        double median = Quantiles.of(df.rvar(father), 0.5).values()[0];
        assertTrue(VarDouble.fill(df.rowCount(), median).withName(father).deepEquals(fit1.firstFit()));
    }

    @Test
    public void testL2Regression() {

        L2Regression r1 = L2Regression.create();
        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "\n" +
                "Model class: L2Regression\n" +
                "Model instance: L2Regression\n" +
                "\n" +
                "> model not trained.\n" +
                "\n" +
                "\n", r1.summary());

        r1.fit(df, father);
        assertEquals("Regression predict summary\n" +
                "=======================\n" +
                "\n" +
                "Model class: L2Regression\n" +
                "Model instance: L2Regression\n" +
                "\n" +
                "> input variables: \n" +
                "1. Son double \n" +
                "> target variables: \n" +
                "1. Father double \n" +
                "\n" +
                "Fitted values:\n" +
                "\n" +
                "Target   Estimate \n" +
                "Father 67.6868275 \n" +
                "\n", r1.summary());

        RPrediction fit1 = r1.predict(df, true);
        assertEquals("Regression predict summary\n" +
                        "=======================\n" +
                        "\n" +
                        "Model class: L2Regression\n" +
                        "Model instance: L2Regression\n" +
                        "\n" +
                        "> input variables: \n" +
                        "1. Son double \n" +
                        "> target variables: \n" +
                        "1. Father double \n" +
                        "\n" +
                        "Fit and residuals for Father\n" +
                        "============================\n" +
                        "          Father   Father-residual \n" +
                        "   Min. : 67.687     Min. : -8.687 \n" +
                        "1st Qu. : 67.687  1st Qu. : -1.887 \n" +
                        " Median : 67.687   Median :  0.113 \n" +
                        "   Mean : 67.687     Mean : -0.000 \n" +
                        "2nd Qu. : 67.687  2nd Qu. :  1.913 \n" +
                        "   Max. : 67.687     Max. :  7.713 \n" +
                        "                                   \n" +
                        "Total sum of squares     (TSS) : 8120.113\n" +
                        "Explained sum of squares (ESS) :    0.000\n" +
                        "Residual sum of squares  (RSS) : 8120.113\n" +
                        "\n" +
                        "Coeff. of determination  (R^2) :    0.000\n" +
                        "\n",
                fit1.summary());
    }

    @Test
    public void testRandomValueRegression() {
        RandomSource.setSeed(123);

        RPrediction fit1 = RandomValueRegression.create().fit(df, father).predict(df);
        RPrediction fit2 = RandomValueRegression.create(Normal.of(10, 0.1)).fit(df, father).predict(df);

        // unsignificant if test on true distribution
        assertTrue(KSTestOneSample.from(fit1.firstFit(), Uniform.of(0, 1)).pValue() > 0.01);
        assertTrue(KSTestOneSample.from(fit2.firstFit(), Normal.of(10, 0.1)).pValue() > 0.01);

        // significant if test on a different distribution
        assertTrue(KSTestOneSample.from(fit1.firstFit(), Normal.of(10, 0.1)).pValue() < 0.01);
        assertTrue(KSTestOneSample.from(fit2.firstFit(), Uniform.of(0, 1)).pValue() < 0.01);
    }
}
