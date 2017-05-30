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
import rapaio.data.NumericVar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/8/16.
 */
public class TTestTwoPairedTest {

    @Test
    public void basicTest() {

        final double TOL = 1e-12;

        NumericVar x = NumericVar.copy(18, 21, 16, 22, 19, 24, 17, 21, 23, 18, 14, 16, 16, 19, 18, 20, 12, 22, 15, 17);
        NumericVar y = NumericVar.copy(22, 25, 17, 24, 16, 29, 20, 23, 19, 20, 15, 15, 18, 26, 18, 24, 18, 25, 19, 16);

        TTestTwoPaired t1 = TTestTwoPaired.test(y, x, 0);
        t1.printSummary();

        assertEquals(0, t1.mu(), TOL);
        assertEquals(2.8372521918222215, t1.sd(), TOL);
        assertEquals(0.05, t1.sl(), TOL);
        assertEquals(HTest.Alternative.TWO_TAILS, t1.alt());

        assertEquals(2.05, t1.sampleMean(), TOL);
        assertEquals(3.2312526655803127, t1.t(), TOL);
        assertEquals(19, t1.df(), TOL);
        assertEquals(0.004394965993185667, t1.pValue(), TOL);
        assertEquals(0.7221250995807065, t1.ciLow(), TOL);
        assertEquals(3.377874900419294, t1.ciHigh(), TOL);
        assertEquals(0.05, t1.sl(), TOL);

        TTestTwoPaired t2 = TTestTwoPaired.test(NumericVar.empty(), NumericVar.empty(), -1, 0.03, HTest.Alternative.GREATER_THAN);
        assertTrue(Double.isNaN(t2.pValue()));
    }
}
