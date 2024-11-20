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

package rapaio.ml.model.linear.binarylogistic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.math.MathTools;
import rapaio.narray.NArray;
import rapaio.narray.NArrays;
import rapaio.narray.Order;
import rapaio.narray.Shape;

public class BinaryLogisticNewtonTest {

    private static final double TOL = 1e-12;

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(42);
    }

    @Test
    void testDefaults() {
        var optimizer = new BinaryLogisticIRLS()
                .xp.set(NArrays.eye(1))
                .yp.set(NArrays.zeros(Shape.of(1)))
                .w0.set(NArrays.full(Shape.of(1), 1.));
        assertEquals(1e-20, optimizer.eps.get());
        assertEquals(10, optimizer.maxIter.get());
        assertEquals(0, optimizer.lambdap.get());
        assertTrue(NArrays.eye(1).deepEquals(optimizer.xp.get()));
        assertTrue(NArrays.zeros(Shape.of(1)).deepEquals(optimizer.yp.get()));
        assertTrue(NArrays.full(Shape.of(1), 1.).deepEquals(optimizer.w0.get()));
    }

    @Test
    void testResult() {
        var result = new BinaryLogisticNewton.Result(Collections.emptyList(), Collections.emptyList(), false);
        assertTrue(NArrays.scalar(Double.NaN).deepEquals(result.w()));
        assertEquals(Double.NaN, result.nll());
    }

    @Test
    void testSymmetricAroundZeroSeparable() {

        var x = NArrays.stride(Shape.of(10, 1), -5, -4, -3, -2, -1, 1, 2, 3, 4, 5);
        var y = NArrays.stride(1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
        var w0 = NArrays.zeros(Shape.of(1));

        var result = new BinaryLogisticNewton()
                .xp.set(x)
                .yp.set(y)
                .w0.set(w0)
                .maxIter.set(100)
                .eps.set(0.0001)
                .lambdap.set(10.0)
                .fit();
        assertFalse(result.converged());
        assertEquals(0.5, 1. / (1. + Math.exp(-result.w().get(0) * x.take(1, 0).mean())), 1e-12);
    }

    @Test
    void testSymmetricAroundZeroNotSeparable() {

        var x = NArrays.stride(Shape.of(10, 1), Order.C, -5, -4, -3, 2, -1, 1, -2, 3, 4, 5);
        var y = NArrays.stride(1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
        var w0 = NArrays.zeros(Shape.of(1));

        var result = new BinaryLogisticNewton()
                .xp.set(x)
                .yp.set(y)
                .w0.set(w0)
                .maxIter.set(100)
                .eps.set(0.0001)
                .fit();
        assertTrue(result.converged());
        assertEquals(0.5, 1. / (1. + Math.exp(-result.w().get(0) * x.take(1, 0).mean())), 1e-12);

        // aligned with python
        assertEquals(-0.5584820971090904, result.w().get(0), TOL);

        assertEquals(result.ws().size(), result.nlls().size());
        assertTrue(result.w().deepEquals(result.ws().get(result.nlls().size() - 1)));
        assertEquals(result.nll(), result.nlls().getLast());
    }

    @Test
    void testUnconverged() {
        var x = NArrays.stride(Shape.of(2, 1), -5, 5);
        var y = NArrays.stride(1, 0);
        var w0 = NArrays.zeros(Shape.of(1));

        var result = new BinaryLogisticNewton()
                .xp.set(x)
                .yp.set(y)
                .w0.set(w0)
                .maxIter.set(10)
                .eps.set(0.000000001)
                .fit();
        assertFalse(result.converged());
        assertEquals(0.5, 1. / (1. + Math.exp(-result.w().get(0) * x.take(1, 0).mean())), 1e-12);
    }

    @Test
    void testSeparableL2() {

        VarDouble lambdas = VarDouble.seq(10, 1000, 100);
        VarDouble loss = VarDouble.empty().name("loss");
        for (double lambda : lambdas) {
            var x = NArrays.stride(Shape.of(10, 1), -5, -4, -3, -2, -1, 1, 2, 3, 4, 5);
            var y = NArrays.stride(1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
            var w0 = NArrays.zeros(Shape.of(1));
            var result = new BinaryLogisticNewton()
                    .xp.set(x)
                    .yp.set(y)
                    .w0.set(w0)
                    .maxIter.set(1000)
                    .eps.set(0.000000001)
                    .lambdap.set(lambda)
                    .fit();
            assertFalse(result.converged(), "Model not converge for lambda: " + lambda);
            assertEquals(0.5, 1. / (1. + Math.exp(-result.w().get(0) * x.take(1, 0).mean())), 1e-12);
            loss.addDouble(result.nll());
        }

        VarDouble lossDeltas = VarDouble.from(loss.size() - 1, row -> loss.getDouble(row + 1) - loss.getDouble(row));
        for (double lossDelta : lossDeltas) {
            assertTrue(lossDelta >= 0);
        }
    }

    @Test
    public void testIllConditionedInputs() {
        Normal normal = Normal.of(10, 2);
        VarDouble x1 = VarDouble.from(100, () -> normal.sampleNext(random)).name("x1");
        VarDouble x2 = VarDouble.from(x1, v -> v + normal.sampleNext(random) / 1e8).name("x2");

        VarDouble y1 = VarDouble.from(100, row -> row > 50 ? 1. : 0);

        NArray<Double> x = SolidFrame.byVars(x1, x2).narray();
        NArray<Double> y = y1.narray_();
        NArray<Double> w0 = NArrays.stride(0, 0);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> new BinaryLogisticNewton()
                .xp.set(x)
                .yp.set(y)
                .w0.set(w0)
                .maxIter.set(10000)
                .fit());
        assertEquals("Matrix is rank deficient.", ex.getMessage());
    }

    @Test
    void singleInputTest() {
        int n = 1_000;

        NArray<Double> x = NArrays.zeros(Shape.of(2 * n, 2));
        x.take(1, 0).fill_(1.);
        x.take(1, 1).squeeze(1).narrow(0, true, 0, n).add_(VarDouble.sample(Normal.of(0, 0.5), n).narray_());
        x.take(1, 1).squeeze(1).narrow(0, true, n, 2*n).add_(VarDouble.sample(Normal.of(1.5, 0.5), n).narray_());

        NArray<Double> y = NArrays.full(Shape.of(2 * n), 1.);
        y.narrow(0, true, n, 2 * n).fill_(0.);

        BinaryLogisticIRLS.Result irls = new BinaryLogisticIRLS()
                .w0.set(NArrays.full(Shape.of(2), 0.))
                .xp.set(x)
                .yp.set(y)
                .lambdap.set(0.0)
                .maxIter.set(1000)
                .fit();

        var pred = x.mv(irls.w()).apply_(MathTools::logistic);
        var ypred = pred.apply(v -> v > 0.5 ? 1. : 0);

        double accuracy = pred.sub(ypred).abs_().sum() / pred.size();
        assertTrue(accuracy < 0.2);
    }
}
