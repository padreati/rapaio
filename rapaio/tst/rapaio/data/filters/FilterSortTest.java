/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
import rapaio.data.NomVector;
import rapaio.data.NumVector;
import rapaio.data.Vector;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static rapaio.filters.RowFilters.sort;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FilterSortTest {

    @Test
    public void testValueVector() {
        Vector unsorted = new NumVector(new double[]{0., 1., 2., 3., 4., 5., 6.});
        Vector sorted = sort(unsorted, true);
        for (int i = 1; i < sorted.getRowCount(); i++) {
            assertTrue(sorted.getValue(i - 1) <= sorted.getValue(i));
        }
    }

    @Test
    public void testValueVectorWithNA() {
        Vector unsorted = new NumVector(new double[]{Double.NaN, 0., Double.NaN, 1., Double.NaN, 2.});
        Vector sorted = sort(unsorted);
        for (int i = 0; i < 3; i++) {
            assert (sorted.isMissing(i));
        }
    }

    @Test
    public void testNominalVector() {
        Vector unsorted = new NomVector(3, Arrays.asList(new String[]{"ana", "vasile", "ion"}));
        unsorted.setLabel(0, "ana");
        unsorted.setLabel(1, "vasile");
        unsorted.setLabel(2, "ion");

        Vector sorted = sort(unsorted);
        assertEquals(sorted.getRowCount(), unsorted.getRowCount());
        assertEquals("ana", sorted.getLabel(0));
        assertEquals("ion", sorted.getLabel(1));
        assertEquals("vasile", sorted.getLabel(2));

        sorted = sort(unsorted, true);
        assertEquals(sorted.getRowCount(), unsorted.getRowCount());
        assertEquals("ana", sorted.getLabel(0));
        assertEquals("ion", sorted.getLabel(1));
        assertEquals("vasile", sorted.getLabel(2));

        sorted = sort(unsorted, false);
        assertEquals(sorted.getRowCount(), unsorted.getRowCount());
        assertEquals("vasile", sorted.getLabel(0));
        assertEquals("ion", sorted.getLabel(1));
        assertEquals("ana", sorted.getLabel(2));
    }

    @Test
    public void testNominalVectorWithNA() {
        Vector unsorted = new NomVector(3, Arrays.asList(new String[]{"ana", "vasile", "ion"}));
        unsorted.setLabel(0, "ana");
        unsorted.setLabel(1, "vasile");
        unsorted.setLabel(2, "?");

        Vector sorted = sort(unsorted);
        assertEquals(sorted.getRowCount(), unsorted.getRowCount());
        assertEquals("?", sorted.getLabel(0));
        assertEquals("ana", sorted.getLabel(1));
        assertEquals("vasile", sorted.getLabel(2));

        sorted = sort(unsorted, false);
        assertEquals(sorted.getRowCount(), unsorted.getRowCount());
        assertEquals("vasile", sorted.getLabel(0));
        assertEquals("ana", sorted.getLabel(1));
        assertEquals("?", sorted.getLabel(2));
    }
}
