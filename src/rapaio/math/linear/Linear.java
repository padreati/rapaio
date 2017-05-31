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

        // Solve L*Y = B;
        for (int k = 0; k < ref.getRowCount(); k++) {
            for (int j = 0; j < X.getColCount(); j++) {
                for (int i = 0; i < k; i++) {
                    X.increment(k, j, -X.get(i, j) * ref.get(k, i));
                }
                X.set(k, j, X.get(k, j) / ref.get(k, k));
            }
        }

        // Solve L'*X = Y;
        for (int k = ref.getRowCount() - 1; k >= 0; k--) {
            for (int j = 0; j < X.getColCount(); j++) {
                for (int i = k + 1; i < ref.getRowCount(); i++) {
                    X.increment(k, j, -X.get(i, j) * ref.get(i, k));
                }
                X.set(k, j, X.get(k, j) / ref.get(k, k));
            }
        }
        return X;
    }


    public static EigenPair eigenDecomp(RM s, int maxRuns, double tol) {

    	DecompStrategy decompStrategy = new EigenDecomp();
    	return decompStrategy.eigenDecomp(s, maxRuns, tol);
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
