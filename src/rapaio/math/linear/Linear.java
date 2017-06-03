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

import rapaio.math.linear.dense.EigenDecomposition;
import rapaio.math.linear.dense.SolidRM;
import rapaio.math.linear.dense.SolidRV;

/**
 * Linear algebra tool bag class.
 * Contains various utilities to create and manipulate linear algbra constructs like {@link RM} or {@link RV}
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 2/6/15.
 */
public final class Linear {

    public static RM chol2inv(RM R) {
        return chol2inv(R, SolidRM.identity(R.getRowCount()));
    }

    public static RM chol2inv(RM R, RM B) {
        RM ref = R.t();
        if (B.getRowCount() != R.getRowCount()) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }

        // Copy right hand side.
        RM X = B.solidCopy();

        int n = ref.getRowCount();
        int nx = X.getColCount();
        double[][] L = new double[n][n];
        for (int i = 0; i < n; i++) {
        	for (int j = 0; j < n; j++) {
        		L[i][j] = ref.get(i, j);
        	}
        }
        
        SubstitutionStrategy sStrategy = new ForwardSubstitution();
        X = sStrategy.getSubstitution(n, nx, X, L);
        
        sStrategy = new BackwardSubstitution();
        X = sStrategy.getSubstitution(n, nx, X, L);
        
        return X;
    }

    /*
    public static EigenPair pdEigenDecomp(RM s, int maxRuns, double tol) {

        // runs QR decomposition algorithm for maximum of iterations
        // to provide a solution which has other than diagonals under
        // tolerance

        // this works only for positive definite
        // here we check only symmetry

        if (s.getRowCount() != s.getColCount())
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

        int n = s.getColCount();
        EigenDecomposition evd = EigenDecomposition.from(s);

        double[] _values = evd.getRealEigenvalues();
        RM _vectors = evd.getV();

        RV values = SolidRV.empty(n);
        RM vectors = SolidRM.empty(n, n);

        for (int i = 0; i < values.count(); i++) {
            values.set(values.count() - i - 1, _values[i]);
        }
        for (int i = 0; i < vectors.getRowCount(); i++) {
            for (int j = 0; j < vectors.getColCount(); j++) {
                vectors.set(i, vectors.getColCount() - j - 1, _vectors.get(i, j));
            }
        }
        return EigenPair.from(values, vectors);
    }

    public static RM pdPower(RM s, double power, int maxRuns, double tol) {
        EigenPair eigenPair = eigenDecomp(s, maxRuns, tol);
        RM U = eigenPair.getRM();
        RM lambda = eigenPair.expandedValues();
        for (int i = 0; i < lambda.getRowCount(); i++) {
            //TODO quick fix
            // this is because negative numbers can be produced for small quantities
            lambda.set(i, i, Math.pow(Math.abs(lambda.get(i, i)), power));
        }
        return U.dot(lambda).dot(U.t());
    }

    @SuppressWarnings("unused")
	private static boolean inTolerance(RM s, double tol) {
        for (int i = 0; i < s.getRowCount(); i++) {
            for (int j = i + 1; j < s.getColCount(); j++) {
                if (Math.abs(s.get(i, j)) > tol)
                    return false;
            }
        }
        return true;
    }

}
