/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
import rapaio.data.Numeric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a> on 11/25/14.
 */
@Deprecated
public class WeightedMeanTest {

    @Test
    public void testBasic() {

        Numeric values = Numeric.newCopyOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        Numeric weights = Numeric.newFill(10, 1);
        assertEquals(5.5, new WeightedMean(values, weights).value(), 10e-12);

        weights = Numeric.newCopyOf(1, 2, 1, 2, 1, 2, 1, 2, 1, 2);
        assertEquals(5.666666666666667, new WeightedMean(values, weights).value(), 10e-12);
    }

    @Test
    public void testMissing() {

        Numeric values = Numeric.newCopyOf(Double.NaN, Double.NaN);
        Numeric weights = Numeric.newCopyOf(1, 1);

        assertTrue(Double.isNaN(new WeightedMean(values, weights).value()));

        values = Numeric.newCopyOf(1, 2);
        weights = Numeric.newCopyOf(Double.NaN, Double.NaN);
        assertTrue(Double.isNaN(new WeightedMean(values, weights).value()));

        values = Numeric.newCopyOf(Double.NaN, 1, 2);
        weights = Numeric.newCopyOf(1, 2, Double.NaN);
        assertEquals(1.0, new WeightedMean(values, weights).value(), 10e-12);
    }

}
