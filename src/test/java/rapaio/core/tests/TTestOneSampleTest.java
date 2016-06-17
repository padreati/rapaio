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
import rapaio.core.CoreTools;
import rapaio.data.Numeric;

import static org.junit.Assert.assertEquals;

public class TTestOneSampleTest {

    private static final double TOL = 1e-12;

    @Test
    public void baseTest() {
        Numeric x = Numeric.copy(5, 5.5, 4.5, 5, 5, 6, 5, 5, 4.5, 5, 5, 4.5, 4.5, 5.5, 4, 5, 5, 5.5, 4.5, 5.5, 5, 5.5);
        CoreTools.mean(x).printSummary();
        CoreTools.var(x).printSummary();

        TTestOneSample t1 = TTestOneSample.test(x, 4.7);
        t1.printSummary();

        assertEquals(4.7, t1.mu(), TOL);
        assertEquals(0.05, t1.sl(), TOL);
        assertEquals(HTest.Alternative.TWO_TAILS, t1.alt());

        assertEquals(CoreTools.mean(x).value(), t1.sampleMean(), TOL);
        assertEquals(x.rowCount(), t1.sampleSize());
        assertEquals(x.rowCount() - 1, t1.df());

        assertEquals(CoreTools.var(x).sdValue(), t1.sampleSd(), TOL);
        assertEquals(3.0397368307141313, t1.t(), TOL);
        assertEquals(0.006228673742479382, t1.pValue(), TOL);
        assertEquals(4.794757181899943, t1.ciLow(), TOL);
        assertEquals(5.205242818100057, t1.ciHigh(), TOL);


        TTestOneSample t2 = TTestOneSample.test(CoreTools.mean(x).value(), x.rowCount(), CoreTools.var(x).sdValue(), 4.7);
        assertEquals(4.7, t2.mu(), TOL);
        assertEquals(0.05, t2.sl(), TOL);
        assertEquals(HTest.Alternative.TWO_TAILS, t1.alt());

        assertEquals(CoreTools.mean(x).value(), t2.sampleMean(), TOL);
        assertEquals(x.rowCount(), t2.sampleSize());
        assertEquals(x.rowCount() - 1, t2.df());

        assertEquals(CoreTools.var(x).sdValue(), t2.sampleSd(), TOL);
        assertEquals(3.0397368307141313, t2.t(), TOL);
        assertEquals(0.006228673742479382, t2.pValue(), TOL);
        assertEquals(4.794757181899943, t2.ciLow(), TOL);
        assertEquals(5.205242818100057, t2.ciHigh(), TOL);

        TTestOneSample t3 = TTestOneSample.test(CoreTools.mean(x).value(), x.rowCount(), CoreTools.var(x).sdValue(), 4.7, 0.1, HTest.Alternative.GREATER_THAN);
        assertEquals(4.7, t3.mu(), TOL);
        assertEquals(0.1, t3.sl(), TOL);
        assertEquals(HTest.Alternative.GREATER_THAN, t3.alt());

        assertEquals(CoreTools.mean(x).value(), t3.sampleMean(), TOL);
        assertEquals(x.rowCount(), t3.sampleSize());
        assertEquals(x.rowCount() - 1, t3.df());

        assertEquals(CoreTools.var(x).sdValue(), t3.sampleSd(), TOL);
        assertEquals(3.0397368307141313, t3.t(), TOL);
        assertEquals(0.0031143368712397423, t3.pValue(), TOL);
        assertEquals(4.830175143575739, t3.ciLow(), TOL);
        assertEquals(5.169824856424261, t3.ciHigh(), TOL);

        TTestOneSample t4 = TTestOneSample.test(CoreTools.mean(x).value(), x.rowCount(), CoreTools.var(x).sdValue(), 4.7, 0.1, HTest.Alternative.LESS_THAN);
        assertEquals(4.7, t4.mu(), TOL);
        assertEquals(0.1, t4.sl(), TOL);
        assertEquals(HTest.Alternative.LESS_THAN, t4.alt());

        assertEquals(CoreTools.mean(x).value(), t4.sampleMean(), TOL);
        assertEquals(x.rowCount(), t4.sampleSize());
        assertEquals(x.rowCount() - 1, t4.df());

        assertEquals(CoreTools.var(x).sdValue(), t4.sampleSd(), TOL);
        assertEquals(3.0397368307141313, t4.t(), TOL);
        assertEquals(0.9968856631287603, t4.pValue(), TOL);
        assertEquals(4.830175143575739, t4.ciLow(), TOL);
        assertEquals(5.169824856424261, t4.ciHigh(), TOL);


        TTestOneSample t5 = TTestOneSample.test(Numeric.empty(), 4.7, 0.05, HTest.Alternative.TWO_TAILS);
        assertEquals(4.7, t5.mu(), TOL);
        assertEquals(0.05, t5.sl(), TOL);
        assertEquals(HTest.Alternative.TWO_TAILS, t5.alt());

        assertEquals(Double.NaN, t5.sampleMean(), TOL);
        assertEquals(0, t5.sampleSize());
        assertEquals(-1, t5.df());

        assertEquals(Double.NaN, t5.sampleSd(), TOL);
        assertEquals(Double.NaN, t5.t(), TOL);
        assertEquals(Double.NaN, t5.pValue(), TOL);
        assertEquals(Double.NaN, t5.ciLow(), TOL);
        assertEquals(Double.NaN, t5.ciHigh(), TOL);




    }
}
