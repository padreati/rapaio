package rapaio.math.linear;

import rapaio.math.linear.dense.MappedDMatrix;
import rapaio.math.linear.dense.MatrixMultiplication;
import rapaio.math.linear.dense.SolidDVector;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/8/20.
 */
public abstract class AbstractDMatrix implements DMatrix {

    private static final long serialVersionUID = -8475836385935066885L;

    @Override
    public SolidDVector mapCol(int i) {
        SolidDVector v = SolidDVector.zeros(rowCount());
        for (int j = 0; j < rowCount(); j++) {
            v.set(j, get(j, i));
        }
        return v;
    }

    @Override
    public DVector mapRow(int i) {
        SolidDVector v = SolidDVector.zeros(colCount());
        for (int j = 0; j < colCount(); j++) {
            v.set(j, get(i, j));
        }
        return v;
    }

    @Override
    public DMatrix mapRows(int... indexes) {
        return new MappedDMatrix(this, true, indexes);
    }

    @Override
    public DMatrix rangeRows(int start, int end) {
        int[] rows = new int[end - start];
        for (int i = start; i < end; i++) {
            rows[i - start] = i;
        }
        return new MappedDMatrix(this, true, rows);
    }

    /**
     * Builds a new matrix having all columns and all the rows not specified by given indexes
     *
     * @param indexes rows which will be removed
     * @return new mapped matrix containing all rows not specified by indexes
     */
    @Override
    public DMatrix removeRows(int... indexes) {
        Set<Integer> rem = Arrays.stream(indexes).boxed().collect(Collectors.toSet());
        int[] rows = new int[rowCount() - rem.size()];
        int pos = 0;
        for (int i = 0; i < rowCount(); i++) {
            if (rem.contains(i))
                continue;
            rows[pos++] = i;
        }
        return new MappedDMatrix(this, true, rows);
    }

    @Override
    public DMatrix mapCols(int... indexes) {
        return new MappedDMatrix(this, false, indexes);
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
    public DMatrix plus(double x) {
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) + x);
            }
        }
        return this;
    }

    @Override
    public DMatrix plus(DMatrix B) {
        if ((rowCount() != B.rowCount()) || (colCount() != B.colCount()))
            throw new IllegalArgumentException(String.format(
                    "Matrices are not conform for addition: [%d x %d] + [%d x %d]", rowCount(), colCount(), B.rowCount(), B.colCount()));
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) + B.get(i, j));
            }
        }
        return this;
    }

    @Override
    public DMatrix minus(double x) {
        return plus(-x);
    }

    @Override
    public DMatrix minus(DMatrix B) {
        if ((rowCount() != B.rowCount()) || (colCount() != B.colCount()))
            throw new IllegalArgumentException(String.format(
                    "Matrices are not conform for substraction: [%d x %d] + [%d x %d]", rowCount(), colCount(), B.rowCount(), B.colCount()));
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) - B.get(i, j));
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

    @Override
    public DMatrix dot(double x) {
        for (int i = 0; i < rowCount(); i++) {
            for (int j = 0; j < colCount(); j++) {
                set(i, j, get(i, j) * x);
            }
        }
        return this;
    }

}
