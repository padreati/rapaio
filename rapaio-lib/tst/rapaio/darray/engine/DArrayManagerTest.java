/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.darray.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.darray.DArrayManager;
import rapaio.darray.DType;
import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.darray.layout.StrideLayout;
import rapaio.darray.manager.base.BaseDArrayManager;

public class DArrayManagerTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void mainTestLoop() {
        testManagerSuite(BaseDArrayManager.base());
    }

    void testManagerSuite(DArrayManager manager) {
        testManager(manager, DType.DOUBLE);
        testManager(manager, DType.FLOAT);
        testManager(manager, DType.INTEGER);
        testManager(manager, DType.BYTE);
    }

    <N extends Number> void testManager(DArrayManager manager, DType<N> dt) {
        testZeros(manager, dt);
        testEye(manager, dt);
        testFull(manager, dt);
        testSeq(manager, dt);
        testRandom(manager, dt);
        testStride(manager, dt);
        testWrap(manager, dt);
        testConcatenate(manager, dt);
        testStack(manager, dt);
        testSel(manager, dt);
        testExpand(manager, dt);
    }

    <N extends Number> void testZeros(DArrayManager manager, DType<N> dt) {
        var t = manager.zeros(dt, Shape.of(10, 20));
        assertEquals(manager, t.manager());
        assertEquals(t.shape(), Shape.of(10, 20));
        var it = t.ptrIterator();
        while (it.hasNext()) {
            assertEquals(dt.cast(0), t.ptrGet(it.nextInt()));
        }
    }

    <N extends Number> void testEye(DArrayManager manager, DType<N> dt) {
        var t = manager.eye(dt, 3);
        assertEquals(2, t.rank());
        assertEquals(9, t.size());

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double value = i == j ? 1 : 0;
                assertEquals(dt.cast(value), t.get(i, j));
            }
        }
    }

    <N extends Number> void testFull(DArrayManager manager, DType<N> dt) {
        var t = manager.full(dt, Shape.of(2, 3), 5);
        assertEquals(2, t.rank());
        assertEquals(6, t.size());

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(dt.cast(5), t.get(i, j));
            }
        }
    }

    <N extends Number> void testSeq(DArrayManager manager, DType<N> dt) {
        var t = manager.seq(dt, Shape.of(2, 3, 4));
        var it = t.ptrIterator(Order.C);
        int i = 0;
        while (it.hasNext()) {
            assertEquals(dt.cast(i++), t.ptrGet(it.nextInt()));
        }
    }

    <N extends Number> void testRandom(DArrayManager manager, DType<N> dt) {
        var t = manager.random(dt, Shape.of(3, 4), random);
        var it = t.ptrIterator(Order.C);
        N last = t.ptrGet(it.nextInt());
        while (it.hasNext()) {
            N next = t.ptrGet(it.nextInt());
            assertNotEquals(last, next);
            last = next;
        }
    }

    <N extends Number> void testStride(DArrayManager manager, DType<N> dt) {
        double[] seq = new double[12];
        for (int i = 0; i < seq.length; i++) {
            seq[i] = i;
        }
        var t = manager.stride(dt, StrideLayout.of(Shape.of(4, 3), 0, new int[] {1, 4}), seq);
        int i = 0;
        var it = t.ptrIterator(Order.F);
        while (it.hasNext()) {
            assertEquals(dt.cast(i++), t.ptrGet(it.nextInt()));
        }
    }

    <N extends Number> void testWrap(DArrayManager manager, DType<N> dt) {
        var t = manager.stride(dt, Shape.of(2, 3), Order.C, 1., 2, 3, 4, 5, 6);
        int i = 1;
        var it = t.ptrIterator(Order.C);
        while (it.hasNext()) {
            assertEquals(dt.cast(i++), t.ptrGet(it.nextInt()));
        }
    }

    <N extends Number> void testConcatenate(DArrayManager manager, DType<N> dt) {
        var t1 = manager.stride(dt, Shape.of(2, 3), Order.C, 1., 2, 3, 4, 5, 6);
        var t2 = manager.stride(dt, Shape.of(1, 3), Order.C, 7.f, 8, 9);
        var t3 = manager.stride(dt, Shape.of(3, 3), Order.C, 10., 11, 12, 13, 14, 15, 16, 17, 18);
        var t4 = manager.stride(dt, Shape.of(1, 2), Order.C, 1., 2, 3, 4);


        var t = manager.cat(0, List.of(t1, t2, t3));
        int i = 1;
        var it = t.ptrIterator(Order.C);
        while (it.hasNext()) {
            assertEquals(dt.cast(i++), t.ptrGet(it.nextInt()));
        }

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> manager.cat(0, List.of(t1, t4)));
        assertEquals("NArrays are not valid for concatenation", ex.getMessage());
    }

    <N extends Number> void testStack(DArrayManager manager, DType<N> dt) {
        var t1 = manager.stride(dt, Shape.of(2, 3), Order.C, 1., 2, 3, 4, 5, 6);
        var t2 = manager.stride(dt, Shape.of(2, 3), Order.C, 7.f, 8, 9, 10, 11, 12);
        var t3 = manager.stride(dt, Shape.of(2, 3), Order.C, 13., 14, 15, 16, 17, 18);

        double[][] expected = new double[][] {
                {1., 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18},
                {1., 2, 3, 7, 8, 9, 13, 14, 15, 4, 5, 6, 10, 11, 12, 16, 17, 18},
                {1., 7, 13, 2, 8, 14, 3, 9, 15, 4, 10, 16, 5, 11, 17, 6, 12, 18}
        };
        for (int s = 0; s < 3; s++) {
            var t = manager.stack(s, List.of(t1, t2, t3));
            int i = 0;
            var it = t.ptrIterator(Order.defaultOrder());
            while (it.hasNext()) {
                assertEquals(dt.cast(expected[s][i++]), t.ptrGet(it.nextInt()));
            }
        }

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> manager.stack(0, List.of(t1, t2.t_())));
        assertEquals("NArrays are not valid for stack, they have to have the same dimensions.", ex.getMessage());
    }

    <N extends Number> void testSel(DArrayManager manager, DType<N> dt) {
        DArray<N> t = manager.seq(dt, Shape.of(100));
        assertTrue(manager.seq(dt, Shape.of(3)).deepEquals(t.sel(0, 0, 1, 2)));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> t.sel(-1, 43));
        assertEquals("Axis value -1 is out of bounds.", e.getMessage());

        DArray<N> m = manager.seq(dt, Shape.of(4, 4));
        assertTrue(manager.stride(dt, Shape.of(12), Order.C, 4, 5, 6, 7, 12, 13, 14, 15, 4, 5, 6, 7)
                .deepEquals(m.sel(0, 1, 3, 1).flatten(Order.C)));
        assertTrue(manager.stride(dt, Shape.of(12), Order.C, 1, 3, 1, 5, 7, 5, 9, 11, 9, 13, 15, 13)
                .deepEquals(m.sel(1, 1, 3, 1).flatten(Order.C)));
    }

    <N extends Number> void testExpand(DArrayManager manager, DType<N> dt) {
        DArray<N> t1 = manager.seq(dt, Shape.of(4, 1, 2));
        DArray<N> exp1 = t1.expand(1, 2);
        assertTrue(t1.deepEquals(exp1.narrow(1, true, 0, 1)));
        assertTrue(t1.deepEquals(exp1.narrow(1, true, 1, 2)));
        assertEquals(Shape.of(4, 2, 2), exp1.shape());
        // add 1, which should be added twice
        exp1.add_(1);
        assertTrue(manager.seq(dt, Shape.of(4, 1, 2)).add_(2).deepEquals(t1));

        DArray<N> t2 = manager.seq(dt, Shape.of(4, 2, 1));
        DArray<N> exp2 = t2.expand(2, 2);
        assertTrue(t2.deepEquals(exp2.narrow(2, true, 0, 1)));
        assertTrue(t2.deepEquals(exp2.narrow(2, true, 1, 2)));
        assertEquals(Shape.of(4, 2, 2), exp2.shape());
        // add 1, which should be added twice
        exp2.add_(1);
        assertTrue(manager.seq(dt, Shape.of(4, 2, 1)).add_(2).deepEquals(t2));

        DArray<N> t3 = manager.seq(dt, Shape.of(1, 2, 4));
        DArray<N> exp3 = t3.expand(0, 2);
        assertTrue(t3.deepEquals(exp3.narrow(0, true, 0, 1)));
        assertTrue(t3.deepEquals(exp3.narrow(0, true, 1, 2)));
        assertEquals(Shape.of(2, 2, 4), exp3.shape());
        // add 1, which should be added twice
        exp3.add_(1);
        assertTrue(manager.seq(dt, Shape.of(1, 2, 4)).add_(2).deepEquals(t3));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> manager.seq(dt, Shape.of(2, 3, 4)).expand(0, 10));
        assertEquals("Dimension 0 must have size 1, but have size 2.", e.getMessage());
    }

}
