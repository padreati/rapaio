/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import rapaio.core.stat.Mean;
import rapaio.data.Var;
import rapaio.data.VarDouble;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/14/16.
 */
public class ZTestTwoSamplesTest {

    private static final double TOL = 1e-12;

    @Test
    void zTestTwoSamples() {
        Var x = VarDouble.copy(7.8, 6.6, 6.5, 7.4, 7.3, 7.0, 6.4, 7.1, 6.7, 7.6, 6.8);
        Var y = VarDouble.copy(4.5, 5.4, 6.1, 6.1, 5.4, 5., 4.1, 5.5);

        ZTestTwoSamples z1 = ZTestTwoSamples.test(x, y, 2, 0.5, 0.5, 0.05, HTest.Alternative.TWO_TAILS);
        assertEquals(1.7556818181818183, z1.getSampleMean(), TOL);
        assertEquals(7.0181818181818185, z1.getXSampleMean(), TOL);
        assertEquals(5.2625, z1.getYSampleMean(), TOL);

        assertEquals(-1.051599374295714, z1.getZScore(), TOL);
        assertEquals(0.2929833949856928, z1.pValue(), TOL);
        assertEquals(1.3003232007875778, z1.ciLow(), TOL);
        assertEquals(2.211040435576059, z1.ciHigh(), TOL);


        z1 = ZTestTwoSamples.test(Mean.of(x).value(), x.size(), Mean.of(y).value(), y.size(), 2, 0.5, 0.5, 0.05, HTest.Alternative.TWO_TAILS);
        assertEquals(1.7556818181818183, z1.getSampleMean(), TOL);
        assertEquals(7.0181818181818185, z1.getXSampleMean(), TOL);
        assertEquals(5.2625, z1.getYSampleMean(), TOL);

        assertEquals(-1.051599374295714, z1.getZScore(), TOL);
        assertEquals(0.2929833949856928, z1.pValue(), TOL);
        assertEquals(1.3003232007875778, z1.ciLow(), TOL);
        assertEquals(2.211040435576059, z1.ciHigh(), TOL);

        ZTestTwoSamples z2 = ZTestTwoSamples.test(x, y, 0, 0.5, 0.6, 0.10, HTest.Alternative.TWO_TAILS);

        assertEquals(6.7462746482071205, z2.getZScore(), TOL);
        assertEquals(1.5169976386175676E-11, z2.pValue(), TOL);
        assertEquals(1.3276174779349252, z2.ciLow(), TOL);
        assertEquals(2.1837461584287112, z2.ciHigh(), TOL);

        ZTestTwoSamples z3 = ZTestTwoSamples.test(x, VarDouble.empty(), 0, 0.5, 0.5);

        assertEquals(Double.NaN, z3.getZScore(), TOL);
        assertEquals(Double.NaN, z3.pValue(), TOL);
        assertEquals(Double.NaN, z3.ciLow(), TOL);
        assertEquals(Double.NaN, z3.ciHigh(), TOL);

        ZTestTwoSamples z4 = ZTestTwoSamples.test(x, y, 2, 0.5, 0.5, 0.05, HTest.Alternative.GREATER_THAN);
        assertEquals(-1.051599374295714, z4.getZScore(), TOL);
        assertEquals(0.8535083025071536, z4.pValue(), TOL);
        assertEquals(1.3003232007875778, z4.ciLow(), TOL);
        assertEquals(2.211040435576059, z4.ciHigh(), TOL);


        ZTestTwoSamples z5 = ZTestTwoSamples.test(x, y, 2, 0.5, 0.5, 0.05, HTest.Alternative.LESS_THAN);
        assertEquals(-1.051599374295714, z5.getZScore(), TOL);
        assertEquals(0.1464916974928464, z5.pValue(), TOL);
        assertEquals(1.3003232007875778, z5.ciLow(), TOL);
        assertEquals(2.211040435576059, z5.ciHigh(), TOL);
    }
}
