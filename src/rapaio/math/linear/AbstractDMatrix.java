package rapaio.math.linear;

import rapaio.math.MTools;
import rapaio.math.linear.dense.MappedDMatrix;
import rapaio.math.linear.dense.MatrixMultiplication;
import rapaio.math.linear.dense.SVDecomposition;
import rapaio.math.linear.dense.SolidDMatrix;
import rapaio.math.linear.dense.SolidDVector;
import rapaio.printer.format.Format;
import rapaio.sys.WS;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/8/20.
 */
public abstract class AbstractDMatrix implements DMatrix {

    private static final long serialVersionUID = -8475836385935066885L;

    protected void checkMatrixSameSize(DMatrix b) {
        if (rowCount() != b.rowCount()) {
            throw new IllegalArgumentException("Matrix rows count are not the same.");
        }
        if (colCount() != b.colCount()) {
            throw new IllegalArgumentException("Matrix column count are not the same.");
        }
    }

    @Override
    public DVector mapRow(final int row) {
        return mapRowCopy(row);
    }

    @Override
    public DVector mapRowCopy(final int row) {
        SolidDVector v = SolidDVector.zeros(colCount());
        for (int j = 0; j < colCount(); j++) {
            v.set(j, get(row, j));
        }
        return v;
    }

    @Override
    public DMatrix mapRows(final int... indexes) {
        return new MappedDMatrix(this, true, indexes);
    }

    @Override
    public DMatrix mapRowsCopy(final int... rows) {
        SolidDMatrix copy = SolidDMatrix.empty(rows.length, colCount());
        for (int i = 0; i < rows.length; i++) {
            for (int j = 0; j < colCount(); j++) {
                copy.set(i, j, get(rows[i], j));
            }
        }
        return copy;
    }

    @Override
    public DMatrix rangeRows(final int start, final int end) {
        int[] rows = new int[end - start];
        for (int i = start; i < end; i++) {
            rows[i - start] = i;
        }
        return new MappedDMatrix(this, true, rows);
    }

    @Override
    public DMatrix rangeRowsCopy(int start, int end) {
        SolidDMatrix copy = SolidDMatrix.empty(end - start, colCount());
        for (int i = start; i < end; i++) {
            for (int j = 0; j < colCount(); j++) {
                copy.set(i - start, j, get(i, j));
            }
        }
        return copy;
    }

    @Override
    public DMatrix removeRows(int... indexes) {
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
        return new MappedDMatrix(this, true, rows);
    }

