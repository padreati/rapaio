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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.darray.Shape;
import rapaio.nn.Autograd;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;
import rapaio.nn.layer.Linear;
import rapaio.nn.layer.MaxPool1D;
import rapaio.nn.layer.MaxPool2D;
import rapaio.nn.layer.MaxPool3D;
import rapaio.nn.layer.Sequential;

/**
 * Max pooling nodes and layers on small inputs with hand computed results. The gradients are also checked against
 * finite differences in {@code GradientCheckTest}.
 */
public class MaxPoolNodeTest {

    private static final double TOL = 1e-6;

    private static void backward(Tensor y) {
        Tensor s = y.sum();
        s.setGrad(y.tm().scalarArray(1));
        Autograd.backward(s);
    }

    @Test
    void maxPool1dForwardAndBackward() {
        for (TensorManager tm : new TensorManager[] {TensorManager.ofDouble(), TensorManager.ofFloat()}) {
            try (tm) {
                // windows of 2, stride 2: [1 5] [3 2] [0 4]
                Tensor x = tm.strideTensor(Shape.of(1, 1, 6), 1., 5., 3., 2., 0., 4.).requiresGrad(true);
                Tensor y = x.maxPool1d(2);
                assertTrue(y.value().deepEquals(tm.strideArray(Shape.of(1, 1, 3), 5., 3., 4.), TOL));
                backward(y);
                assertTrue(x.grad().deepEquals(tm.strideArray(Shape.of(1, 1, 6), 0., 1., 1., 0., 0., 1.), TOL));
            }
        }
    }

    @Test
    void overlappingWindowsSumTheirGradients() {
        try (TensorManager tm = TensorManager.ofDouble()) {
            // windows of 3, stride 1: [1 9 2] [9 2 3] [2 3 0] [3 0 1]; 9 wins two windows, 3 wins two windows
            Tensor x = tm.strideTensor(Shape.of(1, 6), 1., 9., 2., 3., 0., 1.).requiresGrad(true);
            Tensor y = x.maxPool1d(3, 1, 0, 1, false);
            assertTrue(y.value().deepEquals(tm.strideArray(Shape.of(1, 4), 9., 9., 3., 3.), TOL));

            // a weighted objective, so the summed contributions are visible: 9 gets 1 + 2, 3 gets 3 + 4
            Tensor s = y.mul(tm.var(tm.strideArray(Shape.of(1, 4), 1., 2., 3., 4.))).sum();
            s.setGrad(tm.scalarArray(1));
            Autograd.backward(s);
            assertTrue(x.grad().deepEquals(tm.strideArray(Shape.of(1, 6), 0., 3., 0., 7., 0., 0.), TOL));
        }
    }

    @Test
    void maxPool2dForwardAndBackward() {
        try (TensorManager tm = TensorManager.ofDouble()) {
            // two channels of 4x4, square windows of 2
            Tensor x = tm.var(tm.seqArray(Shape.of(1, 2, 4, 4))).requiresGrad(true);
            Tensor y = x.maxPool2d(2);
            // each window's maximum is its bottom right element
            assertTrue(y.value().deepEquals(tm.strideArray(Shape.of(1, 2, 2, 2), 5., 7., 13., 15., 21., 23., 29., 31.), TOL));
            backward(y);
            DArray<?> expected = tm.zerosArray(Shape.of(1, 2, 4, 4));
            for (int c = 0; c < 2; c++) {
                for (int i = 1; i < 4; i += 2) {
                    for (int j = 1; j < 4; j += 2) {
                        expected.setDouble(1, 0, c, i, j);
                    }
                }
            }
            assertTrue(x.grad().deepEquals(expected, TOL));
        }
    }

