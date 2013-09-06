package incubator.rapaio.data.filters;

import org.junit.Test;
import rapaio.data.NominalVector;
import rapaio.data.NumericVector;
import rapaio.data.Vector;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static rapaio.core.BaseFilters.sort;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class FilterSortTest {

    @Test
    public void testValueVector() {
        Vector unsorted = new NumericVector("test", new double[]{0., 1., 2., 3., 4., 5., 6.});
        Vector sorted = sort(unsorted, true);
        for (int i = 1; i < sorted.getRowCount(); i++) {
            assertTrue(sorted.getValue(i - 1) <= sorted.getValue(i));
        }
    }

    @Test
    public void testValueVectorWithNA() {
        Vector unsorted = new NumericVector("test", new double[]{Double.NaN, 0., Double.NaN, 1., Double.NaN, 2.});
        Vector sorted = sort(unsorted);
        for (int i = 0; i < 3; i++) {
            assert (sorted.isMissing(i));
        }
    }

    @Test
    public void testNominalVector() {
        Vector unsorted = new NominalVector("test", 3, Arrays.asList(new String[]{"ana", "vasile", "ion"}));
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
        Vector unsorted = new NominalVector("test", 3, Arrays.asList(new String[]{"ana", "vasile", "ion"}));
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
