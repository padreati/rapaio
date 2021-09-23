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

import java.util.stream.DoubleStream;

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.MType;
import rapaio.math.linear.base.AbstractDMatrix;
import rapaio.util.function.Double2DoubleFunction;

public abstract class DMatrixDense extends AbstractDMatrix {

    protected final MType type;
    protected final int rowCount;
    protected final int colCount;
    protected final double[] values;

    protected DMatrixDense(MType type, int rowCount, int colCount, double[] values) {
        this.type = type;
        this.rowCount = rowCount;
        this.colCount = colCount;
        this.values = values;
    }

    @Override
    public MType innerType() {
        return type;
    }

    @Override
    public MType type() {
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

    public double[] getElements() {
        return values;
    }

    @Override
    public DMatrix apply(Double2DoubleFunction fun) {
        for (int i = 0; i < values.length; i++) {
            values[i] = fun.apply(values[i]);
        }
        return this;
    }

    @Override
    public DoubleStream valueStream() {
        return DoubleStream.of(values);
    }
}
