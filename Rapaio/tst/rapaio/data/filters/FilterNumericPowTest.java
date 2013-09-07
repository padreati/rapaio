/*
 * Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.data.filters;

import org.junit.Test;
import rapaio.data.NominalVector;
import rapaio.data.NumericVector;
import rapaio.data.Vector;
import rapaio.filters.FilterNumericPow;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FilterNumericPowTest {

    @Test
    public void testNormalCase() {
        Vector before = new NumericVector("test", new double[]{0, -1, 2, -3, 4});
        check(before, 1, 0, new double[]{0, -1, 2, -3, 4});
        check(before, 1, 1, new double[]{1, 0, 3, -2, 5});
        check(before, 2, 0, new double[]{0, 1, 4, 9, 16});
        check(before, 2, -1, new double[]{1, 4, 1, 16, 9});
        check(before, 3, 0, new double[]{0, -1, 8, -27, 64});
    }

    private void check(Vector v, double a, double b, double[] values) {
        Vector after = new FilterNumericPow().filter(v, a, b);
        if (b == 0) {
            after = new FilterNumericPow().filter(v, a);
        }
        assertEquals(after.getRowCount(), values.length);
        for (int i = 0; i < after.getRowCount(); i++) {
            assertEquals(after.getValue(i), values[i], 1e-10);
        }
    }

    @Test
    public void testNotNumeric() {
        Vector v = new NominalVector("test", 1, Arrays.asList(new String[]{}));
        try {
            new FilterNumericPow().filter(v, 0);
        } catch (IllegalArgumentException ex) {
            return;
        }
        assertFalse(true);
    }
}
