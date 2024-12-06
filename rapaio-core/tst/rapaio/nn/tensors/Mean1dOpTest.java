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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import rapaio.darray.Shape;
import rapaio.nn.Autograd;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class Mean1dOpTest extends AbstractTensorTest {

    @ParameterizedTest
    @MethodSource("managers")
    void testOpAxisMeanScalarInput(TensorManager tm) {
        Tensor x = tm.strideTensor(Shape.of(), 3).requiresGrad(true).name("x");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> x.mean1d(0));
        assertEquals("Input node must have at least one dimension.", ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource("managers")
    void testOpAxisMeanVectorInput(TensorManager tm) {
        Tensor x = tm.strideTensor(Shape.of(3), 1, 3, 4).requiresGrad(true).name("x");
        Tensor m = x.mean1d(0);
        m.setGrad(tm.scalarArray(1));

        Autograd.backward(m);

        assertNotNull(m.grad());
        assertNotNull(x.grad());

        assertEquals(x.grad().shape(), Shape.of(3));
        double value = m.grad().getDouble() / x.value().dim(0);
        assertTrue(x.grad().deepEquals(tm.strideArray(Shape.of(3), value, value, value)));
    }

    @ParameterizedTest
    @MethodSource("managers")
    void testOpAxisMeanMatrixInput(TensorManager tm) {
        Tensor x = tm.seqTensor(Shape.of(3, 4)).requiresGrad(true).name("x");

        Tensor m0 = x.mean1d(0).name("m0");
        m0.setGrad(tm.strideArray(Shape.of(4), 1, 2, 3, 4));
        var graph = Autograd.backward(m0);
        assertEquals(x.grad().shape(), x.value().shape());
        assertTrue(x.grad().deepEquals(tm.strideArray(Shape.of(4), 1, 2, 3, 4).div_(3).strexp(0, 3)));
        graph.covered().forEach(Tensor::zeroGrad);

        Tensor m1 = x.mean1d(1).name("m1");
        m1.setGrad(tm.strideArray(Shape.of(3), 2, 3, 4));
        Autograd.backward(m1);
        assertEquals(x.grad().shape(), x.value().shape());
        assertTrue(x.grad().deepEquals(tm.strideArray(Shape.of(3), 2, 3, 4).div_(x.value().dim(1)).strexp(1, 4)));
    }

    @ParameterizedTest
    @MethodSource("managers")
    void testOpAxisMeanTensorInput(TensorManager tm) {
        Tensor x = tm.seqTensor(Shape.of(3, 4, 3, 4)).requiresGrad(true).name("x");

        for (int axis = 0; axis < 4; axis++) {
            Tensor m = x.mean1d(axis);
            m.setGrad(tm.fullArray(m.value().shape(), 1));
            Autograd.backward(m);
            assertEquals(x.grad().shape(), x.value().shape());
            assertTrue(x.grad().deepEquals(tm.fullArray(x.value().shape(), 1. / x.value().dim(axis))));
            x.zeroGrad();
        }
    }
}
