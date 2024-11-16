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

package rapaio.nn.operations;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.nn.Autograd;
import rapaio.nn.Tensor;
import rapaio.math.narray.DType;
import rapaio.math.narray.NArrayManager;
import rapaio.math.narray.Shape;

public class NegOpTest {

    private final NArrayManager.OfType<?> tm = NArrayManager.base().ofDouble();

    @Test
    void testScalar() {

        Tensor a = Autograd.scalar(DType.DOUBLE, 1).requiresGrad(true).name("a");

        Tensor sum = a.neg().sum();
        sum.setGrad(tm.scalar(1));

        var graph = Autograd.backward(sum);
        assertTrue(a.grad().deepEquals(tm.scalar(-1)));
        graph.resetGrad();
    }

    @Test
    void test1D() {
        Tensor a = Autograd.var(tm.seq(Shape.of(4))).requiresGrad(true).name("a");

        Tensor s1 = a.neg().sum();
        s1.setGrad(tm.scalar(1));

        var graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tm.full(Shape.of(4), -1)));
        graph.resetGrad();
    }

    @Test
    void test2D() {
        Tensor a = Autograd.var(tm.seq(Shape.of(4, 3)).sub_(6)).requiresGrad(true).name("a");

        Tensor s1 = a.neg().sum();
        s1.setGrad(tm.full(s1.shape(), 1));

        Autograd.ComputeGraph graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tm.full(a.shape(), -1)));
        graph.resetGrad();
    }

    @Test
    void test4D() {
        Tensor a = Autograd.var(tm.seq(Shape.of(4, 3, 4, 3))).requiresGrad(true).name("a");

        Tensor s1 = a.neg().sum();
        s1.setGrad(tm.full(s1.shape(), 1));

        Autograd.ComputeGraph graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tm.full(a.shape(), -1)));
        graph.resetGrad();
    }
}
