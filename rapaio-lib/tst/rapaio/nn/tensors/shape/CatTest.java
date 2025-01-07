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

package rapaio.nn.tensors.shape;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.darray.Shape;
import rapaio.nn.Autograd;
import rapaio.nn.TensorManager;

public class CatTest {

    private TensorManager tm;

    @BeforeEach
    void beforeEach() {
        tm = TensorManager.ofFloat();
    }

    @Test
    void testVector() {
        var t1 = tm.randomTensor(Shape.of(4)).requiresGrad(true);
        var t2 = tm.randomTensor(Shape.of(3)).requiresGrad(true);
        var t3 = tm.cat(0, t1, t2);
        t3.setGrad(tm.randomArray(Shape.of(7)));
        Autograd.backward(t3);

        assertNotNull(t1.grad());
        assertNotNull(t2.grad());

        assertTrue(t3.grad().narrow(0, 0, 4).deepEquals(t1.grad()));
        assertTrue(t3.grad().narrow(0, 4, 7).deepEquals(t2.grad()));
    }

    @Test
    void testTensor() {
        var t1 = tm.randomTensor(Shape.of(4, 3, 2)).requiresGrad(true);
        var t2 = tm.randomTensor(Shape.of(4, 3, 2)).requiresGrad(true);

        for (int axis = 0; axis < 3; axis++) {
            var t3 = tm.cat(axis, t1, t2);

            t3.setGrad(tm.randomArray(t3.shape()));
            var graph = Autograd.backward(t3);

            assertNotNull(t1.grad());
            assertNotNull(t2.grad());

            assertTrue(t3.grad().narrow(axis, 0, t1.dim(axis)).deepEquals(t1.grad()));
            assertTrue(t3.grad().narrow(axis, t1.dim(axis), t1.dim(axis) + t2.dim(axis)).deepEquals(t2.grad()));

            graph.resetGrad();
        }

    }
}
