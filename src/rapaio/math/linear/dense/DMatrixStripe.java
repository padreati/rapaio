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

package rapaio.math.linear.dense;

import java.io.Serial;
import java.util.Arrays;
import java.util.stream.DoubleStream;

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.MType;
import rapaio.math.linear.base.AbstractDMatrix;
import rapaio.math.linear.option.AlgebraOption;
import rapaio.math.linear.option.AlgebraOptions;
import rapaio.util.function.Double2DoubleFunction;

/**
 * A stripe matrix is a matrix which stores the value as arrays of arrays.
 * Depending on the storage order, it can store arrays of rows or arrays of columns.
 * <p>
 * This memory layout is also known as Liffe lists and it is the standard way of
 * working with bi-dimensional arrays in java.
 * <p>
 * In general, when a stripe matrix is created it will be present a storing order
 * parameter of the type {@code SOrder}. If this parameter is not present, it will
 * be assumed {@code SOrder.R} value or storage in row major order (arrays of rows).
 * This is to meet the default usage of a {@code double[][]} array.
 * <p>
 * Depending on the operation which will use the matrix, the user will chose the
 * appropriate type of matrix storage. Whenever possible, vector and matrix operations
 * will be implemented using parallelization and various cache friendly co-locations.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/2/21.
 */
public abstract class DMatrixStripe extends AbstractDMatrix {

    @Serial
    private static final long serialVersionUID = -1798941400862688438L;

    protected final MType type;
    protected final int rowCount;
    protected final int colCount;
    protected final double[][] values;

    protected DMatrixStripe(MType type, int rowCount, int colCount, double[][] values) {
        this.type = type;
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.values = values;
    }

    @Override
    public MType type() {
        return type;
    }

    @Override
    public MType innerType() {
        return type;
    }

    @Override
    public int rowCount() {
        return rowCount;
    }

    @Override
    public int colCount() {
        return colCount;
    }

    public double[][] getElements() {
        return values;
    }

    @Override
    public DMatrix apply(Double2DoubleFunction fun, AlgebraOption<?>... opts) {
        double[][] ref = values;
        if (AlgebraOptions.from(opts).isCopy()) {
            ref = new double[values.length][];
            for (int i = 0; i < ref.length; i++) {
                ref[i] = new double[values[i].length];
            }
        }
        for (int i = 0; i < ref.length; i++) {
            for (int j = 0; j < ref[i].length; j++) {
                ref[i][j] = fun.apply(values[i][j]);
            }
        }
        return type == MType.RSTRIPE ?
                new DMatrixStripeR(rowCount, colCount, ref) :
                new DMatrixStripeC(rowCount, colCount, ref);
    }

    @Override
    public DoubleStream valueStream() {
        return Arrays.stream(values).flatMapToDouble(Arrays::stream);
    }

}
