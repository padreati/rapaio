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

package rapaio.math.nn.operations;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.math.nn.Autograd;
import rapaio.math.nn.Node;
import rapaio.math.tensor.DType;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorManager;

public class OpAxisAddTest {

    private final TensorManager.OfType<?> tmt = TensorManager.base().ofDouble();

    @Test
    void testScalar() {

        Node a = Autograd.scalar(DType.DOUBLE, 1).requiresGrad(true).name("a");
        Node b = Autograd.scalar(DType.DOUBLE, 3).requiresGrad(true).name("b");

        Node sum = a.add(b);
        sum.setGrad(tmt.scalar(1));

        var graph = Autograd.backward(sum);
        assertTrue(a.grad().deepEquals(tmt.scalar(1.)));
        assertTrue(b.grad().deepEquals(tmt.scalar(1.)));
        graph.resetGrad();

        Node s = a.add(2);
        s.setGrad(tmt.scalar(2));

        graph = Autograd.backward(s);
        assertTrue(a.grad().deepEquals(tmt.scalar(2.)));
        graph.resetGrad();
    }

    @Test
    void testVector() {
        Node a = Autograd.var(tmt.seq(Shape.of(4))).requiresGrad(true).name("a");
        Node b = Autograd.var(tmt.full(Shape.of(), 1.)).requiresGrad(true).name("b");

        Node s1 = a.add(b).sum();
        s1.setGrad(tmt.scalar(1));

        var graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tmt.stride(Shape.of(4), 1, 1, 1, 1)));
        assertTrue(b.grad().deepEquals(tmt.scalar(4)));
        graph.resetGrad();

        Node s2 = b.add(a).sum();
        s2.setGrad(tmt.scalar(1));
        graph = Autograd.backward(s2);
        assertTrue(a.grad().deepEquals(tmt.stride(Shape.of(4), 1, 1, 1, 1)));
        assertTrue(b.grad().deepEquals(tmt.scalar(4)));
        graph.resetGrad();
    }

}
