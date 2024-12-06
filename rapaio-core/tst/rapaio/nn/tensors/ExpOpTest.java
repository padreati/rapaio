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
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class ExpOpTest extends AbstractTensorTest {

    @ParameterizedTest
    @MethodSource("managers")
    void testExp(TensorManager tm) {
        Tensor x = tm.randomTensor(Shape.of(4,32), random).requiresGrad(true);
        Tensor exp = x.exp();
        Tensor sum = exp.sum();
        sum.setGrad(tm.scalarArray(2));

        Autograd.backward(sum);

        assertNotNull(x.grad());
        assertEquals(x.grad().shape(), x.value().shape());

        var valIt = x.value().iterator(Order.C);
        var gradIt = x.grad().iterator(Order.C);

        while (valIt.hasNext()) {
            assertEquals(Math.exp(valIt.next().doubleValue())*2, gradIt.next().doubleValue(), 1e-6);
        }
    }
}
