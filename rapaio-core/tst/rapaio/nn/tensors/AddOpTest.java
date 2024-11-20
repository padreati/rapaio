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

import org.junit.jupiter.api.Test;

import rapaio.narray.Shape;
import rapaio.nn.Autograd;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class AddOpTest {

    private final TensorManager tm = TensorManager.ofDouble();

    @Test
    void testScalar() {

        Tensor a = tm.scalarTensor(1).requiresGrad(true).name("a");
        Tensor b = tm.scalarTensor(3).requiresGrad(true).name("b");

        Tensor sum = a.add(b);
        sum.setGrad(tm.scalarTensor(1).value());

        var graph = Autograd.backward(sum);
        assertTrue(a.grad().deepEquals(tm.scalarArray(1.)));
        assertTrue(b.grad().deepEquals(tm.scalarArray(1.)));
        graph.resetGrad();

        Tensor s = a.add(2);
        s.setGrad(tm.scalarTensor(2).value());

        graph = Autograd.backward(s);
        assertTrue(a.grad().deepEquals(tm.scalarArray(2.)));
        graph.resetGrad();
    }

    @Test
    void test1D() {
        Tensor a = tm.seqTensor(Shape.of(4)).requiresGrad(true).name("a");
        Tensor b = tm.scalarTensor(1).requiresGrad(true).name("b");

        Tensor s1 = a.add(b).sum();
        s1.setGrad(tm.scalarTensor(1).value());

        var graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tm.strideArray(Shape.of(4), 1, 1, 1, 1)));
        assertTrue(b.grad().deepEquals(tm.scalarArray(4)));
        graph.resetGrad();

        Tensor s2 = b.add(a).sum();
        s2.setGrad(tm.scalarTensor(1).value());
        graph = Autograd.backward(s2);
        assertTrue(a.grad().deepEquals(tm.strideArray(Shape.of(4), 1, 1, 1, 1)));
        assertTrue(b.grad().deepEquals(tm.scalarArray(4)));
        graph.resetGrad();
    }

    @Test
    void test2D() {
        Tensor a = tm.seqTensor(Shape.of(4, 3)).requiresGrad(true).name("a");
        Tensor b = tm.scalarTensor(1).requiresGrad(true).name("b");

        Tensor s1 = a.add(b).sum();
        s1.setGrad(tm.fullTensor(s1.shape(), 1).value());

        Autograd.ComputeGraph graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), b.size())));
        assertTrue(b.grad().deepEquals(tm.fullArray(b.shape(), a.size())));
        graph.resetGrad();

        Tensor s2 = b.add(a).sum();
        s2.setGrad(tm.fullTensor(s2.value().shape(), 1).value());

        graph = Autograd.backward(s2);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), b.size())));
        assertTrue(b.grad().deepEquals(tm.fullArray(b.shape(), a.size())));
        graph.resetGrad();

        Tensor c = tm.seqTensor(Shape.of(4, 1)).requiresGrad(true).name("c");
        Tensor s3 = a.add(c).sum();
        s3.setGrad(tm.fullTensor(s3.shape(), 1).value());

        graph = Autograd.backward(s3);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), 1)));
        assertTrue(c.grad().deepEquals(tm.fullArray(c.shape(), a.size() / c.size())));
        graph.resetGrad();

        Tensor s4 = c.add(a).sum();
        s4.setGrad(tm.fullTensor(s4.shape(), 1).value());

        graph = Autograd.backward(s4);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), 1)));
        assertTrue(c.grad().deepEquals(tm.fullArray(c.shape(), a.size() / c.size())));
        graph.resetGrad();

        Tensor d = tm.seqTensor(Shape.of(4));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> d.add(a));
        assertEquals("Nodes are not valid for elementwise broadcast.", ex.getMessage());
    }

    @Test
    void test4D() {
        Tensor a = tm.seqTensor(Shape.of(4, 3, 4, 3)).requiresGrad(true).name("a");
        Tensor b = tm.scalarTensor(1).requiresGrad(true).name("b");

        Tensor s1 = a.add(b).sum();
        s1.setGrad(tm.fullTensor(s1.shape(), 1).value());

        Autograd.ComputeGraph graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), b.size())));
        assertTrue(b.grad().deepEquals(tm.fullArray(b.shape(), a.size())));
        graph.resetGrad();

        Tensor s2 = b.add(a).sum();
        s2.setGrad(tm.fullTensor(s2.value().shape(), 1).value());

        graph = Autograd.backward(s2);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), b.size())));
        assertTrue(b.grad().deepEquals(tm.fullArray(b.shape(), a.size())));
        graph.resetGrad();

        Tensor c = tm.seqTensor(Shape.of(4, 1)).requiresGrad(true).name("c");
        Tensor s3 = a.add(c).sum();
        s3.setGrad(tm.fullTensor(s3.shape(), 1).value());

        graph = Autograd.backward(s3);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), 1)));
        assertTrue(c.grad().deepEquals(tm.fullArray(c.shape(), a.size() / c.size())));
        graph.resetGrad();

        Tensor s4 = c.add(a).sum();
        s4.setGrad(tm.fullTensor(s4.value().shape(), 1).value());

        graph = Autograd.backward(s4);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), 1)));
        assertTrue(c.grad().deepEquals(tm.fullArray(c.shape(), a.size() / c.size())));
        graph.resetGrad();

        Tensor d = tm.seqTensor(Shape.of(4));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> d.add(a));
        assertEquals("Nodes are not valid for elementwise broadcast.", ex.getMessage());
    }
}
