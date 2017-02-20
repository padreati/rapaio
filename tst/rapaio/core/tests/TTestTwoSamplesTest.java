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

package rapaio.core.tests;

import org.junit.Assert;
import org.junit.Test;
import rapaio.data.Numeric;
import rapaio.data.Var;

import static rapaio.core.CoreTools.mean;
import static rapaio.core.CoreTools.var;

public class TTestTwoSamplesTest {

    private static final double TOL = 1e-12;

    @Test
    public void testTTest() {

        Var x = Numeric.copy(5, 5.5, 4.5, 5, 5, 6, 5, 5, 4.5, 5, 5, 4.5, 4.5, 5.5, 4, 5, 5, 5.5, 4.5, 5.5, 5, 5.5).withName("x");
        Var y = Numeric.copy(7, 3, 5, 6, 6, 10).withName("y");

        TTestTwoSamples t1 = TTestTwoSamples.test(x, y, 0);
        t1.printSummary();

        Assert.assertEquals(x.rowCount(), t1.xSampleSize(), TOL);
        Assert.assertEquals(mean(x).value(), t1.xSampleMean(), TOL);
        Assert.assertEquals(var(x).sdValue(), t1.xSampleSd(), TOL);

        Assert.assertEquals(y.rowCount(), t1.ySampleSize(), TOL);
        Assert.assertEquals(mean(y).value(), t1.ySampleMean(), TOL);
        Assert.assertEquals(var(y).sdValue(), t1.ySampleSd(), TOL);

        Assert.assertEquals(true, t1.equalVars());
        Assert.assertEquals(-1.166666666666667, t1.sampleMean(), TOL);

        Assert.assertEquals(0, t1.mu(), TOL);
        Assert.assertEquals(0.05, t1.sl(), TOL);
        Assert.assertEquals(HTest.Alternative.TWO_TAILS, t1.alt());

        Assert.assertEquals(26, t1.df(), TOL);
        Assert.assertEquals(-2.307480895935302, t1.t(), TOL);
        Assert.assertEquals(0.029245857024058096, t1.pValue(), TOL);
        Assert.assertEquals(-0.12738712912378114, t1.ciHigh(), TOL);
        Assert.assertEquals(-2.205946204209553, t1.ciLow(), TOL);

        TTestTwoSamples t2 = TTestTwoSamples.test(x, y, 2, 0.1, HTest.Alternative.LESS_THAN);
        t2.printSummary();

        Assert.assertEquals(2, t2.mu(), TOL);
        Assert.assertEquals(0.1, t2.sl(), TOL);
        Assert.assertEquals(HTest.Alternative.LESS_THAN, t2.alt());

        TTestTwoSamples t3 = TTestTwoSamples.test(x, y, 2, 0.1, HTest.Alternative.GREATER_THAN);
        Assert.assertEquals(HTest.Alternative.GREATER_THAN, t3.alt());

        HTest t4 = TTestTwoSamples.test(Numeric.empty(), x, 0);
        Assert.assertEquals(Double.NaN, t4.pValue(), TOL);
    }

    @Test
    public void testWelchTTest() {

        Var x = Numeric.copy(5, 5.5, 4.5, 5, 5, 6, 5, 5, 4.5, 5, 5, 4.5, 4.5, 5.5, 4, 5, 5, 5.5, 4.5, 5.5, 5, 5.5).withName("x");
        Var y = Numeric.copy(7, 3, 5, 6, 6, 10).withName("y");

        TTestTwoSamples t1 = TTestTwoSamples.welchTest(x, y, 0);
        t1.printSummary();

        Assert.assertEquals(x.rowCount(), t1.xSampleSize(), TOL);
        Assert.assertEquals(mean(x).value(), t1.xSampleMean(), TOL);
        Assert.assertEquals(var(x).sdValue(), t1.xSampleSd(), TOL);

        Assert.assertEquals(y.rowCount(), t1.ySampleSize(), TOL);
        Assert.assertEquals(mean(y).value(), t1.ySampleMean(), TOL);
        Assert.assertEquals(var(y).sdValue(), t1.ySampleSd(), TOL);

        Assert.assertEquals(false, t1.equalVars());
        Assert.assertEquals(-1.166666666666667, t1.sampleMean(), TOL);

        Assert.assertEquals(0, t1.mu(), TOL);
        Assert.assertEquals(0.05, t1.sl(), TOL);
        Assert.assertEquals(HTest.Alternative.TWO_TAILS, t1.alt());

        Assert.assertEquals(-1.226925553339566, t1.t(), TOL);
        Assert.assertEquals(5.109345983643555, t1.df(), TOL);
        Assert.assertEquals(0.2733648790307336, t1.pValue(), TOL);
        Assert.assertEquals(1.2620136800377284, t1.ciHigh(), TOL);
        Assert.assertEquals(-3.5953470133710637, t1.ciLow(), TOL);


        TTestTwoSamples t2 = TTestTwoSamples.welchTest(x, y, 0, 0.05, HTest.Alternative.TWO_TAILS);

        Assert.assertEquals(t1.summary(), t2.summary());
    }

}
