package rapaio.math.linear;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.util.collection.DoubleArrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/9/20.
 */
public abstract class StandardDVectorTest {

    protected static final double TOL = 1e-12;
    protected Normal normal;
    protected double[] values;
    protected static final int N = 100;

    protected DVector x;
    protected DVector z;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
        normal = Normal.std();
        values = DoubleArrays.newFrom(0, 100, row -> normal.sampleNext());
        x = generateWrap(values);
        z = generateFill(100, 10);
    }

    public abstract DVector generateWrap(double[] values);

    public abstract DVector generateFill(int size, double fill);

    public abstract DVector generateZeros(int size);

    public abstract String className();

    public DVector generateOnesWithMissing() {
        DVector v = generateFill(10, 1);
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                v.set(i, Double.NaN);
            }
        }
        return v;
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
        DVector y = generateFill(50, 10);
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
        DVector y = generateZeros(50);
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
        assertEquals(x.sum() * 10, result, TOL);
    }

    @Test
    void normTest() {
        assertEquals(100, x.norm(0), TOL);
        assertEquals(x.copy().apply(Math::abs).sum(), x.norm(1), TOL);
        assertEquals(Math.pow(x.copy().apply(v -> Math.pow(Math.abs(v), 1.2)).sum(), 1 / 1.2), x.norm(1.2), TOL);
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

    @Test
    void testNaN() {
        assertEquals(5, generateOnesWithMissing().nansum(), TOL);
        assertEquals(5, generateOnesWithMissing().nancount(), TOL);
        assertEquals(1, generateOnesWithMissing().nanmean(), TOL);
        assertEquals(0, generateOnesWithMissing().nanvariance(), TOL);
    }

    @Test
    void testPrintable() {
        assertEquals(className() + "{size:10, values:[2,2,2,2,2,2,2,2,2,2]}", generateFill(10, 2).toString());
        assertEquals("[0] 2 [4] 2 [8] 2 \n" +
                "[1] 2 [5] 2 [9] 2 \n" +
                "[2] 2 [6] 2 \n" +
                "[3] 2 [7] 2 \n", generateFill(10, 2).toContent());
        assertEquals(generateFill(10, 2).toContent(), generateFill(10, 2).toFullContent());
        assertEquals(generateFill(10, 2).toContent(), generateFill(10, 2).toSummary());

        assertEquals(className() + "{size:30, values:[2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,...]}", generateFill(30, 2).toString());
        assertEquals(" [0]  2   [6]  2  [12]  2  [18]  2  \n" +
                " [1]  2   [7]  2  [13]  2  [19]  2  \n" +
                " [2]  2   [8]  2  [14]  2  ...  ... \n" +
                " [3]  2   [9]  2  [15]  2  [28]  2  \n" +
                " [4]  2  [10]  2  [16]  2  [29]  2  \n" +
                " [5]  2  [11]  2  [17]  2  \n", generateFill(30, 2).toContent());
        assertEquals(" [0] 2  [6] 2 [12] 2 [18] 2 [24] 2 \n" +
                " [1] 2  [7] 2 [13] 2 [19] 2 [25] 2 \n" +
                " [2] 2  [8] 2 [14] 2 [20] 2 [26] 2 \n" +
                " [3] 2  [9] 2 [15] 2 [21] 2 [27] 2 \n" +
                " [4] 2 [10] 2 [16] 2 [22] 2 [28] 2 \n" +
                " [5] 2 [11] 2 [17] 2 [23] 2 [29] 2 \n", generateFill(30, 2).toFullContent());
        assertEquals(" [0]  2   [6]  2  [12]  2  [18]  2  \n" +
                " [1]  2   [7]  2  [13]  2  [19]  2  \n" +
                " [2]  2   [8]  2  [14]  2  ...  ... \n" +
                " [3]  2   [9]  2  [15]  2  [28]  2  \n" +
                " [4]  2  [10]  2  [16]  2  [29]  2  \n" +
                " [5]  2  [11]  2  [17]  2  \n", generateFill(30, 2).toSummary());


    }
}
