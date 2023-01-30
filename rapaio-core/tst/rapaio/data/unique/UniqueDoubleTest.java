/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.unique;

import static org.junit.jupiter.api.Assertions.*;

import static rapaio.sys.With.*;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.sys.WS;
import rapaio.util.collection.DoubleArrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueDoubleTest {

    private static final double TOL = 1e-20;

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(123);
    }

    @Test
    void testRandomUnsorted() {
        final int N = 100;
        double[] values = new double[N];
        for (int i = 0; i < N; i++) {
            values[i] = random.nextDouble();
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
        double[] sample = new double[] {3.4, 1.2, 7.8, 5.6, Double.NaN};
        final int N = 100;
        double[] values = new double[N];
        for (int i = 0; i < N; i++) {
            values[i] = sample[random.nextInt(sample.length)];
        }
        VarDouble x = VarDouble.copy(values);
        DoubleArrays.quickSort(values, 0, N, (u, v) -> {
            int cmp = Double.compare(u, v);
            if (cmp == 0) {
                return 0;
            }
            if (Double.isNaN(u)) {
                return -1;
            }
            if (Double.isNaN(v)) {
                return 1;
            }
            return cmp;
        });

        UniqueDouble unique = UniqueDouble.of(x, false);
        assertEquals(sample.length, unique.uniqueCount());

        VarInt valueSortedIds = unique.valueSortedIds();
        double[] sortedValues = valueSortedIds.intStream().mapToDouble(unique::uniqueValue).toArray();
        for (int i = 0; i < sample.length; i++) {
            assertEquals(sortedValues[i], unique.uniqueValue(valueSortedIds.getInt(i)), TOL);
        }
        VarInt valueSortedIds2 = unique.valueSortedIds();
        for (int i = 0; i < valueSortedIds2.size(); i++) {
            assertEquals(sortedValues[i], unique.uniqueValue(valueSortedIds2.getInt(i)), TOL);
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
        double[] sample = new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
                21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, Double.NaN};
        final int N = 200;
        VarDouble x1 = VarDouble.from(N, row -> sample[random.nextInt(sample.length)] / 3.3);
        double[] values1 = x1.stream().mapToDouble().toArray();
        Arrays.sort(values1);
        UniqueDouble ui1 = UniqueDouble.of(x1, true);

        assertEquals(
                "UniqueDouble{count=43, values=[0.3030303:3,0.6060606:2,0.9090909:5,1.2121212:6,1.5151515:6,1.8181818:4,2.1212121:5,2.4242424:3,2.7272727:1,3.030303:6,..]}",
                ui1.toString());
        assertEquals("""
                  Value    Count Percentage   Value    Count Percentage   Value    Count Percentage   Value    Count Percentage\s
                 0.3030303     3      0.015  3.6363636     4      0.020   6.969697     5      0.025 10.3030303     3      0.015\s
                 0.6060606     2      0.010  3.9393939     4      0.020  7.2727273     2      0.010 10.6060606     6      0.030\s
                 0.9090909     5      0.025  4.2424242     8      0.040  7.5757576     3      0.015 10.9090909     7      0.035\s
                 1.2121212     6      0.030  4.5454545     4      0.020  7.8787879     5      0.025 11.2121212     3      0.015\s
                 1.5151515     6      0.030  4.8484848     7      0.035  8.1818182     8      0.040 11.5151515     4      0.020\s
                 1.8181818     4      0.020  5.1515152     4      0.020  8.4848485     8      0.040 11.8181818     4      0.020\s
                 2.1212121     5      0.025  5.4545455     3      0.015  8.7878788     3      0.015 12.1212121     3      0.015\s
                 2.4242424     3      0.015  5.7575758     5      0.025  9.0909091     5      0.025 12.4242424     7      0.035\s
                 2.7272727     1      0.005  6.0606061    11      0.055  9.3939394     4      0.020 12.7272727     3      0.015\s
                  3.030303     6      0.030  6.3636364     3      0.015  9.6969697     4      0.020          ?     6      0.030\s
                 3.3333333     3      0.015  6.6666667     5      0.025         10     5      0.025\s
                """, ui1.toFullContent());
        assertEquals(ui1.toString(), ui1.toSummary());

        VarDouble x2 = VarDouble.from(N, row -> sample[random.nextInt(5)] / 3.3);
        double[] values2 = x2.stream().mapToDouble().toArray();
        Arrays.sort(values2);
        UniqueDouble ui2 = UniqueDouble.of(x2, true);

        assertEquals("UniqueDouble{count=5, values=[0.3030303:43,0.6060606:50,0.9090909:50,1.2121212:31,1.5151515:26]}", ui2.toString());
        assertEquals("""
                  Value   Count Percentage   Value   Count Percentage\s
                0.3030303    43      0.215 1.2121212    31      0.155\s
                0.6060606    50      0.250 1.5151515    26      0.130\s
                0.9090909    50      0.250\s
                """, ui2.toFullContent());
        assertEquals(ui2.toString(), ui2.toSummary());
    }
}
