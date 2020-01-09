package rapaio.math.linear;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.VarDouble;
import rapaio.math.linear.dense.BaseDVector;
import rapaio.math.linear.dense.SolidDVector;
import rapaio.util.collection.DoubleArrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/9/20.
 */
public class AbstractDVectorTest {

    private static final double TOL = 1e-12;
    private Normal normal;
    private double[] values;
    private BaseDVector x;
    private BaseDVector z;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
        normal = Normal.std();
        values = DoubleArrays.newFrom(0, 100, row -> normal.sampleNext());
        x = BaseDVector.wrap(values);
        z = BaseDVector.wrap(VarDouble.fill(100, 10).elements());
    }

    @Test
    void scalarPlusTest() {
        DVector y = x.copy().plus(10);
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) + 10, y.get(i), TOL);
        }
    }

    @Test
    void vectorPlusTest() {
        DVector y = x.copy().plus(z);
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) + z.get(i), y.get(i), TOL);
        }
    }

    @Test
    void vectorPlusNonconformantTest() {
        DVector y = BaseDVector.wrap(VarDouble.fill(50, 10).elements());
        assertThrows(IllegalArgumentException.class, () -> x.plus(y));
    }

    @Test
    void scalarMinusTest() {
        DVector y = x.copy().minus(10);
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) - 10, y.get(i), TOL);
        }
    }

    @Test
    void vectorMinusTest() {
        DVector y = x.copy().minus(z);

        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) - z.get(i), y.get(i), TOL);
        }
    }

    @Test
    void vectorMinusNonconformantTest() {
        DVector y = SolidDVector.zeros(50);
        assertThrows(IllegalArgumentException.class, () -> x.minus(y));
    }

    @Test
    void scalarTimesTest() {
        DVector y = x.copy().times(10);
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) * 10, y.get(i), TOL);
        }
    }

    @Test
    void vectorTimesTest() {
        DVector y = x.copy().times(z);
        assertEquals(100, y.size());
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) * 10, y.get(i), TOL);
        }
    }


    @Test
    void scalarDivTest() {
        DVector y = x.copy().div(10);
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) / 10, y.get(i), TOL);
        }
    }

    @Test
    void vectorDivTest() {
        DVector y = x.copy().div(z);
        assertEquals(100, y.size());
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) / 10, y.get(i), TOL);
        }
    }

    @Test
    void vectorDotTest() {
        double result = x.copy().dot(z);
        assertEquals(x.sum()*10, result, TOL);
    }

    @Test
    void normTest() {
        assertEquals(100, x.norm(0), TOL);
        assertEquals(x.copy().apply(Math::abs).sum(), x.norm(1), TOL);
        assertEquals(Math.pow(x.copy().apply(v -> Math.pow(Math.abs(v), 1.2)).sum(), 1/1.2), x.norm(1.2), TOL);
        assertEquals(x.copy().valueStream().max().orElse(Double.NaN), x.norm(Double.POSITIVE_INFINITY), TOL);
    }

    @Test
    void normalizeTest() {
        assertEquals(1, x.copy().apply(Math::abs).normalize(1).sum(), TOL);
    }

    @Test
    void meanVarTest() {
        assertEquals(Mean.of(x.asVarDouble()).value(), x.mean(), 1e-12);
        assertEquals(Variance.of(x.asVarDouble()).value(), x.variance(), 1e-12);
    }

}
