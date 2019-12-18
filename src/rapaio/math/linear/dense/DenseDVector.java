package rapaio.math.linear.dense;

import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.printer.format.Format;
import rapaio.printer.format.TextTable;
import rapaio.util.collection.DoubleArrays;

import java.util.stream.DoubleStream;

/**
 * Implements a dense double vector of values.
 * The vector is always considered a columnar matrix
 *
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 12/3/19.
 */
public class DenseDVector implements DVector {

    public static DenseDVector ones(int rows) {
        return new DenseDVector(rows, DoubleArrays.newFill(rows, 1d));
    }

    public static DenseDVector zeros(int rows) {
        return new DenseDVector(rows, DoubleArrays.newFill(rows, 0d));
    }

    public static DenseDVector wrap(double... values) {
        return new DenseDVector(values.length, values);
    }

    public static DenseDVector wrap(VarDouble var) {
        return new DenseDVector(var.rowCount(), var.elements());
    }

    private final int size;
    private final double[] values;

    private DenseDVector(int size, double[] values) {
        this.size = size;
        this.values = values;
    }

    @Override
    public int ndim() {
        return 1;
    }

    @Override
    public int[] shape() {
        return new int[]{size};
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public double get(int... index) {
        return values[index[0]];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DenseDVector{size=").append(size).append(", values=");
        sb.append(String.join(",", DoubleStream.of(values).limit(Math.max(12, size)).mapToObj(Format::floatFlex).toArray(String[]::new)));
        sb.append(size >= 12 ? ",...}" : '}');
        return sb.toString();
    }

    @Override
    public String toContent() {
        if (size > 100) {
            TextTable tt = TextTable.empty(102, 2, 1, 1);
            tt.textCenter(0, 0, "row");
            tt.textCenter(0, 1, "value");
            for (int i = 0; i < 80; i++) {
                tt.intRow(i + 1, 0, i);
                tt.floatFlex(i + 1, 1, values[i]);
            }
            tt.textCenter(80, 0, "...");
            tt.textCenter(80, 1, "...");
            for (int i = size - 20; i < size; i++) {
                tt.intRow(i - size + 20 + 81, 0, i);
                tt.floatFlex(i - size + 20 + 81, 1, values[i]);
            }
            return tt.getDynamicText();
        }
        return toFullContent();
    }

    @Override
    public String toFullContent() {
        TextTable tt = TextTable.empty(size + 1, 2, 1, 1);
        tt.textCenter(0, 0, "row");
        tt.textCenter(0, 1, "value");
        for (int i = 0; i < size; i++) {
            tt.intRow(i + 1, 0, i);
            tt.floatFlex(i + 1, 1, values[i]);
        }
        return tt.getDynamicText();
    }

}
