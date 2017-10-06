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
import rapaio.data.NomVar;
import rapaio.data.NumVar;
import rapaio.data.Var;
import rapaio.data.filter.var.VFSort;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class FFilterSortTest {

    @Test
    public void testValueVector() {
        Var unsorted = NumVar.copy(0., 1., 2., 3., 4., 5., 6.);
        Var sorted = new VFSort().fitApply(unsorted);
        for (int i = 1; i < sorted.rowCount(); i++) {
            assertTrue(sorted.value(i - 1) <= sorted.value(i));
        }
    }

    @Test
    public void testValueVectorWithNA() {
        Var unsorted = NumVar.copy(Double.NaN, 0., Double.NaN, 1., Double.NaN, 2.);
        Var sorted = new VFSort().fitApply(unsorted);
        for (int i = 0; i < 3; i++) {
            assert (sorted.isMissing(i));
        }
    }

    @Test
    public void testNominalVector() {
        Var unsorted = NomVar.empty(3, "ana", "vasile", "ion");
        unsorted.setLabel(0, "ana");
        unsorted.setLabel(1, "vasile");
        unsorted.setLabel(2, "ion");

        Var sorted = new VFSort().fitApply(unsorted);
        assertEquals(sorted.rowCount(), unsorted.rowCount());
        assertEquals("ana", sorted.label(0));
        assertEquals("ion", sorted.label(1));
        assertEquals("vasile", sorted.label(2));

        sorted = new VFSort().fitApply(unsorted);
        assertEquals(sorted.rowCount(), unsorted.rowCount());
        assertEquals("ana", sorted.label(0));
        assertEquals("ion", sorted.label(1));
        assertEquals("vasile", sorted.label(2));

        sorted = new VFSort(false).fitApply(unsorted);
        assertEquals(sorted.rowCount(), unsorted.rowCount());
        assertEquals("vasile", sorted.label(0));
        assertEquals("ion", sorted.label(1));
        assertEquals("ana", sorted.label(2));
    }

    @Test
    public void testNominalVectorWithNA() {
        Var unsorted = NomVar.empty(3, "ana", "vasile", "ion");
        unsorted.setLabel(0, "ana");
        unsorted.setLabel(1, "vasile");
        unsorted.setLabel(2, "?");

        Var sorted = new VFSort().fitApply(unsorted);
        assertEquals(sorted.rowCount(), unsorted.rowCount());
        assertEquals("?", sorted.label(0));
        assertEquals("ana", sorted.label(1));
        assertEquals("vasile", sorted.label(2));

        sorted = new VFSort(false).fitApply(unsorted);
        assertEquals(sorted.rowCount(), unsorted.rowCount());
        assertEquals("vasile", sorted.label(0));
        assertEquals("ana", sorted.label(1));
        assertEquals("?", sorted.label(2));
    }
}
