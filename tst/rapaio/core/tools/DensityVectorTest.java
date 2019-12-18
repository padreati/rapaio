/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.core.tools;

import org.junit.jupiter.api.Test;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DensityVectorTest {

    private static final double TOL = 1e-20;

    @Test
    void testBuilders() {
        assertTrue(DensityVector.empty(true, "a", "b").equalsFull(DensityVector.empty(false, Arrays.asList("?", "a", "b"))));
        assertTrue(DensityVector.fromCounts(true, VarNominal.copy("a", "b", "a"))
                .equalsFull(DensityVector.fromCounts(true, VarNominal.copy("a", "a", "b"))));
        assertTrue(DensityVector.empty(false, 2).equalsFull(DensityVector.empty(true, "v1")));
        assertTrue(DensityVector.fromCounts(true, VarNominal.copy("a", "a", "b", "c"))
                .equalsFull(DensityVector.fromWeights(
                        true,
                        VarNominal.copy("a", "a", "b", "c"),
                        VarDouble.copy(0.5, 1.5, 1, 1))));
        assertTrue(DensityVector.fromCounts(true, VarNominal.copy("a", "a", "b", "c"))
                .equalsFull(DensityVector.fromWeights(
                        true,
                        VarNominal.copy("a", "a", "b", "c"),
                        VarDouble.copy(0.5, 1.5, 1, 1),
                        "?", "a", "b", "c")));
    }

    @Test
    void testUseFirst() {
        assertTrue(DensityVector.fromCounts(true, VarNominal.copy("a", "b", "c")).isFirstUsed());
        assertFalse(DensityVector.fromCounts(false, VarNominal.copy("a", "b", "c")).isFirstUsed());

        assertEquals(0, DensityVector.fromCounts(true, VarNominal.copy("a", "b", "c")).start());
        assertEquals(1, DensityVector.fromCounts(false, VarNominal.copy("a", "b", "c")).start());

        double[] v1 = DensityVector.fromCounts(true, VarNominal.copy("a", "b", "c")).streamValues().toArray();
        double[] v2 = DensityVector.fromCounts(false, VarNominal.copy("a", "b", "c")).streamValues().toArray();

        assertEquals(v1.length, v2.length + 1);
        for (int i = 0; i < v2.length; i++) {
            assertEquals(v1[i + 1], v2[i], TOL);
        }

        assertEquals("? a b c \n" +
                "- - - - \n" +
                "0 1 1 1 \n", DensityVector.fromCounts(true, VarNominal.copy("a", "b", "c")).toSummary());
    }

    @Test
    void testLevels() {
        DensityVector dv = DensityVector.fromCounts(true, VarNominal.copy("a", "a", "b", "a", "b"));
        List<String> levels = dv.levels();
        assertEquals(3, levels.size());
        assertEquals("?", levels.get(0));
        assertEquals("a", levels.get(1));
        assertEquals("b", levels.get(2));

        assertEquals("?", dv.level(0));
        assertEquals("a", dv.level(1));
        assertEquals("b", dv.level(2));

        assertEquals(0, dv.get("?"), TOL);
        assertEquals(3, dv.get("a"), TOL);
        assertEquals(2, dv.get("b"), TOL);
    }

    @Test
    void testIncrement() {
        DensityVector dv1 = DensityVector.fromWeights(true, VarNominal.copy("a", "b", "c"), VarDouble.copy(1.0, 7.0, 5.0));

        DensityVector dv2 = DensityVector.empty(true, "?", "a", "b", "c");
        dv2.increment(1, 1);
        dv2.increment(2, 4);
        dv2.increment("b", 3);
        dv2.increment(3, 5);

        assertTrue(dv1.equalsFull(dv2));

        dv1.plus(dv2, 0);
        assertTrue(dv1.equalsFull(dv2));

        dv2.plus(dv1, 2);
        for (int i = 0; i < dv2.levels().size(); i++) {
            assertEquals(dv1.get(i) * 3, dv2.get(i), TOL);
        }
    }

    @Test
    void invalidPlusSize() {
        var ex = assertThrows(IllegalArgumentException.class, () -> DensityVector.empty(true, 10).plus(DensityVector.empty(true, 9), 0));
        assertEquals("Cannot update density vector, row count is different", ex.getMessage());
    }

    @Test
    void testSet() {
        DensityVector dv1 = DensityVector.fromWeights(false,
                VarNominal.copy("?", "a", "b", "c"),
                VarDouble.copy(0.0, 1.0, 7.0, 5.0)
        );

        DensityVector dv2 = DensityVector.empty(false, "?", "a", "b", "c");
        dv2.increment(1, dv1.get(1));
        dv2.set(2, 4);
        dv2.increment(2, 3);
        dv2.set("c", 5);

        assertTrue(dv1.equalsFull(dv2));
    }

    @Test
    void testFindBestIndex() {
        DensityVector dv1 = DensityVector.fromWeights(true,
                VarNominal.copy("a", "b", "c"),
                VarDouble.copy(1.0, 7.0, 5.0)
        );
        dv1.set(0, 100);
        assertEquals(0, dv1.findBestIndex());

        DensityVector dv2 = DensityVector.fromWeights(true,
                VarNominal.copy("a", "b", "c"),
                VarDouble.copy(1.0, 2.0, 2.0)
        );
        for (int i = 0; i < 100; i++) {
            int index = dv2.findBestIndex();
            assertTrue(index >= 2);
            assertTrue(index <= 3);
        }
    }

    @Test
    void testNormalize() {
        DensityVector dv1 = DensityVector.fromWeights(true,
                VarNominal.copy("a", "b", "c"),
                VarDouble.copy(1.0, 1.0, 1.0)
        );
        dv1.normalize();

        for (int i = 1; i < dv1.rowCount(); i++) {
            assertEquals(1.0 / 3.0, dv1.get(i), TOL);
        }

        dv1.set(0, 1.0 / 3.0);
        dv1.normalize();

        for (int i = 0; i < dv1.rowCount(); i++) {
            assertEquals(dv1.get(i), 1.0 / 4.0, TOL);
        }

        DensityVector dv2 = DensityVector.fromWeights(true,
                VarNominal.copy("a", "b", "c"),
                VarDouble.copy(-2, 0, 2));

        dv2.normalize();
        assertEquals(-2, dv2.get(1), TOL);
        assertEquals(0, dv2.get(2), TOL);
        assertEquals(2, dv2.get(3), TOL);

        dv2.normalize(2);

        assertEquals(-1. / 4, dv2.get(1), TOL);
        assertEquals(0, dv2.get(2), TOL);
        assertEquals(1. / 4, dv2.get(3), TOL);
    }

    @Test
    void testSums() {
        DensityVector dv = DensityVector.fromWeights(false,
                VarNominal.copy("?", "a", "b", "c"),
                VarDouble.copy(1.0, 2.0, 3.0, 4.0)
        );

        assertEquals(9.0, dv.sum(), 1e-20);
        assertEquals(6.0, dv.sumExcept(2), 1e-20);
    }


    @Test
    void testCountValues() {
        DensityVector dv = DensityVector.fromWeights(true,
                VarNominal.copy("?", "a", "b", "c"),
                VarDouble.copy(1.0, 2.0, 3.0, 4.0)
        );

        assertEquals(2, dv.countValues(x -> x <= 2));
    }

    @Test
    void testSolidCopy() {
        DensityVector dv1 = DensityVector.fromWeights(
                false,
                VarNominal.copy("?", "a", "b", "c"),
                VarDouble.copy(1.0, 2.0, 3.0, 4.0)
        );

        DensityVector dv2 = dv1.copy();
        assertEquals(dv1.toString(), dv2.toString());
    }
}
