/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.ml.classifier.linear.binarylogistic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

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
        var optimizer = BinaryLogisticIRLS.builder()
                .withX(DMatrix.identity(1))
                .withY(DVector.zeros(1))
                .withW0(DVector.ones(1))
                .build();
        assertEquals(1e-20, optimizer.getEps());
        assertEquals(10, optimizer.getMaxIter());
        assertEquals(0, optimizer.getLambda());
        assertTrue(DMatrix.identity(1).deepEquals(optimizer.getX()));
        assertTrue(DVector.zeros(1).deepEquals(optimizer.getY()));
        assertTrue(DVector.ones(1).deepEquals(optimizer.getW0()));
    }

    @Test
    void testResult() {
        BinaryLogisticIRLS.Result result = BinaryLogisticIRLS.Result.builder()
                .withWs(Collections.emptyList())
                .withNlls(Collections.emptyList())
                .build();
        assertNull(result.getW());
        assertEquals(Double.NaN, result.getNll());
    }

    @Test
    void testSymmetricAroundZeroSeparable() {

        var x = DMatrix.copy(10, 1, -5, -4, -3, -2, -1, 1, 2, 3, 4, 5);
        var y = DVector.wrap(1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
        var w0 = DVector.zeros(1);

        var result = BinaryLogisticIRLS.builder()
                .withX(x)
                .withY(y)
                .withW0(w0)
                .withMaxIter(100)
                .withEps(0.0001)
                .build()
                .fit();
        assertTrue(result.isConverged());
        assertEquals(0.5, 1. / (1. + Math.exp(-result.getW().get(0) * x.mapCol(0).mean())), 1e-12);
    }

    @Test
    void testSymmetricAroundZeroNotSeparable() {

        var x = DMatrix.copy(10, 1, -5, -4, -3, 2, -1, 1, -2, 3, 4, 5);
        var y = DVector.wrap(1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
        var w0 = DVector.zeros(1);

        var result = BinaryLogisticIRLS.builder()
                .withX(x)
                .withY(y)
                .withW0(w0)
                .withMaxIter(100)
                .withEps(0.0001)
                .build()
                .fit();
        assertTrue(result.isConverged());
        assertEquals(0.5, 1. / (1. + Math.exp(-result.getW().get(0) * x.mapCol(0).mean())), 1e-12);

        // aligned with python
        assertEquals(-0.5584820971090904, result.getW().get(0));

        assertEquals(result.getWs().size(), result.getNlls().size());
        assertTrue(result.getW().deepEquals(result.getWs().get(result.getNlls().size() - 1)));
        assertEquals(result.getNll(), result.getNlls().get(result.getNlls().size() - 1));
    }

    @Test
    void testUnconverged() {
        var x = DMatrix.copy(2, 1, -5, 5);
        var y = DVector.wrap(1, 0);
        var w0 = DVector.zeros(1);

        var result = BinaryLogisticIRLS.builder()
                .withX(x)
                .withY(y)
                .withW0(w0)
                .withMaxIter(10)
                .withEps(0.000000001)
                .build()
                .fit();
        assertFalse(result.isConverged());
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
            var result = BinaryLogisticIRLS.builder()
                    .withX(x)
                    .withY(y)
                    .withW0(w0)
                    .withMaxIter(100)
                    .withEps(0.000000001)
                    .withLambda(lambda)
                    .build()
                    .fit();
            assertTrue(result.isConverged());
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

        RuntimeException ex = assertThrows(RuntimeException.class, () -> BinaryLogisticIRLS.builder()
                .withX(x)
                .withY(y)
                .withW0(w0)
                .build()
                .fit());
        assertEquals("Matrix is rank deficient.", ex.getMessage());
    }
}
