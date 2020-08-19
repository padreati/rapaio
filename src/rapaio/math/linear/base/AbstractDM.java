package rapaio.math.linear.base;

import rapaio.math.MTools;
import rapaio.math.linear.DM;
import rapaio.math.linear.DV;
import rapaio.math.linear.decomposition.MatrixMultiplication;
import rapaio.math.linear.decomposition.SVDecomposition;
import rapaio.math.linear.dense.DMMap;
import rapaio.math.linear.dense.DMStripe;
import rapaio.math.linear.dense.DVDense;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.TextTable;
import rapaio.printer.opt.POption;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/8/20.
 */
public abstract class AbstractDM implements DM {

    private static final long serialVersionUID = -8475836385935066885L;

    protected void checkMatrixSameSize(DM b) {
        if ((rowCount() != b.rowCount()) || (colCount() != b.colCount())) {
            throw new IllegalArgumentException("Matrices are not conform with this operation.");
        }
    }

    @Override
    public DV mapRow(final int row) {
        return mapRowCopy(row);
    }

    @Override
    public DV mapRowCopy(final int row) {
        DVDense v = DVDense.zeros(colCount());
        for (int j = 0; j < colCount(); j++) {
            v.set(j, get(row, j));
        }
        return v;
    }

    @Override
    public DM mapRows(final int... indexes) {
        return new DMMap(this, true, indexes);
    }

