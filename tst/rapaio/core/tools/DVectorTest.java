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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rapaio.data.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DVectorTest {

    private static final double TOL = 1e-20;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testBuilders() {
        assertTrue(DVector.empty(true, "a", "b").equalsFull(DVector.empty(false, Arrays.asList("?", "a", "b"))));
        assertTrue(DVector.fromCounts(true, VarNominal.copy("a", "b", "a"))
                .equalsFull(DVector.fromCounts(true, VarNominal.copy("a", "a", "b"))));
        assertTrue(DVector.empty(false, 2).equalsFull(DVector.empty(true, "v1")));
        assertTrue(DVector.fromCounts(true, VarNominal.copy("a", "a", "b", "c"))
                .equalsFull(DVector.fromWeights(
                        true,
                        VarNominal.copy("a", "a", "b", "c"),
                        VarDouble.copy(0.5, 1.5, 1, 1))));
        assertTrue(DVector.fromCounts(true, VarNominal.copy("a", "a", "b", "c"))
                .equalsFull(DVector.fromWeights(
                        true,
                        VarNominal.copy("a", "a", "b", "c"),
                        VarDouble.copy(0.5, 1.5, 1, 1),
                        "?", "a", "b", "c")));
    }

    @Test
    public void testUseFirst() {
        assertTrue(DVector.fromCounts(true, VarNominal.copy("a", "b", "c")).isFirstUsed());
        assertFalse(DVector.fromCounts(false, VarNominal.copy("a", "b", "c")).isFirstUsed());

        assertEquals(0, DVector.fromCounts(true, VarNominal.copy("a", "b", "c")).start());
        assertEquals(1, DVector.fromCounts(false, VarNominal.copy("a", "b", "c")).start());

        double[] v1 = DVector.fromCounts(true, VarNominal.copy("a", "b", "c")).streamValues().toArray();
        double[] v2 = DVector.fromCounts(false, VarNominal.copy("a", "b", "c")).streamValues().toArray();

        assertEquals(v1.length, v2.length + 1);
        for (int i = 0; i < v2.length; i++) {
            assertEquals(v1[i + 1], v2[i], TOL);
        }

        assertEquals("? a b c \n" +
                "- - - - \n" +
                "0 1 1 1 \n", DVector.fromCounts(true, VarNominal.copy("a", "b", "c")).summary());
    }

    @Test
    public void testLevels() {
        DVector dv = DVector.fromCounts(true, VarNominal.copy("a", "a", "b", "a", "b"));
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
    public void testIncrement() {
        DVector dv1 = DVector.fromWeights(true, VarNominal.copy("a", "b", "c"), VarDouble.copy(1.0, 7.0, 5.0));

        DVector dv2 = DVector.empty(true, "?", "a", "b", "c");
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
    public void invalidPlusSize() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot update density vector, row count is different");
        DVector.empty(true, 10).plus(DVector.empty(true, 9), 0);
    }

    @Test
    public void testSet() {
        DVector dv1 = DVector.fromWeights(false,
                VarNominal.copy("?", "a", "b", "c"),
                VarDouble.copy(0.0, 1.0, 7.0, 5.0)
        );

        DVector dv2 = DVector.empty(false, "?", "a", "b", "c");
        dv2.increment(1, dv1.get(1));
        dv2.set(2, 4);
        dv2.increment(2, 3);
        dv2.set("c", 5);

        assertTrue(dv1.equalsFull(dv2));
    }

    @Test
    public void testFindBestIndex() {
        DVector dv1 = DVector.fromWeights(true,
                VarNominal.copy("a", "b", "c"),
                VarDouble.copy(1.0, 7.0, 5.0)
        );
        dv1.set(0, 100);
        assertEquals(0, dv1.findBestIndex());

        DVector dv2 = DVector.fromWeights(true,
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
    public void testNormalize() {
        DVector dv1 = DVector.fromWeights(true,
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

        DVector dv2 = DVector.fromWeights(true,
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
    public void testSums() {
        DVector dv = DVector.fromWeights(false,
                VarNominal.copy("?", "a", "b", "c"),
                VarDouble.copy(1.0, 2.0, 3.0, 4.0)
        );

        assertEquals(9.0, dv.sum(), 1e-20);
        assertEquals(6.0, dv.sumExcept(2), 1e-20);
    }


    @Test
    public void testCountValues() {
        DVector dv = DVector.fromWeights(true,
                VarNominal.copy("?", "a", "b", "c"),
                VarDouble.copy(1.0, 2.0, 3.0, 4.0)
        );

        assertEquals(2, dv.countValues(x -> x <= 2));
    }

    @Test
    public void testSolidCopy() {
        DVector dv1 = DVector.fromWeights(
                false,
                VarNominal.copy("?", "a", "b", "c"),
                VarDouble.copy(1.0, 2.0, 3.0, 4.0)
        );

        DVector dv2 = dv1.copy();
        assertEquals(dv1.toString(), dv2.toString());
    }
}
