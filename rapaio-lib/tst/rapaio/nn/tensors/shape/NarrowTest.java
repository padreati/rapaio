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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import rapaio.darray.Shape;
import rapaio.nn.Autograd;
import rapaio.nn.TensorManager;

public class NarrowTest {

    private final TensorManager tm = TensorManager.ofFloat().seed(42);

    @Test
    void testNarrow() {
        var t1 = tm.randomTensor(Shape.of(3,4,5)).requiresGrad(true).name("t1");
        for(int axis = 0; axis < 3; axis++) {
            var t2 = t1.narrow(axis, 1, 2).name("t2");
            t2.setGrad(tm.fullArray(t2.shape(), 1));
            var graph = Autograd.backward(t2);

            assertNotNull(t1.grad());
            assertEquals(t2.grad().sum().doubleValue(), t1.grad().sum().doubleValue());
            assertEquals(t2.grad().sum().doubleValue(), t1.grad().narrow(axis, 1, 2).sum().doubleValue());

            graph.resetGrad();
        }
    }
}
