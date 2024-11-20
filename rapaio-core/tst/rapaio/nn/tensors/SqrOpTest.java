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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import rapaio.narray.Shape;
import rapaio.nn.Autograd;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class SqrOpTest extends AbstractTensorTest {

    @ParameterizedTest
    @MethodSource("managers")
    void testScalar(TensorManager tm) {

        Tensor a = tm.scalarTensor(1).requiresGrad(true).name("a");

        Tensor sum = a.sqr().sum();
        sum.setGrad(tm.scalarArray(1));

        var graph = Autograd.backward(sum);
        assertTrue(a.grad().deepEquals(tm.scalarArray(2)));
        graph.resetGrad();
    }

    @ParameterizedTest
    @MethodSource("managers")
    void test1D(TensorManager tm) {
        Tensor a = tm.seqTensor(Shape.of(4)).requiresGrad(true).name("a");
        Tensor sqr = a.sqr();

        Tensor s1 = sqr.sum();
        s1.setGrad(tm.scalarArray(1));

        var graph = Autograd.backward(s1);
        assertTrue(sqr.value().deepEquals(tm.strideArray(Shape.of(4), 0, 1, 4, 9)));
        assertTrue(a.grad().deepEquals(tm.strideArray(Shape.of(4), 0, 2, 4, 6)));
        graph.resetGrad();
    }

    @ParameterizedTest
    @MethodSource("managers")
    void test2D(TensorManager tm) {
        Tensor a = tm.var(tm.seqArray(Shape.of(4, 3)).sub_(6)).requiresGrad(true).name("a");
        Tensor sqr = a.sqr();

        Tensor s1 = sqr.sum();
        s1.setGrad(tm.fullArray(s1.shape(), 1));

        Autograd.ComputeGraph graph = Autograd.backward(s1);
        assertTrue(sqr.value().deepEquals(tm.strideArray(Shape.of(4, 3), 36, 25, 16, 9, 4, 1, 0, 1, 4, 9, 16, 25)));
        assertTrue(a.grad().deepEquals(tm.strideArray(a.shape(), -12, -10, -8, -6, -4, -2, 0, 2, 4, 6, 8, 10)));
        graph.resetGrad();
    }

    @ParameterizedTest
    @MethodSource("managers")
    void test4D(TensorManager tm) {
        Tensor a = tm.seqTensor(Shape.of(2, 3, 2, 3)).requiresGrad(true).name("a");
        Tensor sqr = a.sqr();

        Tensor s1 = sqr.sum();
        s1.setGrad(tm.fullArray(s1.shape(), 1));

        Autograd.ComputeGraph graph = Autograd.backward(s1);
        assertTrue(sqr.value().deepEquals(tm.seqArray(Shape.of(2, 3, 2, 3)).sqr()));
        assertTrue(a.grad().deepEquals(a.value().mul(2)));
        graph.resetGrad();
    }
}
