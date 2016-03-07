/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.filter.var.VFToNumeric;
import rapaio.datasets.Datasets;
import rapaio.ml.regression.RFit;

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
}
