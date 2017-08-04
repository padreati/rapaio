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
import rapaio.data.NumericVar;
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
        Var x = NumericVar.copy(65, 78, 88, 55, 48, 95, 66, 57, 79, 81);

        ZTestOneSample z1 = ZTestOneSample.test(x, mu, sd);
        z1.printSummary();

        assertEquals(x.getRowCount(), z1.getSampleSize());
        assertEquals(HTest.Alternative.TWO_TAILS, z1.getAlt());
        assertEquals(mu, z1.getMu(), TOL);
        assertEquals(sd, z1.getSd(), TOL);

        assertEquals(71.2, z1.getSampleMean(), TOL);
        assertEquals(-0.6675919504799908, z1.getZScore(), TOL);
        assertEquals(0.5043940973335608, z1.getPValue(), TOL);
        assertEquals(60.0436894185179, z1.getCILow(), TOL);
        assertEquals(82.3563105814821, z1.getCIHigh(), TOL);
        assertEquals(0.05, z1.getSl(), TOL);

        z1 = ZTestOneSample.test(71.2, x.getRowCount(), mu, sd);
        z1.printSummary();
        assertEquals(71.2, z1.getSampleMean(), TOL);
        assertEquals(-0.6675919504799908, z1.getZScore(), TOL);
        assertEquals(0.5043940973335608, z1.getPValue(), TOL);
        assertEquals(60.0436894185179, z1.getCILow(), TOL);
        assertEquals(82.3563105814821, z1.getCIHigh(), TOL);
        assertEquals(0.05, z1.getSl(), TOL);


        z1 = ZTestOneSample.test(71.2, x.getRowCount(), mu, sd, 0.05, HTest.Alternative.TWO_TAILS);
        z1.printSummary();
        assertEquals(71.2, z1.getSampleMean(), TOL);
        assertEquals(-0.6675919504799908, z1.getZScore(), TOL);
        assertEquals(0.5043940973335608, z1.getPValue(), TOL);
        assertEquals(60.0436894185179, z1.getCILow(), TOL);
        assertEquals(82.3563105814821, z1.getCIHigh(), TOL);
        assertEquals(0.05, z1.getSl(), TOL);


        ZTestOneSample z2 = ZTestOneSample.test(x, mu, sd, 0.05, HTest.Alternative.LESS_THAN);
        z2.printSummary();
        assertEquals(71.2, z2.getSampleMean(), TOL);
        assertEquals(-0.6675919504799908, z2.getZScore(), TOL);
        assertEquals(0.2521970486667804, z2.getPValue(), TOL);
        assertEquals(60.0436894185179, z2.getCILow(), TOL);
        assertEquals(82.3563105814821, z2.getCIHigh(), TOL);
        assertEquals(0.05, z2.getSl(), TOL);


        ZTestOneSample z3 = ZTestOneSample.test(x, mu, sd, 0.01, HTest.Alternative.GREATER_THAN);
        z3.printSummary();
        assertEquals(71.2, z3.getSampleMean(), TOL);
        assertEquals(-0.6675919504799908, z3.getZScore(), TOL);
        assertEquals(0.7478029513332196, z3.getPValue(), TOL);
        assertEquals(56.53812256656457, z3.getCILow(), TOL);
        assertEquals(85.86187743343541, z3.getCIHigh(), TOL);
        assertEquals(0.01, z3.getSl(), TOL);

        ZTestOneSample z4 = ZTestOneSample.test(NumericVar.copy(Double.NaN, Double.NaN), 0, 1);
        z4.printSummary();

        assertEquals(Double.NaN, z4.getSampleMean(), TOL);
        assertEquals(Double.NaN, z4.getZScore(), TOL);
        assertEquals(Double.NaN, z4.getPValue(), TOL);
        assertEquals(Double.NaN, z4.getCILow(), TOL);
        assertEquals(Double.NaN, z4.getCIHigh(), TOL);
        assertEquals(0.05, z4.getSl(), TOL);

    }

}
