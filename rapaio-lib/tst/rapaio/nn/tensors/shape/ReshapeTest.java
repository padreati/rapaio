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

import org.junit.jupiter.api.Test;

import rapaio.darray.Shape;
import rapaio.nn.Autograd;
import rapaio.nn.TensorManager;

public class ReshapeTest {

    final TensorManager tm = TensorManager.ofFloat().seed(12345L);

    @Test
    void testReshape() {
        var t1 = tm.randomTensor(Shape.of(2,3,4)).requiresGrad(true);
        var t2 = t1.reshape(Shape.of(2,12));
        t2.setGrad(tm.randomArray(t2.shape()));
        Autograd.backward(t2);

        assertNotNull(t1.grad());
        assertTrue(t1.grad().deepEquals(t2.grad().reshape(t1.shape())));
    }
}
