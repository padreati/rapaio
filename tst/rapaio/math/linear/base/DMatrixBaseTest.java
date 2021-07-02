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

package rapaio.math.linear.base;

import org.junit.jupiter.api.Test;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.MType;
import rapaio.math.linear.StandardDMatrixTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/16/20.
 */
public class DMatrixBaseTest extends StandardDMatrixTest {

    @Override
    protected DMatrix generateSequential(int n, int m) {
        DMatrix matrix = DMatrix.empty(MType.BASE, n, m);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix.set(i, j, i * m + j);
            }
        }
        return matrix;
    }

    @Override
    protected DMatrix generateFill(int n, int m, double fill) {
        DMatrix matrix = DMatrix.empty(MType.BASE, n, m);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix.set(i, j, fill);
            }
        }
        return matrix;
    }

    @Override
    protected DMatrix generateIdentity(int n) {
        DMatrix matrix = DMatrix.empty(MType.BASE, n, n);
        for (int i = 0; i < n; i++) {
            matrix.set(i, i, 1);
        }
        return matrix;
    }

    @Override
    protected DMatrix generateCopy(double[][] values) {
        return DMatrix.wrap(MType.BASE, true, values);
    }

    @Override
    protected String className() {
        return "DMatrixBase";
    }

    @Test
    void testBuilders() {
        DMatrix m = DMatrix.empty(MType.BASE, 10, 11);
        assertEquals(10, m.rowCount());
        assertEquals(11, m.colCount());

        for (int i = 0; i < m.rowCount(); i++) {
            for (int j = 0; j < m.colCount(); j++) {
                assertEquals(0, m.get(i, j));
            }
        }
    }
}
