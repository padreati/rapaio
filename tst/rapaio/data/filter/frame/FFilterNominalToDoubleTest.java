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

package rapaio.data.filter.frame;

import org.junit.Test;
import rapaio.data.NominalVar;
import rapaio.data.Var;
import rapaio.data.filter.var.VFToNumeric;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class FFilterNominalToDoubleTest {

    @Test
    public void testNormalCase() {
        int n = 10;
        ArrayList<String> dict = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            dict.add(String.valueOf(Math.pow(i, 1.5)));
        }
        Var v = NominalVar.empty(10, dict);
        for (int i = 0; i < v.getRowCount(); i++) {
            String value = String.valueOf(Math.pow(i, 1.5));
            v.setLabel(i, value);
        }
        Var filtered = VFToNumeric.byDefault().fitApply(v);
        for (int i = 0; i < v.getRowCount(); i++) {
            double value = Math.pow(i, 1.5);
            assertEquals(value, filtered.getValue(i), 1e-10);
        }
    }

    @Test
    public void testNullVector() {
        try {
            new FFRefSort(null).fitApply(null);
        } catch (Exception ex) {
            assertTrue(true);
        }
    }

    @Test
    public void testNFE() {
        Var filtered = NominalVar.empty(1, "abc");
        filtered.setLabel(0, "abc");
        Var numeric = VFToNumeric.byDefault().fitApply(filtered);
        assertEquals(numeric.getValue(0), numeric.getValue(0), 1e-10);
        assertTrue(numeric.isMissing(0));
    }
}