    @Override
    public DMatrix removeRowsCopy(int... indexes) {
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
    public DVector mapCol(int col) {
        return mapColCopy(col);
    }

    @Override
    public DVector mapColCopy(int col) {
        SolidDVector v = SolidDVector.zeros(rowCount());
        for (int j = 0; j < rowCount(); j++) {
            v.set(j, get(j, col));
        }
        return v;
    }

    @Override
    public DMatrix mapCols(int... indexes) {
        return new MappedDMatrix(this, false, indexes);
    }

    @Override
    public DMatrix mapColsCopy(int... cols) {
        SolidDMatrix copy = SolidDMatrix.empty(rowCount(), cols.length);
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < cols.length; j++) {
                copy.set(i, j, get(i, cols[j]));
            }
        }
        return copy;
    }

    @Override
    public DMatrix rangeCols(int start, int end) {
        int[] cols = new int[end - start];
        for (int i = start; i < end; i++) {
            cols[i - start] = i;
        }
        return new MappedDMatrix(this, false, cols);
    }

    @Override
    public DMatrix rangeColsCopy(int start, int end) {
        int[] cols = new int[end - start];
        for (int i = start; i < end; i++) {
            cols[i - start] = i;
        }
        return mapColsCopy(cols);
    }

    @Override
    public DMatrix removeCols(int... indexes) {
        Set<Integer> rem = Arrays.stream(indexes).boxed().collect(Collectors.toSet());
        int[] cols = new int[colCount() - rem.size()];
        int pos = 0;
        for (int i = 0; i < colCount(); i++) {
            if (rem.contains(i))
                continue;
            cols[pos++] = i;
        }
        return new MappedDMatrix(this, false, cols);
    }

    @Override
    public DMatrix removeColsCopy(int... indexes) {
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
    public DMatrix plus(double x) {
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) + x);
            }
        }
        return this;
    }

    @Override
    public DMatrix plus(DMatrix b) {
        checkMatrixSameSize(b);
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) + b.get(i, j));
            }
        }
        return this;
    }

    @Override
    public DMatrix minus(double x) {
        return plus(-x);
    }

    @Override
    public DMatrix minus(DMatrix b) {
        checkMatrixSameSize(b);
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) - b.get(i, j));
            }
        }
        return this;
    }

    @Override
    public DMatrix times(double x) {
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) * x);
            }
        }
        return this;
    }

    @Override
    public DMatrix times(DMatrix b) {
        checkMatrixSameSize(b);
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) * b.get(i, j));
            }
        }
        return this;
    }

    @Override
    public DMatrix div(double x) {
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) / x);
            }
        }
        return this;
    }

    @Override
    public DMatrix div(DMatrix b) {
        checkMatrixSameSize(b);
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) / b.get(i, j));
            }
        }
        return this;
    }

    @Override
    public DMatrix dot(DMatrix B) {
        return MatrixMultiplication.ikjParallel(this, B);
    }

    @Override
    public DVector dot(DVector b) {
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
    public DVector diag() {
        DVector DVector = SolidDVector.zeros(rowCount());
        for (int i = 0; i < rowCount(); i++) {
            DVector.set(i, get(i, i));
        }
        return DVector;
    }

    @Override
    public DMatrix scatter() {
        DMatrix scatter = SolidDMatrix.empty(colCount(), colCount());
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

    ///////////////////////
    // other tools
    ///////////////////////

    @Override
    public DVector rowValueMax() {
        SolidDVector max = SolidDVector.copy(mapCol(0));
        for (int i = 1; i < colCount(); i++) {
            for (int j = 0; j < rowCount(); j++) {
                if (max.get(j) < get(j, i)) {
                    max.set(j, get(j, i));
                }
            }
        }
        return max;
    }

    /**
     * Does not override equals since this is a costly
     * algorithm and can slow down processing as a side effect.
     *
     * @param DMatrix given matrix
     * @return true if dimension and elements are equal
     */
    @Override
    public boolean isEqual(DMatrix DMatrix) {
        return isEqual(DMatrix, 1e-20);
    }

    @Override
    public boolean isEqual(DMatrix DMatrix, double tol) {
        if (rowCount() != DMatrix.rowCount())
            return false;
        if (colCount() != DMatrix.colCount())
            return false;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                if (!MTools.eq(get(i, j), DMatrix.get(i, j), tol))
                    return false;
            }
        }
        return true;
    }

    @Override
    public String toSummary() {

        StringBuilder sb = new StringBuilder();

        String[][] m = new String[rowCount()][colCount()];
        int max = 1;
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                m[i][j] = Format.floatShort(get(i, j));
                max = Math.max(max, m[i][j].length() + 1);
            }
        }
        max = Math.max(max, String.format("[,%d]", rowCount()).length());
        max = Math.max(max, String.format("[%d,]", colCount()).length());

        int hCount = (int) Math.floor(WS.getPrinter().textWidth() / (double) max);
        int vCount = Math.min(rowCount() + 1, 101);
        int hLast = 0;
        while (hLast < colCount()) {

            // take vertical stripes

            int hStart = hLast;
            int hEnd = Math.min(hLast + hCount, colCount());
            int vLast = 0;

            while (vLast < rowCount()) {

                // print rows

                int vStart = vLast;
                int vEnd = Math.min(vLast + vCount, rowCount());

                for (int i = vStart; i <= vEnd; i++) {
                    for (int j = hStart; j <= hEnd; j++) {
                        if (i == vStart && j == hStart) {
                            sb.append(String.format("%" + (max) + "s| ", ""));
                            continue;
                        }
                        if (i == vStart) {
                            sb.append(String.format("%" + Math.max(1, max - 1) + "d|", j - 1));
                            continue;
                        }
                        if (j == hStart) {
                            sb.append(String.format("%" + Math.max(1, max - 1) + "d |", i - 1));
                            continue;
                        }
                        sb.append(String.format("%" + max + "s", m[i - 1][j - 1]));
                    }
                    sb.append("\n");
                }
                vLast = vEnd;
            }
            hLast = hEnd;
        }
        return sb.toString();
    }
}
