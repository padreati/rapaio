package rapaio.data.unique;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rapaio.core.RandomSource;
import rapaio.core.distributions.DUniform;
import rapaio.data.Unique;
import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarLong;
import rapaio.data.VarNominal;
import rapaio.data.filter.VRefSort;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    public void testSortedUnsortedDouble() {
        Var x = VarDouble.from(100, DUniform.of(0, 10)::sampleNext);

        Unique unsorted = Unique.of(x, false);
        Unique sorted = Unique.of(x, true);

        assertTrue(sorted.isSorted());
        assertFalse(unsorted.isSorted());

        VarInt unsortedIds = unsorted.valueSortedIds();
        VarInt sortedIds = sorted.valueSortedIds();

        Var secondSorted = unsortedIds.fapply(VRefSort.from(unsortedIds.refComparator()));

        assertFalse(unsortedIds.deepEquals(secondSorted));
        assertTrue(sortedIds.deepEquals(secondSorted));
    }

    @Test
    public void testSortedUnsortedInt() {
        VarInt x = VarInt.from(100, row -> RandomSource.nextInt(10000));

        Unique unsorted = Unique.of(x, false);
        Unique sorted = Unique.of(x, true);

        VarInt unsortedIds = unsorted.valueSortedIds();
        VarInt sortedIds = sorted.valueSortedIds();

        Var secondSorted = unsortedIds.fapply(VRefSort.from(unsortedIds.refComparator()));

        assertFalse(unsortedIds.deepEquals(secondSorted));
        assertTrue(sortedIds.deepEquals(secondSorted));
    }

    @Test
    public void testSortedUnsortedBinary() {
        Var x = VarBinary.from(100, row -> {
            int v = RandomSource.nextInt(3);
            if (v == 0) {
                return null;
            }
            return v == 1;
        });

        Unique unsorted = Unique.of(x, false);
        Unique sorted = Unique.of(x, true);

        VarInt unsortedIds = unsorted.valueSortedIds();
        VarInt sortedIds = sorted.valueSortedIds();

        Var secondSorted = unsortedIds.fapply(VRefSort.from(unsortedIds.refComparator()));

        assertFalse(unsortedIds.deepEquals(secondSorted));
        assertTrue(sortedIds.deepEquals(secondSorted));
    }

    @Test
    public void testSortedUnsortedLabel() {
        Var x = VarNominal.from(100, row -> {
            int len = RandomSource.nextInt(3);
            if (len == 0) {
                return "?";
            }
            char[] chars = new char[len];
            for (int i = 0; i < len; i++) {
                chars[i] = (char) ('a' + RandomSource.nextInt(3));
            }
            return String.valueOf(chars);
        });

        Unique unsorted = Unique.of(x, false);
        Unique sorted = Unique.of(x, true);

        VarInt unsortedIds = unsorted.valueSortedIds();
        VarInt sortedIds = sorted.valueSortedIds();

        Var secondSorted = unsortedIds.fapply(VRefSort.from(unsortedIds.refComparator()));

        assertFalse(unsortedIds.deepEquals(secondSorted));
        assertTrue(sortedIds.deepEquals(secondSorted));
    }

    @Test
    public void testUnimplemented() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot build unique structure for given type: not implemented.");
        Unique.of(VarLong.copy(1, 2, 3, 4), false);
    }
}
