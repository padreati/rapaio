/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/21/20.
 */
public class BinaryLogisticIRLSTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void testDefaults() {
        var optimizer = new BinaryLogisticIRLS()
                .x.set(DMatrix.identity(1))
                .y.set(DVector.zeros(1))
                .w0.set(DVector.ones(1));
        assertEquals(1e-20, optimizer.eps.get());
        assertEquals(10, optimizer.maxIter.get());
        assertEquals(0, optimizer.lambda.get());
        assertTrue(DMatrix.identity(1).deepEquals(optimizer.x.get()));
        assertTrue(DVector.zeros(1).deepEquals(optimizer.y.get()));
        assertTrue(DVector.ones(1).deepEquals(optimizer.w0.get()));
    }

    @Test
    void testResult() {
        BinaryLogisticIRLS.Result result = new BinaryLogisticIRLS.Result(Collections.emptyList(), Collections.emptyList(), false);
        assertNull(result.getW());
        assertEquals(Double.NaN, result.getNll());
    }

    @Test
    void testSymmetricAroundZeroSeparable() {

        var x = DMatrix.copy(10, 1, -5, -4, -3, -2, -1, 1, 2, 3, 4, 5);
        var y = DVector.wrap(1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
        var w0 = DVector.zeros(1);

        var result = new BinaryLogisticIRLS()
                .x.set(x)
                .y.set(y)
                .w0.set(w0)
                .maxIter.set(100)
                .eps.set(0.0001)
                .fit();
        assertTrue(result.converged());
        assertEquals(0.5, 1. / (1. + Math.exp(-result.getW().get(0) * x.mapCol(0).mean())), 1e-12);
    }

    @Test
    void testSymmetricAroundZeroNotSeparable() {

        var x = DMatrix.copy(10, 1, -5, -4, -3, 2, -1, 1, -2, 3, 4, 5);
        var y = DVector.wrap(1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
        var w0 = DVector.zeros(1);

        var result = new BinaryLogisticIRLS()
                .x.set(x)
                .y.set(y)
                .w0.set(w0)
                .maxIter.set(100)
                .eps.set(0.0001)
                .fit();
        assertTrue(result.converged());
        assertEquals(0.5, 1. / (1. + Math.exp(-result.getW().get(0) * x.mapCol(0).mean())), 1e-12);

        // aligned with python
        assertEquals(-0.5584820971090904, result.getW().get(0));

        assertEquals(result.ws().size(), result.nlls().size());
        assertTrue(result.getW().deepEquals(result.ws().get(result.nlls().size() - 1)));
        assertEquals(result.getNll(), result.nlls().get(result.nlls().size() - 1));
    }

    @Test
    void testUnconverged() {
        var x = DMatrix.copy(2, 1, -5, 5);
        var y = DVector.wrap(1, 0);
        var w0 = DVector.zeros(1);

        var result = new BinaryLogisticIRLS()
                .x.set(x)
                .y.set(y)
                .w0.set(w0)
                .maxIter.set(10)
                .eps.set(0.000000001)
                .fit();
        assertFalse(result.converged());
        assertEquals(0.5, 1. / (1. + Math.exp(-result.getW().get(0) * x.mapCol(0).mean())), 1e-12);
    }

    @Test
    void testSeparableL2() {

        VarDouble lambdas = VarDouble.seq(0, 10, 0.2);
        VarDouble loss = VarDouble.empty().name("loss");
        for (double lambda : lambdas) {
            var x = DMatrix.copy(10, 1, -5, -4, -3, -2, -1, 1, 2, 3, 4, 5);
            var y = DVector.wrap(1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
            var w0 = DVector.zeros(1);
            var result = new BinaryLogisticIRLS()
                    .x.set(x)
                    .y.set(y)
                    .w0.set(w0)
                    .maxIter.set(100)
                    .eps.set(0.000000001)
                    .lambda.set(lambda)
                    .fit();
            assertTrue(result.converged());
            assertEquals(0.5, 1. / (1. + Math.exp(-result.getW().get(0) * x.mapCol(0).mean())), 1e-12);
            loss.addDouble(result.getNll());
        }

        VarDouble lossDeltas = VarDouble.from(loss.size() - 1, row -> loss.getDouble(row + 1) - loss.getDouble(row));
        for (double lossDelta : lossDeltas) {
            assertTrue(lossDelta >= 0);
        }
    }

    @Test
    public void testIllConditionedInputs() {
        Normal normal = Normal.of(10, 2);
        VarDouble x1 = VarDouble.from(100, normal::sampleNext).name("x1");
        VarDouble x2 = VarDouble.from(x1, v -> v + normal.sampleNext() / 1e8).name("x2");

        VarDouble y1 = VarDouble.from(100, row -> row > 50 ? 1. : 0);

        DMatrix x = DMatrix.copy(x1, x2);
        DVector y = DVector.from(y1);
        DVector w0 = DVector.wrap(0, 0);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> new BinaryLogisticIRLS()
                .x.set(x)
                .y.set(y)
                .w0.set(w0)
                .maxIter.set(10000)
                .fit());
        assertEquals("Matrix is rank deficient.", ex.getMessage());
    }
}
