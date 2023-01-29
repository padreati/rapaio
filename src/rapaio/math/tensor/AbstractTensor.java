/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.tensor;

import java.util.Arrays;
import java.util.stream.Collectors;

import rapaio.math.tensor.storage.Storage;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;

public abstract class AbstractTensor<N extends Number, S extends Storage<N>, T extends Tensor<N, S, T>> implements Tensor<N, S, T> {

    protected final Shape shape;
    protected final Type type;

    public AbstractTensor(Shape shape, Type type) {
        this.shape = shape;
        this.type = type;
    }

    @Override
    public final Shape shape() {
        return shape;
    }

    @Override
    public Type type() {
        return type;
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {

        final int MAX_COL_VALUES = 21;
        boolean maxColHit = false;
        int cols = 2 + shape.dim(-1);
        if (shape.dim(-1) > MAX_COL_VALUES) {
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

        int row = 0;
        if (maxRowHit) {
            for (; row < MAX_ROW_VALUES - 1; row++) {
                tt.textCenter(row, 0, rowStart(shape, row));
                tt.textLeft(row, cols - 1, rowEnd(shape, row));
                appendValues(printer, tt, row, cols, maxColHit);
            }
            for (int i = 0; i < cols; i++) {
                tt.textCenter(row, i, "...");
            }
        } else {
            for (; row < rows; row++) {
                tt.textCenter(row, 0, rowStart(shape, row));
                tt.textLeft(row, cols - 1, rowEnd(shape, row));
                appendValues(printer, tt, row, cols, maxColHit);
            }
        }

        return tt.getText(-1);
    }

    private String rowStart(Shape shape, int row) {
        int[] index = shape.index(Order.RowMajor, row * shape.dim(-1));
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
        int[] index = shape.index(Order.RowMajor, (row + 1) * shape.dim(-1) - 1);
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
        if (maxColHit) {
            for (int i = 0; i < cols - 2; i++) {
                double value = get(shape.index(Order.RowMajor, row * shape.dim(shape.rank() - 1) + i)).doubleValue();
                tt.floatString(row, i + 1, printer.getOptions().floatFormat().format(value));
            }
            tt.textCenter(row, cols - 2, "...");
        } else {
            for (int i = 0; i < cols - 2; i++) {
                double value = get(shape.index(Order.RowMajor, row * shape.dim(shape.rank() - 1) + i)).doubleValue();
                tt.floatString(row, i + 1, printer.getOptions().floatFormat().format(value));
            }
        }
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        int cols = 2 + shape.dim(shape.rank() - 1);
        int rows = shape().size() / shape().dim(shape.rank() - 1);

        TextTable tt = TextTable.empty(rows, cols, 0, 0);

        int row = 0;
        for (; row < rows; row++) {
            tt.textCenter(row, 0, rowStart(shape, row));
            tt.textLeft(row, cols - 1, rowEnd(shape, row));
            appendValues(printer, tt, row, cols, false);
        }

        return tt.getText(-1);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + " {rank:" + shape().rank() + ",type:" + type.name() + ",dims:"
                + Arrays.stream(shape().dims()).mapToObj(String::valueOf).collect(Collectors.joining(",", "[", "]"))
                + "}";
    }

    protected int pointer(int offset, int[] strides, int[] index) {
        int pointer = offset;
        for (int i = 0; i < index.length; i++) {
            pointer += index[i] * strides[i];
        }
        return pointer;
    }
}
