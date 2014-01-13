package rapaio.data.matrix;

import rapaio.data.NumVector;

import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class Matrix {

    int m; // rows
    int n; // cols
    List<NumVector> data;

    public Matrix(int m, int n) {
        this.m = m;
        this.n = n;
        data = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            data.add(new NumVector(m, m, 0));
        }
    }

    public Matrix(NumVector... vectors) {
        m = vectors[0].getRowCount();
        n = vectors.length;
        data = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            data.add(vectors[i]);
        }
    }

    public Matrix(double[][] source) {
        this(source, 0, source.length - 1, 0, source[0].length - 1);
    }


    public Matrix(double[][] source, int mFirst, int mLast, int nFirst, int nLast) {
        m = mLast - mFirst + 1;
        n = nLast - nFirst + 1;
        data = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            data.add(new NumVector(m, m, 0));
        }
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                set(i, j, source[i + mFirst][j + nFirst]);
            }
        }
    }

    public double get(int i, int j) {
        return data.get(j).getValue(i);
    }

    public void set(int i, int j, double x) {
        data.get(j).setValue(i, x);
    }

    public int getRowDimension() {
        return m;
    }

    public int getColumnDimension() {
        return n;
    }

    /**
     * Copy the internal two-dimensional array.
     *
     * @return Two-dimensional array copy of rapaio.data.matrix elements.
     */
    public double[][] getArrayCopy() {
        double[][] C = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                C[i][j] = get(i, j);
            }
        }
        return C;
    }

    public void print(NumberFormat format, int width) {
        print(new PrintWriter(System.out, true), format, width);
    }

    // DecimalFormat is a little disappointing coming from Fortran or C's printf.
    // Since it doesn't pad on the left, the elements will come out different
    // widths.  Consequently, we'll pass the desired column width in as an
    // argument and do the extra padding ourselves.

    /**
     * Print the matrix to the output stream.  Line the elements up in columns.
     * Use the format object, and right justify within columns of width
     * characters.
     * Note that is the matrix is to be read back in, you probably will want
     * to use a NumberFormat that is set to US Locale.
     *
     * @param output the output stream.
     * @param format A formatting object to format the matrix elements
     * @param width  Column width.
     * @see java.text.DecimalFormat#setDecimalFormatSymbols
     */

    public void print(PrintWriter output, NumberFormat format, int width) {
        output.println();  // start on new line.
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                String s = format.format(data.get(j).getValue(i)); // format the number
                int padding = Math.max(1, width - s.length()); // At _least_ 1 space
                for (int k = 0; k < padding; k++)
                    output.print(' ');
                output.print(s);
            }
            output.println();
        }
        output.println();   // end with blank line.
    }

    /**
     * Linear algebraic matrix multiplication, A * B
     *
     * @param B another matrix
     * @return Matrix product, A * B
     * @throws IllegalArgumentException Matrix inner dimensions must agree.
     */

    public Matrix times(Matrix B) {
        if (B.m != n) {
            throw new IllegalArgumentException("Matrix inner dimensions must agree.");
        }
        Matrix C = new Matrix(m, B.n);
        double[] Bcolj = new double[n];
        for (int j = 0; j < B.n; j++) {
            for (int k = 0; k < n; k++) {
                Bcolj[k] = B.get(k, j);
            }
            for (int i = 0; i < m; i++) {
//                double[] Arowi = A[i];
                double s = 0;
                for (int k = 0; k < n; k++) {
                    s += get(i, k) * Bcolj[k];
                }
                C.set(i, j, s);
            }
        }
        return C;
    }

}
