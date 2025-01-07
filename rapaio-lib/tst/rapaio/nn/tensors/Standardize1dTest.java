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

import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.darray.Shape;
import rapaio.nn.Autograd;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class Standardize1dTest {

    private static final double TOL = 1e-6;

    @Test
    void testStandardize() {
        try(TensorManager tm = TensorManager.ofFloat()) {
            tm.seed(42);
            testStandardizeWith(tm.randomTensor(Shape.of(5, 3)).requiresGrad(true).name("x"), 0);
            testStandardizeWith(tm.randomTensor(Shape.of(5, 3)).requiresGrad(true).name("x"), 1);
        }
        try(TensorManager tm = TensorManager.ofDouble()) {
            testStandardizeWith(tm.randomTensor(Shape.of(5, 3)).requiresGrad(true).name("x"), 0);
            testStandardizeWith(tm.randomTensor(Shape.of(5, 3)).requiresGrad(true).name("x"), 1);
        }
    }

    void testStandardizeWith(Tensor x, int axis) {
        var m = x.mean1d(axis).name("mean1d");
        var std = x.std1d(axis, 0, m).name("std1d");
        var s = x.sub(m.stretch(axis)).div(std.stretch(axis)).name("s");

        var sum = s.sub(0.5).sqr().sum().name("sum");

        sum.setGrad(x.tm().scalarArray(1));
        var graph = Autograd.backward(sum);

        DArray<?> xGrad = x.grad().copy();
        DArray<?> mean1 = m.value().copy();
        DArray<?> std1 = std.value().copy();

        graph.resetGrad();

        Standardize1d standardize1d = x.standardize1d(axis, 0, 1e-3);
        sum = standardize1d.sub(0.5).sqr().sum().name("sum");
        sum.setGrad(x.tm().scalarArray(1));
        Autograd.backward(sum);

        assertTrue(xGrad.deepEquals(x.grad(), TOL));
        assertTrue(mean1.deepEquals(m.value(), TOL));
        assertTrue(std1.deepEquals(std.value(), TOL));
    }
}
