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


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.DUniform;
import rapaio.data.Unique;
import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarLong;
import rapaio.data.VarNominal;
import rapaio.data.preprocessing.VarRefSort;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/22/18.
 */
public class UniqueTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void testSortedUnsortedDouble() {
        Var x = VarDouble.from(100, DUniform.of(0, 10)::sampleNext);

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
        VarInt x = VarInt.from(100, row -> RandomSource.nextInt(10000));

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
        Var x = VarBinary.from(100, row -> {
            int v = RandomSource.nextInt(3);
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

        assertFalse(unsortedIds.deepEquals(secondSorted));
        assertTrue(sortedIds.deepEquals(secondSorted));
    }

    @Test
    void testSortedUnsortedLabel() {
        Var x = VarNominal.from(100, row -> {
            int len = RandomSource.nextInt(3);
            if (len == 0) {
                return "?";
            }
            char[] chars = new char[len];
            for (int i = 0; i < len; i++) {
                chars[i] = (char) ('a' + RandomSource.nextInt(3));
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

    @Test
    void testUnimplemented() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> Unique.of(VarLong.copy(1, 2, 3, 4), false));
        assertEquals("Cannot build unique structure for given type: not implemented.", ex.getMessage());
    }
}
