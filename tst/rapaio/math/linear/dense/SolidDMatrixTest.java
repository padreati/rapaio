/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.math.linear.dense;

import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;

import static org.junit.jupiter.api.Assertions.*;

public class SolidDMatrixTest {

    private static final double TOL = 1e-20;

    @Test
    void buildersTest() {

        DMatrix i3 = SolidDMatrix.identity(3);
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    assertEquals(1.0, i3.get(i, j), TOL);
                } else {
                    assertEquals(0.0, i3.get(i, j), TOL);
                }
            }

        DMatrix empty = SolidDMatrix.empty(3, 4);
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 4; j++)
                assertEquals(0, empty.get(i, j), TOL);


        DMatrix fill = SolidDMatrix.fill(3, 4, 12);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(12, fill.get(i, j), TOL);
            }
        }

        DMatrix fillFun = SolidDMatrix.fill(3, 4, (i, j) -> Math.sqrt(i * j));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(Math.sqrt(i * j), fillFun.get(i, j), TOL);
            }
        }

        Frame iris = Datasets.loadIrisDataset().mapVars(VRange.onlyTypes(VType.DOUBLE));
        DMatrix copy1 = SolidDMatrix.copy(iris);
        for (int i = 0; i < iris.varCount(); i++) {
            for (int j = 0; j < iris.rowCount(); j++) {
                assertEquals(iris.getDouble(j, i), copy1.get(j, i), TOL);
            }
        }

        DMatrix copy2 = SolidDMatrix.copy(iris.varStream().toArray(Var[]::new));
        for (int i = 0; i < iris.rowCount(); i++) {
            for (int j = 0; j < iris.varCount(); j++) {
                assertEquals(copy1.get(i, j), copy2.get(i, j), TOL);
            }
        }

        double[] values = new double[]{
                1, 2, 3, 4,
                5, 6, 7, 8,
                9, 10, 11, 12
        };
        DMatrix copy3 = SolidDMatrix.copy(3, 4, values);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(values[i * 4 + j], copy3.get(i, j), TOL);
            }
        }

        double[][] m = new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12}
        };
        DMatrix copy4 = SolidDMatrix.copy(m);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(m[i][j], copy4.get(i, j), TOL);
            }
        }

        DMatrix copy5 = SolidDMatrix.copy(m, 1, 3, 1, 4);
        assertEquals("       |      0|     1|     2|\n" +
                "     0 |  6.000  7.000  8.000\n" +
                "     1 | 10.000 11.000 12.000\n", copy5.toSummary());
    }

    @Test
    void testMappings() {

        double[][] values = new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12}
        };

        DMatrix x = SolidDMatrix.copy(values);
        DMatrix x_t = x.t();
        DMatrix y = x_t.t();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                double v = values[i][j];
                assertEquals(v, x.get(i, j), TOL);
                assertEquals(v, x_t.get(j, i), TOL);
                assertEquals(v, y.get(i, j), TOL);
            }
        }

        double[] col = new double[]{2, 6, 10};
        DVector vcol = x.mapCol(1);
        for (int i = 0; i < col.length; i++) {
            assertEquals(col[i], vcol.get(i), TOL);
        }

        double[] row = new double[]{5, 6, 7, 8};
        DVector vrow = x.mapRow(1);
        for (int i = 0; i < row.length; i++) {
            assertEquals(row[i], vrow.get(i), TOL);
        }

        double[] vals = x.valueStream().toArray();
        for (int i = 0; i < vals.length; i++) {
            assertEquals(i + 1, vals[i], TOL);
        }

        DMatrix xx = x.copy();
        for (int i = 0; i < x.rowCount(); i++) {
            for (int j = 0; j < x.colCount(); j++) {
                assertEquals(x.get(i, j), xx.get(i, j), TOL);
            }
        }
    }

    @Test
    void testOps() {
        DMatrix x = SolidDMatrix.empty(2, 2);
        x.set(0, 0, 1);
        x.increment(0, 1, 2);
        x.set(1, 0, 3);
        x.increment(1, 1, 6);
        x.increment(1, 1, -2);

        double[] vals = new double[]{1, 2, 3, 4};
        double[] xvals = x.valueStream().toArray();

        assertArrayEquals(vals, xvals, TOL);
    }
}
