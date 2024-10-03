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

package rapaio.experiment.math.linear;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.experiment.math.linear.dense.DMatrixDenseR;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/14/20.
 */
public abstract class StandardDMatrixTest {

    protected static final double TOL = 1e-15;
    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(123);
    }

    protected abstract DMatrix generateSequential(int n, int m);

    protected abstract DMatrix generateIdentity(int n);

    protected abstract DMatrix generateFill(int n, int m, double fill);

    protected abstract DMatrix generateCopy(double[][] values);

    protected abstract String className();

    @Test
    void testMapRow() {
        DMatrix m = generateSequential(10, 11);
        DVector vector1 = m.mapRow(3);
        DVector vector2 = m.mapRowNew(3);
        for (int i = 0; i < vector1.size(); i++) {
            assertEquals(33.0 + i, vector1.get(i), TOL);
            assertEquals(33.0 + i, vector2.get(i), TOL);
        }
    }

    @Test
    void testMapRows() {
        DMatrix m = generateSequential(10, 11);

        DMatrix view = m.mapRows(3, 4);
        DMatrix copy = m.mapRowsNew(3, 4);

        for (int i = 0; i < view.rows(); i++) {
            for (int j = 0; j < view.cols(); j++) {
                assertEquals(view.get(i, j), copy.get(i, j), TOL);
            }
        }

        view.set(0, 0, 100);
        copy.set(0, 0, 200);

        assertEquals(m.get(3, 0), view.get(0, 0), TOL);
        assertEquals(100, m.get(3, 0), TOL);
        assertEquals(200, copy.get(0, 0), TOL);
    }

    @Test
    void testRangeRows() {
        DMatrix m = generateSequential(10, 11);

        DMatrix view = m.rangeRows(3, 5);
        DMatrix copy = m.rangeRowsNew(3, 5);

        for (int i = 0; i < view.rows(); i++) {
            for (int j = 0; j < view.cols(); j++) {
                assertEquals(view.get(i, j), copy.get(i, j), TOL);
            }
        }

        view.set(0, 0, 100);
        copy.set(0, 0, 200);

        assertEquals(m.get(3, 0), view.get(0, 0), TOL);
        assertEquals(100, m.get(3, 0), TOL);
        assertEquals(200, copy.get(0, 0), TOL);
    }

    @Test
    void testRemoveRows() {
        DMatrix m = generateSequential(10, 11);

        DMatrix view = m.removeRows(0, 1, 2, 4, 6, 7, 8, 9, 10);
        DMatrix copy = m.removeRowsNew(0, 1, 2, 4, 6, 7, 8, 9, 10);

        for (int i = 0; i < view.rows(); i++) {
            for (int j = 0; j < view.cols(); j++) {
                assertEquals(view.get(i, j), copy.get(i, j), TOL);
            }
        }

        view.set(0, 0, 100);
        copy.set(0, 0, 200);

        assertEquals(m.get(3, 0), view.get(0, 0), TOL);
        assertEquals(100, m.get(3, 0), TOL);
        assertEquals(200, copy.get(0, 0), TOL);
    }

    @Test
    void testMapCol() {
        DMatrix m = generateSequential(10, 11);
        DVector vector1 = m.mapCol(3);
        DVector vector2 = m.mapColNew(3);
        for (int i = 0; i < vector1.size(); i++) {
            assertEquals(3.0 + i * 11, vector1.get(i), TOL);
            assertEquals(3.0 + i * 11, vector2.get(i), TOL);
        }
    }

    @Test
    void testMapCols() {
        DMatrix m = generateSequential(10, 11);

        DMatrix view = m.mapCols(3, 4);
        DMatrix copy = m.mapColsNew(3, 4);

        for (int i = 0; i < view.rows(); i++) {
            for (int j = 0; j < view.cols(); j++) {
                assertEquals(view.get(i, j), copy.get(i, j), TOL);
            }
        }

        view.set(0, 0, 100);
        copy.set(0, 0, 200);

        assertEquals(m.get(0, 3), view.get(0, 0), TOL);
        assertEquals(100, m.get(0, 3), TOL);
        assertEquals(200, copy.get(0, 0), TOL);
    }

    @Test
    void testRangeCols() {
        DMatrix m = generateSequential(10, 11);

        DMatrix view = m.rangeCols(3, 5);
        DMatrix copy = m.rangeColsNew(3, 5);

        for (int i = 0; i < view.rows(); i++) {
            for (int j = 0; j < view.cols(); j++) {
                assertEquals(view.get(i, j), copy.get(i, j), TOL);
            }
        }

        view.set(0, 0, 100);
        copy.set(0, 0, 200);

        assertEquals(m.get(0, 3), view.get(0, 0), TOL);
        assertEquals(100, m.get(0, 3), TOL);
        assertEquals(200, copy.get(0, 0), TOL);
    }

    @Test
    void testRemoveCols() {
        DMatrix m = generateSequential(10, 11);

        DMatrix view = m.removeCols(0, 1, 2, 4, 6, 7, 8, 9, 10);
        DMatrix copy = m.removeColsNew(0, 1, 2, 4, 6, 7, 8, 9, 10);

        for (int i = 0; i < view.rows(); i++) {
            for (int j = 0; j < view.cols(); j++) {
                assertEquals(view.get(i, j), copy.get(i, j), TOL);
            }
        }

        view.set(0, 0, 100);
        copy.set(0, 0, 200);

        assertEquals(m.get(0, 3), view.get(0, 0), TOL);
        assertEquals(100, m.get(0, 3), TOL);
        assertEquals(200, copy.get(0, 0), TOL);
    }

    @Test
    void testAdd() {
        DMatrix m1 = generateSequential(20, 10);
        DMatrix m2 = generateSequential(20, 10);

        DMatrix t1 = m1.copy().add(m2);
        DMatrix t2 = m1.copy().add(1);
        DMatrix t3 = m1.add(1).add(m2);

        assertEquals(20, t1.rows());
        assertEquals(10, t1.cols());
        assertEquals(20, t2.rows());
        assertEquals(10, t2.cols());
        assertEquals(20, t3.rows());
        assertEquals(10, t3.cols());

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                assertEquals(2 * (i * 10 + j), t1.get(i, j), TOL);
                assertEquals(i * 10 + j + 1, t2.get(i, j), TOL);
                assertEquals(2 * (i * 10 + j) + 1, t3.get(i, j));
            }
        }

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> generateSequential(10, 10).add(generateSequential(11, 11)));
        Assertions.assertEquals("Matrices are not conform with this operation.", ex.getMessage());
    }

    @Test
    void testAddAxis() {
        DMatrix m1 = generateFill(20, 10, 1);

        DVector v1 = DVector.fill(10, 1);
        DVector v2 = DVector.fill(20, 1);

        assertTrue(generateFill(20, 10, 2).deepEquals(m1.copy().add(v1, 0)));
        assertTrue(generateFill(20, 10, 2).deepEquals(m1.copy().add(v2, 1)));
    }

    @Test
    void testSubtract() {
        DMatrix m1 = generateSequential(20, 10);
        DMatrix m2 = generateSequential(20, 10);

        DMatrix t1 = m1.copy().sub(m2);
        DMatrix t2 = m1.copy().sub(1);
        DMatrix t3 = m1.sub(1).sub(m2);

        assertEquals(20, t1.rows());
        assertEquals(10, t1.cols());
        assertEquals(20, t2.rows());
        assertEquals(10, t2.cols());
        assertEquals(20, t3.rows());
        assertEquals(10, t3.cols());

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                assertEquals(0, t1.get(i, j), TOL);
                assertEquals(i * 10 + j - 1, t2.get(i, j), TOL);
                assertEquals(-1, t3.get(i, j));
            }
        }

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> generateSequential(10, 10).sub(generateSequential(9, 10)));
        Assertions.assertEquals("Matrices are not conform with this operation.", ex.getMessage());
    }

    @Test
    void testSubtractAxis() {
        DMatrix m1 = generateFill(20, 10, 1);

        DVector v1 = DVector.fill(10, 1);
        DVector v2 = DVector.fill(20, 1);

        assertTrue(generateFill(20, 10, 0).deepEquals(m1.copy().sub(v1, 0)));
        assertTrue(generateFill(20, 10, 0).deepEquals(m1.copy().sub(v2, 1)));
    }

    @Test
    void testMultiply() {
        DMatrix m1 = generateSequential(20, 10);
        DMatrix m2 = generateSequential(20, 10);

        DMatrix t1 = m1.copy().mul(m2);
        DMatrix t2 = m1.copy().mul(2);
        DMatrix t3 = m1.mul(2).mul(m2);

        assertEquals(20, t1.rows());
        assertEquals(10, t1.cols());
        assertEquals(20, t2.rows());
        assertEquals(10, t2.cols());
        assertEquals(20, t3.rows());
        assertEquals(10, t3.cols());

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                assertEquals(Math.pow(i * 10 + j, 2), t1.get(i, j), TOL);
                assertEquals(2 * (i * 10 + j), t2.get(i, j), TOL);
                assertEquals(2 * Math.pow(i * 10 + j, 2), t3.get(i, j));
            }
        }

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> generateSequential(10, 10).mul(generateSequential(10, 9)));
        Assertions.assertEquals("Matrices are not conform with this operation.", ex.getMessage());

    }

    @Test
    void testDivide() {
        DMatrix m1 = generateSequential(20, 10);
        DMatrix m2 = generateSequential(20, 10);

        DMatrix t1 = m1.copy().div(m2);
        DMatrix t2 = m1.copy().div(2);
        DMatrix t3 = m1.div(2).div(m2);

        assertEquals(20, t1.rows());
        assertEquals(10, t1.cols());
        assertEquals(20, t2.rows());
        assertEquals(10, t2.cols());
        assertEquals(20, t3.rows());
        assertEquals(10, t3.cols());

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                if (i == 0 && j == 0) {
                    assertEquals(Double.NaN, t1.get(i, j), TOL);
                    assertEquals(0, t2.get(i, j), TOL);
                    assertEquals(Double.NaN, t3.get(i, j));
                } else {
                    assertEquals(1, t1.get(i, j), TOL);
                    assertEquals((i * 10. + j) / 2, t2.get(i, j), TOL);
                    assertEquals(1 / 2., t3.get(i, j));
                }
            }
        }

        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> generateSequential(10, 10).div(generateSequential(9, 9)));
        Assertions.assertEquals("Matrices are not conform with this operation.", ex.getMessage());
    }

    @Test
    void testSum() {
        var m1 = generateSequential(5, 3);
        assertEquals(105, m1.sum());
        assertTrue(DVector.wrap(30, 35, 40).deepEquals(m1.sum(0)));
        assertTrue(DVector.wrap(3, 12, 21, 30, 39).deepEquals(m1.sum(1)));
    }

    @Test
    void testApply() {
        var m = generateSequential(10, 10);
        var copy = m.copy();
        m.apply(x -> x - 10).apply(x -> x + 10);
        assertTrue(m.deepEquals(copy));
    }

    @Test
    void testDot() {

        DVector v = DVector.ones(10);
        DMatrix m = generateSequential(10, 10);

        var m1 = m.dot(v);
        for (int i = 0; i < m1.size(); i++) {
            assertEquals(45 + 100 * i, m1.get(i), TOL);
        }

        DMatrix n1 = generateSequential(10, 20);
        DMatrix n2 = generateSequential(20, 30);

        var m2 = n1.dot(n2);
        assertEquals(10, m2.rows());
        assertEquals(30, m2.cols());

        for (int i = 0; i < m2.rows(); i++) {
            for (int j = 0; j < m2.cols(); j++) {
                assertEquals(n1.mapRow(i).dot(n2.mapCol(j)), m2.get(i, j), TOL);
            }
        }
    }

    @Test
    void dotDiagTest() {
        var m = generateFill(10, 3, 1);
        var v1 = DVector.wrap(1, 2, 3);

        var d = DMatrixDenseR.wrap(3, 3,
                1, 0, 0,
                0, 2, 0,
                0, 0, 3
        );
        var r1 = m.copy().dot(d);


        assertTrue(r1.deepEquals(m.copy().mul(v1, 0)));
    }

    @Test
    void testTrace() {
        for (int i = 1; i < 10; i++) {
            int len = i + 1;
            var m = generateSequential(len, len);
            assertEquals((len + 1) * (len) * (len - 1) / 2., m.trace(), TOL);
        }

        var ex = assertThrows(IllegalArgumentException.class, () -> generateSequential(2, 3).trace());
        Assertions.assertEquals("Matrix is not squared, trace of the matrix is not defined.", ex.getMessage());
    }

    @Test
    void testDiag() {
        var m = generateSequential(10, 10);
        var d = m.diag();
        for (int i = 1; i < 10; i++) {
            assertEquals(i * 11, d.get(i), TOL);
        }
    }

    @Test
    void testScatter() {
        for (int i = 1; i < 10; i++) {
            var x = generateSequential(i, i);
            var s = x.scatter();

            var c = generateIdentity(i).sub(generateFill(i, i, 1.0 / i));
            var s2 = x.t().dot(c).dot(x);

            assertTrue(s2.deepEquals(s, 1e-11));
        }
    }

    @Test
    void maxValues() {
        var m = generateCopy(new double[][] {
                {1, 2, 3, 4},
                {5, 6, 7, 8}
        });
        assertArrayEquals(new double[] {5, 6, 7, 8}, m.max(0).valueStream().toArray());
        assertArrayEquals(new double[] {4, 8}, m.max(1).valueStream().toArray());

        assertArrayEquals(new double[] {1, 2, 3, 4}, m.min(0).valueStream().toArray());
        assertArrayEquals(new double[] {1, 5}, m.min(1).valueStream().toArray());
    }

    @Test
    void testArgMaxArgMin() {
        double[][] m = new double[][] {
                {1, -1, 2},
                {2, 1, 1},
                {1, 2, 2},
                {2, 2, 1}
        };

        assertArrayEquals(new int[] {1, 2, 0}, generateCopy(m).argmax(0));
        assertArrayEquals(new int[] {2, 0, 1, 0}, generateCopy(m).argmax(1));

        assertArrayEquals(new int[] {0, 0, 1}, generateCopy(m).argmin(0));
        assertArrayEquals(new int[] {1, 1, 0, 2}, generateCopy(m).argmin(1));
    }

    @Test
    void deepEqualsTest() {
        DMatrix m1 = generateIdentity(2);

        DMatrix m2 = DMatrixDenseR.wrap(2, 2, 1, 0, 0, 1);
        DMatrix m3 = DMatrix.eye(2);

        assertTrue(m1.deepEquals(m2));
        assertTrue(m2.deepEquals(m3));

        m2.inc(1, 1, 1);
        m3.inc(0, 1, 2);

        assertFalse(m1.deepEquals(m2));
        assertFalse(m1.deepEquals(m3));

        var m4 = DMatrix.fill(2, 3, 0);
        var m5 = DMatrix.fill(3, 2, 0);

        assertFalse(m4.deepEquals(m1));
        assertFalse(m5.deepEquals(m1));

        m1 = generateCopy(new double[][] {
                {1, 1, 2},
                {1, 2, 4}
        });
        m2 = generateCopy(new double[][] {
                {1d + 0x1.0p-47, 1, 2},
                {1, 2, 4}
        });

        assertTrue(m1.deepEquals(m2));
        assertTrue(m2.deepEquals(m1));
        assertTrue(m1.deepEquals(m2, 1e-14));
        assertFalse(m1.deepEquals(m2, 1e-30));

        assertFalse(m1.deepEquals(generateSequential(2, 2)));
        assertFalse(m1.deepEquals(generateSequential(3, 3)));
    }

    @Test
    void testPrintable() {

        var id3 = generateIdentity(3).add(0x.1p-22);

        assertEquals(className() + "{rowCount:3, colCount:3, values:\n"
                + "[\n"
                + " [ 1.0000000149011612         0.000000014901161193847656 0.000000014901161193847656 ], \n"
                + " [ 0.000000014901161193847656 1.0000000149011612         0.000000014901161193847656 ], \n"
                + " [ 0.000000014901161193847656 0.000000014901161193847656 1.0000000149011612         ], \n"
                + "]}", id3.toString());

        assertEquals("""
                                           [0]                        [1]                        [2]\s
                [0] 1.0000000149011612         0.000000014901161193847656 0.000000014901161193847656\s
                [1] 0.000000014901161193847656 1.0000000149011612         0.000000014901161193847656\s
                [2] 0.000000014901161193847656 0.000000014901161193847656 1.0000000149011612        \s
                """, id3.toSummary());

        assertEquals("""
                                           [0]                        [1]                        [2]\s
                [0] 1.0000000149011612         0.000000014901161193847656 0.000000014901161193847656\s
                [1] 0.000000014901161193847656 1.0000000149011612         0.000000014901161193847656\s
                [2] 0.000000014901161193847656 0.000000014901161193847656 1.0000000149011612        \s
                """, id3.toContent());

        assertEquals("""
                                           [0]                        [1]                        [2]\s
                [0] 1.0000000149011612         0.000000014901161193847656 0.000000014901161193847656\s
                [1] 0.000000014901161193847656 1.0000000149011612         0.000000014901161193847656\s
                [2] 0.000000014901161193847656 0.000000014901161193847656 1.0000000149011612        \s
                """, id3.toFullContent());


        var id25 = generateIdentity(25);

        assertEquals(className() + "{rowCount:25, colCount:25, values:\n" +
                "[\n" +
                " [ 1  0  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  1  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  1  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  1  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  1  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  1  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  1  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  1  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  1  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  0  1  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ 0  0  0  0  0  0  0  0  0  0  .. ], \n" +
                " [ .. .. .. .. .. .. .. .. .. .. .. ], \n" +
                "]}", id25.toString());

        assertEquals("""
                             [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10] [11] [12] [13] [14] [15] [16] [17] [18] [19] ... [23] [24]\s
                         [0]  1   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [1]  0   1   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [2]  0   0   1   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [3]  0   0   0   1   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [4]  0   0   0   0   1   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [5]  0   0   0   0   0   1   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [6]  0   0   0   0   0   0   1   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [7]  0   0   0   0   0   0   0   1   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [8]  0   0   0   0   0   0   0   0   1   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [9]  0   0   0   0   0   0   0   0   0   1   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                        [10]  0   0   0   0   0   0   0   0   0   0   1    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                        [11]  0   0   0   0   0   0   0   0   0   0   0    1    0    0    0    0    0    0    0    0   ...  0    0  \s
                        [12]  0   0   0   0   0   0   0   0   0   0   0    0    1    0    0    0    0    0    0    0   ...  0    0  \s
                        [13]  0   0   0   0   0   0   0   0   0   0   0    0    0    1    0    0    0    0    0    0   ...  0    0  \s
                        [14]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    1    0    0    0    0    0   ...  0    0  \s
                        [15]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    1    0    0    0    0   ...  0    0  \s
                        [16]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    1    0    0    0   ...  0    0  \s
                        [17]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    1    0    0   ...  0    0  \s
                        [18]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    1    0   ...  0    0  \s
                        [19]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    1   ...  0    0  \s
                        ...  ... ... ... ... ... ... ... ... ... ... ...  ...  ...  ...  ...  ...  ...  ...  ...  ...  ... ...  ... \s
                        [23]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  1    0  \s
                        [24]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    1  \s
                        """,
                id25.toSummary());

        assertEquals("""
                             [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10] [11] [12] [13] [14] [15] [16] [17] [18] [19] ... [23] [24]\s
                         [0]  1   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [1]  0   1   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [2]  0   0   1   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [3]  0   0   0   1   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [4]  0   0   0   0   1   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [5]  0   0   0   0   0   1   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [6]  0   0   0   0   0   0   1   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [7]  0   0   0   0   0   0   0   1   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [8]  0   0   0   0   0   0   0   0   1   0   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                         [9]  0   0   0   0   0   0   0   0   0   1   0    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                        [10]  0   0   0   0   0   0   0   0   0   0   1    0    0    0    0    0    0    0    0    0   ...  0    0  \s
                        [11]  0   0   0   0   0   0   0   0   0   0   0    1    0    0    0    0    0    0    0    0   ...  0    0  \s
                        [12]  0   0   0   0   0   0   0   0   0   0   0    0    1    0    0    0    0    0    0    0   ...  0    0  \s
                        [13]  0   0   0   0   0   0   0   0   0   0   0    0    0    1    0    0    0    0    0    0   ...  0    0  \s
                        [14]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    1    0    0    0    0    0   ...  0    0  \s
                        [15]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    1    0    0    0    0   ...  0    0  \s
                        [16]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    1    0    0    0   ...  0    0  \s
                        [17]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    1    0    0   ...  0    0  \s
                        [18]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    1    0   ...  0    0  \s
                        [19]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    1   ...  0    0  \s
                        ...  ... ... ... ... ... ... ... ... ... ... ...  ...  ...  ...  ...  ...  ...  ...  ...  ...  ... ...  ... \s
                        [23]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  1    0  \s
                        [24]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0   ...  0    1  \s
                        """,
                id25.toContent());

        assertEquals(
                """
                             [0] [1] [2] [3] [4] [5] [6] [7] [8] [9] [10] [11] [12] [13] [14] [15] [16] [17] [18] [19] [20] [21] [22] [23] [24]\s
                         [0]  1   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0    0    0    0    0    0  \s
                         [1]  0   1   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0    0    0    0    0    0  \s
                         [2]  0   0   1   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0    0    0    0    0    0  \s
                         [3]  0   0   0   1   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0    0    0    0    0    0  \s
                         [4]  0   0   0   0   1   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0    0    0    0    0    0  \s
                         [5]  0   0   0   0   0   1   0   0   0   0   0    0    0    0    0    0    0    0    0    0    0    0    0    0    0  \s
                         [6]  0   0   0   0   0   0   1   0   0   0   0    0    0    0    0    0    0    0    0    0    0    0    0    0    0  \s
                         [7]  0   0   0   0   0   0   0   1   0   0   0    0    0    0    0    0    0    0    0    0    0    0    0    0    0  \s
                         [8]  0   0   0   0   0   0   0   0   1   0   0    0    0    0    0    0    0    0    0    0    0    0    0    0    0  \s
                         [9]  0   0   0   0   0   0   0   0   0   1   0    0    0    0    0    0    0    0    0    0    0    0    0    0    0  \s
                        [10]  0   0   0   0   0   0   0   0   0   0   1    0    0    0    0    0    0    0    0    0    0    0    0    0    0  \s
                        [11]  0   0   0   0   0   0   0   0   0   0   0    1    0    0    0    0    0    0    0    0    0    0    0    0    0  \s
                        [12]  0   0   0   0   0   0   0   0   0   0   0    0    1    0    0    0    0    0    0    0    0    0    0    0    0  \s
                        [13]  0   0   0   0   0   0   0   0   0   0   0    0    0    1    0    0    0    0    0    0    0    0    0    0    0  \s
                        [14]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    1    0    0    0    0    0    0    0    0    0    0  \s
                        [15]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    1    0    0    0    0    0    0    0    0    0  \s
                        [16]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    1    0    0    0    0    0    0    0    0  \s
                        [17]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    1    0    0    0    0    0    0    0  \s
                        [18]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    1    0    0    0    0    0    0  \s
                        [19]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    1    0    0    0    0    0  \s
                        [20]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0    1    0    0    0    0  \s
                        [21]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0    0    1    0    0    0  \s
                        [22]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0    0    0    1    0    0  \s
                        [23]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0    0    0    0    1    0  \s
                        [24]  0   0   0   0   0   0   0   0   0   0   0    0    0    0    0    0    0    0    0    0    0    0    0    0    1  \s
                        """,
                id25.toFullContent());
    }
}
