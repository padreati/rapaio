package rapaio.data.unique;

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueDoubleTest {

    private static final double TOL = 1e-20;

    @Before
    public void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    public void testRandom() {
        final int N = 100;
        double[] values = new double[N];
        for (int i = 0; i < N; i++) {
            values[i] = RandomSource.nextDouble();
        }
        VarDouble x = VarDouble.copy(values);
        Arrays.sort(values);

        UniqueDouble unique = UniqueDouble.of(x);
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
    public void testDuplicates() {
        double[] sample = new double[]{3.4, 1.2, 7.8, 5.6, Double.NaN};
        final int N = 100;
        double[] values = new double[N];
        for (int i = 0; i < N; i++) {
            values[i] = sample[RandomSource.nextInt(sample.length)];
        }
        VarDouble x = VarDouble.copy(values);
        DoubleArrays.quickSort(values, (u, v) -> {
            int cmp = Double.compare(u, v);
            if (cmp == 0) {
                return 0;
            }
            if (Double.isNaN(u)) return -1;
            if (Double.isNaN(v)) return 1;
            return cmp;
        });

        UniqueDouble unique = UniqueDouble.of(x);
        assertEquals(sample.length, unique.uniqueCount());

        IntList valueSortedIds = unique.valueSortedIds();
        double[] sortedValues = valueSortedIds.stream().map(unique::uniqueValue).mapToDouble(v -> v).toArray();
        for (int i = 0; i < sample.length; i++) {
            assertEquals(sortedValues[i], unique.uniqueValue(valueSortedIds.getInt(i)), TOL);
        }
        IntList valueSortedIds2 = unique.valueSortedIds();
        for (int i = 0; i < valueSortedIds2.size(); i++) {
            assertEquals(sortedValues[i], unique.uniqueValue(valueSortedIds2.getInt(i)), TOL);
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
