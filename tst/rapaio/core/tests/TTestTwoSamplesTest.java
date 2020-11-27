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

package rapaio.core.tests;

import org.junit.jupiter.api.Test;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Var;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.*;

public class TTestTwoSamplesTest {

    private static final double TOL = 1e-12;

    @Test
    void precomputedTest() {
        Normal n = Normal.of(0, 10);
        Var x = VarDouble.from(100, n::sampleNext).name("x");
        Var y = VarDouble.from(100, n::sampleNext).name("y");
        Var z = VarDouble.from(40, n::sampleNext).name("z");

        TTestTwoSamples t1 = TTestTwoSamples.test(x, y, 0);
        TTestTwoSamples t2 = TTestTwoSamples.test(Mean.of(x).value(), x.size(), Mean.of(y).value(), y.size(), 0, Variance.of(x).sdValue(), Variance.of(y).sdValue());
        TTestTwoSamples t3 = TTestTwoSamples.welchTest(x, z, 0);
        TTestTwoSamples t4 = TTestTwoSamples.welchTest(Mean.of(x).value(), x.size(), Mean.of(z).value(), z.size(), 0, Variance.of(x).sdValue(), Variance.of(z).sdValue());

        assertEquals(t1.pValue(), t2.pValue(), TOL);
        assertEquals(t3.pValue(), t4.pValue(), TOL);
    }

    @Test
    void testTTest() {

        Var x = VarDouble.copy(5, 5.5, 4.5, 5, 5, 6, 5, 5, 4.5, 5, 5, 4.5, 4.5, 5.5, 4, 5, 5, 5.5, 4.5, 5.5, 5, 5.5).name("x");
        Var y = VarDouble.copy(7, 3, 5, 6, 6, 10).name("y");

        TTestTwoSamples t1 = TTestTwoSamples.test(x, y, 0);
        t1.printSummary();

        assertEquals(x.size(), t1.getXSampleSize(), TOL);
        assertEquals(Mean.of(x).value(), t1.getXSampleMean(), TOL);
        assertEquals(Variance.of(x).sdValue(), t1.getXSampleSd(), TOL);

        assertEquals(y.size(), t1.getYSampleSize(), TOL);
        assertEquals(Mean.of(y).value(), t1.getYSampleMean(), TOL);
        assertEquals(Variance.of(y).sdValue(), t1.getYSampleSd(), TOL);

        assertTrue(t1.hasEqualVars());
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

        HTest t4 = TTestTwoSamples.test(VarDouble.empty(), x, 0);
        assertEquals(Double.NaN, t4.pValue(), TOL);
    }

    @Test
    public void testWelchTTest() {

        Var x = VarDouble.copy(5, 5.5, 4.5, 5, 5, 6, 5, 5, 4.5, 5, 5, 4.5, 4.5, 5.5, 4, 5, 5, 5.5, 4.5, 5.5, 5, 5.5).name("x");
        Var y = VarDouble.copy(7, 3, 5, 6, 6, 10).name("y");

        TTestTwoSamples t1 = TTestTwoSamples.welchTest(x, y, 0);
        t1.printSummary();

        assertEquals(x.size(), t1.getXSampleSize(), TOL);
        assertEquals(Mean.of(x).value(), t1.getXSampleMean(), TOL);
        assertEquals(Variance.of(x).sdValue(), t1.getXSampleSd(), TOL);

        assertEquals(y.size(), t1.getYSampleSize(), TOL);
        assertEquals(Mean.of(y).value(), t1.getYSampleMean(), TOL);
        assertEquals(Variance.of(y).sdValue(), t1.getYSampleSd(), TOL);

        assertFalse(t1.hasEqualVars());
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

        assertEquals(t1.toSummary(), t2.toSummary());
    }

}
