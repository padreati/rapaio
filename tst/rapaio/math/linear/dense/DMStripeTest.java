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
import rapaio.math.linear.DM;
import rapaio.math.linear.StandardDMTest;

import static org.junit.jupiter.api.Assertions.*;

public class DMStripeTest extends StandardDMTest {

    @Override
    protected DM.Type type() {
        return DM.Type.STRIPE;
    }

    @Override
    protected DM generateSequential(int n, int m) {
        DMStripe matrix = rapaio.math.linear.dense.DMStripe.empty(n, m);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix.set(i, j, i * m + j);
            }
        }
        return matrix;
    }

    @Override
    protected DM generateIdentity(int n) {
        return rapaio.math.linear.dense.DMStripe.identity(n);
    }

    @Override
    protected DM generateFill(int n, int m, double fill) {
        return rapaio.math.linear.dense.DMStripe.fill(n, m, fill);
    }

    @Override
    protected DM generateWrap(double[][] values) {
        return rapaio.math.linear.dense.DMStripe.wrap(values);
    }

    @Override
    protected String className() {
        return "DMStripe";
    }

    @Test
    void buildersTest() {

        DM i3 = rapaio.math.linear.dense.DMStripe.identity(3);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == j) {
                    assertEquals(1.0, i3.get(i, j), TOL);
                } else {
                    assertEquals(0.0, i3.get(i, j), TOL);
                }
            }
        }

        DM empty = rapaio.math.linear.dense.DMStripe.empty(3, 4);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(0, empty.get(i, j), TOL);
            }
        }


        DM fill = rapaio.math.linear.dense.DMStripe.fill(3, 4, 12);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(12, fill.get(i, j), TOL);
            }
        }

        DM fillFun = rapaio.math.linear.dense.DMStripe.fill(3, 4, (i, j) -> Math.sqrt(i * j));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(Math.sqrt(i * j), fillFun.get(i, j), TOL);
            }
        }

        Frame iris = Datasets.loadIrisDataset().mapVars(VRange.onlyTypes(VType.DOUBLE));
        DM copy1 = rapaio.math.linear.dense.DMStripe.copy(iris);
        for (int i = 0; i < iris.varCount(); i++) {
            for (int j = 0; j < iris.rowCount(); j++) {
                assertEquals(iris.getDouble(j, i), copy1.get(j, i), TOL);
            }
        }

        DM copy2 = rapaio.math.linear.dense.DMStripe.copy(iris.varStream().toArray(Var[]::new));
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
        DM copy3 = rapaio.math.linear.dense.DMStripe.copy(3, 4, values);
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
        DM copy4 = rapaio.math.linear.dense.DMStripe.copy(m);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(m[i][j], copy4.get(i, j), TOL);
            }
        }

        DM copy5 = rapaio.math.linear.dense.DMStripe.copy(m, 1, 3, 1, 4);
        assertTrue(copy5.deepEquals(rapaio.math.linear.dense.DMStripe.wrap(new double[][]{{6, 7, 8}, {10, 11, 12}})));

        DM copy6 = rapaio.math.linear.dense.DMStripe.random(2, 2);
        assertEquals(4, copy6.valueStream().filter(Double::isFinite).filter(v -> v != 0).count());
    }
}
