/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.tensor.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.math.tensor.DType;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.TensorManager;
import rapaio.math.tensor.manager.varray.VectorizedArrayTensorManager;
import rapaio.math.tensor.layout.StrideLayout;

public class TensorManagerTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void mainTestLoop() {

        testManagerSuite(new VectorizedArrayTensorManager());
        testManagerSuite(new VectorizedArrayTensorManager());
    }

    void testManagerSuite(TensorManager engine) {
        testManager(engine, engine.ofType(DType.DOUBLE));
        testManager(engine, engine.ofType(DType.FLOAT));
        testManager(engine, engine.ofType(DType.INTEGER));
        testManager(engine, engine.ofType(DType.BYTE));
    }

    <N extends Number> void testManager(TensorManager engine, TensorManager.OfType<N> ofType) {
        testZeros(engine, ofType);
        testEye(engine, ofType);
        testFull(engine, ofType);
        testSeq(engine, ofType);
        testRandom(engine, ofType);
        testStride(engine, ofType);
        testWrap(engine, ofType);
        testConcatenate(engine, ofType);
        testStack(engine, ofType);
        testTake(engine, ofType);
        testExpand(engine, ofType);
    }

    <N extends Number> void testZeros(TensorManager engine, TensorManager.OfType<N> ofType) {
        var t = ofType.zeros(Shape.of(10, 20));
        assertEquals(engine, t.manager());
        assertEquals(t.shape(), Shape.of(10, 20));
        var it = t.ptrIterator();
        while (it.hasNext()) {
            assertEquals(ofType.dtype().castValue(0), t.ptrGet(it.nextInt()));
        }
    }

    <N extends Number> void testEye(TensorManager engine, TensorManager.OfType<N> ofType) {
        var t = ofType.eye(3);
        assertEquals(2, t.rank());
        assertEquals(9, t.size());

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double value = i == j ? 1 : 0;
                assertEquals(ofType.dtype().castValue(value), t.get(i, j));
            }
        }
    }

    <N extends Number> void testFull(TensorManager engine, TensorManager.OfType<N> ofType) {
        var t = ofType.full(Shape.of(2, 3), ofType.dtype().castValue(5));
        assertEquals(2, t.rank());
        assertEquals(6, t.size());

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(ofType.dtype().castValue(5), t.get(i, j));
            }
        }
    }

    <N extends Number> void testSeq(TensorManager engine, TensorManager.OfType<N> ofType) {
        var t = ofType.seq(Shape.of(2, 3, 4));
        var it = t.ptrIterator(Order.C);
        int i = 0;
        while (it.hasNext()) {
            assertEquals(ofType.dtype().castValue(i++), t.ptrGet(it.nextInt()));
        }
    }

    <N extends Number> void testRandom(TensorManager engine, TensorManager.OfType<N> ofType) {
        var t = ofType.random(Shape.of(3, 4), random);
        var it = t.ptrIterator(Order.C);
        N last = t.ptrGet(it.nextInt());
        while (it.hasNext()) {
            N next = t.ptrGet(it.nextInt());
            assertNotEquals(last, next);
            last = next;
        }
    }

    <N extends Number> void testStride(TensorManager engine, TensorManager.OfType<N> ofType) {
        double[] seq = new double[12];
        for (int i = 0; i < seq.length; i++) {
            seq[i] = i;
        }
        var t = ofType.stride(StrideLayout.of(Shape.of(4, 3), 0, new int[] {1, 4}), seq);
        int i = 0;
        var it = t.ptrIterator(Order.F);
        while (it.hasNext()) {
            assertEquals(ofType.dtype().castValue(i++), t.ptrGet(it.nextInt()));
        }
    }

    <N extends Number> void testWrap(TensorManager engine, TensorManager.OfType<N> ofType) {
        var t = ofType.stride(Shape.of(2, 3), Order.C, 1., 2, 3, 4, 5, 6);
        int i = 1;
        var it = t.ptrIterator(Order.C);
        while (it.hasNext()) {
            assertEquals(ofType.dtype().castValue(i++), t.ptrGet(it.nextInt()));
        }
    }

    <N extends Number> void testConcatenate(TensorManager engine, TensorManager.OfType<N> ofType) {
        var t1 = ofType.stride(Shape.of(2, 3), Order.C, ofType.storage().from(1., 2, 3, 4, 5, 6));
        var t2 = ofType.stride(Shape.of(1, 3), Order.C, ofType.storage().from(7.f, 8, 9));
        var t3 = ofType.stride(Shape.of(3, 3), Order.C, ofType.storage().from(10., 11, 12, 13, 14, 15, 16, 17, 18));
        var t4 = ofType.stride(Shape.of(1, 2), Order.C, ofType.storage().from(1., 2, 3, 4));


        var t = engine.concat(0, List.of(t1, t2, t3));
        int i = 1;
        var it = t.ptrIterator(Order.C);
        while (it.hasNext()) {
            assertEquals(ofType.dtype().castValue(i++), t.ptrGet(it.nextInt()));
        }

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> engine.concat(0, List.of(t1, t4)));
        assertEquals("Tensors are not valid for concatenation", ex.getMessage());
    }

    <N extends Number> void testStack(TensorManager engine, TensorManager.OfType<N> ofType) {
        var t1 = ofType.stride(Shape.of(2, 3), Order.C, ofType.storage().from(1., 2, 3, 4, 5, 6));
        var t2 = ofType.stride(Shape.of(2, 3), Order.C, ofType.storage().from(7.f, 8, 9, 10, 11, 12));
        var t3 = ofType.stride(Shape.of(2, 3), Order.C, ofType.storage().from(13., 14, 15, 16, 17, 18));

        double[][] expected = new double[][] {
                {1., 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18},
                {1., 2, 3, 7, 8, 9, 13, 14, 15, 4, 5, 6, 10, 11, 12, 16, 17, 18},
                {1., 7, 13, 2, 8, 14, 3, 9, 15, 4, 10, 16, 5, 11, 17, 6, 12, 18}
        };
        for (int s = 0; s < 3; s++) {
            var t = engine.stack(s, List.of(t1, t2, t3));
            int i = 0;
            var it = t.ptrIterator(Order.defaultOrder());
            while (it.hasNext()) {
                assertEquals(ofType.dtype().castValue(expected[s][i++]), t.ptrGet(it.nextInt()));
            }
        }

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> engine.stack(0, List.of(t1, t2.t_())));
        assertEquals("Tensors are not valid for stack, they have to have the same dimensions.", ex.getMessage());
    }

    <N extends Number> void testTake(TensorManager engine, TensorManager.OfType<N> ofType) {
        Tensor<N> t = ofType.seq(Shape.of(100));
        assertTrue(ofType.seq(Shape.of(3)).deepEquals(t.take(0, 0, 1, 2)));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> t.take(-1, 43));
        assertEquals("Axis value -1 is out of bounds.", e.getMessage());

        Tensor<N> m = ofType.seq(Shape.of(4, 4));
        assertTrue(ofType.stride(Shape.of(12), Order.C, 4, 5, 6, 7, 12, 13, 14, 15, 4, 5, 6, 7)
                .deepEquals(m.take(0, 1, 3, 1).flatten(Order.C)));
        assertTrue(ofType.stride(Shape.of(12), Order.C, 1, 3, 1, 5, 7, 5, 9, 11, 9, 13, 15, 13)
                .deepEquals(m.take(1, 1, 3, 1).flatten(Order.C)));
    }

    <N extends Number> void testExpand(TensorManager engine, TensorManager.OfType<N> ofType) {
        Tensor<N> t1 = ofType.seq(Shape.of(4, 1, 2));
        Tensor<N> exp1 = t1.expand(1, 2);
        assertTrue(t1.deepEquals(exp1.narrow(1, true, 0, 1)));
        assertTrue(t1.deepEquals(exp1.narrow(1, true, 1, 2)));
        assertEquals(Shape.of(4,2,2), exp1.shape());
        // add 1, which should be added twice
        exp1.add_(ofType.dtype().castValue(1));
        assertTrue(ofType.seq(Shape.of(4,1,2)).add_(ofType.dtype().castValue(2)).deepEquals(t1));

        Tensor<N> t2 = ofType.seq(Shape.of(4, 2, 1));
        Tensor<N> exp2 = t2.expand(2, 2);
        assertTrue(t2.deepEquals(exp2.narrow(2, true, 0, 1)));
        assertTrue(t2.deepEquals(exp2.narrow(2, true, 1, 2)));
        assertEquals(Shape.of(4,2,2), exp2.shape());
        // add 1, which should be added twice
        exp2.add_(ofType.dtype().castValue(1));
        assertTrue(ofType.seq(Shape.of(4,2,1)).add_(ofType.dtype().castValue(2)).deepEquals(t2));

        Tensor<N> t3 = ofType.seq(Shape.of(1, 2, 4));
        Tensor<N> exp3 = t3.expand(0, 2);
        assertTrue(t3.deepEquals(exp3.narrow(0, true, 0, 1)));
        assertTrue(t3.deepEquals(exp3.narrow(0, true, 1, 2)));
        assertEquals(Shape.of(2,2,4), exp3.shape());
        // add 1, which should be added twice
        exp3.add_(ofType.dtype().castValue(1));
        assertTrue(ofType.seq(Shape.of(1,2,4)).add_(ofType.dtype().castValue(2)).deepEquals(t3));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> ofType.seq(Shape.of(2,3,4)).expand(0, 10));
        assertEquals("Dimension 0 must have size 1, but have size 2.", e.getMessage());
    }

}
