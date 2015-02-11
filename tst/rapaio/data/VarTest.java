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
 */

package rapaio.data;

import org.junit.Test;
import rapaio.data.filter.var.VFSort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class VarTest {

    @Test
    public void testDictionary() {
        Var x = Nominal.newCopyOf("x", "y", "x", "z");
        Var y = Nominal.newCopyOf("x", "y", "x", "z");

        x.setDictionary("a", "b", "c");
        List<String> dict = new ArrayList<>();
        dict.add("a");
        dict.add("b");
        dict.add("c");
        y.setDictionary(dict);

        assertEquals(4, x.rowCount());
        assertEquals(4, y.rowCount());

        for (int i = 0; i < 4; i++) {
            assertEquals(x.label(i), y.label(i));
        }
    }

    @Test
    public void testNumericCollector() {
        Var x = Numeric.newWrapOf(1, 2, 3, 4, 5);
        Var y = Arrays.stream(new double[]{1, 2, 3, 4, 5}).mapToObj(d -> d).parallel().collect(Var.numericCollector());
        y = new VFSort().fitApply(y);

        assertEquals(5, x.rowCount());
        assertEquals(5, y.rowCount());

        for (int i = 0; i < 5; i++) {
            assertEquals(x.value(i), y.value(i), 1e-12);
        }
    }

    @Test
    public void testIndexCollector() {
        Var x = Index.newWrapOf(1, 2, 3, 4, 5);
        Var y = Arrays.stream(new int[]{1, 2, 3, 4, 5}).mapToObj(d -> d).parallel().collect(Var.indexCollector());
        y = new VFSort().fitApply(y);

        assertEquals(5, x.rowCount());
        assertEquals(5, y.rowCount());

        for (int i = 0; i < 5; i++) {
            assertEquals(x.value(i), y.value(i), 1e-12);
        }
    }
}
