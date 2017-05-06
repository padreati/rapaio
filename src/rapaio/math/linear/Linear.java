/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import rapaio.math.linear.dense.EigenvalueDecomposition;
import rapaio.math.linear.dense.SolidRM;
import rapaio.math.linear.dense.SolidRV;

/**
 * Linear algebra tool bag class.
 * Contains various utilities to create and manipulate linear algbra constructs like {@link RM} or {@link RV}
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/6/15.
 */
@SuppressWarnings("deprecation")
public final class Linear {

    public static RM chol2inv(RM R) {
        return chol2inv(R, SolidRM.identity(R.rowCount()));
    }

    public static RM chol2inv(RM R, RM B) {
        RM ref = R.t();
        if (B.rowCount() != R.rowCount()) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }

        // Copy right hand side.
        RM X = B.solidCopy();

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

    /*
    public static EigenPair pdEigenDecomp(RM s, int maxRuns, double tol) {

        // runs QR decomposition algorithm for maximum of iterations
        // to provide a solution which has other than diagonals under
        // tolerance

        // this works only for positive definite
        // here we check only symmetry

        if (s.rowCount() != s.colCount())
            throw new IllegalArgumentException("This eigen pair method works only for positive definite matrices");
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
        return EigenPair.from(s.diag(), ev.solidCopy());
    }*/

    public static EigenPair eigenDecomp(RM s, int maxRuns, double tol) {

        int n = s.colCount();
        EigenvalueDecomposition evd = new EigenvalueDecomposition(s);

        double[] _values = evd.getRealEigenvalues();
        RM _vectors = evd.getV();

        RV values = SolidRV.empty(n);
        RM vectors = SolidRM.empty(n, n);

        for (int i = 0; i < values.count(); i++) {
            values.set(values.count() - i - 1, _values[i]);
        }
        for (int i = 0; i < vectors.rowCount(); i++) {
            for (int j = 0; j < vectors.colCount(); j++) {
                vectors.set(i, vectors.colCount() - j - 1, _vectors.get(i, j));
            }
        }
        return EigenPair.from(values, vectors);
    }

    public static RM pdPower(RM s, double power, int maxRuns, double tol) {
        EigenPair eigenPair = eigenDecomp(s, maxRuns, tol);
        RM U = eigenPair.getRM();
        RM lambda = eigenPair.expandedValues();
        for (int i = 0; i < lambda.rowCount(); i++) {
            //TODO quick fix
            // this is because negative numbers can be produced for small quantities
            lambda.set(i, i, Math.pow(Math.abs(lambda.get(i, i)), power));
        }
        return U.dot(lambda).dot(U.t());
    }
/*
    @SuppressWarnings("unused")
	private static boolean inTolerance(RM s, double tol) {
        for (int i = 0; i < s.rowCount(); i++) {
            for (int j = i + 1; j < s.colCount(); j++) {
                if (Math.abs(s.get(i, j)) > tol)
                    return false;
            }
        }
        return true;
    }
*/
}
