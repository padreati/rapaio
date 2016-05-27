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

package rapaio.core;

import org.junit.Test;
import rapaio.core.tests.ZTestOneSample;
import rapaio.core.tests.ZTestTwoSample;
import rapaio.data.Numeric;
import rapaio.data.Var;

import static org.junit.Assert.assertEquals;

public class HTToolsTest {

    private static double TOL = 1e-20;

    @Test
    public void zTestOneSampleTest() {

        RandomSource.setSeed(1234);
        double mu = 75;
        double sd = 18;
        Var x = Numeric.copy(65, 78, 88, 55, 48, 95, 66, 57, 79, 81);

        ZTestOneSample z1 = HTTools.zTestOneSample(x, mu, sd);
        z1.printSummary();
        assertEquals(71.2, z1.sampleMean, TOL);
        assertEquals(-0.6675919504799908, z1.zScore, TOL);
        assertEquals(0.5043940973335608, z1.pValue, TOL);
        assertEquals(60.0436894185179, z1.ciLow, TOL);
        assertEquals(82.3563105814821, z1.ciHigh, TOL);
        assertEquals(0.05, z1.sl, TOL);

        ZTestOneSample z2 = HTTools.zTestOneSample(x, mu, sd, 0.05, HTTools.Alternative.LESS_THAN);
        z2.printSummary();
        assertEquals(71.2, z2.sampleMean, TOL);
        assertEquals(-0.6675919504799908, z2.zScore, TOL);
        assertEquals(0.2521970486667804, z2.pValue, TOL);
        assertEquals(60.0436894185179, z2.ciLow, TOL);
        assertEquals(82.3563105814821, z2.ciHigh, TOL);
        assertEquals(0.05, z2.sl, TOL);


        ZTestOneSample z3 = HTTools.zTestOneSample(x, mu, sd, 0.01, HTTools.Alternative.GREATER_THAN);
        z3.printSummary();
        assertEquals(71.2, z3.sampleMean, TOL);
        assertEquals(-0.6675919504799908, z3.zScore, TOL);
        assertEquals(0.7478029513332196, z3.pValue, TOL);
        assertEquals(56.53812256656457, z3.ciLow, TOL);
        assertEquals(85.86187743343541, z3.ciHigh, TOL);
        assertEquals(0.01, z3.sl, TOL);

        ZTestOneSample z4 = HTTools.zTestOneSample(Numeric.copy(Double.NaN, Double.NaN), 0, 1);
        z4.printSummary();

        assertEquals(Double.NaN, z4.sampleMean, TOL);
        assertEquals(Double.NaN, z4.zScore, TOL);
        assertEquals(Double.NaN, z4.pValue, TOL);
        assertEquals(Double.NaN, z4.ciLow, TOL);
        assertEquals(Double.NaN, z4.ciHigh, TOL);
        assertEquals(0.05, z4.sl, TOL);

    }

    @Test
    public void zTestTwoSamples() {
        Var x = Numeric.copy(7.8, 6.6, 6.5, 7.4, 7.3, 7.0, 6.4, 7.1, 6.7, 7.6, 6.8);
        Var y = Numeric.copy(4.5, 5.4, 6.1, 6.1, 5.4, 5., 4.1, 5.5);

        ZTestTwoSample z1 = HTTools.zTestTwoSample(x, y, 2, 0.5, 0.5, 0.05, HTTools.Alternative.TWO_TAILS);
        z1.printSummary();
        assertEquals(1.7556818181818183, z1.sampleMean, TOL);
        assertEquals(7.0181818181818185, z1.xSampleMean, TOL);
        assertEquals(5.2625, z1.ySampleMean, TOL);

        assertEquals(-1.051599374295714, z1.zScore, TOL);
        assertEquals(0.2929833949856928, z1.pValue, TOL);
        assertEquals(1.3003232007875778, z1.ciLow, TOL);
        assertEquals(2.211040435576059, z1.ciHigh, TOL);


        ZTestTwoSample z2 = HTTools.zTestTwoSample(x, y, 0, 0.5, 0.6, 0.10, HTTools.Alternative.TWO_TAILS);
        z2.printSummary();

        assertEquals(6.7462746482071205, z2.zScore, TOL);
        assertEquals(1.5169976386175676E-11, z2.pValue, TOL);
        assertEquals(1.3276174779349252, z2.ciLow, TOL);
        assertEquals(2.1837461584287112, z2.ciHigh, TOL);

        ZTestTwoSample z3 = HTTools.zTestTwoSample(x, Numeric.empty(), 0, 0.5, 0.5);
        z3.printSummary();

        assertEquals(Double.NaN, z3.zScore, TOL);
        assertEquals(Double.NaN, z3.pValue, TOL);
        assertEquals(Double.NaN, z3.ciLow, TOL);
        assertEquals(Double.NaN, z3.ciHigh, TOL);

        ZTestTwoSample z4 = HTTools.zTestTwoSample(x, y, 2, 0.5, 0.5, 0.05, HTTools.Alternative.GREATER_THAN);
        z2.printSummary();
        assertEquals(-1.051599374295714, z4.zScore, TOL);
        assertEquals(0.8535083025071536, z4.pValue, TOL);
        assertEquals(1.3003232007875778, z4.ciLow, TOL);
        assertEquals(2.211040435576059, z4.ciHigh, TOL);


        ZTestTwoSample z5 = HTTools.zTestTwoSample(x, y, 2, 0.5, 0.5, 0.05, HTTools.Alternative.LESS_THAN);
        z1.printSummary();
        assertEquals(-1.051599374295714, z5.zScore, TOL);
        assertEquals(0.1464916974928464, z5.pValue, TOL);
        assertEquals(1.3003232007875778, z5.ciLow, TOL);
        assertEquals(2.211040435576059, z5.ciHigh, TOL);



    }
}
