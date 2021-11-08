/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.unique;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.sys.With.textWidth;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Unique;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;
import rapaio.data.stream.VSpot;
import rapaio.sys.WS;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueLabelTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void testRandomUnsorted() {
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

        VarInt valueSortedIds = unique.valueSortedIds();
        for (int i = 0; i < valueSortedIds.size(); i++) {
            assertEquals(values[i], unique.uniqueValue(valueSortedIds.getInt(i)));
        }
        VarInt valueSortedIds2 = unique.valueSortedIds();
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
    void testDuplicatesUnsorted() {
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
    void testIdsByRowUnsorted() {
        VarNominal x = VarNominal.copy("a", "b", "c", "d", "e");
        Unique unique = Unique.of(x, false);
        for (int i = 0; i < x.size(); i++) {
            assertEquals(i, unique.idByRow(i));
        }
    }

    @Test
    void testString() {

        WS.getPrinter().withOptions(textWidth(100));
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
        assertEquals("Value Count Percentage Value Count Percentage Value Count Percentage Value Count Percentage Value Count Percentage \n" +
                "    ?     6      0.030    16     7      0.035    23     5      0.025    30     5      0.025    40     3      0.015 \n" +
                "    1     3      0.015    17     4      0.020    24     2      0.010    31     4      0.020    41     7      0.035 \n" +
                "   10     6      0.030    18     3      0.015    25     3      0.015    32     4      0.020    42     3      0.015 \n" +
                "   11     3      0.015    19     5      0.025    26     5      0.025    33     5      0.025     5     6      0.030 \n" +
                "   12     4      0.020     2     2      0.010    27     8      0.040    34     3      0.015     6     4      0.020 \n" +
                "   13     4      0.020    20    11      0.055    28     8      0.040    35     6      0.030     7     5      0.025 \n" +
                "   14     8      0.040    21     3      0.015    29     3      0.015  ...   ...                 8     3      0.015 \n" +
                "   15     4      0.020    22     5      0.025     3     5      0.025     4     6      0.030     9     1      0.005 \n", ui1.toContent());
        assertEquals("Value Count Percentage Value Count Percentage Value Count Percentage Value Count Percentage Value Count Percentage \n" +
                "    ?     6      0.030    17     4      0.020    25     3      0.015    33     5      0.025    41     7      0.035 \n" +
                "    1     3      0.015    18     3      0.015    26     5      0.025    34     3      0.015    42     3      0.015 \n" +
                "   10     6      0.030    19     5      0.025    27     8      0.040    35     6      0.030     5     6      0.030 \n" +
                "   11     3      0.015     2     2      0.010    28     8      0.040    36     7      0.035     6     4      0.020 \n" +
                "   12     4      0.020    20    11      0.055    29     3      0.015    37     3      0.015     7     5      0.025 \n" +
                "   13     4      0.020    21     3      0.015     3     5      0.025    38     4      0.020     8     3      0.015 \n" +
                "   14     8      0.040    22     5      0.025    30     5      0.025    39     4      0.020     9     1      0.005 \n" +
                "   15     4      0.020    23     5      0.025    31     4      0.020     4     6      0.030 \n" +
                "   16     7      0.035    24     2      0.010    32     4      0.020    40     3      0.015 \n", ui1.toFullContent());
        assertEquals(ui1.toString(), ui1.toSummary());

        VarNominal x2 = VarNominal.from(N, row -> sample[RandomSource.nextInt(5)]);
        String[] values2 = x2.stream().map(VSpot::getLabel).toArray(String[]::new);
        Arrays.sort(values2);
        UniqueLabel ui2 = UniqueLabel.of(x2, true);

        assertEquals("UniqueLabel{count=5, values=[1:43,2:50,3:50,4:31,5:26]}", ui2.toString());
        assertEquals("Value Count Percentage Value Count Percentage \n" +
                "    1    43      0.215     4    31      0.155 \n" +
                "    2    50      0.250     5    26      0.130 \n" +
                "    3    50      0.250 \n", ui2.toContent());
        assertEquals("Value Count Percentage Value Count Percentage \n" +
                "    1    43      0.215     4    31      0.155 \n" +
                "    2    50      0.250     5    26      0.130 \n" +
                "    3    50      0.250 \n", ui2.toFullContent());
        assertEquals(ui2.toString(), ui2.toSummary());
    }
}
