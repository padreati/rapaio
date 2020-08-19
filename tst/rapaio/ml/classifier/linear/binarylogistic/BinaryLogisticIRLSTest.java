package rapaio.ml.classifier.linear.binarylogistic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;
import rapaio.math.linear.DM;
import rapaio.math.linear.DV;
import rapaio.math.linear.dense.DMStripe;
import rapaio.math.linear.dense.DVDense;

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
                .withX(DMStripe.identity(1))
                .withY(DVDense.zeros(1))
                .withW0(DVDense.ones(1))
                .build();
        assertEquals(1e-20, optimizer.getEps());
        assertEquals(10, optimizer.getMaxIter());
        assertEquals(0, optimizer.getLambda());
        assertTrue(rapaio.math.linear.dense.DMStripe.identity(1).deepEquals(optimizer.getX()));
        assertTrue(DVDense.zeros(1).deepEquals(optimizer.getY()));
        assertTrue(DVDense.ones(1).deepEquals(optimizer.getW0()));
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

        var x = rapaio.math.linear.dense.DMStripe.copy(10, 1, -5, -4, -3, -2, -1, 1, 2, 3, 4, 5);
        var y = DVDense.wrap(1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
        var w0 = DVDense.zeros(1);

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

        var x = rapaio.math.linear.dense.DMStripe.copy(10, 1, -5, -4, -3, 2, -1, 1, -2, 3, 4, 5);
        var y = DVDense.wrap(1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
        var w0 = DVDense.zeros(1);

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
        assertEquals(-0.5584820971090906, result.getW().get(0));

        assertEquals(result.getWs().size(), result.getNlls().size());
        assertTrue(result.getW().deepEquals(result.getWs().get(result.getNlls().size() - 1)));
        assertEquals(result.getNll(), result.getNlls().get(result.getNlls().size() - 1));
    }

    @Test
    void testUnconverged() {
        var x = rapaio.math.linear.dense.DMStripe.copy(2, 1, -5, 5);
        var y = DVDense.wrap(1, 0);
        var w0 = DVDense.zeros(1);

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
        VarDouble loss = VarDouble.empty().withName("loss");
        for (double lambda : lambdas) {
            var x = rapaio.math.linear.dense.DMStripe.copy(10, 1, -5, -4, -3, -2, -1, 1, 2, 3, 4, 5);
            var y = DVDense.wrap(1, 1, 1, 1, 1, 0, 0, 0, 0, 0);
            var w0 = DVDense.zeros(1);
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

        VarDouble lossDeltas = VarDouble.from(loss.rowCount() - 1, row -> loss.getDouble(row + 1) - loss.getDouble(row));
        for (double lossDelta : lossDeltas) {
            assertTrue(lossDelta >= 0);
        }
    }

    @Test
    public void testIllConditionedInputs() {
        Normal normal = Normal.of(10, 2);
        VarDouble x1 = VarDouble.from(100, normal::sampleNext).withName("x1");
        VarDouble x2 = VarDouble.from(x1, v -> v + normal.sampleNext() / 1e8).withName("x2");

        VarDouble y1 = VarDouble.from(100, row -> row > 50 ? 1. : 0);

        DM x = rapaio.math.linear.dense.DMStripe.copy(x1, x2);
        DV y = DVDense.from(y1);
        DV w0 = DVDense.wrap(0, 0);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> BinaryLogisticIRLS.builder()
                .withX(x)
                .withY(y)
                .withW0(w0)
                .build()
                .fit());
        assertEquals("Matrix is rank deficient.", ex.getMessage());
    }
}
