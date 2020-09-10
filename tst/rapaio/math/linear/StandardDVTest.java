package rapaio.math.linear;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.math.linear.dense.DMStripe;
import rapaio.util.collection.DoubleArrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/9/20.
 */
public abstract class StandardDVTest {

    protected static final double TOL = 1e-12;
    protected Normal normal;
    protected double[] values;
    protected static final int N = 100;

    protected DV x;
    protected DV z;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
        normal = Normal.std();
        values = DoubleArrays.newFrom(0, 100, row -> normal.sampleNext());
        x = generateWrap(values);
        z = generateFill(100, 10);
    }

    public abstract DV.Type type();

    public abstract DV generateWrap(double[] values);

    public abstract DV generateFill(int size, double fill);

    public abstract String className();

    public DV generateOnesWithMissing() {
        DV v = generateFill(10, 1);
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                v.set(i, Double.NaN);
            }
        }
        return v;
    }

    @Test
    void typeTest() {
        assertEquals(type(), generateFill(10, 1).type());
    }

    @Test
    void testAddScalar() {
        DV y = x.copy().add(10);
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) + 10, y.get(i), TOL);
        }
    }

    @Test
    void testAddVector() {
        DV y = x.copy().add(z);
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) + z.get(i), y.get(i), TOL);
        }
    }

    @Test
    void testVectorAddNonconformant() {
        DV y = generateFill(50, 10);
        assertThrows(IllegalArgumentException.class, () -> x.add(y));
    }

    @Test
    void testScalarSubtract() {
        DV y = x.copy().sub(10);
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) - 10, y.get(i), TOL);
        }
    }

    @Test
    void testVectorSubtract() {
        DV y = x.copy().sub(z);
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) - z.get(i), y.get(i), TOL);
        }
    }

    @Test
    void testVectorMinusNonconformant() {
        DV y = generateFill(50, 0);
        assertThrows(IllegalArgumentException.class, () -> x.sub(y));
    }

    @Test
    void testScalarMultiply() {
        DV y = x.copy().mult(10);
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) * 10, y.get(i), TOL);
        }
    }

    @Test
    void testVectorMultiply() {
        DV y = x.copy().mult(z);
        assertEquals(100, y.size());
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) * 10, y.get(i), TOL);
        }
    }


    @Test
    void testScalarDivide() {
        DV y = x.copy().div(10);
        for (int i = 0; i < y.size(); i++) {
            assertEquals(x.get(i) / 10, y.get(i), TOL);
        }
    }

    @Test
    void vectorDivTest() {
        DV y = x.copy().div(z);
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
        assertTrue(Double.isNaN(generateWrap(new double[0]).mean()));
        assertTrue(Double.isNaN(generateWrap(new double[0]).nanmean()));
        assertTrue(Double.isNaN(generateWrap(new double[0]).variance()));
        assertTrue(Double.isNaN(generateWrap(new double[0]).nanvariance()));
    }

    @Test
    void testNaN() {
        assertEquals(5, generateOnesWithMissing().nansum(), TOL);
        assertEquals(5, generateOnesWithMissing().nancount(), TOL);
        assertEquals(1, generateOnesWithMissing().nanmean(), TOL);
        assertEquals(0, generateOnesWithMissing().nanvariance(), TOL);
    }

    @Test
    void copyTest() {
        var v = generateFill(10, 1);

        var copy1 = v.copy(DV.Type.BASE);
        var copy2 = v.copy(DV.Type.DENSE);

        assertTrue(v.deepEquals(copy1));
        assertTrue(v.deepEquals(copy2));

        assertEquals(DV.Type.BASE, copy1.type());
        assertEquals(DV.Type.DENSE, copy2.type());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> v.copy(DV.Type.VIEW));
        assertNotNull(ex);
        assertEquals("DVType.VIEW cannot be used to create a copy.", ex.getMessage());
    }

    @Test
    void applyTest() {
        var v1 = generateFill(100, 1);
        var v2 = v1.copy();

        assertTrue(v2.deepEquals(v1.apply(x -> x - 10).apply(x -> x + 10)));
        assertTrue(v2.deepEquals(v1.apply((i, x) -> x - i).apply(Double::sum)));
    }

    @Test
    void asMatrixTest() {

        var v1 = generateWrap(new double[]{1, 3, 9});
        var m1 = DMStripe.wrap(new double[][]{{1}, {3}, {9}});

        assertTrue(m1.deepEquals(v1.asMatrix()));
    }

    @Test
    void deepEqualsTest() {

        var v1 = generateFill(10, 1);
        var v2 = generateFill(10, 1);

        assertTrue(v1.deepEquals(v2));
        v2.inc(2, 1);

        assertFalse(v1.deepEquals(v2));

        assertFalse(generateFill(100, 1).deepEquals(v1));
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
