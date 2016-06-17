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

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.Numeric;
import rapaio.data.Var;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/14/16.
 */
public class ZTestOneSampleTest {
    private static double TOL = 1e-20;

    @Test
    public void zTestOneSampleTest() {

        RandomSource.setSeed(1234);
        double mu = 75;
        double sd = 18;
        Var x = Numeric.copy(65, 78, 88, 55, 48, 95, 66, 57, 79, 81);

        ZTestOneSample z1 = ZTestOneSample.test(x, mu, sd);
        z1.printSummary();

        assertEquals(x.rowCount(), z1.sampleSize());
        assertEquals(HTest.Alternative.TWO_TAILS, z1.alt());
        assertEquals(mu, z1.mu(), TOL);
        assertEquals(sd, z1.sd(), TOL);

        assertEquals(71.2, z1.sampleMean(), TOL);
        assertEquals(-0.6675919504799908, z1.zScore(), TOL);
        assertEquals(0.5043940973335608, z1.pValue(), TOL);
        assertEquals(60.0436894185179, z1.ciLow(), TOL);
        assertEquals(82.3563105814821, z1.ciHigh(), TOL);
        assertEquals(0.05, z1.sl(), TOL);

        z1 = ZTestOneSample.test(71.2, x.rowCount(), mu, sd);
        z1.printSummary();
        assertEquals(71.2, z1.sampleMean(), TOL);
        assertEquals(-0.6675919504799908, z1.zScore(), TOL);
        assertEquals(0.5043940973335608, z1.pValue(), TOL);
        assertEquals(60.0436894185179, z1.ciLow(), TOL);
        assertEquals(82.3563105814821, z1.ciHigh(), TOL);
        assertEquals(0.05, z1.sl(), TOL);


        z1 = ZTestOneSample.test(71.2, x.rowCount(), mu, sd, 0.05, HTest.Alternative.TWO_TAILS);
        z1.printSummary();
        assertEquals(71.2, z1.sampleMean(), TOL);
        assertEquals(-0.6675919504799908, z1.zScore(), TOL);
        assertEquals(0.5043940973335608, z1.pValue(), TOL);
        assertEquals(60.0436894185179, z1.ciLow(), TOL);
        assertEquals(82.3563105814821, z1.ciHigh(), TOL);
        assertEquals(0.05, z1.sl(), TOL);


        ZTestOneSample z2 = ZTestOneSample.test(x, mu, sd, 0.05, HTest.Alternative.LESS_THAN);
        z2.printSummary();
        assertEquals(71.2, z2.sampleMean(), TOL);
        assertEquals(-0.6675919504799908, z2.zScore(), TOL);
        assertEquals(0.2521970486667804, z2.pValue(), TOL);
        assertEquals(60.0436894185179, z2.ciLow(), TOL);
        assertEquals(82.3563105814821, z2.ciHigh(), TOL);
        assertEquals(0.05, z2.sl(), TOL);


        ZTestOneSample z3 = ZTestOneSample.test(x, mu, sd, 0.01, HTest.Alternative.GREATER_THAN);
        z3.printSummary();
        assertEquals(71.2, z3.sampleMean(), TOL);
        assertEquals(-0.6675919504799908, z3.zScore(), TOL);
        assertEquals(0.7478029513332196, z3.pValue(), TOL);
        assertEquals(56.53812256656457, z3.ciLow(), TOL);
        assertEquals(85.86187743343541, z3.ciHigh(), TOL);
        assertEquals(0.01, z3.sl(), TOL);

        ZTestOneSample z4 = ZTestOneSample.test(Numeric.copy(Double.NaN, Double.NaN), 0, 1);
        z4.printSummary();

        assertEquals(Double.NaN, z4.sampleMean(), TOL);
        assertEquals(Double.NaN, z4.zScore(), TOL);
        assertEquals(Double.NaN, z4.pValue(), TOL);
        assertEquals(Double.NaN, z4.ciLow(), TOL);
        assertEquals(Double.NaN, z4.ciHigh(), TOL);
        assertEquals(0.05, z4.sl(), TOL);

    }

}
