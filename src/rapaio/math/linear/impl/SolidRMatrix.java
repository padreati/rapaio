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
 */

package rapaio.math.linear.impl;

import rapaio.math.linear.RMatrix;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/3/15.
 */
public class SolidRMatrix implements RMatrix {

    private final int rowCount;
    private final int colCount;
    private final double[] values;

    public SolidRMatrix(int rowCount, int colCount) {
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.values = new double[rowCount * colCount];
    }

    public SolidRMatrix(int rowCount, int colCount, double[] values) {
        if (rowCount * colCount != values.length) {
            throw new IllegalArgumentException("rows*cols does not match the number of given values");
        }
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.values = values;
    }

    @Override
    public int rowCount() {
        return rowCount;
    }

    @Override
    public int colCount() {
        return colCount;
    }

    @Override
    public double get(int i, int j) {
        return values[i * colCount + j];
    }

    @Override
    public void set(int i, int j, double value) {
        values[i * colCount + j] = value;
    }
}
