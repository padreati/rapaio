package rapaio.data.unique;

import it.unimi.dsi.fastutil.ints.IntList;
import org.junit.Before;
import org.junit.Test;
import rapaio.core.*;
import rapaio.data.*;
import rapaio.data.stream.*;
import rapaio.sys.*;

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

    @Test
    public void testString() {

        int oldTextWidth = WS.getPrinter().textWidth();
        WS.getPrinter().withTextWidth(100);
        String[] sample = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
                "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26",
                "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40",
                "41", "42", "?"};
        final int N = 200;
        VarNominal x1 = VarNominal.from(N, row -> sample[RandomSource.nextInt(sample.length)]);
        String[] values1 = x1.stream().map(VSpot::getLabel).toArray(String[]::new);
        Arrays.sort(values1);
        UniqueLabel ui1 = UniqueLabel.of(x1, true);

        assertEquals("UniqueLabel{count=43, values=[?:6,1:3,10:6,11:3,12:4,13:4,14:8,15:4,16:7,17:4,..]}", ui1.toString());
        assertEquals("Value Count Value Count Value Count Value Count Value Count Value Count \n" +
                "    ?     6    15     4    21     3    28     8    34     3     5     6 \n" +
                "    1     3    16     7    22     5    29     3    35     6     6     4 \n" +
                "   10     6    17     4    23     5     3     5  ...   ...      7     5 \n" +
                "   11     3    18     3    24     2    30     5     4     6     8     3 \n" +
                "   12     4    19     5    25     3    31     4    40     3     9     1 \n" +
                "   13     4     2     2    26     5    32     4    41     7 \n" +
                "   14     8    20    11    27     8    33     5    42     3 \n", ui1.content());
        assertEquals("Value Count Value Count Value Count Value Count Value Count Value Count \n" +
                "    ?     6    16     7    23     5    30     5    38     4     7     5 \n" +
                "    1     3    17     4    24     2    31     4    39     4     8     3 \n" +
                "   10     6    18     3    25     3    32     4     4     6     9     1 \n" +
                "   11     3    19     5    26     5    33     5    40     3 \n" +
                "   12     4     2     2    27     8    34     3    41     7 \n" +
                "   13     4    20    11    28     8    35     6    42     3 \n" +
                "   14     8    21     3    29     3    36     7     5     6 \n" +
                "   15     4    22     5     3     5    37     3     6     4 \n", ui1.fullContent());
        assertEquals(ui1.toString(), ui1.summary());

        VarNominal x2 = VarNominal.from(N, row -> sample[RandomSource.nextInt(5)]);
        String[] values2 = x2.stream().map(VSpot::getLabel).toArray(String[]::new);
        Arrays.sort(values2);
        UniqueLabel ui2 = UniqueLabel.of(x2, true);

        assertEquals("UniqueLabel{count=5, values=[1:43,2:50,3:50,4:31,5:26]}", ui2.toString());
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
