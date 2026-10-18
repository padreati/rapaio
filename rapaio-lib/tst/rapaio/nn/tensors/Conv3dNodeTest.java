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

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.darray.Shape;
import rapaio.nn.Autograd;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;
import rapaio.nn.layer.Conv1D;
import rapaio.nn.layer.Conv2D;
import rapaio.nn.layer.Conv3D;

/**
 * Tests for the 3D convolution node and layer. The gradients are checked against finite differences in
 * {@code GradientCheckTest}; here a 3D convolution over a depth of one is compared with the 2D convolution,
 * which is independently tested against PyTorch values in {@link Conv2dNodeTest}.
 */
public class Conv3dNodeTest {

    private static final double TOL = 1e-12;

    private TensorManager tm;

    @BeforeEach
    void beforeEach() {
        tm = TensorManager.ofDouble().seed(42);
    }

    @AfterEach
    void afterEach() {
        tm.close();
    }

    @Test
    void depthOneMatchesConv2d() {
        // {stride, padding, dilation, groups}; padding also pads the depth axis, so with a depth of one and a kernel
        // depth of one the 2D result is the depth slice at index padding (stride 1) or 0 (no padding)
        int[][] configs = {
                {1, 0, 1, 1},
                {2, 0, 1, 2},
                {1, 1, 1, 2},
                {1, 2, 2, 1},
        };
        for (int[] c : configs) {
            int stride = c[0];
            int padding = c[1];
            int dilation = c[2];
            int groups = c[3];
            String msg = String.format("stride %d, padding %d, dilation %d, groups %d: ", stride, padding, dilation, groups);

            DArray<?> x = tm.randomArray(Shape.of(2, 4, 5, 6));
            DArray<?> w = tm.randomArray(Shape.of(6, 4 / groups, 3, 2));
            DArray<?> b = tm.randomArray(Shape.of(6));

            Tensor x2 = tm.var(x.copy()).requiresGrad(true);
            Tensor w2 = tm.var(w.copy()).requiresGrad(true);
            Tensor b2 = tm.var(b.copy()).requiresGrad(true);
            Tensor y2 = x2.conv2d(w2, b2, stride, padding, dilation, groups);

            Tensor x3 = tm.var(x.copy().stretch(2)).requiresGrad(true);
            Tensor w3 = tm.var(w.copy().stretch(2)).requiresGrad(true);
            Tensor b3 = tm.var(b.copy()).requiresGrad(true);
            Tensor y3 = x3.conv3d(w3, b3, stride, padding, dilation, groups);

            int outD = (1 + 2 * padding - 1) / stride + 1;
            int real = padding;
            assertEquals(Shape.of(2, 6, outD, y2.dim(2), y2.dim(3)), y3.shape(), msg + "output shape");
            assertTrue(y2.value().deepEquals(y3.value().narrow(2, true, real, real + 1).squeeze(2), TOL), msg + "forward");

            // the same random upstream gradient on the real slice, nothing on the padded ones
            DArray<?> g = tm.randomArray(y2.shape());
            Tensor l2 = y2.mul(tm.var(g)).sum();
            l2.setGrad(tm.scalarArray(1));
            Autograd.backward(l2);

            Tensor l3 = y3.narrow(2, real, real + 1).reshape(y2.shape()).mul(tm.var(g)).sum();
            l3.setGrad(tm.scalarArray(1));
            Autograd.backward(l3);

            assertTrue(x2.grad().deepEquals(x3.grad().squeeze(2), TOL), msg + "input gradient");
            assertTrue(w2.grad().deepEquals(w3.grad().squeeze(2), TOL), msg + "weight gradient");
            assertTrue(b2.grad().deepEquals(b3.grad(), TOL), msg + "bias gradient");
        }
    }

    @Test
    void convLayersUseTheSameInitialization() {
        // fanIn = 16 / 2 * 6 for Conv1D, 16 / 2 * 3 * 2 for Conv2D and 16 / 2 * 1 * 3 * 2 for Conv3D, all 48
        double range = 1 / Math.sqrt(48);
        Conv1D conv1 = new Conv1D(tm, 16, 8, 6, 1, 0, 1, 2, true);
        Conv2D conv2 = new Conv2D(tm, 16, 8, 3, 2, 1, 0, 1, 2, true);
        Conv3D conv3 = new Conv3D(tm, 16, 8, 1, 3, 2, 1, 0, 1, 2, true);
        for (var params : List.of(conv1.parameters(), conv2.parameters(), conv3.parameters())) {
            for (Tensor p : params) {
                double maxAbs = p.value().abs().amax().doubleValue();
                assertTrue(maxAbs <= range, "parameter outside U(-1/sqrt(fanIn), 1/sqrt(fanIn))");
                // spread over the interval, not a constant like the former bias of 1
                assertTrue(maxAbs > range / 4, "parameter values are not spread over the interval");
            }
        }
    }

    @Test
    void layerShapesAndParameters() {
        Conv3D conv = new Conv3D(tm, 4, 6, 3, 2, 2, 1, 1, 1, 2, true);
        assertEquals(2, conv.parameters().size());
        assertEquals(Shape.of(6, 2, 3, 2, 2), conv.parameters().get(0).shape());
        assertEquals(Shape.of(6), conv.parameters().get(1).shape());

        // fanIn = 4 / 2 * 3 * 2 * 2 = 24
        double range = 1 / Math.sqrt(24);
        for (Tensor p : conv.parameters()) {
            assertTrue(p.value().abs().amax().doubleValue() <= range);
        }

        Tensor x = tm.randomTensor(Shape.of(2, 4, 5, 6, 7));
        Tensor y = conv.forward11(x);
        // D: 5 + 2 - 2 - 1 + 1 = 5, H: 6 + 2 - 1 - 1 + 1 = 7, W: 7 + 2 - 1 - 1 + 1 = 8
        assertEquals(Shape.of(2, 6, 5, 7, 8), y.shape());

        Tensor loss = y.sum();
        loss.setGrad(tm.scalarArray(1));
        Autograd.backward(loss);
        for (Tensor p : conv.parameters()) {
            assertEquals(p.shape(), p.grad().shape());
        }

        assertEquals(1, new Conv3D(tm, 4, 6, 3, 3, 3, 1, 0, 1, 1, false).parameters().size());
        assertThrows(IllegalArgumentException.class, () -> conv.forward11(tm.randomTensor(Shape.of(2, 3, 5, 6, 7))));
        assertThrows(IllegalArgumentException.class, () -> new Conv3D(tm, 3, 6, 3, 3, 3, 1, 0, 1, 2, true));
    }
}
