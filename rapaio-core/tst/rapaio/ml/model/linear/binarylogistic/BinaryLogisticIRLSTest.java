/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.model.linear.binarylogistic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.math.tensor.Shape;
import rapaio.math.tensor.TensorManager;
import rapaio.sys.WS;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/21/20.
 */
public class BinaryLogisticIRLSTest {

    private static final double TOL = 1e-12;

    private Random random;
    private static final TensorManager.OfType<Double> tmd = WS.tm().ofDouble();

    @BeforeEach
    void beforeEach() {
        random = new Random(123);
    }

    @Test
    void testDefaults() {
        var optimizer = new BinaryLogisticIRLS()
                .xp.set(tmd.eye(1))
                .yp.set(tmd.zeros(Shape.of(1)))
                .w0.set(tmd.full(Shape.of(1), 1.));
        assertEquals(1e-20, optimizer.eps.get());
        assertEquals(10, optimizer.maxIter.get());
        assertEquals(0, optimizer.lambdap.get());
        assertTrue(tmd.eye(1).deepEquals(optimizer.xp.get()));
        assertTrue(tmd.zeros(Shape.of(1)).deepEquals(optimizer.yp.get()));
        assertTrue(tmd.full(Shape.of(1), 1.).deepEquals(optimizer.w0.get()));
    }

    @Test
    void testResult() {
        BinaryLogisticIRLS.Result result = new BinaryLogisticIRLS.Result(Collections.emptyList(), Collections.emptyList(), false);
        assertTrue(tmd.scalar(Double.NaN).deepEquals(result.w()));
        assertEquals(Double.NaN, result.nll());
    }

    @Test
    void testSymmetricAroundZeroSeparable() {

        var x = tmd.stride(Shape.of(10, 1), -5, -4, -3, -2, -1, 1, 2, 3, 4, 5);
        var y = tmd.stride(1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
        var w0 = tmd.zeros(Shape.of(1));

        var result = new BinaryLogisticIRLS()
                .xp.set(x)
                .yp.set(y)
                .w0.set(w0)
                .maxIter.set(100)
                .eps.set(0.0001)
                .lambdap.set(10.0)
                .fit();
        assertTrue(result.converged());
        assertEquals(0.5, 1. / (1. + Math.exp(-result.w().get(0) * x.take(1, 0).stats().mean())), 1e-12);
    }

    @Test
    void testSymmetricAroundZeroNotSeparable() {

        var x = tmd.stride(Shape.of(10, 1), -5, -4, -3, 2, -1, 1, -2, 3, 4, 5);
        var y = tmd.stride(1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
        var w0 = tmd.zeros(Shape.of(1));

        var result = new BinaryLogisticIRLS()
                .xp.set(x)
                .yp.set(y)
                .w0.set(w0)
                .maxIter.set(100)
                .eps.set(0.0001)
                .fit();
        assertTrue(result.converged());
        assertEquals(0.5, 1. / (1. + Math.exp(-result.w().get(0) * x.take(1, 0).stats().mean())), 1e-12);

        // aligned with python
        assertEquals(-0.5584820971090904, result.w().get(0), TOL);

        assertEquals(result.ws().size(), result.nlls().size());
        assertTrue(result.w().deepEquals(result.ws().get(result.nlls().size() - 1)));
        assertEquals(result.nll(), result.nlls().get(result.nlls().size() - 1));
    }

    /*
    @Test
    void testUnconverged() {
        var x = DMatrix.copy(2, 1, -5, 5);
        var y = DVector.wrap(1, 0);
        var w0 = DVector.zeros(1);

        var result = new BinaryLogisticIRLS()
                .xp.set(x)
                .yp.set(y)
                .w0.set(w0)
                .maxIter.set(10)
                .eps.set(0.000000001)
                .fit();
        assertFalse(result.converged());
        assertEquals(0.5, 1. / (1. + Math.exp(-result.w().get(0) * x.mapCol(0).mean())), 1e-12);
    }

    @Test
    void testSeparableL2() {

        VarDouble lambdas = VarDouble.seq(0.2, 10, 0.2);
        VarDouble loss = VarDouble.empty().name("loss");
        for (double lambda : lambdas) {
            var x = DMatrix.copy(10, 1, -5, -4, -3, -2, -1, 1, 2, 3, 4, 5);
            var y = DVector.wrap(1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
            var w0 = DVector.zeros(1);
            var result = new BinaryLogisticIRLS()
                    .xp.set(x)
                    .yp.set(y)
                    .w0.set(w0)
                    .maxIter.set(100)
                    .eps.set(0.000000001)
                    .lambdap.set(lambda)
                    .fit();
            assertTrue(result.converged(), "Model not converge for lambda: " + lambda);
            assertEquals(0.5, 1. / (1. + Math.exp(-result.w().get(0) * x.mapCol(0).mean())), 1e-12);
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

        DMatrix x = DMatrix.copy(x1, x2);
        DVector y = y1.dv();
        DVector w0 = DVector.wrap(0, 0);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> new BinaryLogisticIRLS()
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

        DMatrix x = DMatrix.empty(2 * n, 2);
        x.mapCol(0).fill(1);
        VarDouble.sample(Normal.of(0, 0.5), n).dv().addTo(x.mapCol(1).range(0, n), 0);
        VarDouble.sample(Normal.of(1.5, 0.5), n).dv().addTo(x.mapCol(1).range(n, 2 * n), 0);

        DVector y = DVector.fill(2 * n, 1);
        y.range(n, 2 * n).fill(0);

        BinaryLogisticIRLS.Result irls = new BinaryLogisticIRLS()
                .w0.set(DVector.fill(2, 0))
                .xp.set(x)
                .yp.set(y)
                .lambdap.set(0.0)
                .maxIter.set(1000)
                .fit();

        DVector pred = x.dot(irls.w()).applyNew(MathTools::logistic);
        DVector ypred = pred.applyNew(v -> v > 0.5 ? 1 : 0);

        double accuracy = pred.subNew(ypred).apply(StrictMath::abs).sum() / pred.size();
        assertTrue(accuracy < 0.2);
    }
    */
}
