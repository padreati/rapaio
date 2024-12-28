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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import rapaio.darray.Shape;
import rapaio.nn.Autograd;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class MulTest extends AbstractTensorTest {

    @ParameterizedTest
    @MethodSource("managers")
    void testScalar(TensorManager tm) {

        Tensor a = tm.scalarTensor(1).requiresGrad(true).name("a");
        Tensor b = tm.scalarTensor(3).requiresGrad(true).name("b");

        Tensor sum = a.mul(b);
        sum.setGrad(tm.scalarArray(1));

        var graph = Autograd.backward(sum);
        assertTrue(a.grad().deepEquals(tm.scalarArray(3)));
        assertTrue(b.grad().deepEquals(tm.scalarArray(1)));
        graph.resetGrad();

        Tensor s = a.mul(2);
        s.setGrad(tm.scalarArray(2));

        graph = Autograd.backward(s);
        assertTrue(a.grad().deepEquals(tm.scalarArray(4.)));
        graph.resetGrad();
    }

    @ParameterizedTest
    @MethodSource("managers")
    void test1D(TensorManager tm) {
        Tensor a = tm.seqTensor(Shape.of(4)).requiresGrad(true).name("a");
        Tensor b = tm.scalarTensor(1).requiresGrad(true).name("b");

        Tensor s1 = a.mul(b).sum();
        s1.setGrad(tm.scalarArray(1));

        var graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tm.strideArray(Shape.of(4), 1, 1, 1, 1)));
        assertTrue(b.grad().deepEquals(tm.scalarArray(6)));
        graph.resetGrad();

        Tensor s2 = b.mul(a).sum();
        s2.setGrad(tm.scalarArray(1));
        graph = Autograd.backward(s2);
        assertTrue(a.grad().deepEquals(tm.strideArray(Shape.of(4), 1, 1, 1, 1)));
        assertTrue(b.grad().deepEquals(tm.scalarArray(6)));
        graph.resetGrad();
    }

    @ParameterizedTest
    @MethodSource("managers")
    void test2D(TensorManager tm) {
        Tensor a = tm.seqTensor(Shape.of(4, 3)).requiresGrad(true).name("a");
        Tensor b = tm.scalarTensor(1).requiresGrad(true).name("b");

        Tensor s1 = a.mul(b).sum();
        s1.setGrad(tm.fullArray(s1.shape(), 1));

        Autograd.ComputeGraph graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), b.size())));
        assertTrue(b.grad().deepEquals(tm.fullArray(b.shape(), 66)));
        graph.resetGrad();

        Tensor s2 = b.mul(a).sum();
        s2.setGrad(tm.fullArray(s2.shape(), 1));

        graph = Autograd.backward(s2);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), b.size())));
        assertTrue(b.grad().deepEquals(tm.fullArray(b.shape(), 66)));
        graph.resetGrad();

        Tensor c = tm.seqTensor(Shape.of(4, 1)).requiresGrad(true).name("c");
        Tensor s3 = a.mul(c).sum();
        s3.setGrad(tm.fullArray(s3.shape(), 1));

        graph = Autograd.backward(s3);
        assertTrue(a.grad().deepEquals(tm.seqArray(Shape.of(4)).strexp(1, 3)));
        assertTrue(c.grad().deepEquals(tm.strideArray(Shape.of(4, 1), 3, 12, 21, 30)));
        graph.resetGrad();

        Tensor s4 = c.mul(a).sum();
        s4.setGrad(tm.fullArray(s4.shape(), 1));

        graph = Autograd.backward(s4);
        assertTrue(a.grad().deepEquals(tm.seqArray(Shape.of(4)).strexp(1, 3)));
        assertTrue(c.grad().deepEquals(tm.strideArray(Shape.of(4, 1), 3, 12, 21, 30)));
        graph.resetGrad();

        Tensor d = tm.seqTensor(Shape.of(4));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> d.mul(a));
        assertEquals("Nodes are not valid for elementwise broadcast.", ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource("managers")
    void test4D(TensorManager tm) {
        Tensor a = tm.seqTensor(Shape.of(4, 3, 4, 3)).requiresGrad(true).name("a");
        Tensor b = tm.scalarTensor(1).requiresGrad(true).name("b");

        Tensor s1 = a.mul(b).sum();
        s1.setGrad(tm.fullArray(s1.shape(), 1));

        Autograd.ComputeGraph graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), b.size())));
        assertTrue(b.grad().deepEquals(tm.scalarArray(a.value().sum().doubleValue())));
        graph.resetGrad();

        Tensor s2 = b.mul(a).sum();
        s2.setGrad(tm.fullArray(s2.shape(), 1));

        graph = Autograd.backward(s2);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), b.size())));
        assertTrue(b.grad().deepEquals(tm.scalarArray(a.value().sum().doubleValue())));
        graph.resetGrad();

        Tensor c = tm.seqTensor(Shape.of(4, 1)).requiresGrad(true).name("c");
        Tensor s3 = a.mul(c).sum();
        s3.setGrad(tm.fullArray(s3.shape(), 1));

        graph = Autograd.backward(s3);
        assertTrue(a.grad().deepEquals(c.value().expand(1, 3).strexp(0, 3).strexp(0, 4)));
        assertTrue(c.grad().deepEquals(tm.strideArray(Shape.of(4, 1), 2412, 2520, 2628, 2736)));
        graph.resetGrad();

        Tensor s4 = c.mul(a).sum();
        s4.setGrad(tm.fullArray(s4.shape(), 1));

        graph = Autograd.backward(s4);
        assertTrue(a.grad().deepEquals(c.value().expand(1, 3).strexp(0, 3).strexp(0, 4)));
        assertTrue(c.grad().deepEquals(tm.strideArray(Shape.of(4, 1), 2412, 2520, 2628, 2736)));
        graph.resetGrad();

        Tensor d = tm.seqTensor(Shape.of(4));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> d.mul(a));
        assertEquals("Nodes are not valid for elementwise broadcast.", ex.getMessage());
    }
}
