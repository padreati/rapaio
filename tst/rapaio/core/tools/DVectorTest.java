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

import org.junit.Test;
import rapaio.data.NominalVar;
import rapaio.data.NumericVar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DVectorTest {

    @Test
    public void testBuilders() {
        assertTrue(DVector.empty(true, "a", "b").equalsFull(DVector.empty(false, "?", "a", "b")));
        assertTrue(DVector.fromCount(true, NominalVar.copy("a", "b", "a")).equalsFull(DVector.fromCount(true, NominalVar.copy("a", "a", "b"))));
        assertTrue(DVector.empty(false, 2).equalsFull(DVector.empty(true, "v1")));
        assertTrue(DVector.fromCount(true, NominalVar.copy("a", "a", "b", "c"))
                .equalsFull(DVector.fromWeights(
                        true,
                        NominalVar.copy("a", "a", "b", "c"),
                        NumericVar.copy(0.5, 1.5, 1, 1))));
        assertTrue(DVector.fromCount(true, NominalVar.copy("a", "a", "b", "c"))
                .equalsFull(DVector.fromWeights(
                        true,
                        NominalVar.copy("a", "a", "b", "c"),
                        NumericVar.copy(0.5, 1.5, 1, 1),
                        "?", "a", "b", "c")));
    }

    @Test
    public void testIncrement() {
        DVector ndv1 = DVector.fromWeights(
                true,
                NominalVar.copy("a", "b", "c"),
                NumericVar.copy(1.0, 7.0, 5.0)
        );

        DVector ndv2 = DVector.empty(true, "?", "a", "b", "c");
        ndv2.increment(1, 1);
        ndv2.increment(2, 4);
        ndv2.increment(2, 3);
        ndv2.increment(3, 5);

        assertTrue(ndv1.equalsFull(ndv2));
    }

    @Test
    public void testSet() {
        DVector ndv1 = DVector.fromWeights(false,
                NominalVar.copy("?", "a", "b", "c"),
                NumericVar.copy(0.0, 1.0, 7.0, 5.0)
        );

        DVector ndv2 = DVector.empty(false, "?", "a", "b", "c");
        ndv2.increment(1, ndv1.get(1));
        ndv2.set(2, 4);
        ndv2.increment(2, 3);
        ndv2.increment(3, 5);

        assertTrue(ndv1.equalsFull(ndv2));
    }

    @Test
    public void testFindBestIndex() {
        DVector dv1 = DVector.fromWeights(true,
                NominalVar.copy("a", "b", "c"),
                NumericVar.copy(1.0, 7.0, 5.0)
        );
        dv1.set(0, 100);
        assertEquals(0, dv1.findBestIndex());

        DVector dv2 = DVector.fromWeights(true,
                NominalVar.copy("a", "b", "c"),
                NumericVar.copy(1.0, 2.0, 2.0)
        );
        for (int i = 0; i < 100; i++) {
            int index = dv2.findBestIndex();
            assertTrue(index >= 2);
            assertTrue(index <= 3);
        }
    }

    @Test
    public void testNormalize() {
        DVector dv = DVector.fromWeights(true,
                NominalVar.copy("a", "b", "c"),
                NumericVar.copy(1.0, 1.0, 1.0)
        );
        dv.normalize();

        for (int i = 1; i < dv.getRowCount(); i++) {
            assertEquals(1.0 / 3.0, dv.get(i), 1e-20);
        }

        dv.set(0, 1.0 / 3.0);
        dv.normalize();

        for (int i = 0; i < dv.getRowCount(); i++) {
            assertEquals(dv.get(i), 1.0 / 4.0, 1e-20);
        }
    }

    @Test
    public void testSums() {
        DVector dv = DVector.fromWeights(false,
                NominalVar.copy("?", "a", "b", "c"),
                NumericVar.copy(1.0, 2.0, 3.0, 4.0)
        );

        assertEquals(9.0, dv.withFirst(false).sum(), 1e-20);
        assertEquals(10.0, dv.withFirst(true).sum(), 1e-20);
        assertEquals(6.0, dv.withFirst(false).sumExcept(2), 1e-20);
        assertEquals(7.0, dv.withFirst(true).sumExcept(2), 1e-20);
    }


    @Test
    public void testCountValues() {
        DVector dv = DVector.fromWeights(true,
                NominalVar.copy("?", "a", "b", "c"),
                NumericVar.copy(1.0, 2.0, 3.0, 4.0)
        );

        assertEquals(2, dv.withFirst(true).countValues(x -> x <= 2));
        assertEquals(1, dv.withFirst(false).countValues(x -> x <= 2));
    }

    @Test
    public void testSolidCopy() {
        DVector dv1 = DVector.fromWeights(
                false,
                NominalVar.copy("?", "a", "b", "c"),
                NumericVar.copy(1.0, 2.0, 3.0, 4.0)
        );

        DVector dv2 = dv1.solidCopy();
        assertEquals(dv1.toString(), dv2.toString());
    }
}
