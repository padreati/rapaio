/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.math.linear.impl;

import rapaio.data.Var;
import rapaio.math.linear.RV;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/6/15.
 */
@Deprecated
public class SolidRV implements RV {

    final int rows;
    final int cols;
    final double[] data;

    public SolidRV(int rows) {
        this.rows = rows;
        this.cols = 1;
        this.data = new double[rows];
    }

    public SolidRV(Var var) {
        this.rows = var.rowCount();
        this.cols = 1;
        this.data = new double[var.rowCount()];
        for (int i = 0; i < data.length; i++) {
            data[i] = var.value(i);
        }
    }

    @Override
    public int rowCount() {
        return rows;
    }

    @Override
    public int colCount() {
        return cols;
    }

    @Override
    public double get(int i, int j) {
        if (rowCount() == 1 && i == 0)
            return data[j];
        if (colCount() == 1 && j == 0)
            return data[i];
        throw new IllegalArgumentException("This shortcut method can be called only for vectors or special matrices");
    }

    @Override
    public void set(int i, int j, double value) {
        if (rows == 1 && i == 0) {
            data[j] = value;
            return;
        }
        if (cols == 1 && j == 0) {
            data[i] = value;
            return;
        }
        throw new IllegalArgumentException("This shortcut method can be called only for vectors or special matrices");
    }

    @Override
    public RV t() {
        return new TransposeRV(this);
    }
}
