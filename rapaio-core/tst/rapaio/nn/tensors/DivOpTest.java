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

public class DivOpTest {

    private final TensorManager tm = TensorManager.ofDouble();

    @Test
    void testScalar() {

        Tensor a = tm.scalarTensor(1).requiresGrad(true).name("a");
        Tensor b = tm.scalarTensor(3).requiresGrad(true).name("b");

        Tensor sum = a.div(b);
        sum.setGrad(tm.scalarTensor(1).value());

        var graph = Autograd.backward(sum);
        assertTrue(a.grad().deepEquals(tm.scalarArray(1. / 3)));
        assertTrue(b.grad().deepEquals(tm.scalarArray(-1. / 9)));
        graph.resetGrad();

        Tensor s = a.div(2);
        s.setGrad(tm.scalarTensor(2).value());

        graph = Autograd.backward(s);
        assertTrue(a.grad().deepEquals(tm.scalarArray(2 * 0.5)));
        graph.resetGrad();
    }

    @Test
    void test1D() {
        Tensor a = tm.seqTensor(Shape.of(4)).requiresGrad(true).name("a");
        Tensor b = tm.scalarTensor(1).requiresGrad(true).name("b");

        Tensor s1 = a.div(b).sum();
        s1.setGrad(tm.scalarTensor(1).value());

        var graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tm.strideArray(Shape.of(4), 1, 1, 1, 1)));
        assertTrue(b.grad().deepEquals(tm.scalarArray(-6)));
        graph.resetGrad();

        Tensor s2 = b.div(a).sum();
        s2.setGrad(tm.scalarTensor(1).value());
        graph = Autograd.backward(s2);
        assertTrue(a.grad().deepEquals(tm.strideArray(Shape.of(4), Double.NEGATIVE_INFINITY, -1, -1. / 4, -1. / 9)));
        assertTrue(b.grad().deepEquals(tm.scalarArray(Double.POSITIVE_INFINITY)));
        graph.resetGrad();
    }

    @Test
    void test2D() {
        Tensor a = tm.var(tm.seqArray(Shape.of(4, 3)).add_(1)).requiresGrad(true).name("a");
        Tensor b = tm.scalarTensor(1).requiresGrad(true).name("b");

        Tensor s1 = a.div(b).sum();
        s1.setGrad(tm.fullTensor(s1.shape(), 1).value());

        Autograd.ComputeGraph graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), b.size())));
        assertTrue(b.grad().deepEquals(tm.fullArray(b.shape(), -78)));
        graph.resetGrad();

        Tensor s2 = b.div(a).sum();
        s2.setGrad(tm.fullArray(s2.shape(), 1));

        graph = Autograd.backward(s2);
        assertTrue(a.grad().deepEquals(tm.scalarArray(1).div(a.value().sqr()).neg_()));
        assertTrue(b.grad().deepEquals(tm.scalarArray(tm.scalarArray(1).div(a.value()).sum().doubleValue()), 1e-12));
        graph.resetGrad();

        Tensor c = tm.var(tm.seqArray(Shape.of(4, 1)).add_(1)).requiresGrad(true).name("c");
        Tensor s3 = a.div(c).sum();
        s3.setGrad(tm.fullArray(s3.shape(), 1));

        graph = Autograd.backward(s3);
        assertTrue(a.grad().deepEquals(tm.scalarArray(1).div(tm.seqArray(Shape.of(4)).add_(1)).strexp(1, 3)));
        assertTrue(c.grad().deepEquals(tm.strideArray(Shape.of(4, 1), -6, -3.75, -2.6666666666666665, -2.0625), 1e-12));
        graph.resetGrad();

        Tensor s4 = c.div(a).sum();
        s4.setGrad(tm.fullArray(s4.shape(), 1));

        graph = Autograd.backward(s4);
        assertTrue(a.grad().deepEquals(c.value().neg().div(a.value().sqr())));
        assertTrue(c.grad().deepEquals(tm.scalarArray(1).div(a.value()).sum1d(1).stretch(1)));
        graph.resetGrad();

        Tensor d = tm.var(tm.seqArray(Shape.of(4)));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> d.div(a));
        assertEquals("Nodes are not valid for elementwise broadcast.", ex.getMessage());
    }

    @Test
    void test4D() {
        Tensor a = tm.seqTensor(Shape.of(4, 3, 4, 3)).requiresGrad(true).name("a");
        Tensor b = tm.scalarTensor(1).requiresGrad(true).name("b");

        Tensor s1 = a.div(b).sum();
        s1.setGrad(tm.fullArray(s1.shape(), 1));

        Autograd.ComputeGraph graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), b.size())));
        assertTrue(b.grad().deepEquals(tm.scalarArray(a.value().neg().div(b.value().sqr()).sum().doubleValue())));
        graph.resetGrad();

        Tensor s2 = b.div(a).sum();
        s2.setGrad(tm.fullArray(s2.shape(), 1));

        graph = Autograd.backward(s2);
        assertTrue(a.grad().deepEquals(tm.scalarArray(1).div(a.value().sqr()).neg_()));
        assertTrue(b.grad().deepEquals(tm.scalarArray(tm.scalarArray(1).div(a.value()).sum().doubleValue())));
        graph.resetGrad();

        Tensor c = tm.seqTensor(Shape.of(4, 1)).requiresGrad(true).name("c");
        Tensor s3 = a.div(c).sum();
        s3.setGrad(tm.fullArray(s3.shape(), 1));

        graph = Autograd.backward(s3);
        assertTrue(a.grad().deepEquals(tm.scalarArray(1).div(c.value()).expand(1, 3).strexp(0, 3).strexp(0, 4)));
        assertTrue(c.grad().deepEquals(tm.strideArray(Shape.of(4, 1), Double.NaN, -2520, -657, -304)));
        graph.resetGrad();

        Tensor s4 = c.div(a).sum();
        s4.setGrad(tm.fullArray(s4.shape(), 1));

        graph = Autograd.backward(s4);
        assertTrue(a.grad().deepEquals(c.value().neg().div(a.value().sqr())));
        assertTrue(c.grad().deepEquals(tm.strideArray(Shape.of(4, 1), Double.POSITIVE_INFINITY,1.4347934880782072,1.0300564484939163,0.8531336948688647)));
        graph.resetGrad();

        Tensor d = tm.seqTensor(Shape.of(4));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> d.div(a));
        assertEquals("Nodes are not valid for elementwise broadcast.", ex.getMessage());
    }
}
