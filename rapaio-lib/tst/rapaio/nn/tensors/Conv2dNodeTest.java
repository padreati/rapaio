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

/**
 * Tests for Conv2d tensor verified against PyTorch:
 * <pre>
 * import torch, torch.nn.functional as F
 *
 * # testForwardAndBackward
 * x = torch.arange(1,37,dtype=torch.float64).reshape(2,2,3,3).requires_grad_(True)
 * w = torch.arange(1,17,dtype=torch.float64).reshape(2,2,2,2).requires_grad_(True)
 * b = torch.tensor([1.,2.],dtype=torch.float64).requires_grad_(True)
 * y = F.conv2d(x, w, b)
 * y.sum().backward()
 * print(y)        # forward
 * print(x.grad)   # grad_x
 * print(w.grad)   # grad_w
 * print(b.grad)   # grad_b
 *
 * # testForwardNoBias (padding=1, stride=2)
 * x2 = torch.arange(1,10,dtype=torch.float64).reshape(1,1,3,3).requires_grad_(True)
 * w2 = torch.ones(1,1,2,2,dtype=torch.float64).requires_grad_(True)
 * y2 = F.conv2d(x2, w2, padding=1, stride=2)
 * y2.sum().backward()
 * print(y2)
 * print(x2.grad)
 * print(w2.grad)
 * </pre>
 */
public class Conv2dNodeTest {

    private static final double TOL = 1e-5;

    static Stream<TensorManager> tensorManagers() {
        return Stream.of(TensorManager.ofDouble());
    }

    @ParameterizedTest
    @MethodSource("tensorManagers")
    void testForwardAndBackward(TensorManager tm) {
        // x: (N=2, C_in=2, H=3, W=3)
        Tensor x = tm.strideTensor(Shape.of(2, 2, 3, 3),
                1., 2., 3., 4., 5., 6., 7., 8., 9.,
                10., 11., 12., 13., 14., 15., 16., 17., 18.,
                19., 20., 21., 22., 23., 24., 25., 26., 27.,
                28., 29., 30., 31., 32., 33., 34., 35., 36.
        ).requiresGrad(true).name("x");

        // w: (C_out=2, C_in=2, kH=2, kW=2)
        Tensor w = tm.strideTensor(Shape.of(2, 2, 2, 2),
                1., 2., 3., 4.,
                5., 6., 7., 8.,
                9., 10., 11., 12.,
                13., 14., 15., 16.
        ).requiresGrad(true).name("w");

        // b: (C_out=2)
        Tensor b = tm.strideTensor(Shape.of(2), 1., 2.)
                .requiresGrad(true).name("b");


        int padding = 0;
        int stride = 1;
        int dilation = 1;
        int groups = 1;
        Tensor y = new Conv2dNode(x, w, b, stride, padding, dilation, groups);

        // Forward: y shape (2, 2, 2, 2)
        // PyTorch: y = F.conv2d(x, w, b)
        assertTrue(y.value().deepEquals(
                tm.strideArray(Shape.of(2, 2, 2, 2),
                        357., 393., 465., 501.,
                        838., 938., 1138., 1238.,
                        1005., 1041., 1113., 1149.,
                        2638., 2738., 2938., 3038.),
                TOL), "forward output mismatch");

        var sum = y.sum();
        sum.setGrad(tm.fullArray(sum.value().shape(), 1.));
        var graph = Autograd.backward(sum);

        // x.grad: (2, 2, 3, 3)
        assertTrue(x.grad().deepEquals(
                tm.strideArray(Shape.of(2, 2, 3, 3),
                        10., 22., 12., 24., 52., 28., 14., 30., 16.,
                        18., 38., 20., 40., 84., 44., 22., 46., 24.,
                        10., 22., 12., 24., 52., 28., 14., 30., 16.,
                        18., 38., 20., 40., 84., 44., 22., 46., 24.
                ),
                TOL), "x gradient mismatch");

        // w.grad: (2, 2, 2, 2)
        assertTrue(w.grad().deepEquals(
                tm.strideArray(Shape.of(2, 2, 2, 2),
                        96., 104., 120., 128.,
                        168., 176., 192., 200.,
                        96., 104., 120., 128.,
                        168., 176., 192., 200),
                TOL), "w gradient mismatch");

        // b.grad: (2)
        assertTrue(b.grad().deepEquals(
                tm.strideArray(Shape.of(2), 8., 8.),
                TOL), "b gradient mismatch");

        graph.resetGrad();
    }

    @ParameterizedTest
    @MethodSource("tensorManagers")
    void testForwardNoBias(TensorManager tm) {
        // x: (N=1, C_in=1, H=3, W=3)
        Tensor x = tm.strideTensor(Shape.of(1, 1, 3, 3),
                1., 2., 3., 4., 5., 6., 7., 8., 9.
        ).requiresGrad(true).name("x");

        // w: (C_out=1, C_in=1, kH=2, kW=2) all ones
        Tensor w = tm.strideTensor(Shape.of(1, 1, 2, 2),
                1., 1., 1., 1.
        ).requiresGrad(true).name("w");

        // padding=1, stride=2
        Tensor y = new Conv2dNode(x, w, null, 2, 1, 1, 1);

        // PyTorch: F.conv2d(x, w, padding=1, stride=2) -> shape (1,1,2,2)
        assertTrue(y.value().deepEquals(
                tm.strideArray(Shape.of(1, 1, 2, 2),
                        1., 5., 11., 28.),
                TOL), "forward no-bias stride=2 padding=1 mismatch");

        y.setGrad(tm.fullArray(y.value().shape(), 1.));
        var graph = Autograd.backward(y);

        // x.grad: (1, 1, 3, 3)
        assertTrue(x.grad().deepEquals(
                tm.strideArray(Shape.of(1, 1, 3, 3),
                        1., 1., 1., 1., 1., 1., 1., 1., 1.),
                TOL), "x gradient no-bias mismatch");

        // w.grad: (1, 1, 2, 2)
        assertTrue(w.grad().deepEquals(
                tm.strideArray(Shape.of(1, 1, 2, 2),
                        5., 10., 10., 20.),
                TOL), "w gradient no-bias mismatch");

        graph.resetGrad();
    }
}
