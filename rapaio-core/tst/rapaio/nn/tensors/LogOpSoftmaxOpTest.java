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
import rapaio.nn.Loss;
import rapaio.nn.Tensor;
import rapaio.nn.TensorManager;
import rapaio.nn.loss.NegativeLikelihoodLoss;

public class LogOpSoftmaxOpTest extends AbstractTensorTest{

    @ParameterizedTest
    @MethodSource("managers")
    void stabilityTest(TensorManager tm) {

        Tensor t = tm.var(tm.strideArray(Shape.of(2,3), 1, 2, 3, 4, 5, 6).mul_(-10_000)).name("t").requiresGrad(true);
        Tensor y = tm.strideTensor(Shape.of(2, 3), 0, 1, 0, 1, 0, 0);

        Loss loss = new NegativeLikelihoodLoss();
        loss.forward(t, y);
        loss.backward();

        for(double value : t.grad().asDoubleArray()) {
            assertTrue(Double.isFinite(value));
        }
    }
}
