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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.math.tensor.DTensor;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorEngine;
import rapaio.math.tensor.engine.parallel.ParallelTensorEngine;
import rapaio.math.tensor.engine.base.BaseTensorEngine;
import rapaio.math.tensor.storage.array.ArrayStorageFactory;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TensorEngineTest {

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void mainTestLoop() {
        testManager(new BaseTensorEngine(new ArrayStorageFactory()));
        testManager(new ParallelTensorEngine(new ArrayStorageFactory()));
    }

    void testManager(TensorEngine manager) {
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

    void testOfDoubleZeros(TensorEngine manager) {
        DTensor t = manager.ofDoubleZeros(Shape.of(10, 20));
        assertEquals(manager, t.manager());
        assertEquals(t.shape(), Shape.of(10, 20));
        var it = t.pointerIterator();
        while (it.hasNext()) {
            assertEquals(0, t.storage().get(it.nextInt()));
        }
    }

    void testOfDoubleSeq(TensorEngine manager) {
        var t = manager.ofDoubleSeq(Shape.of(2, 3, 4));
        var it = t.pointerIterator(Order.C);
        int i = 0;
        while (it.hasNext()) {
            assertEquals(i++, t.storage().get(it.nextInt()));
        }
    }

    void testOfDoubleRandom(TensorEngine manager) {
        var t = manager.ofDoubleRandom(Shape.of(3, 4), random);
        var it = t.pointerIterator(Order.C);
        double last = t.storage().get(it.nextInt());
        while (it.hasNext()) {
            double next = t.storage().get(it.nextInt());
            assertNotEquals(last, next);
            last = next;
        }
    }

    void testOfDoubleStride(TensorEngine manager) {
        var t = manager.ofDoubleStride(Shape.of(4, 3), 0, new int[] {1, 4}, manager.storageFactory().ofDoubleSeq(0, 12));
        int i = 0;
        var it = t.pointerIterator(Order.F);
        while(it.hasNext()) {
            assertEquals(i++, t.storage().get(it.nextInt()));
        }
    }

    void testOfDoubleWrap(TensorEngine manager) {
        var t = manager.ofDoubleWrap(Shape.of(2,3), new double[] {1., 2, 3, 4, 5, 6}, Order.C);
        int i=1;
        var it = t.pointerIterator(Order.C);
        while(it.hasNext()) {
            assertEquals(i++, t.storage().get(it.nextInt()));
        }
    }

    void testOfFloatZeros(TensorEngine manager) {
        var t = manager.ofFloatZeros(Shape.of(10, 20));
        assertEquals(t.shape(), Shape.of(10, 20));
        var it = t.pointerIterator();
        while (it.hasNext()) {
            assertEquals(0, t.storage().get(it.nextInt()));
        }
    }

    void testOfFloatSeq(TensorEngine manager) {
        var t = manager.ofFloatSeq(Shape.of(2, 3, 4));
        var it = t.pointerIterator(Order.C);
        int i = 0;
        while (it.hasNext()) {
            assertEquals(i++, t.storage().get(it.nextInt()));
        }
    }

    void testOfFloatRandom(TensorEngine manager) {
        var t = manager.ofFloatRandom(Shape.of(3, 4), random);
        var it = t.pointerIterator(Order.C);
        double last = t.storage().get(it.nextInt());
        while (it.hasNext()) {
            float next = t.storage().get(it.nextInt());
            assertNotEquals(last, next);
            last = next;
        }
    }

    void testOfFloatStride(TensorEngine manager) {
        var t = manager.ofFloatStride(Shape.of(4, 3), 0, new int[] {1, 4}, manager.storageFactory().ofFloatSeq(0, 12));
        int i = 0;
        var it = t.pointerIterator(Order.F);
        while(it.hasNext()) {
            assertEquals(i++, t.storage().get(it.nextInt()));
        }
    }

    void testOfFloatWrap(TensorEngine manager) {
        var t = manager.ofFloatWrap(Shape.of(2,3), new float[] {1.f, 2.f, 3.f, 4.f, 5.f, 6.f}, Order.C);
        int i=1;
        var it = t.pointerIterator(Order.C);
        while(it.hasNext()) {
            assertEquals(i++, t.storage().get(it.nextInt()));
        }
    }
}
