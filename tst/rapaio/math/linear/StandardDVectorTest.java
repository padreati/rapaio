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

package rapaio.math.linear;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.SamplingTools;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.VarDouble;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.collection.IntArrays;

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
    protected DMatrix m;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
        normal = Normal.std();
        values = DoubleArrays.newFrom(0, 100, row -> normal.sampleNext());
        x = generateWrap(values);
        z = generateFill(100, 10);
        m = DMatrix.identity(MType.RDENSE, 100).mult(2);
    }

    public abstract VType type();

    public abstract DVector generateWrap(double[] values);

    public abstract DVector generateFill(int size, double fill);

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
    void testBuilderZeros() {
        var zeros = DVector.zeros(N);
        assertNotNull(zeros);
        for (int i = 0; i < N; i++) {
            assertEquals(0, zeros.get(i), TOL);
        }
    }

    @Test
    void testBuildersOnes() {
        var ones = DVector.ones(N);
        assertNotNull(ones);
        assertEquals(N, ones.size());
        for (int i = 0; i < ones.size(); i++) {
            assertEquals(1., ones.get(i), TOL);
        }
    }

    @Test
    void testBuildersFill() {
        var fill = DVector.fill(N, 13);
        assertNotNull(fill);
        assertEquals(N, fill.size());
        for (int i = 0; i < fill.size(); i++) {
            assertEquals(13, fill.get(i), TOL);
        }
    }

    @Test
    void testBuilders() {
        x = DVector.from(VarDouble.seq(N - 1));
        assertNotNull(x);
        for (int i = 0; i < N; i++) {
            assertEquals(i, x.get(i), TOL);
        }

        DVector y = DVector.from(VarDouble.seq(N - 1));
        x = DVector.copy(y);
        assertNotNull(x);
        for (int i = 0; i < N; i++) {
            assertEquals(i, x.get(i), TOL);
        }

        x = DVector.from(VarDouble.fill(N, 1).bindRows(VarDouble.seq(N - 1)));
        assertNotNull(x);
        for (int i = 0; i < N; i++) {
            assertEquals(1, x.get(i), TOL);
            assertEquals(i, x.get(i + N), TOL);
        }

        x = DVector.wrap(0, 1, 2, 3, 4, 5);
        assertNotNull(x);
        for (int i = 0; i < 6; i++) {
            assertEquals(i, x.get(i), TOL);
        }

        x = DVector.from(10, Math::sqrt);
        assertNotNull(x);
        for (int i = 0; i < 10; i++) {
            assertEquals(Math.sqrt(i), x.get(i), TOL);
        }
    }

    @Test
    void typeTest() {
        assertEquals(type(), generateFill(10, 1).type());
    }

    @Test
    void mapCopyTest() {
        int[] sample = SamplingTools.sampleWOR(100, 10);
        DVector mapCopy = x.mapCopy(sample);
        assertEquals(sample.length, mapCopy.size());
        for (int i = 0; i < 10; i++) {
            assertEquals(x.get(sample[i]), mapCopy.get(i), TOL);
        }
        assertEquals(VType.DENSE, mapCopy.type());
    }

    @Test
    void axpyCopyTest() {
        DVector ones = DVector.ones(x.size());
        double result = x.axpyCopy(1, ones).sum();
        assertEquals(x.sum() + x.size(), result);
        DVector map = ones.map(IntArrays.newSeq(0, x.size()));
        result = x.axpyCopy(1, map).sum();
        assertEquals(x.sum() + ones.size(), result);
    }

    @Test
    void testAddScalar() {
        DVector xcopy = x.copy();
        x.add(10);
        for (int i = 0; i < x.size(); i++) {
            assertEquals(xcopy.get(i) + 10, x.get(i), TOL);
        }
    }

    @Test
    void testAddVector() {
        DVector xcopy = x.copy();
        x.add(z);
        for (int i = 0; i < x.size(); i++) {
            assertEquals(xcopy.get(i) + z.get(i), x.get(i), TOL);
        }
    }

    @Test
    void testAddVectorMap() {
        DVector xcopy = x.copy();
        x.add(z.map(IntArrays.newSeq(0, x.size())));
        for (int i = 0; i < x.size(); i++) {
            assertEquals(xcopy.get(i) + z.get(i), x.get(i), TOL);
        }
    }

    @Test
    void testVectorAddNonconformant() {
        DVector y = generateFill(50, 10);
        assertThrows(IllegalArgumentException.class, () -> x.add(y));
    }

    @Test
    void testScalarSubtract() {
        DVector xcopy = x.copy();
        x.sub(10);
        for (int i = 0; i < x.size(); i++) {
            assertEquals(xcopy.get(i) - 10, x.get(i), TOL);
        }
    }

    @Test
    void testVectorSubtract() {
        DVector xcopy = x.copy();
        x.sub(z);
        for (int i = 0; i < x.size(); i++) {
            assertEquals(xcopy.get(i) - z.get(i), x.get(i), TOL);
        }
    }

    @Test
    void testVectorSubtractMap() {
        DVector xcopy = x.copy();
        x.sub(z.map(IntArrays.newSeq(0, x.size())));
        for (int i = 0; i < x.size(); i++) {
            assertEquals(xcopy.get(i) - z.get(i), x.get(i), TOL);
        }
    }

    @Test
    void testVectorMinusNonconformant() {
        DVector y = generateFill(50, 0);
        assertThrows(IllegalArgumentException.class, () -> x.sub(y));
    }

    @Test
    void testScalarMultiply() {
        DVector xcopy = x.copy();
        x.mult(10);
        for (int i = 0; i < x.size(); i++) {
            assertEquals(xcopy.get(i) * 10, x.get(i), TOL);
        }
    }

    @Test
    void testVectorMultiply() {
        DVector xcopy = x.copy();
        x.mult(z);
        assertEquals(100, x.size());
        for (int i = 0; i < x.size(); i++) {
            assertEquals(xcopy.get(i) * 10, x.get(i), TOL);
        }
    }

    @Test
    void testVectorMultiplyMap() {
        DVector xcopy = x.copy();
        x.mult(z.map(IntArrays.newSeq(0, x.size())));
        assertEquals(100, x.size());
        for (int i = 0; i < x.size(); i++) {
            assertEquals(xcopy.get(i) * 10, x.get(i), TOL);
        }
    }

    @Test
    void testScalarDivide() {
        DVector xcopy = x.copy();
        x.div(10);
        for (int i = 0; i < x.size(); i++) {
            assertEquals(xcopy.get(i) / 10, x.get(i), TOL);
        }
    }

    @Test
    void vectorDivTest() {
        DVector xcopy = x.copy();
        x.div(z);
        assertEquals(100, x.size());
        for (int i = 0; i < x.size(); i++) {
            assertEquals(xcopy.get(i) / 10, x.get(i), TOL);
        }
    }

    @Test
    void vectorDivTestMap() {
        DVector xcopy = x.copy();
        x.div(z.map(IntArrays.newSeq(0, x.size())));
        assertEquals(100, x.size());
        for (int i = 0; i < x.size(); i++) {
            assertEquals(xcopy.get(i) / 10, x.get(i), TOL);
        }
    }

    @Test
    void vectorDotTest() {
        double result = x.dot(z);
        assertEquals(x.sum() * 10, result, TOL);
    }

    @Test
    void vectorDotTestMap() {
        double result = x.dot(z.map(IntArrays.newSeq(0, z.size())));
        assertEquals(x.sum() * 10, result, TOL);
    }

    @Test
    void dotBilinearTest() {
        double result = z.dotBilinear(m, z);
        assertEquals(Math.pow(z.norm(2), 2) * 2, result);

        result = z.dotBilinear(m);
        assertEquals(Math.pow(z.norm(2), 2) * 2, result);

        var ex = assertThrows(IllegalArgumentException.class, () -> z.dotBilinear(DMatrix.identity(10), z));
        assertEquals("Bilinear matrix and vector are not conform for multiplication.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> z.dotBilinear(DMatrix.identity(10)));
        assertEquals("Bilinear matrix is not conform for multiplication.", ex.getMessage());
    }

    @Test
    void dotBilinearDiag() {
        assertEquals(x.dotBilinearDiag(m), x.dotBilinearDiag(m, x));
        assertEquals(x.dotBilinearDiag(z), x.dotBilinearDiag(z, x));

        assertEquals(assertThrows(IllegalArgumentException.class,
                () -> x.dotBilinearDiag(DMatrix.identity(1))).getMessage(),
                "Bilinear matrix is not conform for multiplication.");

        assertEquals(assertThrows(IllegalArgumentException.class,
                () -> x.dotBilinearDiag(DMatrix.identity(1), z)).getMessage(),
                "Bilinear matrix is not conform for multiplication.");

        assertEquals(assertThrows(IllegalArgumentException.class,
                () -> x.dotBilinearDiag(DVector.ones(1))).getMessage(),
                "Bilinear diagonal vector is not conform for multiplication.");

        assertEquals(assertThrows(IllegalArgumentException.class,
                () -> x.dotBilinearDiag(DVector.ones(1), z)).getMessage(),
                "Bilinear diagonal vector is not conform for multiplication.");
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
        assertEquals(1, x.apply(Math::abs).normalize(1).sum(), TOL);
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
        DVector v = generateOnesWithMissing();
        double nanprod = v.valueStream().filter(Double::isFinite).reduce(1.0, (left, right) -> left * right);
        assertEquals(nanprod, v.nanprod(), TOL);
    }

    @Test
    void testCumSum() {
        z.cumsum();
        assertTrue(z.deepEquals(DVector.wrap(DoubleArrays.newSeq(1, z.size() + 1)).mult(10)));
    }

    @Test
    void testProduct() {
        assertEquals(1.0000000000000006E100, z.prod());

        DVector v = generateFill(10, 2);
        v.cumprod();
        for (int i = 0; i < v.size(); i++) {
            assertEquals(2 << i, v.get(i));
        }
    }

    @Test
    void copyTest() {
        var v = generateFill(10, 1);
        var copy1 = v.copy();
        assertTrue(v.deepEquals(copy1));
        assertEquals(VType.DENSE, copy1.type());
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
        var m1 = DMatrix.wrap(new double[][]{{1}, {3}, {9}});

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

    @Test
    void testStream() {
        double xsum = z.valueStream().sum();
        assertEquals(1_000, xsum, TOL);
    }
}
