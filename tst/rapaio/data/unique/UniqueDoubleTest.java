package rapaio.data.unique;

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Before;
import org.junit.Test;
import rapaio.core.*;
import rapaio.data.*;
import rapaio.sys.*;

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
    public void testRandomUnsorted() {
        final int N = 100;
        double[] values = new double[N];
        for (int i = 0; i < N; i++) {
            values[i] = RandomSource.nextDouble();
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

        UniqueDouble unique = UniqueDouble.of(x, false);
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

    @Test
    public void testString() {

        int oldTextWidth = WS.getPrinter().textWidth();
        WS.getPrinter().withTextWidth(100);
        double[] sample = new double[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, Double.NaN};
        final int N = 200;
        VarDouble x1 = VarDouble.from(N, row -> sample[RandomSource.nextInt(sample.length)]/3.3);
        double[] values1 = x1.stream().mapToDouble().toArray();
        DoubleArrays.quickSort(values1);
        UniqueDouble ui1 = UniqueDouble.of(x1, true);

        assertEquals("UniqueDouble{count=43, values=[?:6,0.3030303:3,0.6060606:2,0.9090909:5,1.2121212:6,1.5151515:6,1.8181818:4,2.1212121:5,2.4242424:3,2.7272727:1,..]}",
                ui1.toString());
        assertEquals("  Value    Count   Value    Count   Value    Count   Value    Count   Value    Count   Value    Count \n" +
                "         ?     6  2.1212121     5  4.2424242     8  6.3636364     3  8.4848485     8 11.5151515     4 \n" +
                " 0.3030303     3  2.4242424     3  4.5454545     4  6.6666667     5  8.7878788     3 11.8181818     4 \n" +
                " 0.6060606     2  2.7272727     1  4.8484848     7   6.969697     5    ...      ...  12.1212121     3 \n" +
                " 0.9090909     5   3.030303     6  5.1515152     4  7.2727273     2 10.3030303     3 12.4242424     7 \n" +
                " 1.2121212     6  3.3333333     3  5.4545455     3  7.5757576     3 10.6060606     6 12.7272727     3 \n" +
                " 1.5151515     6  3.6363636     4  5.7575758     5  7.8787879     5 10.9090909     7 \n" +
                " 1.8181818     4  3.9393939     4  6.0606061    11  8.1818182     8 11.2121212     3 \n", ui1.content());
        assertEquals("  Value    Count   Value    Count   Value    Count   Value    Count   Value    Count   Value    Count \n" +
                "         ?     6  2.4242424     3  4.8484848     7  7.2727273     2  9.6969697     4 12.1212121     3 \n" +
                " 0.3030303     3  2.7272727     1  5.1515152     4  7.5757576     3         10     5 12.4242424     7 \n" +
                " 0.6060606     2   3.030303     6  5.4545455     3  7.8787879     5 10.3030303     3 12.7272727     3 \n" +
                " 0.9090909     5  3.3333333     3  5.7575758     5  8.1818182     8 10.6060606     6 \n" +
                " 1.2121212     6  3.6363636     4  6.0606061    11  8.4848485     8 10.9090909     7 \n" +
                " 1.5151515     6  3.9393939     4  6.3636364     3  8.7878788     3 11.2121212     3 \n" +
                " 1.8181818     4  4.2424242     8  6.6666667     5  9.0909091     5 11.5151515     4 \n" +
                " 2.1212121     5  4.5454545     4   6.969697     5  9.3939394     4 11.8181818     4 \n", ui1.fullContent());
        assertEquals(ui1.toString(), ui1.summary());

        VarDouble x2 = VarDouble.from(N, row -> sample[RandomSource.nextInt(5)]/3.3);
        double[] values2 = x2.stream().mapToDouble().toArray();
        DoubleArrays.quickSort(values2);
        UniqueDouble ui2 = UniqueDouble.of(x2, true);

        assertEquals("UniqueDouble{count=5, values=[0.3030303:43,0.6060606:50,0.9090909:50,1.2121212:31,1.5151515:26]}", ui2.toString());
        assertEquals("  Value   Count   Value   Count \n" +
                "0.3030303    43 1.2121212    31 \n" +
                "0.6060606    50 1.5151515    26 \n" +
                "0.9090909    50 \n", ui2.content());
        assertEquals("  Value   Count   Value   Count \n" +
                "0.3030303    43 1.2121212    31 \n" +
                "0.6060606    50 1.5151515    26 \n" +
                "0.9090909    50 \n", ui2.fullContent());
        assertEquals(ui2.toString(), ui2.summary());
    }
}
