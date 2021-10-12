/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.core.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.VarDouble;

public class TTestOneSampleTest {

    private static final double TOL = 1e-12;

    @Test
    void baseTest() {
        VarDouble x = VarDouble.copy(5, 5.5, 4.5, 5, 5, 6, 5, 5, 4.5, 5, 5, 4.5, 4.5, 5.5, 4, 5, 5, 5.5, 4.5, 5.5, 5, 5.5);

        TTestOneSample t1 = TTestOneSample.test(x, 4.7);

        assertEquals(4.7, t1.getMu(), TOL);
        assertEquals(0.05, t1.getSl(), TOL);
        assertEquals(HTest.Alternative.TWO_TAILS, t1.getAlt());

        assertEquals(Mean.of(x).value(), t1.getSampleMean(), TOL);
        assertEquals(x.size(), t1.getSampleSize());
        assertEquals(x.size() - 1, t1.getDegrees());

        assertEquals(Variance.of(x).sdValue(), t1.getSampleSd(), TOL);
        assertEquals(3.0397368307141313, t1.getT(), TOL);
        assertEquals(0.006228673742479382, t1.pValue(), TOL);
        assertEquals(4.794757181899943, t1.ciLow(), TOL);
        assertEquals(5.205242818100057, t1.ciHigh(), TOL);


        TTestOneSample t2 = TTestOneSample.test(Mean.of(x).value(), x.size(), Variance.of(x).sdValue(), 4.7);
        assertEquals(4.7, t2.getMu(), TOL);
        assertEquals(0.05, t2.getSl(), TOL);
        assertEquals(HTest.Alternative.TWO_TAILS, t1.getAlt());

        assertEquals(Mean.of(x).value(), t2.getSampleMean(), TOL);
        assertEquals(x.size(), t2.getSampleSize());
        assertEquals(x.size() - 1, t2.getDegrees());

        assertEquals(Variance.of(x).sdValue(), t2.getSampleSd(), TOL);
        assertEquals(3.0397368307141313, t2.getT(), TOL);
        assertEquals(0.006228673742479382, t2.pValue(), TOL);
        assertEquals(4.794757181899943, t2.ciLow(), TOL);
        assertEquals(5.205242818100057, t2.ciHigh(), TOL);

        TTestOneSample t3 = TTestOneSample.test(Mean.of(x).value(), x.size(), Variance.of(x).sdValue(), 4.7, 0.1, HTest.Alternative.GREATER_THAN);
        assertEquals(4.7, t3.getMu(), TOL);
        assertEquals(0.1, t3.getSl(), TOL);
        assertEquals(HTest.Alternative.GREATER_THAN, t3.getAlt());

        assertEquals(Mean.of(x).value(), t3.getSampleMean(), TOL);
        assertEquals(x.size(), t3.getSampleSize());
        assertEquals(x.size() - 1, t3.getDegrees());

        assertEquals(Variance.of(x).sdValue(), t3.getSampleSd(), TOL);
        assertEquals(3.0397368307141313, t3.getT(), TOL);
        assertEquals(0.0031143368712397423, t3.pValue(), TOL);
        assertEquals(4.869410944346031, t3.ciLow(), TOL);
        assertEquals(Double.POSITIVE_INFINITY, t3.ciHigh(), TOL);

        TTestOneSample t4 = TTestOneSample.test(Mean.of(x).value(), x.size(), Variance.of(x).sdValue(), 4.7, 0.1, HTest.Alternative.LESS_THAN);
        assertEquals(4.7, t4.getMu(), TOL);
        assertEquals(0.1, t4.getSl(), TOL);
        assertEquals(HTest.Alternative.LESS_THAN, t4.getAlt());

        assertEquals(Mean.of(x).value(), t4.getSampleMean(), TOL);
        assertEquals(x.size(), t4.getSampleSize());
        assertEquals(x.size() - 1, t4.getDegrees());

        assertEquals(Variance.of(x).sdValue(), t4.getSampleSd(), TOL);
        assertEquals(3.0397368307141313, t4.getT(), TOL);
        assertEquals(0.9968856631287603, t4.pValue(), TOL);
        assertEquals(Double.NEGATIVE_INFINITY, t4.ciLow(), TOL);
        assertEquals(5.130589055653969, t4.ciHigh(), TOL);


        TTestOneSample t5 = TTestOneSample.test(VarDouble.empty(), 4.7, 0.05, HTest.Alternative.TWO_TAILS);
        assertEquals(4.7, t5.getMu(), TOL);
        assertEquals(0.05, t5.getSl(), TOL);
        assertEquals(HTest.Alternative.TWO_TAILS, t5.getAlt());

        assertEquals(Double.NaN, t5.getSampleMean(), TOL);
        assertEquals(0, t5.getSampleSize());
        assertEquals(-1, t5.getDegrees());

        assertEquals(Double.NaN, t5.getSampleSd(), TOL);
        assertEquals(Double.NaN, t5.getT(), TOL);
        assertEquals(Double.NaN, t5.pValue(), TOL);
        assertEquals(Double.NaN, t5.ciLow(), TOL);
        assertEquals(Double.NaN, t5.ciHigh(), TOL);
    }
}
