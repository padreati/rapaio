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

package rapaio.core.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

public class DensityVectorTest {

    private static final double TOL = 1e-20;

    @Test
    void testBuilders() {
        assertTrue(DensityVector.emptyByLabels(true, "a", "b")
                .equalsFull(DensityVector.emptyByLabels(false, Arrays.asList("?", "a", "b"))));

        assertTrue(DensityVector.emptyByLabels(true, Arrays.asList("a", "b"))
                .equalsFull(DensityVector.emptyByLabels(false, "?", "a", "b")));

        assertTrue(DensityVector.fromLevelCounts(true, VarNominal.copy("a", "b", "a"))
                .equalsFull(DensityVector.fromLevelCounts(true, VarNominal.copy("a", "a", "b"))));
        assertTrue(DensityVector.emptyByLabels(1).equalsFull(DensityVector.emptyByLabels(true, "v0")));
        assertTrue(DensityVector.fromLevelCounts(true, VarNominal.copy("a", "a", "b", "c"))
                .equalsFull(DensityVector.fromLevelWeights(
                        true,
                        VarNominal.copy("a", "a", "b", "c"),
                        VarDouble.copy(0.5, 1.5, 1, 1))));
        assertTrue(DensityVector.fromLevelCounts(true, VarNominal.copy("a", "a", "b", "c"))
                .equalsFull(DensityVector.fromLevelWeights(
                        true,
                        VarNominal.copy("a", "a", "b", "c"),
                        VarDouble.copy(0.5, 1.5, 1, 1))));
    }

    @Test
    void testNewInstance() {
        var dv1 = DensityVector.emptyByLabels(true, "a", "b");
        var dv2 = dv1.newInstance();

        assertNotNull(dv1);
        assertNotNull(dv2);
        assertEquals(dv1.rowCount(), dv2.rowCount());
        assertTrue(dv1.equalsFull(dv2));

        assertEquals(dv1.toSummary(), dv2.toFullContent());
    }

    @Test
    void testUseFirst() {
        assertEquals(4, DensityVector.fromLevelCounts(true, VarNominal.copy("a", "b", "c")).index().size());
        assertEquals(3, DensityVector.fromLevelCounts(false, VarNominal.copy("a", "b", "c")).index().size());

        double[] v1 = DensityVector.fromLevelCounts(true, VarNominal.copy("a", "b", "c")).streamValues().toArray();
        double[] v2 = DensityVector.fromLevelCounts(false, VarNominal.copy("a", "b", "c")).streamValues().toArray();

        assertEquals(v1.length, v2.length + 1);
        for (int i = 0; i < v2.length; i++) {
            assertEquals(v1[i + 1], v2[i], TOL);
        }

        assertEquals("? a b c \n" +
                "- - - - \n" +
                "0 1 1 1 \n", DensityVector.fromLevelCounts(true, VarNominal.copy("a", "b", "c")).toSummary());
    }

    @Test
    void testIndexValues() {
        var dv = DensityVector.fromLevelCounts(true, VarNominal.copy("a", "a", "b", "a", "b"));
        List<String> levels = dv.index().getValues();

        assertEquals(Arrays.asList("?", "a", "b"), dv.index().getValues());
        assertEquals(Arrays.asList("?", "a", "b"), dv.index().getValueStrings());

        assertEquals(0, dv.get("?"), TOL);
        assertEquals(3, dv.get("a"), TOL);
        assertEquals(2, dv.get("b"), TOL);

        assertEquals("?", dv.getIndexValue(0));
        assertEquals("a", dv.getIndexValue(1));
        assertEquals("b", dv.getIndexValue(2));
    }

    @Test
    void testIncrement() {
        var dv1 = DensityVector.fromLevelWeights(true, VarNominal.copy("a", "b", "c"), VarDouble.copy(1.0, 7.0, 5.0));

        var dv2 = DensityVector.emptyByLabels(true, "?", "a", "b", "c");
        dv2.increment(1, 1);
        dv2.increment(2, 4);
        dv2.increment("b", 3);
        dv2.increment(3, 5);

        assertTrue(dv1.equalsFull(dv2));

        dv1.plus(dv2, 0);
        assertTrue(dv1.equalsFull(dv2));

        dv2.plus(dv1, 2);
        for (int i = 0; i < dv2.index().size(); i++) {
            assertEquals(dv1.get(i) * 3, dv2.get(i), TOL);
        }
    }

    @Test
    void invalidPlusSize() {
        var ex = assertThrows(IllegalArgumentException.class, () ->
                DensityVector.emptyByLabels(10).plus(DensityVector.emptyByLabels(9), 0));
        assertEquals("Cannot update density vector, row count is different", ex.getMessage());
    }

    @Test
    void testSet() {
        var dv1 = DensityVector.fromLevelWeights(false,
                VarNominal.copy("?", "a", "b", "c"),
                VarDouble.copy(0.0, 1.0, 7.0, 5.0)
        );

        var dv2 = DensityVector.emptyByLabels(false, "?", "a", "b", "c");
        dv2.increment(0, dv1.get(0));
        dv2.set(1, 4);
        dv2.increment(1, 3);
        dv2.set("c", 5);

        assertTrue(dv1.equalsFull(dv2));
    }

    @Test
    void testFindBestIndex() {
        var dv1 = DensityVector.fromLevelWeights(true,
                VarNominal.copy("a", "b", "c"),
                VarDouble.copy(1.0, 7.0, 5.0)
        );
        dv1.set(0, 100);
        assertEquals(0, dv1.findBestIndex());
        assertEquals("?", dv1.findBestLabel());

        var dv2 = DensityVector.fromLevelWeights(true,
                VarNominal.copy("a", "b", "c"),
                VarDouble.copy(1.0, 2.0, 2.0)
        );
        for (int i = 0; i < 100; i++) {
            int index = dv2.findBestIndex();
            String label = dv2.findBestLabel();
            assertTrue(label.equals("b") || label.equals("c"));
            assertTrue(index >= 2);
            assertTrue(index <= 3);
        }
    }

    @Test
    void testNormalize() {
        var dv1 = DensityVector.fromLevelWeights(true,
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

        var dv2 = DensityVector.fromLevelWeights(true,
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
        var dv = DensityVector.fromLevelWeights(false,
                VarNominal.copy("?", "a", "b", "c"),
                VarDouble.copy(1.0, 2.0, 3.0, 4.0)
        );

        assertEquals(9.0, dv.sum(), TOL);
        assertEquals(6.0, dv.sumExcept(1), TOL);
        assertEquals(6.0, dv.sumExcept("b"));

        var ex = assertThrows(IllegalArgumentException.class, () -> dv.sumExcept(-1));
        assertEquals("Except index must be greater or equal with 0.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> dv.sumExcept("ex"));
        assertEquals("Except value: ex not found.", ex.getMessage());
    }


    @Test
    void testCountValues() {
        var dv = DensityVector.fromLevelWeights(true,
                VarNominal.copy("?", "a", "b", "c"),
                VarDouble.copy(1.0, 2.0, 3.0, 4.0)
        );

        assertEquals(2, dv.countValues(x -> x <= 2));
    }

    @Test
    void testSolidCopy() {
        var dv1 = DensityVector.fromLevelWeights(
                false,
                VarNominal.copy("?", "a", "b", "c"),
                VarDouble.copy(1.0, 2.0, 3.0, 4.0)
        );

        var dv2 = dv1.copy();
        assertEquals(dv1.toString(), dv2.toString());
    }
}
