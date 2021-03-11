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

package rapaio.math.linear.dense;

import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.SOrder;
import rapaio.math.linear.StandardDMatrixTest;

import static org.junit.jupiter.api.Assertions.*;

public class DMatrixStripeCTest extends StandardDMatrixTest {

    @Override
    protected SOrder order() {
        return SOrder.C;
    }

    @Override
    protected DMatrix generateSequential(int n, int m) {
        DMatrixStripe matrix = DMatrixStripe.empty(SOrder.C, n, m);
        int seq = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix.set(i, j, seq++);
            }
        }
        return matrix;
    }

    @Override
    protected DMatrix generateIdentity(int n) {
        return DMatrixStripe.identity(SOrder.C, n);
    }

    @Override
    protected DMatrix generateFill(int n, int m, double fill) {
        return DMatrixStripe.fill(SOrder.C, n, m, fill);
    }

    @Override
    protected DMatrix generateCopy(double[][] values) {
        return DMatrixStripe.copy(SOrder.C, values);
    }

    @Override
    protected String className() {
        return "DMatrixStripeC";
    }

    @Test
    void buildersTest() {

        DMatrix i3 = DMatrixStripe.identity(SOrder.C, 3);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    assertEquals(1.0, i3.get(i, j), TOL);
                } else {
                    assertEquals(0.0, i3.get(i, j), TOL);
                }
            }
        }

        DMatrix empty = DMatrixStripe.empty(SOrder.C, 3, 4);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(0, empty.get(i, j), TOL);
            }
        }


        DMatrix fill = DMatrixStripe.fill(SOrder.C, 3, 4, 12);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(12, fill.get(i, j), TOL);
            }
        }

        DMatrix fillFun = DMatrixStripe.fill(SOrder.C, 3, 4, (i, j) -> Math.sqrt(i * j));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(Math.sqrt(i * j), fillFun.get(i, j), TOL);
            }
        }

        Frame iris = Datasets.loadIrisDataset().mapVars(VRange.onlyTypes(VType.DOUBLE));
        DMatrix copy1 = DMatrixStripe.copy(SOrder.C, iris);
        for (int i = 0; i < iris.varCount(); i++) {
            for (int j = 0; j < iris.rowCount(); j++) {
                assertEquals(iris.getDouble(j, i), copy1.get(j, i), TOL);
            }
        }

        DMatrix copy2 = DMatrixStripe.copy(SOrder.C, iris.varStream().toArray(Var[]::new));
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
        DMatrix copy3 = DMatrixStripe.copy(SOrder.C, 3, 4, values);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(values[j * 3 + i], copy3.get(i, j), TOL);
            }
        }

        double[][] m = new double[][]{
                {1, 2, 3, 4},
                {5, 6, 7, 8},
                {9, 10, 11, 12}
        };
        DMatrix copy4 = DMatrixStripe.copy(SOrder.C, m);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(m[i][j], copy4.get(i, j), TOL);
            }
        }

        DMatrix copy5 = DMatrixStripe.copy(SOrder.C, m, 1, 3, 1, 4);
        assertTrue(copy5.deepEquals(DMatrixStripe.wrap(new double[][]{{6, 7, 8}, {10, 11, 12}})));

        DMatrix copy6 = DMatrixStripe.random(SOrder.C, 2, 2);
        assertEquals(4, copy6.valueStream().filter(Double::isFinite).filter(v -> v != 0).count());
    }
}
