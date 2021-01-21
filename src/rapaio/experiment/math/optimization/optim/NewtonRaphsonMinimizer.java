/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.experiment.math.optimization.optim;

import rapaio.data.VarDouble;
import rapaio.experiment.math.functions.RDerivative;
import rapaio.experiment.math.functions.RFunction;
import rapaio.experiment.math.functions.RHessian;
import rapaio.experiment.math.optimization.optim.linesearch.BacktrackLineSearch;
import rapaio.experiment.math.optimization.optim.linesearch.LineSearch;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.decomposition.CholeskyDecomposition;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/18/17.
 */
public class NewtonRaphsonMinimizer implements Minimizer {

    private final double tol = 1e-3;
    private final int maxIt;

    private final LineSearch lineSearch = BacktrackLineSearch.from();

    private final DVector x;
    private final RFunction f;
    private final RDerivative d1f;
    private final RHessian d2f;

    private DVector sol;

    private final List<DVector> solutions = new ArrayList<>();
    private VarDouble errors;
    private boolean converged = false;

    public NewtonRaphsonMinimizer(
            DVector x,
            RFunction f,
            RDerivative d1f,
            RHessian d2f,
            int maxIt) {
        this.x = x;
        this.f = f;
        this.d1f = d1f;
        this.d2f = d2f;
        this.maxIt = maxIt;
    }

    public VarDouble getErrors() {
        return errors;
    }

    @Override
    public void compute() {
        converged = false;
        sol = x.copy();
        for (int i = 0; i < maxIt; i++) {
            solutions.add(sol.copy());
            DVector d1f_x = d1f.apply(sol);
            DMatrix d2f_x = d2f.apply(sol);
            DVector d1f_x_n = d1f_x.copy().mult(-1);

            DVector delta_x;

            // try LU decomposition, otherwise work with QR
//            LUDecomposition lu = LUDecomposition.from(d2f_x);
//            if (lu.isNonSingular()) {
//                delta_x = lu.solve(d1f_x_n.asMatrix()).mapCol(0);
//            } else {
//                delta_x = QRDecomposition.from(d2f_x)
//                        .solve(d1f_x_n.asMatrix()).mapCol(0);
//            }

            // apply Cholesky modified
            CholeskyDecomposition chol = modifiedCholesky(d2f_x);
            delta_x = chol.solve(d1f_x_n.asMatrix()).mapCol(0);

            double error = d1f_x.copy().dot(delta_x);
            if (pow(error, 2) / 2 < tol) {
                converged = true;
                break;
            }

            double t = lineSearch.find(f, d1f, x, delta_x);
            sol.add(delta_x.mult(t));
        }
    }

    private CholeskyDecomposition modifiedCholesky(DMatrix A) {
        double beta = 0.001;

        // find minimum diagonal element

        double minac = A.get(0, 0);
        for (int i = 1; i < A.rowCount(); i++) {
            minac = min(minac, A.get(i, i));
        }

        // compute modifying constant
        double sigma = (minac > 0) ? 0 : -minac + beta;

        // update matrix
        DMatrix Ac = A.copy();
        for (int i = 0; i < Ac.rowCount(); i++) {
            Ac.set(i, i, A.get(i, i) + sigma);
        }

        // compute Cholesky
        CholeskyDecomposition chol = CholeskyDecomposition.from(Ac);
        if (chol.isSPD()) {
            return chol;
        }

        while (true) {
            // update sigma
            sigma = max(100 * sigma, beta);
            // update matrix
            DMatrix Acc = Ac.copy();
            for (int i = 0; i < Acc.rowCount(); i++) {
                Acc.set(i, i, Ac.get(i, i) + sigma);
            }

            // compute Cholesky
            CholeskyDecomposition chol2 = CholeskyDecomposition.from(Acc);
            if (chol2.isSPD()) {
                return chol2;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("solution: ").append(sol.toString()).append("\n");
        return sb.toString();
    }

    public List<DVector> solutions() {
        return solutions;
    }

    public DVector solution() {
        return sol;
    }

    @Override
    public boolean hasConverged() {
        return converged;
    }
}
