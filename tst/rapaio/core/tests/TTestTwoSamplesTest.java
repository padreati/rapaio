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
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.NumVar;
import rapaio.data.Var;

import static org.junit.Assert.assertEquals;
import static rapaio.core.CoreTools.mean;
import static rapaio.core.CoreTools.variance;

public class TTestTwoSamplesTest {

    private static final double TOL = 1e-12;

    @Test
    public void precomputedTest() {
        Normal n = Normal.from(0, 10);
        Var x = NumVar.from(100, n::sampleNext).withName("x");
        Var y = NumVar.from(100, n::sampleNext).withName("y");
        Var z = NumVar.from(40, n::sampleNext).withName("z");

        TTestTwoSamples t1 = TTestTwoSamples.test(x, y, 0);
        TTestTwoSamples t2 = TTestTwoSamples.test(Mean.from(x).value(), x.rowCount(), Mean.from(y).value(), y.rowCount(), 0, Variance.from(x).sdValue(), Variance.from(y).sdValue());
        TTestTwoSamples t3 = TTestTwoSamples.welchTest(x, z, 0);
        TTestTwoSamples t4 = TTestTwoSamples.welchTest(Mean.from(x).value(), x.rowCount(), Mean.from(z).value(), z.rowCount(), 0, Variance.from(x).sdValue(), Variance.from(z).sdValue());

        assertEquals(t1.pValue(), t2.pValue(), TOL);
        assertEquals(t3.pValue(), t4.pValue(), TOL);
    }

    @Test
    public void testTTest() {

        Var x = NumVar.copy(5, 5.5, 4.5, 5, 5, 6, 5, 5, 4.5, 5, 5, 4.5, 4.5, 5.5, 4, 5, 5, 5.5, 4.5, 5.5, 5, 5.5).withName("x");
        Var y = NumVar.copy(7, 3, 5, 6, 6, 10).withName("y");

        TTestTwoSamples t1 = TTestTwoSamples.test(x, y, 0);
        t1.printSummary();

        assertEquals(x.rowCount(), t1.getXSampleSize(), TOL);
        assertEquals(mean(x).value(), t1.getXSampleMean(), TOL);
        assertEquals(variance(x).sdValue(), t1.getXSampleSd(), TOL);

        assertEquals(y.rowCount(), t1.getYSampleSize(), TOL);
        assertEquals(mean(y).value(), t1.getYSampleMean(), TOL);
        assertEquals(variance(y).sdValue(), t1.getYSampleSd(), TOL);

        assertEquals(true, t1.hasEqualVars());
        assertEquals(-1.166666666666667, t1.getSampleMean(), TOL);

        assertEquals(0, t1.getMu(), TOL);
        assertEquals(0.05, t1.getSl(), TOL);
        assertEquals(HTest.Alternative.TWO_TAILS, t1.getAlt());

        assertEquals(26, t1.getDf(), TOL);
        assertEquals(-2.307480895935302, t1.getT(), TOL);
        assertEquals(0.029245857024058096, t1.pValue(), TOL);
        assertEquals(-0.12738712912378114, t1.ciHigh(), TOL);
        assertEquals(-2.205946204209553, t1.ciLow(), TOL);

        TTestTwoSamples t2 = TTestTwoSamples.test(x, y, 2, 0.1, HTest.Alternative.LESS_THAN);
        t2.printSummary();

        assertEquals(2, t2.getMu(), TOL);
        assertEquals(0.1, t2.getSl(), TOL);
        assertEquals(HTest.Alternative.LESS_THAN, t2.getAlt());

        TTestTwoSamples t3 = TTestTwoSamples.test(x, y, 2, 0.1, HTest.Alternative.GREATER_THAN);
        assertEquals(HTest.Alternative.GREATER_THAN, t3.getAlt());

        HTest t4 = TTestTwoSamples.test(NumVar.empty(), x, 0);
        assertEquals(Double.NaN, t4.pValue(), TOL);
    }

    @Test
    public void testWelchTTest() {

        Var x = NumVar.copy(5, 5.5, 4.5, 5, 5, 6, 5, 5, 4.5, 5, 5, 4.5, 4.5, 5.5, 4, 5, 5, 5.5, 4.5, 5.5, 5, 5.5).withName("x");
        Var y = NumVar.copy(7, 3, 5, 6, 6, 10).withName("y");

        TTestTwoSamples t1 = TTestTwoSamples.welchTest(x, y, 0);
        t1.printSummary();

        assertEquals(x.rowCount(), t1.getXSampleSize(), TOL);
        assertEquals(mean(x).value(), t1.getXSampleMean(), TOL);
        assertEquals(variance(x).sdValue(), t1.getXSampleSd(), TOL);

        assertEquals(y.rowCount(), t1.getYSampleSize(), TOL);
        assertEquals(mean(y).value(), t1.getYSampleMean(), TOL);
        assertEquals(variance(y).sdValue(), t1.getYSampleSd(), TOL);

        assertEquals(false, t1.hasEqualVars());
        assertEquals(-1.166666666666667, t1.getSampleMean(), TOL);

        assertEquals(0, t1.getMu(), TOL);
        assertEquals(0.05, t1.getSl(), TOL);
        assertEquals(HTest.Alternative.TWO_TAILS, t1.getAlt());

        assertEquals(-1.226925553339566, t1.getT(), TOL);
        assertEquals(5.109345983643555, t1.getDf(), TOL);
        assertEquals(0.2733648790307336, t1.pValue(), TOL);
        assertEquals(1.2620136800377284, t1.ciHigh(), TOL);
        assertEquals(-3.5953470133710637, t1.ciLow(), TOL);


        TTestTwoSamples t2 = TTestTwoSamples.welchTest(x, y, 0, 0.05, HTest.Alternative.TWO_TAILS);

        assertEquals(t1.summary(), t2.summary());
    }

}
