package rapaio.data.unique;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.sys.*;

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
    public void testString() {

        int oldTextWidth = WS.getPrinter().textWidth();
        WS.getPrinter().withTextWidth(100);
        int[] sample = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, Integer.MIN_VALUE};
        final int N = 200;
        VarInt x1 = VarInt.from(N, row -> sample[RandomSource.nextInt(sample.length)]);
        int[] values1 = x1.stream().mapToInt().toArray();
        IntArrays.quickSort(values1);
        UniqueInt ui1 = UniqueInt.of(x1, true);

        assertEquals("UniqueInt{count=43, values=[?:6,1:3,2:2,3:5,4:6,5:6,6:4,7:5,8:3,9:1,..]}", ui1.toString());
        assertEquals("Value Count Value Count Value Count Value Count Value Count Value Count \n" +
                "    ?     6     7     5    14     8    21     3    28     8    38     4 \n" +
                "    1     3     8     3    15     4    22     5    29     3    39     4 \n" +
                "    2     2     9     1    16     7    23     5  ...   ...     40     3 \n" +
                "    3     5    10     6    17     4    24     2    34     3    41     7 \n" +
                "    4     6    11     3    18     3    25     3    35     6    42     3 \n" +
                "    5     6    12     4    19     5    26     5    36     7 \n" +
                "    6     4    13     4    20    11    27     8    37     3 \n", ui1.content());
        assertEquals("Value Count Value Count Value Count Value Count Value Count Value Count \n" +
                "    ?     6     8     3    16     7    24     2    32     4    40     3 \n" +
                "    1     3     9     1    17     4    25     3    33     5    41     7 \n" +
                "    2     2    10     6    18     3    26     5    34     3    42     3 \n" +
                "    3     5    11     3    19     5    27     8    35     6 \n" +
                "    4     6    12     4    20    11    28     8    36     7 \n" +
                "    5     6    13     4    21     3    29     3    37     3 \n" +
                "    6     4    14     8    22     5    30     5    38     4 \n" +
                "    7     5    15     4    23     5    31     4    39     4 \n", ui1.fullContent());
        assertEquals(ui1.toString(), ui1.summary());

        VarInt x2 = VarInt.from(N, row -> sample[RandomSource.nextInt(5)]);
        int[] values2 = x2.stream().mapToInt().toArray();
        IntArrays.quickSort(values2);
        UniqueInt ui2 = UniqueInt.of(x2, true);

        assertEquals("UniqueInt{count=5, values=[1:43,2:50,3:50,4:31,5:26]}", ui2.toString());
        assertEquals("Value Count Value Count \n" +
                "    1    43     4    31 \n" +
                "    2    50     5    26 \n" +
                "    3    50 \n", ui2.content());
        assertEquals("Value Count Value Count \n" +
                "    1    43     4    31 \n" +
                "    2    50     5    26 \n" +
                "    3    50 \n", ui2.fullContent());
        assertEquals(ui2.toString(), ui2.summary());
    }
}