    @Override
    public DM mapRowsCopy(final int... rows) {
        DMStripe copy = rapaio.math.linear.dense.DMStripe.empty(rows.length, colCount());
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < colCount(); j++) {
                copy.set(i, j, get(rows[i], j));
            }
        }
        return copy;
    }

    @Override
    public DM rangeRows(final int start, final int end) {
        int[] rows = new int[end - start];
        for (int i = start; i < end; i++) {
            rows[i - start] = i;
        }
        return new DMMap(this, true, rows);
    }

    @Override
    public DM rangeRowsCopy(int start, int end) {
        DMStripe copy = rapaio.math.linear.dense.DMStripe.empty(end - start, colCount());
        for (int i = start; i < end; i++) {
            for (int j = 0; j < colCount(); j++) {
                copy.set(i - start, j, get(i, j));
            }
        }
        return copy;
    }

    @Override
    public DM removeRows(int... indexes) {
        Set<Integer> rem = Arrays.stream(indexes).boxed()
                .filter(v -> v >= 0)
                .filter(v -> v < rowCount())
                .collect(Collectors.toSet());
        int[] rows = new int[rowCount() - rem.size()];
        int pos = 0;
        for (int i = 0; i < rowCount(); i++) {
            if (rem.contains(i)) {
                continue;
            }
            rows[pos++] = i;
        }
        return new DMMap(this, true, rows);
    }

    @Override
    public DM removeRowsCopy(int... indexes) {
        Set<Integer> rem = Arrays.stream(indexes).boxed()
                .filter(v -> v >= 0)
                .filter(v -> v < rowCount())
                .collect(Collectors.toSet());
        int[] rows = new int[rowCount() - rem.size()];
        int pos = 0;
        for (int i = 0; i < rowCount(); i++) {
            if (rem.contains(i)) {
                continue;
            }
            rows[pos++] = i;
        }
        return mapRowsCopy(rows);
    }

    @Override
    public DV mapCol(int col) {
        return mapColCopy(col);
    }

    @Override
    public DV mapColCopy(int col) {
        DVDense v = DVDense.zeros(rowCount());
        for (int j = 0; j < rowCount(); j++) {
            v.set(j, get(j, col));
        }
        return v;
    }

    @Override
    public DM mapCols(int... indexes) {
        return new DMMap(this, false, indexes);
    }

    @Override
    public DM mapColsCopy(int... cols) {
        DMStripe copy = rapaio.math.linear.dense.DMStripe.empty(rowCount(), cols.length);
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < cols.length; j++) {
                copy.set(i, j, get(i, cols[j]));
            }
        }
        return copy;
    }

    @Override
    public DM rangeCols(int start, int end) {
        int[] cols = new int[end - start];
        for (int i = start; i < end; i++) {
            cols[i - start] = i;
        }
        return new DMMap(this, false, cols);
    }

    @Override
    public DM rangeColsCopy(int start, int end) {
        int[] cols = new int[end - start];
        for (int i = start; i < end; i++) {
            cols[i - start] = i;
        }
        return mapColsCopy(cols);
    }

    @Override
    public DM removeCols(int... indexes) {
        Set<Integer> rem = Arrays.stream(indexes).boxed().collect(Collectors.toSet());
        int[] cols = new int[colCount() - rem.size()];
        int pos = 0;
        for (int i = 0; i < colCount(); i++) {
            if (rem.contains(i))
                continue;
            cols[pos++] = i;
        }
        return new DMMap(this, false, cols);
    }

    @Override
    public DM removeColsCopy(int... indexes) {
        Set<Integer> rem = Arrays.stream(indexes).boxed().collect(Collectors.toSet());
        int[] cols = new int[colCount() - rem.size()];
        int pos = 0;
        for (int i = 0; i < colCount(); i++) {
            if (rem.contains(i))
                continue;
            cols[pos++] = i;
        }
        return mapColsCopy(cols);
    }

    @Override
    public DM plus(double x) {
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) + x);
            }
        }
        return this;
    }

    @Override
    public DM plus(DM b) {
        checkMatrixSameSize(b);
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) + b.get(i, j));
            }
        }
        return this;
    }

    @Override
    public DM minus(double x) {
        return plus(-x);
    }

    @Override
    public DM minus(DM b) {
        checkMatrixSameSize(b);
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) - b.get(i, j));
            }
        }
        return this;
    }

    @Override
    public DM times(double x) {
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) * x);
            }
        }
        return this;
    }

    @Override
    public DM times(DM b) {
        checkMatrixSameSize(b);
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) * b.get(i, j));
            }
        }
        return this;
    }

    @Override
    public DM div(double x) {
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) / x);
            }
        }
        return this;
    }

    @Override
    public DM div(DM b) {
        checkMatrixSameSize(b);
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) / b.get(i, j));
            }
        }
        return this;
    }

    @Override
    public DM dot(DM B) {
        return MatrixMultiplication.ikjParallel(this, B);
    }

    @Override
    public DV dot(DV b) {
        return MatrixMultiplication.ikjParallel(this, b);
    }

    /**
     * Trace of the matrix, if the matrix is square. The trace of a squared
     * matrix is the sum of the elements from the main diagonal.
     * Otherwise returns an exception.
     *
     * @return value of the matrix trace
     */
    @Override
    public double trace() {
        if (rowCount() != colCount()) {
            throw new IllegalArgumentException("Matrix is not squared, trace of the matrix is not defined.");
        }
        double sum = 0;
        for (int i = 0; i < rowCount(); i++) {
            sum += get(i, i);
        }
        return sum;
    }

    /**
     * Matrix rank
     *
     * @return effective numerical rank, obtained from SVD.
     */
    @Override
    public int rank() {
        return SVDecomposition.from(this).rank();
    }

    /**
     * Diagonal vector of values
     */
    @Override
    public DV diag() {
        DV DV = DVDense.zeros(rowCount());
        for (int i = 0; i < rowCount(); i++) {
            DV.set(i, get(i, i));
        }
        return DV;
    }

    @Override
    public DM scatter() {
        DM scatter = rapaio.math.linear.dense.DMStripe.empty(colCount(), colCount());
        double[] mean = new double[colCount()];
        for (int i = 0; i < colCount(); i++) {
            mean[i] = mapCol(i).mean();
        }
        for (int k = 0; k < rowCount(); k++) {
            double[] row = new double[colCount()];
            for (int i = 0; i < colCount(); i++)
                row[i] = get(k, i) - mean[i];
            for (int i = 0; i < row.length; i++) {
                for (int j = 0; j < row.length; j++) {
                    scatter.set(i, j, scatter.get(i, j) + row[i] * row[j]);
                }
            }
        }
        return scatter;
    }

    @Override
    public DV rowMaxValues() {
        DVDense max = DVDense.copy(mapCol(0));
        for (int i = 1; i < colCount(); i++) {
            for (int j = 0; j < rowCount(); j++) {
                if (max.get(j) < get(j, i)) {
                    max.set(j, get(j, i));
                }
            }
        }
        return max;
    }

    @Override
    public int[] rowMaxIndexes() {
        int[] max = new int[rowCount()];
        for (int i = 1; i < colCount(); i++) {
            for (int j = 0; j < rowCount(); j++) {
                if (get(j, max[j]) < get(j, i)) {
                    max[j] = i;
                }
            }
        }
        return max;
    }

    @Override
    public boolean deepEquals(DM m, double eps) {
        if (rowCount() != m.rowCount()) {
            return false;
        }
        if (colCount() != m.colCount()) {
            return false;
        }
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                if (!MTools.eq(get(i, j), m.get(i, j), eps)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getClass().getSimpleName()).append("{");
        sb.append("rowCount:").append(rowCount()).append(", colCount:").append(colCount()).append(", values:");
        sb.append("[");
        for (int i = 0; i < Math.min(10, rowCount()); i++) {
            sb.append("[");
            for (int j = 0; j < Math.min(10, colCount()); j++) {
                sb.append(Format.floatFlexLong(get(i, j)));
                if (j != colCount() - 1) {
                    sb.append(",");
                }
            }
            if (colCount() > 10) {
                sb.append("...");
            }
            sb.append("]");
            if (i != rowCount() - 1) {
                sb.append(",");
            }
        }
        if (rowCount() > 10) {
            sb.append("...");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        return toContent(printer, options);
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        int headRows = 20;
        int headCols = 20;
        int tailRows = 2;
        int tailCols = 2;

        boolean fullRows = headRows + tailRows >= rowCount();
        boolean fullCols = headCols + tailCols >= colCount();

        if (fullRows && fullCols) {
            return toFullContent(printer, options);
        }

        int[] rows = new int[Math.min(headRows + tailRows + 1, rowCount())];

        if (fullRows) {
            for (int i = 0; i < rowCount(); i++) {
                rows[i] = i;
            }
        } else {
            for (int i = 0; i < headRows; i++) {
                rows[i] = i;
            }
            rows[headRows] = -1;
            for (int i = 0; i < tailRows; i++) {
                rows[i + headRows + 1] = i + rowCount() - tailRows;
            }
        }
        int[] cols = new int[Math.min(headCols + tailCols + 1, colCount())];
        if (fullCols) {
            for (int i = 0; i < colCount(); i++) {
                cols[i] = i;
            }
        } else {
            for (int i = 0; i < headCols; i++) {
                cols[i] = i;
            }
            cols[headCols] = -1;
            for (int i = 0; i < tailCols; i++) {
                cols[i + headCols + 1] = i + colCount() - tailCols;
            }
        }
        TextTable tt = TextTable.empty(rows.length + 1, cols.length + 1, 1, 1);
        for (int i = 0; i < rows.length; i++) {
            if (rows[i] == -1) {
                tt.textCenter(i + 1, 0, "...");
            } else {
                tt.intRow(i + 1, 0, rows[i]);
            }
        }
        for (int i = 0; i < cols.length; i++) {
            if (cols[i] == -1) {
                tt.textCenter(0, i + 1, "...");
            } else {
                tt.intRow(0, i + 1, cols[i]);
            }
        }
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < cols.length; j++) {
                if (rows[i] == -1 || cols[j] == -1) {
                    tt.textCenter(i + 1, j + 1, "...");
                } else {
                    tt.floatFlexLong(i + 1, j + 1, get(rows[i], cols[j]));
                }
            }
        }
        return tt.getDynamicText(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {

        TextTable tt = TextTable.empty(rowCount() + 1, colCount() + 1, 1, 1);
        for (int i = 0; i < rowCount(); i++) {
            tt.intRow(i + 1, 0, i);
        }
        for (int i = 0; i < colCount(); i++) {
            tt.intRow(0, i + 1, i);
        }
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                tt.floatFlexLong(i + 1, j + 1, get(i, j));
            }
        }
        return tt.getDynamicText(printer, options);
    }
}
