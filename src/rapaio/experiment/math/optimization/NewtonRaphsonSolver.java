/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.experiment.math.optimization;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import rapaio.data.VarDouble;
import rapaio.math.functions.RDerivative;
import rapaio.math.functions.RFunction;
import rapaio.math.functions.RHessian;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.decomposition.DoubleCholeskyDecomposition;
import rapaio.math.optimization.Solver;
import rapaio.math.optimization.linesearch.BacktrackLineSearch;
import rapaio.math.optimization.linesearch.LineSearch;
import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/18/17.
 */
public class NewtonRaphsonSolver extends ParamSet<NewtonRaphsonSolver> implements Solver {

    @Serial
    private static final long serialVersionUID = -6600678871841923200L;

    public final ValueParam<Double, NewtonRaphsonSolver> tol = new ValueParam<>(this, 1e-3, "tol");
    public final ValueParam<Integer, NewtonRaphsonSolver> maxIt = new ValueParam<>(this, 100, "maxIt");
    public final ValueParam<LineSearch, NewtonRaphsonSolver> lineSearch =
            new ValueParam<>(this, BacktrackLineSearch.newSearch(), "lineSearch");
    public final ValueParam<RFunction, NewtonRaphsonSolver> f = new ValueParam<>(this, null, "f");
    public final ValueParam<RDerivative, NewtonRaphsonSolver> d1f = new ValueParam<>(this, null, "d1f");
    public final ValueParam<RHessian, NewtonRaphsonSolver> d2f = new ValueParam<>(this, null, "d2f");
    public final ValueParam<DVector, NewtonRaphsonSolver> x0 = new ValueParam<>(this, null, "x0");

    private DVector sol;

    private final List<DVector> solutions = new ArrayList<>();
    private VarDouble errors;
    private boolean converged = false;

    @Override
    public VarDouble errors() {
        return errors;
    }

    @Override
    public NewtonRaphsonSolver compute() {
        converged = false;
        sol = x0.get().copy();
        for (int i = 0; i < maxIt.get(); i++) {
            solutions.add(sol.copy());
            DVector d1f_x = d1f.get().apply(sol);
            DMatrix d2f_x = d2f.get().apply(sol);
            DVector d1f_x_n = d1f_x.copy().mul(-1);

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
            DoubleCholeskyDecomposition chol = modifiedCholesky(d2f_x);
            delta_x = chol.solve(d1f_x_n.asMatrix()).mapCol(0);

            double error = d1f_x.copy().dot(delta_x);
            if (pow(error, 2) / 2 < tol.get()) {
                converged = true;
                break;
            }

            double t = lineSearch.get().search(f.get(), d1f.get(), x0.get(), delta_x);
            sol.add(delta_x.mul(t));
        }
        return this;
    }

    private DoubleCholeskyDecomposition modifiedCholesky(DMatrix A) {
        double beta = 0.001;

        // find minimum diagonal element

        double minac = A.get(0, 0);
        for (int i = 1; i < A.rows(); i++) {
            minac = min(minac, A.get(i, i));
        }

        // compute modifying constant
        double sigma = (minac > 0) ? 0 : -minac + beta;

        // update matrix
        DMatrix Ac = A.copy();
        for (int i = 0; i < Ac.rows(); i++) {
            Ac.set(i, i, A.get(i, i) + sigma);
        }

        // compute Cholesky
        DoubleCholeskyDecomposition chol = Ac.cholesky();
        if (chol.isSPD()) {
            return chol;
        }

        while (true) {
            // update sigma
            sigma = max(100 * sigma, beta);
            // update matrix
            DMatrix Acc = Ac.copy();
            for (int i = 0; i < Acc.rows(); i++) {
                Acc.set(i, i, Ac.get(i, i) + sigma);
            }

            // compute Cholesky
            DoubleCholeskyDecomposition chol2 = Acc.cholesky();
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
