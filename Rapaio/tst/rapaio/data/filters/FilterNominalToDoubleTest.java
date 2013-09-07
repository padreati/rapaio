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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import static rapaio.core.BaseMath.pow;
import rapaio.data.IndexOneVector;
import rapaio.data.NominalVector;
import rapaio.data.Vector;
import static rapaio.filters.BaseFilters.toValue;
import static rapaio.filters.RowFilters.sort;

import java.util.Arrays;
import java.util.HashSet;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FilterNominalToDoubleTest {

    @Test
    public void testNormalCase() {
        int n = 10;
        HashSet<String> dict = new HashSet<>();
        for (int i = 0; i < n; i++) {
            dict.add(String.valueOf(pow(i, 1.5)));
        }
        Vector v = new NominalVector("test", 10, dict);
        for (int i = 0; i < v.getRowCount(); i++) {
            String value = String.valueOf(pow(i, 1.5));
            v.setLabel(i, value);
        }
        Vector filtered = toValue(v);
        for (int i = 0; i < v.getRowCount(); i++) {
            double value = pow(i, 1.5);
            assertEquals(value, filtered.getValue(i), 1e-10);
        }
    }

    @Test
    public void testNullVector() {
        try {
            sort(null);
        } catch (Exception ex) {
            assertTrue(true);
        }
    }

    @Test
    public void testNFE() {
        Vector filtered = new NominalVector("test", 1, Arrays.asList(new String[]{"abc"}));
        filtered.setLabel(0, "abc");
        Vector numeric = toValue(filtered);
        assertEquals(numeric.getValue(0), numeric.getValue(0), 1e-10);
        assertTrue(numeric.isMissing(0));
    }

    @Test
    public void testNotNominal() {
        Vector filtered = new IndexOneVector(0);
        try {
            toValue(filtered);
        } catch (IllegalArgumentException ex) {
            assertTrue(true);
            return;
        }
        assertTrue(false);
    }
}
