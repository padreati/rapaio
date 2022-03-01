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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static rapaio.sys.With.*;

import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import rapaio.data.MappedVar;
import rapaio.data.VarDouble;
import rapaio.math.MathTools;
import rapaio.math.linear.dense.DMatrixDenseC;
import rapaio.math.linear.dense.DMatrixDenseR;
import rapaio.math.linear.dense.DVectorDense;
import rapaio.math.linear.dense.DVectorMap;
import rapaio.math.linear.dense.DVectorStride;
import rapaio.math.linear.dense.DVectorVar;
import rapaio.sys.With;
import rapaio.util.collection.IntArrays;

public class DenseAlgebraTest {

    private static final MatrixFactory[] mTypes = new MatrixFactory[] {
            (rows, cols) -> new DMatrixDenseR(rows, cols, newValues(rows, cols)),
            (rows, cols) -> new DMatrixDenseC(rows, cols, newValues(rows, cols)),
            (rows, cols) -> new DMatrixDenseR(rows, cols, newValues(rows, cols))
                    .mapRows(IntArrays.newSeq(rows))
                    .mapCols(IntArrays.newSeq(cols))
                    .mapRows(IntArrays.newSeq(rows))
    };
    private static final VectorFactory[] vectorFactories = new VectorFactory[] {
            // dense vector
            n -> {
                double[] values = new double[10 + n];
                for (int i = 0; i < n; i++) {
                    values[10 + i] = i + 1;
                }
                return new DVectorDense(10, n, values);
            },
            // stride vector
            n -> {
                double[] values = new double[10 + 2 * n];
                for (int i = 0; i < n; i++) {
                    values[10 + i * 2] = i + 1;
                }
                return new DVectorStride(10, n, 2, values);
            },
            // map vector
            n -> {
                double[] values = new double[10 + 2 * n];
                int[] indexes = new int[n];
                for (int i = 0; i < n; i++) {
                    values[10 + i * 2] = i + 1;
                    indexes[i] = 10 + i * 2;
                }
                return new DVectorMap(new DVectorDense(0, 10 + 2 * n, values), indexes);
            },
            // var vector
            n -> {
                double[] values = new double[10 + 2 * n];
                int[] indexes = new int[n];
                for (int i = 0; i < n; i++) {
                    values[10 + i * 2] = i + 1;
                    indexes[i] = 10 + i * 2;
                }
                VarDouble original = VarDouble.wrap(values);
                MappedVar mappedVar = original.mapRows(indexes);
                return new DVectorVar<>(mappedVar);
            }
    };

    interface VectorFactory {
        default DVector newInstance() {
            return newInstance(10);
        }

        DVector newInstance(int size);
    }

    interface MatrixFactory {
        default DMatrix newMatrix() {
            return newMatrix(10, 10);
        }

        DMatrix newMatrix(int rows, int cols);
    }

