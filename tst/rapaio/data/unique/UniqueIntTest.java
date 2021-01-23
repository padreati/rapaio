/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.data.unique;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.sys.WS;
import rapaio.util.collection.IntArrays;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static rapaio.printer.Printer.textWidth;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueIntTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void testRandomUnsorted() {
        final int N = 100;
        int[] values = new int[N];
        for (int i = 0; i < N; i++) {
            values[i] = RandomSource.nextInt(1_000_000);
        }
        VarDouble x = VarDouble.copy(values);
        Arrays.sort(values);

        UniqueDouble unique = UniqueDouble.of(x, false);
        assertEquals(N, unique.uniqueCount());

        VarInt valueSortedIds = unique.valueSortedIds();
        for (int i = 0; i < valueSortedIds.size(); i++) {
            assertEquals(0, Double.compare(values[i], unique.uniqueValue(valueSortedIds.getInt(i))));
        }
        VarInt valueSortedIds2 = unique.valueSortedIds();
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
    void testDuplicatesUnsorted() {
        int[] sample = new int[]{3, 1, 7, 5, VarInt.MISSING_VALUE};
        final int N = 100;
        int[] values = new int[N];
        for (int i = 0; i < N; i++) {
            values[i] = sample[RandomSource.nextInt(sample.length)];
        }
        VarInt x = VarInt.copy(values);
        IntArrays.quickSort(values, 0, N, Integer::compare);

        UniqueInt unique = UniqueInt.of(x, false);
        assertEquals(sample.length, unique.uniqueCount());

        VarInt valueSortedIds = unique.valueSortedIds();
        int[] sortedValues = valueSortedIds.intStream().map(unique::uniqueValue).toArray();
        for (int i = 0; i < sample.length; i++) {
            assertEquals(sortedValues[i], unique.uniqueValue(valueSortedIds.getInt(i)));
        }
        VarInt valueSortedIds2 = unique.valueSortedIds();
        for (int i = 0; i < valueSortedIds2.size(); i++) {
            assertEquals(sortedValues[i], unique.uniqueValue(valueSortedIds2.getInt(i)));
        }

        int[] counts = unique.countSortedIds().intStream().map(id -> unique.rowList(id).size()).toArray();
        for (int i = 1; i < counts.length; i++) {
            assertTrue(counts[i - 1] <= counts[i]);
        }
        int[] counts2 = unique.countSortedIds().intStream().map(id -> unique.rowList(id).size()).toArray();
        for (int i = 1; i < counts2.length; i++) {
            assertTrue(counts2[i - 1] <= counts2[i]);
        }
    }

    @Test
    void testString() {

        int oldTextWidth = WS.getPrinter().getOptions().textWidth();
        WS.getPrinter().withOptions(textWidth(100));
        int[] sample = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, VarInt.MISSING_VALUE};
        final int N = 200;
        VarInt x1 = VarInt.from(N, row -> sample[RandomSource.nextInt(sample.length)]);
        UniqueInt ui1 = UniqueInt.of(x1, true);

        assertEquals("UniqueInt{count=43, values=[?:6,1:3,2:2,3:5,4:6,5:6,6:4,7:5,8:3,9:1,..]}", ui1.toString());
        assertEquals("Value Count Percentage Value Count Percentage Value Count Percentage Value Count Percentage Value Count Percentage \n" +
                "    ?     6      0.030     8     3      0.015    16     7      0.035    24     2      0.010    35     6      0.030 \n" +
                "    1     3      0.015     9     1      0.005    17     4      0.020    25     3      0.015    36     7      0.035 \n" +
                "    2     2      0.010    10     6      0.030    18     3      0.015    26     5      0.025    37     3      0.015 \n" +
                "    3     5      0.025    11     3      0.015    19     5      0.025    27     8      0.040    38     4      0.020 \n" +
                "    4     6      0.030    12     4      0.020    20    11      0.055    28     8      0.040    39     4      0.020 \n" +
                "    5     6      0.030    13     4      0.020    21     3      0.015    29     3      0.015    40     3      0.015 \n" +
                "    6     4      0.020    14     8      0.040    22     5      0.025  ...   ...                41     7      0.035 \n" +
                "    7     5      0.025    15     4      0.020    23     5      0.025    34     3      0.015    42     3      0.015 \n", ui1.toContent());
        assertEquals("Value Count Percentage Value Count Percentage Value Count Percentage Value Count Percentage Value Count Percentage \n" +
                "    ?     6      0.030     9     1      0.005    18     3      0.015    27     8      0.040    36     7      0.035 \n" +
                "    1     3      0.015    10     6      0.030    19     5      0.025    28     8      0.040    37     3      0.015 \n" +
                "    2     2      0.010    11     3      0.015    20    11      0.055    29     3      0.015    38     4      0.020 \n" +
                "    3     5      0.025    12     4      0.020    21     3      0.015    30     5      0.025    39     4      0.020 \n" +
                "    4     6      0.030    13     4      0.020    22     5      0.025    31     4      0.020    40     3      0.015 \n" +
                "    5     6      0.030    14     8      0.040    23     5      0.025    32     4      0.020    41     7      0.035 \n" +
                "    6     4      0.020    15     4      0.020    24     2      0.010    33     5      0.025    42     3      0.015 \n" +
                "    7     5      0.025    16     7      0.035    25     3      0.015    34     3      0.015 \n" +
                "    8     3      0.015    17     4      0.020    26     5      0.025    35     6      0.030 \n", ui1.toFullContent());
        assertEquals(ui1.toString(), ui1.toSummary());

        VarInt x2 = VarInt.from(N, row -> sample[RandomSource.nextInt(5)]);
        int[] values2 = x2.stream().mapToInt().toArray();
        Arrays.sort(values2);
        UniqueInt ui2 = UniqueInt.of(x2, true);

        assertEquals("UniqueInt{count=5, values=[1:44,2:50,3:50,4:31,5:25]}", ui2.toString());
        assertEquals("Value Count Percentage Value Count Percentage \n" +
                "    1    44      0.220     4    31      0.155 \n" +
                "    2    50      0.250     5    25      0.125 \n" +
                "    3    50      0.250 \n", ui2.toContent());
        assertEquals("Value Count Percentage Value Count Percentage \n" +
                "    1    44      0.220     4    31      0.155 \n" +
                "    2    50      0.250     5    25      0.125 \n" +
                "    3    50      0.250 \n", ui2.toFullContent());
        assertEquals(ui2.toString(), ui2.toSummary());
    }
}
