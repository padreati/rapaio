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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rapaio.core.CoreTools;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.filter.var.VFToNumeric;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plotter;
import rapaio.ml.regression.RFit;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import static rapaio.graphics.Plotter.points;

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
        r1.train(df, father);
        RFit fit1 = r1.fit(df);
        fit1.printSummary();

        Assert.assertEquals("ConstantRegression {\n" +
                "\tconstant=66\n" +
                "}\n", r1.summary());

        ConstantRegression r2 = ConstantRegression.with(1);
        r2.train(df, father);
        RFit fit2 = r2.fit(df);
        fit2.printSummary();

        Assert.assertTrue(Numeric.fill(df.rowCount(), 66).withName("Father")
                .deepEquals(fit1.firstFit()));
        Assert.assertTrue(df.var(father).solidCopy().fitApply(VFToNumeric.byValue(x -> x - 66)).withName("Father-residual")
                .deepEquals(fit1.firstResidual()));

        Assert.assertTrue(Numeric.fill(df.rowCount(), 1).withName("Father")
                .deepEquals(fit2.firstFit()));
        Assert.assertTrue(df.var(father).solidCopy().fitApply(VFToNumeric.byValue(x -> x - 1)).withName("Father-residual")
                .deepEquals(fit2.firstResidual()));
    }

    @Test
    public void testL1Regression() {

        L1Regression r1 = L1Regression.create().newInstance();
        r1.train(df, father);

        r1.printSummary();

        RFit fit1 = r1.fit(df);
        fit1.printSummary();

        double median = CoreTools.quantiles(df.var(father), 0.5).values()[0];
        Assert.assertTrue(Numeric.fill(df.rowCount(), median).withName(father)
                .deepEquals(fit1.firstFit()));
    }

    @Test
    public void testL2Regression() {

        L2Regression r1 = L2Regression.create().newInstance();
        r1.train(df, father);
        r1.printSummary();

        RFit fit1 = r1.fit(df);
        fit1.printSummary();

        WS.setPrinter(new IdeaPrinter());
        WS.draw(points(df.var(father), fit1.firstFit()));

    }

    @Test
    public void testRandomValueRegression() {
        RandomValueRegression r1 = RandomValueRegression.create();
        r1.train(df, father);
        RFit fit1 = r1.fit(df);
        fit1.printSummary();
    }
}
