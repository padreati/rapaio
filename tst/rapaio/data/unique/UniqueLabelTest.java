package rapaio.data.unique;

import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarNominal;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueLabelTest {

    @Before
    public void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    public void testRandomUnsorted() {
        final int N = 100;
        String[] values = new String[N];
        for (int i = 0; i < N; i++) {
            values[i] = String.valueOf(RandomSource.nextInt(1_000_000));
        }
        VarNominal x = VarNominal.copy(values);
        Arrays.sort(values, (s1, s2) -> {
            int cmp = s1.compareTo(s2);
            if (cmp == 0) return 0;
            if ("?".equals(s1)) return -1;
            if ("?".equals(s2)) return 1;
            return cmp;
        });

        UniqueLabel unique = UniqueLabel.of(x, false);
        assertEquals(N, unique.uniqueCount());

        IntList valueSortedIds = unique.valueSortedIds();
        for (int i = 0; i < valueSortedIds.size(); i++) {
            assertEquals(values[i], unique.uniqueValue(valueSortedIds.getInt(i)));
        }
        IntList valueSortedIds2 = unique.valueSortedIds();
        for (int i = 0; i < valueSortedIds2.size(); i++) {
            assertEquals(values[i], unique.uniqueValue(valueSortedIds2.getInt(i)));
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
        String[] sample = new String[]{"3", "1", "7", "5", "?"};
        final int N = 100;
        String[] values = new String[N];
        for (int i = 0; i < N; i++) {
            values[i] = sample[RandomSource.nextInt(sample.length)];
        }
        VarNominal x = VarNominal.copy(values);
        Arrays.sort(values, (s1, s2) -> {
            int cmp = s1.compareTo(s2);
            if (cmp == 0) return 0;
            if ("?".equals(s1)) return -1;
            if ("?".equals(s2)) return 1;
            return cmp;
        });

        UniqueInt unique = UniqueInt.of(x, false);
        assertEquals(sample.length, unique.uniqueCount());

        IntList valueSortedIds = unique.valueSortedIds();
        int[] sortedValues = valueSortedIds.stream().map(unique::uniqueValue).mapToInt(v -> v).toArray();
        for (int i = 0; i < sample.length; i++) {
            assertEquals(sortedValues[i], (int) unique.uniqueValue(valueSortedIds.getInt(i)));
        }
        IntList valueSortedIds2 = unique.valueSortedIds();
        for (int i = 0; i < valueSortedIds2.size(); i++) {
            assertEquals(sortedValues[i], (int) unique.uniqueValue(valueSortedIds2.getInt(i)));
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

    @Test
    public void testIdsByRowUnsorted() {
        VarNominal x = VarNominal.copy("a", "b", "c", "d", "e");
        Unique unique = Unique.of(x, false);
        for (int i = 0; i < x.rowCount(); i++) {
            assertEquals(i, unique.idByRow(i));
        }
    }
}
