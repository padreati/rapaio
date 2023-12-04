/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.core.tests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import rapaio.data.Var;
import rapaio.data.VarDouble;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/14/16.
 */
public class ZTestTwoPairedTest {

    private static final double TOL = 1e-12;

    @Test
    void zTestTwoPairedTest() {
        Var x = VarDouble.copy(7.8, 6.6, 6.5, 7.4, 7.3, 7.0, 6.4, 7.1, 6.7, 7.6, 6.8);
        Var y = VarDouble.copy(4.5, 5.4, 6.1, 6.1, 5.4, 5., 4.1, 5.5);

        ZTestTwoPaired z1 = ZTestTwoPaired.test(x, y, 2, 0.5);

        assertEquals(2, z1.getMu(), TOL);
        assertEquals(0.5, z1.getSd(), TOL);
        assertEquals(0.05, z1.getSl(), TOL);
        assertEquals(HTest.Alternative.TWO_TAILS, z1.getAlt());

        assertEquals(1.75, z1.getSampleMean(), TOL);
        assertEquals(-1.4142135623730951, z1.getZScore(), TOL);
        assertEquals(0.15729920705028522, z1.pValue(), TOL);
        assertEquals(1.4035240439125807, z1.ciLow(), TOL);
        assertEquals(2.0964759560874193, z1.ciHigh(), TOL);
        assertEquals(0.05, z1.getSl(), TOL);

        ZTestTwoPaired z2 = ZTestTwoPaired.test(x, VarDouble.copy(Double.NaN, Double.NaN), 2, 0.5, 0.05, HTest.Alternative.LESS_THAN);
        assertEquals(Double.NaN, z2.getSampleMean(), TOL);
        assertEquals(Double.NaN, z2.getZScore(), TOL);
        assertEquals(Double.NaN, z2.pValue(), TOL);
        assertEquals(Double.NaN, z2.ciLow(), TOL);
        assertEquals(Double.NaN, z2.ciHigh(), TOL);
        assertEquals(0.05, z2.getSl(), TOL);

        ZTestTwoPaired z3 = ZTestTwoPaired.test(x, y, 2, 0.5, 0.05, HTest.Alternative.GREATER_THAN);
        assertEquals(1.75, z3.getSampleMean(), TOL);
        assertEquals(-1.4142135623730951, z3.getZScore(), TOL);
        assertEquals(0.9213503964748574, z3.pValue(), TOL);
        assertEquals(1.4035240439125807, z3.ciLow(), TOL);
        assertEquals(2.0964759560874193, z3.ciHigh(), TOL);
        assertEquals(0.05, z3.getSl(), TOL);

        ZTestTwoPaired z4 = ZTestTwoPaired.test(x, y, 2, 0.5, 0.05, HTest.Alternative.LESS_THAN);
        assertEquals(1.75, z4.getSampleMean(), TOL);
        assertEquals(-1.4142135623730951, z4.getZScore(), TOL);
        assertEquals(0.07864960352514261, z4.pValue(), TOL);
        assertEquals(1.4035240439125807, z4.ciLow(), TOL);
        assertEquals(2.0964759560874193, z4.ciHigh(), TOL);
        assertEquals(0.05, z4.getSl(), TOL);
    }
}
