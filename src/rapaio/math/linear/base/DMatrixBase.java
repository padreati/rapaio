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

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.MType;
import rapaio.util.function.Double2DoubleFunction;

import java.io.Serial;
import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * Basic implementation of a matrix which uses array of arrays. This implementation
 * uses only API interface for implementations. The purpose of this implementation is to
 * offer default implementation baselines for all specialization matrices.
 * <p>
 * On purpose, this implementation helps implementers in two purposes:
 * <ul>
 *     <li>offers a performance baseline for specific implementations</li>
 *     <li>offers a skeleton implementation to make performance improvement development possible
 *     in an incremental manner</li>
 * </ul>
 * <p>
 * This class is a reference implementation and it is not intended to be used in
 * performance critical operations.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/9/20.
 */
public class DMatrixBase extends AbstractDMatrix {

    @Serial
    private static final long serialVersionUID = -7586346894985345827L;

    protected final int rowCount;
    protected final int colCount;
    protected final double[][] values;

    public DMatrixBase(int rowCount, int colCount) {
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.values = new double[rowCount][colCount];
    }

    public DMatrixBase(double[][] values) {
        this.rowCount = values.length;
        this.colCount = (values.length == 0) ? 0 : values[0].length;
        this.values = values;
    }

    @Override
    public MType type() {
        return MType.BASE;
    }

    @Override
    public MType innerType() {
        return MType.BASE;
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
    public double get(int row, int col) {
        return values[row][col];
    }

    @Override
    public void set(int row, int col, double value) {
        values[row][col] = value;
    }

    @Override
    public void inc(int row, int col, double value) {
        values[row][col] += value;
    }

    @Override
    public DMatrix apply(Double2DoubleFunction fun) {
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                values[i][j] = fun.applyAsDouble(values[i][j]);
            }
        }
        return this;
    }

    @Override
    public DMatrix t() {
        DMatrixBase t = new DMatrixBase(colCount, rowCount);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                t.set(j, i, get(i, j));
            }
        }
        return t;
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(values).flatMapToDouble(Arrays::stream);
    }

    @Override
    public DMatrixBase copy() {
        DMatrixBase copy = new DMatrixBase(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            System.arraycopy(values[i], 0, copy.values[i], 0, values[i].length);
        }
        return copy;
    }
}
