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

import rapaio.math.linear.base.AbstractDMatrix;

/**
 * Base class for dense matrices. It offers implementation of all API
 * based only on primitive methods like {@link #get(int, int)}, {@link #set(int, int, double)},
 * {@link #inc(int, int, double)}, {@link #rowCount()} and {@link #colCount()}.
 * <p>
 * This class allows implementation of new types of dense matrices much easier since
 * one need only to implement primitive methods and storage. Optimized custom methods
 * can be implemented later in an incremental fashion.
 */
public abstract class DMatrixDense extends AbstractDMatrix {

    protected final int rowCount;
    protected final int colCount;
    protected final double[] values;

    protected DMatrixDense(int rowCount, int colCount, double[] values) {
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

    public double[] getElements() {
        return values;
    }

    @Override
    public DoubleStream valueStream() {
        return DoubleStream.of(values).limit((long) rowCount * colCount);
    }
}
