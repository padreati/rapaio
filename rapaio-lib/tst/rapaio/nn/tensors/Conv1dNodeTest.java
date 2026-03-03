/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import rapaio.darray.Shape;
import rapaio.nn.Autograd;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class Conv1dNodeTest {

    private static final double TOL = 1e-5;

    static Stream<TensorManager> tensorManagers() {
        return Stream.of(TensorManager.ofDouble());
    }

    @ParameterizedTest
    @MethodSource("tensorManagers")
    void testForwardAndBackward(TensorManager tm) {
        // x: (N=1, C_in=2, L=5)
        Tensor x = tm.strideTensor(Shape.of(2, 2, 5),
                1., 2., 3., 4., 5.,
                6., 7., 8., 9., 10.,
                11., 12., 13., 14., 15.,
                16., 17., 18., 19., 20.
        ).requiresGrad(true).name("x");

        // w: (C_out=2, C_in=2, k=3)
        Tensor w = tm.strideTensor(Shape.of(2, 2, 3),
                1., 0., -1.,
                1., 0., -1.,
                0., 1., 0.,
                0., 1., 0.)
                .requiresGrad(true).name("w");

        // b: (C_out=2)
        Tensor b = tm.strideTensor(Shape.of(2), 1., 2.)
                .requiresGrad(true).name("b");

        int padding = 0;
        int stride = 1;
        int dilation = 1;
        int groups = 1;

        Tensor y = new Conv1dNode(x, w, b, padding, stride, dilation, groups);

        // Forward: y shape (2, 2, 3)
        assertTrue(y.value().deepEquals(
                tm.strideArray(Shape.of(2, 2, 3),
                        -3, -3., -3.,
                        11., 13., 15.,
                        -3., -3., -3.,
                        31., 33., 35.),
                TOL), "forward output mismatch");

        // Backward: loss = sum(y)
        var sum = y.sum();
        sum.setGrad(tm.fullArray(sum.value().shape(), 1.));

        var graph = Autograd.backward(sum);

        // x.grad: (2, 2, 5)
        assertTrue(x.grad().deepEquals(
                tm.strideArray(Shape.of(2, 2, 5),
                        1., 2., 1., 0., -1.,
                        1., 2., 1., 0., -1.,
                        1., 2., 1., 0., -1.,
                        1., 2., 1., 0., -1.),
                TOL), "x gradient mismatch");

        // w.grad: (2, 2, 3)
        assertTrue(w.grad().deepEquals(
                tm.strideArray(Shape.of(2, 2, 3),
                        42., 48., 54.,
                        72., 78., 84.,
                        42., 48., 54.,
                        72., 78., 84.),
                TOL), "w gradient mismatch");

        // b.grad: (2)
        assertTrue(b.grad().deepEquals(
                tm.strideArray(Shape.of(2), 6., 6.),
                TOL), "b gradient mismatch");

        graph.resetGrad();
    }

    @ParameterizedTest
    @MethodSource("tensorManagers")
    void testForwardNoBias(TensorManager tm) {
        Tensor x = tm.strideTensor(Shape.of(1, 1, 6),
                        1., 2., 3., 4., 5., 6.)
                .requiresGrad(true).name("x");
        Tensor w = tm.strideTensor(Shape.of(1, 1, 3),
                        1., 2., 1.)
                .requiresGrad(true).name("w");

        int padding = 1;
        int stride = 2;
        int dilation = 1;
        int groups = 1;

        Tensor y = new Conv1dNode(x, w, null, padding, stride, dilation, groups);

        assertTrue(y.value().deepEquals(
                tm.strideArray(Shape.of(1, 1, 3),
                        4., 12., 20.),
                TOL), "forward no-bias stride=2 padding=1 mismatch");

        y.setGrad(tm.fullArray(y.value().shape(), 1.));
        var graph = Autograd.backward(y);

        assertTrue(x.grad().deepEquals(
                tm.strideArray(Shape.of(1, 1, 6),
                        2., 2., 2., 2., 2., 1.),
                TOL), "x gradient no-bias mismatch");

        assertTrue(w.grad().deepEquals(
                tm.strideArray(Shape.of(1, 1, 3),
                        6., 9., 12.),
                TOL), "w gradient no-bias mismatch");

        graph.resetGrad();
    }
}