    public static double[] newValues(int m, int n) {
        double[] values = new double[10 + n * m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                values[i * m + j] = i + j + 1;
            }
        }
        return values;
    }

    private interface F1m {
        void apply(DMatrix m);
    }

    private void t1m(MatrixFactory mType, F1m f) {
        f.apply(mType.newMatrix());
    }

    @Test
    void testOneMatrix() {
        for (MatrixFactory mType : mTypes) {

            t1m(mType, m -> assertTrue(m.add(10, copy()).deepEquals(m.add(10))));
            t1m(mType, m -> assertTrue(m.sub(10, copy()).deepEquals(m.sub(10))));
            t1m(mType, m -> assertTrue(m.mul(10, copy()).deepEquals(m.mul(10))));
            t1m(mType, m -> assertTrue(m.div(10, copy()).deepEquals(m.div(10))));

            t1m(mType, m -> assertEquals(IntStream.range(0, 10).mapToObj(i -> 2 * i + 1).mapToInt(i -> i).sum(), m.trace()));

            t1m(mType, m -> assertTrue(DVector.wrap(IntStream.range(0, 10).mapToDouble(i -> 2.0 * i + 1).toArray()).deepEquals(m.diag())));

            t1m(mType, m -> assertTrue(m.scatter().deepEquals(m.copy().scatter())));

            t1m(mType, m -> assertEquals(1000, m.sum()));
            t1m(mType, m -> assertTrue(m.sum(0).deepEquals(m.sum(1))));
            t1m(mType, m -> assertEquals(16.666666666666668, m.variance()));
            t1m(mType, m -> assertTrue(m.variance(0).deepEquals(m.variance(1))));

            t1m(mType, m -> assertTrue(m.mapValues(m.argmax(0), 0).deepEquals(m.max(0))));
            t1m(mType, m -> assertTrue(m.mapValues(m.argmax(1), 1).deepEquals(m.max(1))));
            t1m(mType, m -> assertTrue(m.mapValues(m.argmin(0), 0).deepEquals(m.min(0))));
            t1m(mType, m -> assertTrue(m.mapValues(m.argmin(1), 1).deepEquals(m.min(1))));

            t1m(mType, m -> assertTrue(m.apply(MathTools::sqrt, With.copy()).deepEquals(m.apply(MathTools::sqrt))));
            t1m(mType, m -> assertTrue(m.t(copy()).t(copy()).deepEquals(m.t().t())));
            t1m(mType, m -> assertEquals(m.sum(), m.valueStream().sum()));

            t1m(mType, m -> assertTrue(m.deepEquals(m.resizeCopy(10, 10, 100))));
            t1m(mType, m -> assertEquals(m.sum(), m.resizeCopy(19, 19, 0).sum()));
            t1m(mType, m -> assertEquals(m.rangeCols(0, 5).rangeRows(0, 5).sum(), m.resizeCopy(5, 5, 0).sum()));

            t1m(mType, m -> assertEquals(m.getClass().getSimpleName() + """
                    {rowCount:10, colCount:10, values:
                    [
                     [  1  2  3  4  5  6  7  8  9 10 ],\s
                     [  2  3  4  5  6  7  8  9 10 11 ],\s
                     [  3  4  5  6  7  8  9 10 11 12 ],\s
                     [  4  5  6  7  8  9 10 11 12 13 ],\s
                     [  5  6  7  8  9 10 11 12 13 14 ],\s
                     [  6  7  8  9 10 11 12 13 14 15 ],\s
                     [  7  8  9 10 11 12 13 14 15 16 ],\s
                     [  8  9 10 11 12 13 14 15 16 17 ],\s
                     [  9 10 11 12 13 14 15 16 17 18 ],\s
                     [ 10 11 12 13 14 15 16 17 18 19 ],\s
                    ]}""", m.toString()));

            t1m(mType, m -> assertEquals("""
                        [0] [1] [2] [3] [4] [5] [6] [7] [8] [9]     [0] [1] [2] [3] [4] [5] [6] [7] [8] [9]     [0] [1] [2] [3] [4] [5] [6] [7] [8] [9]\s
                    [0]  1   2   3   4   5   6   7   8   9  10  [4]  5   6   7   8   9  10  11  12  13  14  [8]  9  10  11  12  13  14  15  16  17  18 \s
                    [1]  2   3   4   5   6   7   8   9  10  11  [5]  6   7   8   9  10  11  12  13  14  15  [9] 10  11  12  13  14  15  16  17  18  19 \s
                    [2]  3   4   5   6   7   8   9  10  11  12  [6]  7   8   9  10  11  12  13  14  15  16 \s
                    [3]  4   5   6   7   8   9  10  11  12  13  [7]  8   9  10  11  12  13  14  15  16  17 \s
                    """, m.toContent()));
            t1m(mType, m -> assertEquals("""
                        [0] [1] [2] [3] [4] [5] [6] [7] [8] [9]     [0] [1] [2] [3] [4] [5] [6] [7] [8] [9]     [0] [1] [2] [3] [4] [5] [6] [7] [8] [9]\s
                    [0]  1   2   3   4   5   6   7   8   9  10  [4]  5   6   7   8   9  10  11  12  13  14  [8]  9  10  11  12  13  14  15  16  17  18 \s
                    [1]  2   3   4   5   6   7   8   9  10  11  [5]  6   7   8   9  10  11  12  13  14  15  [9] 10  11  12  13  14  15  16  17  18  19 \s
                    [2]  3   4   5   6   7   8   9  10  11  12  [6]  7   8   9  10  11  12  13  14  15  16 \s
                    [3]  4   5   6   7   8   9  10  11  12  13  [7]  8   9  10  11  12  13  14  15  16  17 \s
                    """, m.toFullContent()));

            t1m(mType, m -> assertEquals("""
                        [0] [1] [2] [3] [4] [5] [6] [7] [8] [9]     [0] [1] [2] [3] [4] [5] [6] [7] [8] [9]     [0] [1] [2] [3] [4] [5] [6] [7] [8] [9]\s
                    [0]  1   2   3   4   5   6   7   8   9  10  [4]  5   6   7   8   9  10  11  12  13  14  [8]  9  10  11  12  13  14  15  16  17  18 \s
                    [1]  2   3   4   5   6   7   8   9  10  11  [5]  6   7   8   9  10  11  12  13  14  15  [9] 10  11  12  13  14  15  16  17  18  19 \s
                    [2]  3   4   5   6   7   8   9  10  11  12  [6]  7   8   9  10  11  12  13  14  15  16 \s
                    [3]  4   5   6   7   8   9  10  11  12  13  [7]  8   9  10  11  12  13  14  15  16  17 \s
                    """, m.toSummary()));

            assertEquals("""
                         [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10] [11] [12] [13] [14] [15] [16] [17] [18] [19] ... [98] [99]\s
                     [0]   1   2   3   4   5   6   7   8   9  10  11   12   13   14   15   16   17   18   19   20  ...  99  100 \s
                     [1]   2   3   4   5   6   7   8   9  10  11  12   13   14   15   16   17   18   19   20   21  ... 100  101 \s
                     [2]   3   4   5   6   7   8   9  10  11  12  13   14   15   16   17   18   19   20   21   22  ... 101  102 \s
                     [3]   4   5   6   7   8   9  10  11  12  13  14   15   16   17   18   19   20   21   22   23  ... 102  103 \s
                     [4]   5   6   7   8   9  10  11  12  13  14  15   16   17   18   19   20   21   22   23   24  ... 103  104 \s
                     [5]   6   7   8   9  10  11  12  13  14  15  16   17   18   19   20   21   22   23   24   25  ... 104  105 \s
                     [6]   7   8   9  10  11  12  13  14  15  16  17   18   19   20   21   22   23   24   25   26  ... 105  106 \s
                     [7]   8   9  10  11  12  13  14  15  16  17  18   19   20   21   22   23   24   25   26   27  ... 106  107 \s
                     [8]   9  10  11  12  13  14  15  16  17  18  19   20   21   22   23   24   25   26   27   28  ... 107  108 \s
                     [9]  10  11  12  13  14  15  16  17  18  19  20   21   22   23   24   25   26   27   28   29  ... 108  109 \s
                    [10]  11  12  13  14  15  16  17  18  19  20  21   22   23   24   25   26   27   28   29   30  ... 109  110 \s
                    [11]  12  13  14  15  16  17  18  19  20  21  22   23   24   25   26   27   28   29   30   31  ... 110  111 \s
                    [12]  13  14  15  16  17  18  19  20  21  22  23   24   25   26   27   28   29   30   31   32  ... 111  112 \s
                    [13]  14  15  16  17  18  19  20  21  22  23  24   25   26   27   28   29   30   31   32   33  ... 112  113 \s
                    [14]  15  16  17  18  19  20  21  22  23  24  25   26   27   28   29   30   31   32   33   34  ... 113  114 \s
                    [15]  16  17  18  19  20  21  22  23  24  25  26   27   28   29   30   31   32   33   34   35  ... 114  115 \s
                    [16]  17  18  19  20  21  22  23  24  25  26  27   28   29   30   31   32   33   34   35   36  ... 115  116 \s
                    [17]  18  19  20  21  22  23  24  25  26  27  28   29   30   31   32   33   34   35   36   37  ... 116  117 \s
                    [18]  19  20  21  22  23  24  25  26  27  28  29   30   31   32   33   34   35   36   37   38  ... 117  118 \s
                    [19]  20  21  22  23  24  25  26  27  28  29  30   31   32   33   34   35   36   37   38   39  ... 118  119 \s
                    ...  ... ... ... ... ... ... ... ... ... ... ...  ...  ...  ...  ...  ...  ...  ...  ...  ...  ... ...  ... \s
                    [98]  99 100 101 102 103 104 105 106 107 108 109  110  111  112  113  114  115  116  117  118  ... 197  198 \s
                    [99] 100 101 102 103 104 105 106 107 108 109 110  111  112  113  114  115  116  117  118  119  ... 198  199 \s
                    """, mType.newMatrix(100, 100).toContent());
        }
    }

    private interface F1m1v {
        void apply(DMatrix m, DVector v);
    }

    void t1m1v(MatrixFactory mType, VectorFactory vf, F1m1v f) {
        DMatrix m = mType.newMatrix();
        DVector v = vf.newInstance();
        f.apply(m, v);
    }

    @Test
    void testOneMatrixOneVector() {
        for (MatrixFactory mType : mTypes) {
            for (var vf : vectorFactories) {
                t1m1v(mType, vf, (m, v) -> assertTrue(m.add(v, 0, copy()).deepEquals(m.add(v, 0))));
                t1m1v(mType, vf, (m, v) -> assertTrue(m.add(v, 1, copy()).deepEquals(m.add(v, 1))));
                t1m1v(mType, vf, (m, v) -> assertTrue(m.sub(v, 0, copy()).deepEquals(m.sub(v, 0))));
                t1m1v(mType, vf, (m, v) -> assertTrue(m.sub(v, 1, copy()).deepEquals(m.sub(v, 1))));
                t1m1v(mType, vf, (m, v) -> assertTrue(m.mul(v, 0, copy()).deepEquals(m.mul(v, 0))));
                t1m1v(mType, vf, (m, v) -> assertTrue(m.mul(v, 1, copy()).deepEquals(m.mul(v, 1))));
                t1m1v(mType, vf, (m, v) -> assertTrue(m.div(v, 0, copy()).deepEquals(m.div(v, 0))));
                t1m1v(mType, vf, (m, v) -> assertTrue(m.div(v, 1, copy()).deepEquals(m.div(v, 1))));
                t1m1v(mType, vf, (m, v) -> assertTrue(m.dot(v).deepEquals(m.copy().dot(v.copy()))));
            }
        }
    }

    private interface F2m {
        void apply(DMatrix m1, DMatrix m2);
    }

    private void t2m(MatrixFactory mType1, MatrixFactory mType2, F2m f) {
        DMatrix m1 = mType1.newMatrix();
        DMatrix m2 = mType2.newMatrix();
        f.apply(m1, m2);
    }

    @Test
    void testTwoMatrices() {

        for (MatrixFactory mType1 : mTypes) {
            for (MatrixFactory mType2 : mTypes) {
                t2m(mType1, mType2, (m1, m2) -> assertTrue(m1.add(m2, copy()).deepEquals(m1.add(m2))));
                t2m(mType1, mType2, (m1, m2) -> assertTrue(m1.sub(m2, copy()).deepEquals(m1.sub(m2))));
                t2m(mType1, mType2, (m1, m2) -> assertTrue(m1.mul(m2, copy()).deepEquals(m1.mul(m2))));
                t2m(mType1, mType2, (m1, m2) -> assertTrue(m1.div(m2, copy()).deepEquals(m1.div(m2))));
                t2m(mType1, mType2, (m1, m2) -> assertTrue(m1.dot(m2).deepEquals(m1.copy().dot(m2.copy()))));
                int[] in = new int[] {0, 1, 2, 3, 4};
                int[] out = new int[] {5, 6, 7, 8, 9};
                t2m(mType1, mType2, (m1, m2) -> assertTrue(m1.mapRows(in).deepEquals(m2.removeRows(out))));
                t2m(mType1, mType2, (m1, m2) -> assertTrue(m1.mapRows(in, copy()).deepEquals(m2.removeRows(out, copy()))));
                t2m(mType1, mType2, (m1, m2) -> assertTrue(m1.mapCols(in).deepEquals(m2.removeCols(out))));
                t2m(mType1, mType2, (m1, m2) -> assertTrue(m1.mapCols(in, copy()).deepEquals(m2.removeCols(out, copy()))));
            }
        }
    }

    private interface F1v {
        void apply(DVector v);
    }

    private void t1v(VectorFactory vf, F1v f) {
        DVector v = vf.newInstance();
        f.apply(v);
    }

    @Test
    void testOneVector() {

        for (var vf : vectorFactories) {
            t1v(vf, v -> assertTrue(v.add(10, copy()).deepEquals(v.add(10))));
            t1v(vf, v -> assertTrue(v.sub(10, copy()).deepEquals(v.sub(10))));
            t1v(vf, v -> assertTrue(v.mul(10, copy()).deepEquals(v.mul(10))));
            t1v(vf, v -> assertTrue(v.div(10, copy()).deepEquals(v.div(10))));

            t1v(vf, v -> assertEquals(v.pnorm(1), v.sum()));
            t1v(vf, v -> assertEquals(10, v.pnorm(Double.POSITIVE_INFINITY)));
            t1v(vf, v -> assertEquals(55, v.sum()));
            t1v(vf, v -> assertEquals(55, v.nansum()));
            t1v(vf, v -> assertEquals(3628800, v.prod()));
            t1v(vf, v -> assertEquals(3628800, v.nanprod()));
            t1v(vf, v -> assertEquals(10, v.nancount()));
            t1v(vf, v -> assertEquals(5.5, v.mean()));
            t1v(vf, v -> assertEquals(5.5, v.nanmean()));
            t1v(vf, v -> assertEquals(9.166666666666666, v.variance()));
            t1v(vf, v -> assertEquals(9.166666666666666, v.nanvariance()));

            t1v(vf, v -> assertEquals(0, v.argmin()));
            t1v(vf, v -> assertEquals(1, v.min()));
            t1v(vf, v -> assertEquals(9, v.argmax()));
            t1v(vf, v -> assertEquals(10, v.max()));

            t1v(vf, v -> assertTrue(v.apply((row, x) -> row * x, copy()).deepEquals(v.apply((row, x) -> row * x))));

            int[] indexes = new int[] {2, 7, 3, 2};
            t1v(vf, v -> assertArrayEquals(indexes, v.map(indexes).valueStream().mapToInt(x -> (int) x - 1).toArray()));
            t1v(vf, v -> assertArrayEquals(indexes, v.map(indexes, copy()).valueStream().mapToInt(x -> (int) x - 1).toArray()));

            t1v(vf, v -> assertTrue(v.apply(x -> x + 1, copy()).deepEquals(v.apply(x -> x + 1))));

            t1v(vf, v -> {
                DVector cumsum = v.copy();
                for (int i = 1; i < cumsum.size(); i++) {
                    cumsum.inc(i, cumsum.get(i - 1));
                }
                assertTrue(cumsum.deepEquals(v.cumsum()));
            });

            t1v(vf, v -> {
                var cumprod = v.copy();
                for (int i = 1; i < cumprod.size(); i++) {
                    cumprod.set(i, cumprod.get(i) * cumprod.get(i - 1));
                }
                assertTrue(cumprod.deepEquals(v.cumprod()));
            });

            t1v(vf, v -> assertTrue(v.asMatrix().mapCol(0).deepEquals(v)));

            t1v(vf, v -> assertEquals(v.getClass().getSimpleName() + "{size:10, values:[1,2,3,4,5,6,7,8,9,10]}", v.toString()));
            t1v(vf, v -> assertEquals("""
                    [0]  1 [4]  5 [8]  9\s
                    [1]  2 [5]  6 [9] 10\s
                    [2]  3 [6]  7\s
                    [3]  4 [7]  8\s
                    """, v.toContent()));
            assertEquals("""
                     [0]   1  [6]   7 [12]  13 [18]  19\s
                     [1]   2  [7]   8 [13]  14 [19]  20\s
                     [2]   3  [8]   9 [14]  15 ...  ...\s
                     [3]   4  [9]  10 [15]  16 [98]  99\s
                     [4]   5 [10]  11 [16]  17 [99] 100\s
                     [5]   6 [11]  12 [17]  18\s
                    """, vf.newInstance(100).toContent());
            t1v(vf, v -> assertEquals("""
                    [0]  1 [4]  5 [8]  9\s
                    [1]  2 [5]  6 [9] 10\s
                    [2]  3 [6]  7\s
                    [3]  4 [7]  8\s
                    """, v.toFullContent()));
            t1v(vf, v -> assertEquals("""
                    [0]  1 [4]  5 [8]  9\s
                    [1]  2 [5]  6 [9] 10\s
                    [2]  3 [6]  7\s
                    [3]  4 [7]  8\s
                    """, v.toSummary()));
        }
    }

    @Test
    void testTwoVectors() {
        for (VectorFactory vf1 : vectorFactories) {
            for (VectorFactory vf2 : vectorFactories) {
                DVector v1 = vf1.newInstance();
                DVector v2 = vf2.newInstance();

                String msg = String.format("type1: %s, type2: %s", vf1.getClass().getName(), vf2.getClass().getName());
                assertTrue(v1.add(v2, copy()).deepEquals(v1.add(v2)), msg);
                assertTrue(v1.sub(v2, copy()).deepEquals(v1.sub(v2)), msg);
                assertTrue(v1.mul(v2, copy()).deepEquals(v1.mul(v2)), msg);
                assertTrue(v1.div(v2, copy()).deepEquals(v1.div(v2)), msg);

                v1 = vf1.newInstance();
                v2 = vf2.newInstance();
                assertTrue(v1.addMul(10, v2, copy()).deepEquals(v1.addMul(10, v2)), msg);

                v1 = vf1.newInstance();
                v2 = vf2.newInstance();

                assertEquals(385.0, v1.dot(v2));
                assertEquals(385.0, v1.dotBilinear(DMatrix.identity(10), v2));
                assertEquals(385.0, v1.dotBilinear(DMatrix.identity(10)));
                assertEquals(385.0, v1.dotBilinearDiag(DMatrix.identity(10), v2));
                assertEquals(385.0, v1.dotBilinearDiag(DVector.ones(10)));

                DMatrix m = v1.outer(v2);
                assertEquals(v1.size(), m.rowCount());
                assertEquals(v2.size(), m.colCount());
                for (int i = 0; i < v1.size(); i++) {
                    for (int j = 0; j < v2.size(); j++) {
                        assertEquals(v1.get(i) * v2.get(j), m.get(i, j));
                    }
                }
            }
        }
    }

    private interface F1m2v {
        void apply(DVector v1, DVector v2, DMatrix m);
    }

    private void t1m2v(VectorFactory vf1, VectorFactory vf2, MatrixFactory mType, F1m2v f) {
        DVector v1 = vf1.newInstance();
        DVector v2 = vf2.newInstance();
        DMatrix m = mType.newMatrix();
        f.apply(v1, v2, m);
    }

    @Test
    void testOneMatrixTwoVectors() {
        for (var vf1 : vectorFactories) {
            for (var vf2 : vectorFactories) {
                for (MatrixFactory mType1 : mTypes) {
                    t1m2v(vf1, vf2, mType1, (v1, v2, m)
                            -> assertEquals(v1.copy().mul(m.diag()).mul(v2).sum(), v1.dotBilinearDiag(m, v2)));
                    t1m2v(vf1, vf2, mType1, (v1, v2, m)
                            -> assertEquals(v1.copy().mul(m.diag()).mul(v1).sum(), v1.dotBilinearDiag(m)));
                    t1m2v(vf1, vf2, mType1, (v1, v2, m)
                            -> assertEquals(v1.copy().mul(v1).mul(v2).sum(), v1.dotBilinearDiag(v2, v1)));
                    t1m2v(vf1, vf2, mType1, (v1, v2, m)
                            -> assertEquals(v1.copy().mul(v1).mul(v2).sum(), v1.dotBilinearDiag(v2)));
                }
            }
        }
    }

}
