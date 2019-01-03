package rapaio.data.unique;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueIntTest {

    @Before
    public void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    public void testRandomUnsorted() {
        final int N = 100;
        int[] values = new int[N];
        for (int i = 0; i < N; i++) {
            values[i] = RandomSource.nextInt(1_000_000);
        }
        VarDouble x = VarDouble.copy(values);
        Arrays.sort(values);

        UniqueDouble unique = UniqueDouble.of(x, false);
        assertEquals(N, unique.uniqueCount());

        IntList valueSortedIds = unique.valueSortedIds();
        for (int i = 0; i < valueSortedIds.size(); i++) {
            assertEquals(0, Double.compare(values[i], unique.uniqueValue(valueSortedIds.getInt(i))));
        }
        IntList valueSortedIds2 = unique.valueSortedIds();
        for (int i = 0; i < valueSortedIds2.size(); i++) {
            assertEquals(0, Double.compare(values[i], unique.uniqueValue(valueSortedIds2.getInt(i))));
        }

        for (int id : unique.countSortedIds()) {
            assertEquals(1, unique.rowList(id).size());
        }
        for (int id : unique.countSortedIds()) {
            assertEquals(1, unique.rowList(id).size());
        }
    }

    @Test
    public void testDuplicatesUnsorted() {
        int[] sample = new int[]{3, 1, 7, 5, Integer.MIN_VALUE};
        final int N = 100;
        int[] values = new int[N];
        for (int i = 0; i < N; i++) {
            values[i] = sample[RandomSource.nextInt(sample.length)];
        }
        VarInt x = VarInt.copy(values);
        IntArrays.quickSort(values, Integer::compare);

        UniqueInt unique = UniqueInt.of(x, false);
        assertEquals(sample.length, unique.uniqueCount());

        IntList valueSortedIds = unique.valueSortedIds();
        int[] sortedValues = valueSortedIds.stream().map(unique::uniqueValue).mapToInt(v -> v).toArray();
        for (int i = 0; i < sample.length; i++) {
            assertEquals(sortedValues[i], (int)unique.uniqueValue(valueSortedIds.getInt(i)));
        }
        IntList valueSortedIds2 = unique.valueSortedIds();
        for (int i = 0; i < valueSortedIds2.size(); i++) {
            assertEquals(sortedValues[i], (int)unique.uniqueValue(valueSortedIds2.getInt(i)));
        }

        int[] counts = unique.countSortedIds().stream().mapToInt(id -> unique.rowList(id).size()).toArray();
        for (int i = 1; i < counts.length; i++) {
            assertTrue(counts[i - 1] <= counts[i]);
        }
        int[] counts2 = unique.countSortedIds().stream().mapToInt(id -> unique.rowList(id).size()).toArray();
        for (int i = 1; i < counts2.length; i++) {
            assertTrue(counts2[i - 1] <= counts2[i]);
        }
    }
}
