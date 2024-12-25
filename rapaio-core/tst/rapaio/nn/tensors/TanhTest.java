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

package rapaio.nn.tensors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import rapaio.darray.Order;
import rapaio.darray.Shape;
import rapaio.nn.Autograd;
import rapaio.nn.TensorManager;

public class TanhTest extends AbstractTensorTest {

    @ParameterizedTest
    @MethodSource("managers")
    void testTanhOp(TensorManager tm) {
        var x = tm.randomTensor(Shape.of(32, 5), random).requiresGrad(true);
        var tanh = x.tanh();
        var sum = tanh.sum();
        sum.setGrad(tm.scalarArray(1));

        Autograd.backward(sum);

        assertNotNull(x.grad());
        assertEquals(x.value().shape(), x.grad().shape());

        var valIt = x.value().iterator(Order.C);
        var gradIt = x.grad().iterator(Order.C);
        var tanhIt = tanh.value().iterator(Order.C);

        while (valIt.hasNext()) {
            double value = valIt.next().doubleValue();
            assertEquals(1 - Math.tanh(value) * Math.tanh(value), gradIt.next().doubleValue(), 1e-6);
            assertEquals(Math.tanh(value), tanhIt.next().doubleValue(), 1e-6);
        }
    }
}