    @Test
    void paddingNeverWinsAndCeilModeKeepsTheLastWindow() {
        try (TensorManager tm = TensorManager.ofDouble()) {
            // all values negative: a zero padding would win, the negative infinity padding does not
            Tensor x = tm.strideTensor(Shape.of(1, 5), -1., -5., -3., -2., -4.).requiresGrad(true);
            // windows of 3, stride 2, padding 1: [p -1 -5] [-5 -3 -2] [-2 -4 p]
            Tensor y = x.maxPool1d(3, 2, 1, 1, false);
            assertTrue(y.value().deepEquals(tm.strideArray(Shape.of(1, 3), -1., -2., -2.), TOL));
            backward(y);
            assertTrue(x.grad().deepEquals(tm.strideArray(Shape.of(1, 5), 1., 0., 0., 2., 0.), TOL));

            // windows of 2, stride 2 on length 5: floor mode drops the last element, ceil mode pools it alone
            Tensor z = tm.strideTensor(Shape.of(1, 5), 1., 2., 3., 4., 5.);
            assertEquals(Shape.of(1, 2), z.maxPool1d(2, 2, 0, 1, false).shape());
            assertTrue(z.maxPool1d(2, 2, 0, 1, true).value().deepEquals(tm.strideArray(Shape.of(1, 3), 2., 4., 5.), TOL));
        }
    }

    @Test
    void maxPool3dShapes() {
        try (TensorManager tm = TensorManager.ofDouble()) {
            Tensor x = tm.randomTensor(Shape.of(2, 3, 4, 6, 8)).requiresGrad(true);
            Tensor y = x.maxPool3d(2);
            assertEquals(Shape.of(2, 3, 2, 3, 4), y.shape());
            backward(y);
            assertEquals(x.shape(), x.grad().shape());
            // non overlapping windows: every output sends one unit of gradient to one input
            assertEquals(y.size(), x.grad().sum().doubleValue(), TOL);

            Tensor u = tm.randomTensor(Shape.of(3, 5, 5, 5));
            assertEquals(Shape.of(3, 3, 3, 3), u.maxPool3d(3, 3, 3, 2, 1, 1, false).shape());
        }
    }

    @Test
    void invalidArguments() {
        try (TensorManager tm = TensorManager.ofDouble()) {
            Tensor x2 = tm.randomTensor(Shape.of(1, 2, 6, 6));
            // padding more than half of the window
            assertThrows(IllegalArgumentException.class, () -> x2.maxPool2d(2, 2, 2, 2, 1, false));
            assertThrows(IllegalArgumentException.class, () -> x2.maxPool2d(0));
            assertThrows(IllegalArgumentException.class, () -> x2.maxPool2d(2, 2, 0, 0, 1, false));
            // wrong rank
            assertThrows(IllegalArgumentException.class, () -> tm.randomTensor(Shape.of(6, 6)).maxPool2d(2));
            assertThrows(IllegalArgumentException.class, () -> x2.maxPool1d(2));
            assertThrows(IllegalArgumentException.class, () -> tm.randomTensor(Shape.of(6, 6, 6)).maxPool3d(2));
        }
    }

    @Test
    void layers() {
        try (TensorManager tm = TensorManager.ofDouble().seed(42)) {
            MaxPool1D p1 = new MaxPool1D(tm, 2);
            MaxPool2D p2 = new MaxPool2D(tm, 3, 2, 1, 1, 1, false);
            MaxPool3D p3 = new MaxPool3D(tm, 2);
            assertTrue(p1.parameters().isEmpty() && p2.parameters().isEmpty() && p3.parameters().isEmpty());
            assertTrue(p1.state().tensors().isEmpty());

            assertEquals(Shape.of(2, 3, 4), p1.forward11(tm.randomTensor(Shape.of(2, 3, 8))).shape());
            // H: (5 + 2 - 3) / 1 + 1 = 5, W: (6 + 2 - 2) / 1 + 1 = 7
            assertEquals(Shape.of(2, 3, 5, 7), p2.forward11(tm.randomTensor(Shape.of(2, 3, 5, 6))).shape());
            assertEquals(Shape.of(2, 3, 2, 2, 2), p3.forward11(tm.randomTensor(Shape.of(2, 3, 4, 4, 5))).shape());

            // in a network: the gradient reaches the parameters through the pooling
            Linear linear = new Linear(tm, 4, 1, true);
            Sequential nn = new Sequential(tm, new MaxPool1D(tm, 2), new rapaio.nn.layer.Flatten(tm), linear);
            Tensor out = nn.forward11(tm.randomTensor(Shape.of(5, 1, 8)));
            assertEquals(Shape.of(5, 1), out.shape());
            backward(out);
            for (Tensor p : linear.parameters()) {
                assertEquals(p.shape(), p.grad().shape());
            }
        }
    }
}
