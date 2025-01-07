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

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.darray.DArray;
import rapaio.darray.Shape;
import rapaio.nn.Autograd;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;

public class StandardizeOnTest {

    private static final double TOL = 1e-6;

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(-67253673);
    }

    @Test
    void testStandardize() {
//        testStandardizeWith(TensorManager.ofFloat().randomTensor(Shape.of(5, 3), random).requiresGrad(true).name("x"), Shape.of(3));
        testStandardizeWith(TensorManager.ofDouble().randomTensor(Shape.of(5, 3), random).requiresGrad(true).name("x"), Shape.of(3));
    }

    void testStandardizeWith(Tensor x, Shape shape) {
        var m = x.meanOn(shape).name("meanOn");
        var std = x.stdOn(shape, 0, m).name("stdOn");
        var s = x.sub(m).div(std).name("standardizeOn");

        var sum = s.sub(0.5).sqr().sum().name("sum");

        sum.setGrad(x.tm().scalarArray(1));
        var graph = Autograd.backward(sum);

        DArray<?> xGrad = x.grad().copy();
        DArray<?> meanOn = m.value().copy();
        DArray<?> stdOn = std.value().copy();

        graph.resetGrad();

        StandardizeOn standardizeOn = x.standardizeOn(shape, 0, 1e-3);
        sum = standardizeOn.sub(0.5).sqr().sum().name("sum");
        sum.setGrad(x.tm().scalarArray(1));
        Autograd.backward(sum);

        assertTrue(xGrad.deepEquals(x.grad(), TOL));
        assertTrue(meanOn.deepEquals(m.value(), TOL));
        assertTrue(stdOn.deepEquals(std.value(), TOL));
    }
}
