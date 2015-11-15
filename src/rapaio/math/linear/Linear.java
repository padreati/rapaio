/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.math.linear;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.math.linear.impl.SolidRM;
import rapaio.math.linear.impl.SolidRV;

import java.util.Arrays;
import java.util.function.BiFunction;

/**
 * Linear algebra tool bag class.
 * Contains various utilities to create and manipulate linear algbra constructs like {@link RM} or {@link RV}
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/6/15.
 */
public final class Linear {

    /**
     * Builds a new 0 filled matrix with given rows and cols
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @return new matrix object
     */
    public static RM newRMEmpty(int rowCount, int colCount) {
        return new SolidRM(rowCount, colCount);
    }

    public static RM newRMWrapOf(int rowCount, int colCount, double... values) {
        return new SolidRM(rowCount, colCount, values);
    }

    public static RM newRMCopyOf(double[][] source) {
        return newRMCopyOf(source, 0, source.length, 0, source[0].length);
    }

    public static RM newRMCopyOf(double[][] source, int mFirst, int mLast, int nFirst, int nLast) {
        RM mm = new SolidRM(mLast - mFirst, nLast - nFirst);
        for (int i = mFirst; i < mLast; i++) {
            for (int j = nFirst; j < nLast; j++) {
                mm.set(i, j, source[i][j]);
            }
        }
        return mm;
    }

    public static RM newRMCopyOf(Frame df) {
        RM RM = new SolidRM(df.rowCount(), df.varCount());
        for (int i = 0; i < df.rowCount(); i++) {
            for (int j = 0; j < df.varCount(); j++) {
                RM.set(i, j, df.value(i, j));
            }
        }
        return RM;
    }

    public static RM newRMCopyOf(Var... vars) {
        int rowCount = Arrays.stream(vars).mapToInt(Var::rowCount).min().getAsInt();
        RM RM = new SolidRM(rowCount, vars.length);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < vars.length; j++) {
                RM.set(i, j, vars[j].value(i));
            }
        }
        return RM;
    }

    /**
     * Builds a new matrix with given rows and cols, fillen with given value
     *
     * @param rowCount number of rows
     * @param colCount number of columns
     * @param fill     initial value for all matrix cells
     * @return new matrix object
     */
    public static RM newRMFill(int rowCount, int colCount, double fill) {
        if (fill == 0) {
            return newRMEmpty(rowCount, colCount);
        }
        RM RM = new SolidRM(rowCount, colCount);
        for (int i = 0; i < RM.rowCount(); i++) {
            for (int j = 0; j < RM.colCount(); j++) {
                RM.set(i, j, fill);
            }
        }
        return RM;
    }

    public static RM newRMFill(int rowCount, int colCount, BiFunction<Integer, Integer, Double> f) {
        RM RM = new SolidRM(rowCount, colCount);
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < colCount; j++) {
                RM.set(i, j, f.apply(i, j));
            }
        }
        return RM;
    }

    public static RV newRVCopyOf(Var var) {
        return new SolidRV(var);
    }

    public static RV newRVCopyOf(double... values) {
        RV ret = new SolidRV(values.length);
        for (int i = 0; i < values.length; i++) {
            ret.set(i, values[i]);
        }
        return ret;
    }

    public static RV newRVEmpty(int rows) {
        return new SolidRV(rows);
    }

    public static RM newRMId(int n) {
        RM id = Linear.newRMEmpty(n, n);
        for (int i = 0; i < n; i++) {
            id.set(i, i, 1.0);
        }
        return id;
    }

    // tools

    public static RM chol2inv(RM R) {
        return chol2inv(R, Linear.newRMId(R.rowCount()));
    }

    public static RM chol2inv(RM R, RM B) {
        RM ref = R.t();
        if (B.rowCount() != R.rowCount()) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }

        // Copy right hand side.
        RM X = B.copy();

        // Solve L*Y = B;
        for (int k = 0; k < ref.rowCount(); k++) {
            for (int j = 0; j < X.colCount(); j++) {
                for (int i = 0; i < k; i++) {
                    X.increment(k, j, -X.get(i, j) * ref.get(k, i));
                }
                X.set(k, j, X.get(k, j) / ref.get(k, k));
            }
        }

        // Solve L'*X = Y;
        for (int k = ref.rowCount() - 1; k >= 0; k--) {
            for (int j = 0; j < X.colCount(); j++) {
                for (int i = k + 1; i < ref.rowCount(); i++) {
                    X.increment(k, j, -X.get(i, j) * ref.get(i, k));
                }
                X.set(k, j, X.get(k, j) / ref.get(k, k));
            }
        }
        return X;
    }

    public static EigenPair pdEigenDecomp(RM s, int maxRuns, double tol) {

        // runs QR decomposition algoritm for maximum of iterations
        // to provide a solution which has other than diagonals under
        // tolerance

        QR qr = s.qr();
        s = qr.getR().dot(qr.getQ());
        RM ev = qr.getQ();
        for (int i = 0; i < maxRuns - 1; i++) {
            qr = s.qr();
            s = qr.getR().dot(qr.getQ());
            ev = ev.dot(qr.getQ());
            if (inTolerance(s, tol))
                break;
        }
        return EigenPair.newFrom(s.diag(), ev.copy());
    }

    public static RM pdPower(RM s, double power, int maxRuns, double tol) {
        EigenPair p = pdEigenDecomp(s, maxRuns, tol);
        RM U = p.vectors();
        RM lambda = p.expandedValues();
        for (int i = 0; i < lambda.rowCount(); i++) {
            lambda.set(i, i, Math.pow(lambda.get(i, i), power));
        }
        return U.dot(lambda).dot(U.t());
    }

    private static boolean inTolerance(RM s, double tol) {
        for (int i = 0; i < s.rowCount(); i++) {
            for (int j = i + 1; j < s.colCount(); j++) {
                if (Math.abs(s.get(i, j)) > tol)
                    return false;
            }
        }
        return true;
    }

}
