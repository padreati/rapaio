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

import org.junit.jupiter.api.Test;

import rapaio.nn.Autograd;
import rapaio.nn.Tensor;
import rapaio.math.narray.NArrayManager;
import rapaio.math.narray.Shape;

public class Mean1dOpTest {

    private final NArrayManager.OfType<?> tmt = NArrayManager.base().ofDouble();

    @Test
    void testOpAxisMeanScalarInput() {
        Tensor x = Autograd.var(tmt.stride(Shape.of(), 3)).requiresGrad(true).name("x");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> x.mean1d(0));
        assertEquals("Input node must have at least one dimension.", ex.getMessage());
    }

    @Test
    void testOpAxisMeanVectorInput() {
        Tensor x = Autograd.var(tmt.stride(Shape.of(3), 1, 3, 4)).requiresGrad(true).name("x");
        Tensor m = x.mean1d(0);
        m.setGrad(tmt.scalar(1));

        Autograd.backward(m);

        assertNotNull(m.grad());
        assertNotNull(x.grad());

        assertEquals(x.grad().shape(), Shape.of(3));
        double value = m.grad().getDouble() / x.value().dim(0);
        assertTrue(x.grad().deepEquals(tmt.stride(Shape.of(3), value, value, value)));
    }

    @Test
    void testOpAxisMeanMatrixInput() {
        Tensor x = Autograd.var(tmt.seq(Shape.of(3, 4))).requiresGrad(true).name("x");

        Tensor m0 = x.mean1d(0).name("m0");
        m0.setGrad(tmt.stride(Shape.of(4), 1, 2, 3, 4));
        var graph = Autograd.backward(m0);
        assertEquals(x.grad().shape(), x.value().shape());
        assertTrue(x.grad().deepEquals(tmt.stride(Shape.of(4), 1, 2, 3, 4).div_(3).strexp(0, 3)));
        graph.covered().forEach(Tensor::zeroGrad);

        Tensor m1 = x.mean1d(1).name("m1");
        m1.setGrad(tmt.stride(Shape.of(3), 2, 3, 4));
        Autograd.backward(m1);
        assertEquals(x.grad().shape(), x.value().shape());
        assertTrue(x.grad().deepEquals(tmt.stride(Shape.of(3), 2, 3, 4).div_(x.value().dim(1)).strexp(1, 4)));
    }

    @Test
    void testOpAxisMeanTensorInput() {
        Tensor x = Autograd.var(tmt.seq(Shape.of(3, 4, 3, 4))).requiresGrad(true).name("x");

        for (int axis = 0; axis < 4; axis++) {
            Tensor m = x.mean1d(axis);
            m.setGrad(tmt.full(m.value().shape(), 1));
            Autograd.backward(m);
            assertEquals(x.grad().shape(), x.value().shape());
            assertTrue(x.grad().deepEquals(tmt.full(x.value().shape(), 1./x.value().dim(axis))));
            x.zeroGrad();
        }
    }
}
