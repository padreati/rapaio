/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.DUniform;
import rapaio.data.Unique;
import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarFloat;
import rapaio.data.VarInstant;
import rapaio.data.VarInt;
import rapaio.data.VarLong;
import rapaio.data.VarNominal;
import rapaio.data.VarType;
import rapaio.data.transform.VarRefSort;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(123);
    }

    @Test
    void testSortedUnsortedDouble() {
        Var x = VarDouble.from(100, () -> DUniform.of(0, 10).sampleNext(random));

        Unique unsorted = Unique.of(x, false);
        Unique sorted = Unique.of(x, true);

        assertTrue(sorted.isSorted());
        assertFalse(unsorted.isSorted());

        VarInt unsortedIds = unsorted.valueSortedIds();
        VarInt sortedIds = sorted.valueSortedIds();

        Var secondSorted = unsortedIds.fapply(VarRefSort.from(unsortedIds.refComparator()));

        assertFalse(unsortedIds.deepEquals(secondSorted));
        assertTrue(sortedIds.deepEquals(secondSorted));
    }

    @Test
    void testSortedUnsortedInt() {
        VarInt x = VarInt.from(100, _ -> random.nextInt(10000));

        Unique unsorted = Unique.of(x, false);
        Unique sorted = Unique.of(x, true);

        VarInt unsortedIds = unsorted.valueSortedIds();
        VarInt sortedIds = sorted.valueSortedIds();

        Var secondSorted = unsortedIds.fapply(VarRefSort.from(unsortedIds.refComparator()));

        assertFalse(unsortedIds.deepEquals(secondSorted));
        assertTrue(sortedIds.deepEquals(secondSorted));
    }

    @Test
    void testSortedUnsortedBinary() {
        Var x = VarBinary.from(100, _ -> {
            int v = random.nextInt(3);
            if (v == 0) {
                return null;
            }
            return v == 1;
        });

        Unique unsorted = Unique.of(x, false);
        Unique sorted = Unique.of(x, true);

        VarInt unsortedIds = unsorted.valueSortedIds();
        VarInt sortedIds = sorted.valueSortedIds();

        Var secondSorted = unsortedIds.fapply(VarRefSort.from(unsortedIds.refComparator()));

        assertTrue(sortedIds.deepEquals(secondSorted));
    }

    @Test
    void testSortedUnsortedLabel() {
        Var x = VarNominal.from(100, _ -> {
            int len = random.nextInt(3);
            if (len == 0) {
                return "?";
            }
            char[] chars = new char[len];
            for (int i = 0; i < len; i++) {
                chars[i] = (char) ('a' + random.nextInt(3));
            }
            return String.valueOf(chars);
        });

        Unique unsorted = Unique.of(x, false);
        Unique sorted = Unique.of(x, true);

        VarInt unsortedIds = unsorted.valueSortedIds();
        VarInt sortedIds = sorted.valueSortedIds();

        Var secondSorted = unsortedIds.fapply(VarRefSort.from(unsortedIds.refComparator()));

        assertFalse(unsortedIds.deepEquals(secondSorted));
        assertTrue(sortedIds.deepEquals(secondSorted));
    }

    /**
     * {@code Unique.of} used to reject LONG, FLOAT and INSTANT with a generic "not implemented" message;
     * they now dispatch to {@link UniqueLong} and {@link UniqueDouble}, and the switch is exhaustive so no
     * type can be left out.
     */
    @Test
    void everyVarTypeIsSupported() {
        for (VarType type : VarType.values()) {
            Var var = type.newInstance(3);
            Unique unique = Unique.of(var, false);
            assertEquals(1, unique.uniqueCount(), type.name() + ": three missing values are one unique value");
            assertEquals(3, unique.rowList(0).size(), type.name());
        }
    }

    @Test
    void longAndInstantKeepFullPrecision() {
        // these two differ by 1 but are 2^53 apart from zero, so a double-backed implementation would merge them
        long big = (1L << 53) + 1;
        Unique unique = Unique.of(VarLong.copy(big, big + 1, big).name("x"), true);
        assertEquals(2, unique.uniqueCount());
        assertEquals(2, unique.rowList(0).size());
        assertEquals(1, unique.rowList(1).size());
        assertEquals(big, ((UniqueLong) unique).uniqueValue(0));
        assertEquals(big + 1, ((UniqueLong) unique).uniqueValue(1));

        Instant t1 = Instant.parse("2026-09-28T10:15:30.00Z");
        Instant t2 = Instant.parse("2026-09-28T10:15:30.001Z");
        Unique instants = Unique.of(VarInstant.from(3, row -> row == 1 ? t2 : t1).name("t"), true);
        assertEquals(2, instants.uniqueCount());
        assertEquals(t1.toEpochMilli(), ((UniqueLong) instants).uniqueValue(0));
        assertEquals(t2.toEpochMilli(), ((UniqueLong) instants).uniqueValue(1));
    }

    @Test
    void floatUsesTheDoubleImplementation() {
        Unique unique = Unique.of(VarFloat.copy(1.5f, 2.5f, 1.5f).name("x"), true);
        assertEquals(2, unique.uniqueCount());
        assertEquals(1.5, ((UniqueDouble) unique).uniqueValue(0), 1e-12);
        assertEquals(2.5, ((UniqueDouble) unique).uniqueValue(1), 1e-12);
    }

    @Test
    void longValuesSortAndGroupCorrectly() {
        Var x = VarLong.copy(30, 10, 20, 10, VarLong.MISSING_VALUE).name("x");
        Unique unique = Unique.of(x, true);

        assertEquals(4, unique.uniqueCount());
        UniqueLong ul = (UniqueLong) unique;
        assertEquals(10, ul.uniqueValue(0));
        assertEquals(20, ul.uniqueValue(1));
        assertEquals(30, ul.uniqueValue(2));
        // missing sorts last, as NaN does in UniqueDouble
        assertEquals(VarLong.MISSING_VALUE, ul.uniqueValue(3));

        assertEquals(2, unique.rowList(0).size());
        assertEquals(0, unique.idByRow(1));
        assertEquals(0, unique.idByRow(3));
        assertEquals(2, unique.idByRow(0));
        assertEquals(3, unique.idByRow(4));

        // the unsorted variant exposes the same grouping through valueSortedIds
        Unique unsorted = Unique.of(x, false);
        assertEquals(4, unsorted.uniqueCount());
        UniqueLong ulu = (UniqueLong) unsorted;
        int[] ids = unsorted.valueSortedIds().elements();
        long previous = Long.MIN_VALUE;
        for (int i = 0; i < unsorted.uniqueCount(); i++) {
            long value = ulu.uniqueValue(ids[i]);
            assertTrue(value >= previous, "valueSortedIds must be in ascending value order");
            previous = value;
        }
    }
}
