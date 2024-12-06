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

package rapaio.core.stat;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import rapaio.data.VarDouble;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/18.
 */
public class GeometricMeanTest {

    private static final double TOL = 1e-12;

    @Test
    void testDouble() {
        VarDouble x = VarDouble.wrap(2, 2, 2, 2);
        GeometricMean mean = GeometricMean.of(x);
        assertEquals(2, mean.value(), TOL);

        assertEquals("""
                > geometricMean[?]
                total rows: 4 (complete: 4, missing: 0, negative values: 0)
                mean: 2
                """, mean.toSummary());
        assertEquals(4, GeometricMean.of(VarDouble.wrap(2, 4, 8)).value(), TOL);
        assertEquals(Double.NaN, GeometricMean.of(VarDouble.wrap(Double.NaN)).value(), TOL);
        assertFalse(GeometricMean.of(VarDouble.wrap(-1)).isDefined());
    }
}
