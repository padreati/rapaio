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

package rapaio.math.tensor.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorFactory;
import rapaio.math.tensor.factory.basearray.BaseArrayTensorFactory;
import rapaio.math.tensor.factory.parallelarray.ParallelArrayTensorFactory;

public class TensorFactoryTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void mainTestLoop() {
        testManager(new BaseArrayTensorFactory());
        testManager(new ParallelArrayTensorFactory());
    }

    void testManager(TensorFactory manager) {
        testOfDoubleZeros(manager);
        testOfDoubleSeq(manager);
        testOfDoubleRandom(manager);
        testOfDoubleStride(manager);
        testOfDoubleWrap(manager);

        testOfFloatZeros(manager);
        testOfFloatSeq(manager);
        testOfFloatRandom(manager);
        testOfFloatStride(manager);
        testOfFloatWrap(manager);
    }

    void testOfDoubleZeros(TensorFactory manager) {
        DTensor t = manager.ofDouble().zeros(Shape.of(10, 20));
        assertEquals(manager, t.factory());
        assertEquals(t.shape(), Shape.of(10, 20));
        var it = t.pointerIterator();
        while (it.hasNext()) {
            assertEquals(0, t.ptrGet(it.nextInt()));
        }
    }

    void testOfDoubleSeq(TensorFactory manager) {
        var t = manager.ofDouble().seq(Shape.of(2, 3, 4));
        var it = t.pointerIterator(Order.C);
        int i = 0;
        while (it.hasNext()) {
            assertEquals(i++, t.ptrGet(it.nextInt()));
        }
    }

    void testOfDoubleRandom(TensorFactory manager) {
        var t = manager.ofDouble().random(Shape.of(3, 4), random);
        var it = t.pointerIterator(Order.C);
        double last = t.ptrGet(it.nextInt());
        while (it.hasNext()) {
            double next = t.ptrGet(it.nextInt());
            assertNotEquals(last, next);
            last = next;
        }
    }

    void testOfDoubleStride(TensorFactory manager) {
        double[] seq = new double[12];
        for (int i = 0; i < seq.length; i++) {
            seq[i] = i;
        }
        var t = manager.ofDouble().stride(Shape.of(4, 3), 0, new int[] {1, 4}, seq);
        int i = 0;
        var it = t.pointerIterator(Order.F);
        while (it.hasNext()) {
            assertEquals(i++, t.ptrGet(it.nextInt()));
        }
    }

    void testOfDoubleWrap(TensorFactory manager) {
        var t = manager.ofDouble().wrap(Shape.of(2, 3), new double[] {1., 2, 3, 4, 5, 6}, Order.C);
        int i = 1;
        var it = t.pointerIterator(Order.C);
        while (it.hasNext()) {
            assertEquals(i++, t.ptrGet(it.nextInt()));
        }
    }

    void testOfFloatZeros(TensorFactory manager) {
        var t = manager.ofFloat().zeros(Shape.of(10, 20));
        assertEquals(t.shape(), Shape.of(10, 20));
        var it = t.pointerIterator();
        while (it.hasNext()) {
            assertEquals(0, t.ptrGet(it.nextInt()));
        }
    }

    void testOfFloatSeq(TensorFactory manager) {
        var t = manager.ofFloat().seq(Shape.of(2, 3, 4));
        var it = t.pointerIterator(Order.C);
        int i = 0;
        while (it.hasNext()) {
            assertEquals(i++, t.ptrGet(it.nextInt()));
        }
    }

    void testOfFloatRandom(TensorFactory manager) {
        var t = manager.ofFloat().random(Shape.of(3, 4), random);
        var it = t.pointerIterator(Order.C);
        double last = t.ptrGet(it.nextInt());
        while (it.hasNext()) {
            float next = t.ptrGet(it.nextInt());
            assertNotEquals(last, next);
            last = next;
        }
    }

    void testOfFloatStride(TensorFactory manager) {
        float[] seq = new float[12];
        for (int i = 0; i < seq.length; i++) {
            seq[i] = (float) i;
        }
        var t = manager.ofFloat().stride(Shape.of(4, 3), 0, new int[] {1, 4}, seq);
        int i = 0;
        var it = t.pointerIterator(Order.F);
        while (it.hasNext()) {
            assertEquals(i++, t.ptrGet(it.nextInt()));
        }
    }

    void testOfFloatWrap(TensorFactory manager) {
        var t = manager.ofFloat().wrap(Shape.of(2, 3), new float[] {1.f, 2.f, 3.f, 4.f, 5.f, 6.f}, Order.C);
        int i = 1;
        var it = t.pointerIterator(Order.C);
        while (it.hasNext()) {
            assertEquals(i++, t.ptrGet(it.nextInt()));
        }
    }
}
