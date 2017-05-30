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

package rapaio.core.stat;

import org.junit.Test;
import rapaio.data.NumericVar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/25/14.
 */
public class WeightedMeanTest {

    @Test
    public void testBasic() {

        NumericVar values = NumericVar.copy(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        NumericVar weights = NumericVar.fill(10, 1);
        assertEquals(5.5, WeightedMean.from(values, weights).getValue(), 10e-12);

        weights = NumericVar.copy(1, 2, 1, 2, 1, 2, 1, 2, 1, 2);
        assertEquals(5.666666666666667, WeightedMean.from(values, weights).getValue(), 10e-12);
    }

    @Test
    public void testMissing() {

        NumericVar values = NumericVar.copy(Double.NaN, Double.NaN);
        NumericVar weights = NumericVar.copy(1, 1);

        assertTrue(Double.isNaN(WeightedMean.from(values, weights).getValue()));

        values = NumericVar.copy(1, 2);
        weights = NumericVar.copy(Double.NaN, Double.NaN);
        assertTrue(Double.isNaN(WeightedMean.from(values, weights).getValue()));

        values = NumericVar.copy(Double.NaN, 1, 2);
        weights = NumericVar.copy(1, 2, Double.NaN);
        assertEquals(1.0, WeightedMean.from(values, weights).getValue(), 10e-12);
    }

}
