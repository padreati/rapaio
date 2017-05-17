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

package rapaio.data;

import org.junit.Test;
import rapaio.data.filter.var.VFSort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class VarTest {

    @Test
    public void testDictionary() {
        Var x = NominalVar.copy("x", "y", "x", "z");
        Var y = NominalVar.copy("x", "y", "x", "z");

        x.setLevels("a", "b", "c");
        List<String> dict = new ArrayList<>();
        dict.add("a");
        dict.add("b");
        dict.add("c");
        y.setLevels(dict);

        assertEquals(4, x.getRowCount());
        assertEquals(4, y.getRowCount());

        for (int i = 0; i < 4; i++) {
            assertEquals(x.getLabel(i), y.getLabel(i));
        }
    }

    @Test
    public void testNumericCollector() {
        double[] src = IntStream.range(0, 100_000).mapToDouble(x -> x).toArray();
        Var x = NumericVar.wrap(src);
        Var y = Arrays.stream(src).boxed().parallel().collect(NumericVar.collector());
        y = new VFSort().fitApply(y);

        assertTrue(x.deepEquals(y));
    }

    @Test
    public void testIndexCollector() {
        int[] src = IntStream.range(0, 100_000).toArray();
        Var x = IndexVar.wrap(src);
        Var y = Arrays.stream(src).boxed().parallel().collect(IndexVar.collector());
        y = new VFSort().fitApply(y);

        assertTrue(x.deepEquals(y));
    }
}
