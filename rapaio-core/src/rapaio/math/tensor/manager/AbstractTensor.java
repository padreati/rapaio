/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.math.tensor.manager;

import rapaio.data.VarDouble;
import rapaio.math.tensor.Order;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Storage;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.manager.barray.BaseDoubleTensorStride;
import rapaio.math.tensor.manager.varray.VectorizedDoubleTensorStride;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POpt;

public abstract class AbstractTensor<N extends Number> implements Tensor<N> {

    protected final Storage<N> storage;

    public AbstractTensor(Storage<N> storage) {
        this.storage = storage;
    }

    @Override
    public Storage<N> storage() {
        return storage;
    }

    @Override
    public byte getByte(int... indexes) {
        return storage.getByte(layout().pointer(indexes));
    }

    @Override
    public int getInt(int... indexes) {
        return storage.getInt(layout().pointer(indexes));
    }

    @Override
    public float getFloat(int... indexes) {
        return storage.getFloat(layout().pointer(indexes));
    }

    @Override
    public double getDouble(int... indexes) {
        return storage.getDouble(layout().pointer(indexes));
    }

    @Override
    public void setByte(byte value, int... indexes) {
        storage.setByte(layout().pointer(indexes), value);
    }

    @Override
    public void setInt(int value, int... indexes) {
        storage.setInt(layout().pointer(indexes), value);
    }

    @Override
    public void setFloat(float value, int... indexes) {
        storage.setFloat(layout().pointer(indexes), value);
    }

    @Override
    public void setDouble(double value, int... indexes) {
        storage.setDouble(layout().pointer(indexes), value);
    }

    @Override
    public void incByte(byte value, int... indexes) {
        storage.incByte(layout().pointer(indexes), value);
    }

    @Override
    public void incInt(int value, int... indexes) {
        storage.incInt(layout().pointer(indexes), value);
    }

    @Override
    public void incFloat(float value, int... indexes) {
        storage.incFloat(layout().pointer(indexes), value);
    }

    @Override
    public void incDouble(double value, int... indexes) {
        storage.incDouble(layout().pointer(indexes), value);
    }

    @Override
    public byte ptrGetByte(int ptr) {
        return storage.getByte(ptr);
    }

    @Override
    public int ptrGetInt(int ptr) {
        return storage.getInt(ptr);
    }

    @Override
    public float ptrGetFloat(int ptr) {
        return storage.getFloat(ptr);
    }

    @Override
    public double ptrGetDouble(int ptr) {
        return storage.getDouble(ptr);
    }

    @Override
    public void ptrSetByte(int ptr, byte value) {
        storage.setByte(ptr, value);
    }

    @Override
    public void ptrSetInt(int ptr, int value) {
        storage.setInt(ptr, value);
    }

    @Override
    public void ptrSetFloat(int ptr, float value) {
        storage.setFloat(ptr, value);
    }

    @Override
    public void ptrSetDouble(int ptr, double value) {
        storage.setDouble(ptr, value);
    }


    @Override
    public VarDouble dv() {
        if (layout().rank() != 1) {
            throw new IllegalArgumentException("Only one dimensional tensors can be converted to VarDouble.");
        }
        if (this instanceof BaseDoubleTensorStride bs) {
            if (bs.layout().offset() == 0 && bs.layout().stride(0) == 1) {
                return VarDouble.wrap(bs.asArray());
            }
        }
        if (this instanceof VectorizedDoubleTensorStride bs) {
            if (bs.layout().offset() == 0 && bs.layout().stride(0) == 1) {
                return VarDouble.wrap(bs.asArray());
            }
        }
        double[] copy = new double[layout().size()];
        var it = iterator(Order.C);
        for (int i = 0; i < copy.length; i++) {
            copy[i] = it.next().doubleValue();
        }
        return VarDouble.wrap(copy);
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {

        final int MAX_COL_VALUES = 21;
        boolean maxColHit = false;
        int cols = 2 + shape().dim(-1);
        if (shape().dim(-1) > MAX_COL_VALUES) {
            maxColHit = true;
            cols = 2 + MAX_COL_VALUES;
        }

        final int MAX_ROW_VALUES = 41;
        boolean maxRowHit = false;
        int rows = shape().size() / shape().dim(-1);
        if (shape().size() / shape().dim(-1) > MAX_ROW_VALUES) {
            maxRowHit = true;
            rows = MAX_ROW_VALUES;
        }

        TextTable tt = TextTable.empty(rows, cols, 0, 0);

        var p = printer.withOptions(options);
        int row = 0;
        if (maxRowHit) {
            for (; row < MAX_ROW_VALUES - 1; row++) {
                tt.textCenter(row, 0, rowStart(shape(), row));
                tt.textLeft(row, cols - 1, rowEnd(shape(), row));
                appendValues(p, tt, row, cols, maxColHit);
            }
            for (int i = 0; i < cols; i++) {
                tt.textCenter(row, i, "...");
            }
        } else {
            for (; row < rows; row++) {
                tt.textCenter(row, 0, rowStart(shape(), row));
                tt.textLeft(row, cols - 1, rowEnd(shape(), row));
                appendValues(p, tt, row, cols, maxColHit);
            }
        }

        return tt.getText(-1);
    }

    private String rowStart(Shape shape, int row) {
        int[] index = shape.index(Order.C, row * shape.dim(-1));
        StringBuilder sb = new StringBuilder();
        for (int c = shape.rank() - 1; c >= 0; c--) {
            if (index[c] == 0) {
                sb.append("[");
            } else {
                break;
            }
        }
        while (sb.length() < shape.rank()) {
            sb.insert(0, " ");
        }
        return sb.toString();
    }

    private String rowEnd(Shape shape, int row) {
        int[] index = shape.index(Order.C, (row + 1) * shape.dim(-1) - 1);
        StringBuilder sb = new StringBuilder();
        for (int c = shape.rank() - 1; c >= 0; c--) {
            if (index[c] == shape.dim(c) - 1) {
                sb.append("]");
            } else {
                break;
            }
        }
        return sb.toString();
    }

    private void appendValues(Printer printer, TextTable tt, int row, int cols, boolean maxColHit) {
        for (int i = 0; i < cols - 2; i++) {
            double value = get(shape().index(Order.C, row * shape().dim(-1) + i)).doubleValue();
            tt.floatString(row, i + 1, printer.getOptions().getFloatFormat().format(value));
        }
        if (maxColHit) {
            tt.textCenter(row, cols - 2, "...");
        }
    }

    @Override
    public String toFullContent(Printer printer, POpt<?>... options) {
        int cols = 2 + shape().dim(-1);
        int rows = shape().size() / shape().dim(-1);

        TextTable tt = TextTable.empty(rows, cols, 0, 0);

        int row = 0;
        for (; row < rows; row++) {
            tt.textCenter(row, 0, rowStart(shape(), row));
            tt.textLeft(row, cols - 1, rowEnd(shape(), row));
            appendValues(printer, tt, row, cols, false);
        }

        return tt.getText(-1);
    }

    @Override
    public String toString() {
        return STR."\{this.getClass().getSimpleName()} {\{layout().toString()}}";
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        return toString();
    }
}
