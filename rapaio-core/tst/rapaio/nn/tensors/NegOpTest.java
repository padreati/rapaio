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

import rapaio.darray.Shape;
import rapaio.nn.Autograd;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class NegOpTest extends AbstractTensorTest {

    @ParameterizedTest
    @MethodSource("managers")
    void testScalar(TensorManager tm) {

        Tensor a = tm.scalarTensor(1).requiresGrad(true).name("a");

        Tensor sum = a.neg().sum();
        sum.setGrad(tm.scalarArray(1));

        var graph = Autograd.backward(sum);
        assertTrue(a.grad().deepEquals(tm.scalarArray(-1)));
        graph.resetGrad();
    }

    @ParameterizedTest
    @MethodSource("managers")
    void test1D(TensorManager tm) {
        Tensor a = tm.seqTensor(Shape.of(4)).requiresGrad(true).name("a");

        Tensor s1 = a.neg().sum();
        s1.setGrad(tm.scalarArray(1));

        var graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tm.fullArray(Shape.of(4), -1)));
        graph.resetGrad();
    }

    @ParameterizedTest
    @MethodSource("managers")
    void test2D(TensorManager tm) {
        Tensor a = tm.var(tm.seqArray(Shape.of(4, 3)).sub_(6)).requiresGrad(true).name("a");

        Tensor s1 = a.neg().sum();
        s1.setGrad(tm.fullArray(s1.shape(), 1));

        Autograd.ComputeGraph graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), -1)));
        graph.resetGrad();
    }

    @ParameterizedTest
    @MethodSource("managers")
    void test4D(TensorManager tm) {
        Tensor a = tm.seqTensor(Shape.of(4, 3, 4, 3)).requiresGrad(true).name("a");

        Tensor s1 = a.neg().sum();
        s1.setGrad(tm.fullArray(s1.shape(), 1));

        Autograd.ComputeGraph graph = Autograd.backward(s1);
        assertTrue(a.grad().deepEquals(tm.fullArray(a.shape(), -1)));
        graph.resetGrad();
    }
}
