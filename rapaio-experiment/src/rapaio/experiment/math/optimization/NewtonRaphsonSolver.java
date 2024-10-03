/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.experiment.math.optimization;

import static java.lang.Math.max;
import static java.lang.Math.pow;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import rapaio.core.param.ParamSet;
import rapaio.core.param.ValueParam;
import rapaio.data.VarDouble;
import rapaio.math.optimization.Solver;
import rapaio.math.optimization.functions.RDerivative;
import rapaio.math.optimization.functions.RFunction;
import rapaio.math.optimization.functions.RHessian;
import rapaio.math.optimization.linesearch.BacktrackLineSearch;
import rapaio.math.optimization.linesearch.LineSearch;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.matrix.CholeskyDecomposition;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/18/17.
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
    public final ValueParam<Tensor<Double>, NewtonRaphsonSolver> x0 = new ValueParam<>(this, null, "x0");

    private Tensor<Double> sol;

    private final List<Tensor<Double>> solutions = new ArrayList<>();
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
            Tensor<Double> d1f_x = d1f.get().apply(sol);
            Tensor<Double> d2f_x = d2f.get().apply(sol);
            Tensor<Double> d1f_x_n = d1f_x.copy().mul(-1.);

            Tensor<Double> delta_x;

            // try LU decomposition, otherwise work with QR
//            LUDecomposition lu = LUDecomposition.from(d2f_x);
//            if (lu.isNonSingular()) {
//                delta_x = lu.solve(d1f_x_n.asMatrix()).mapCol(0);
//            } else {
//                delta_x = QRDecomposition.from(d2f_x)
//                        .solve(d1f_x_n.asMatrix()).mapCol(0);
//            }

            // apply Cholesky modified
            CholeskyDecomposition<Double> chol = modifiedCholesky(d2f_x);
            delta_x = chol.solve(d1f_x_n);

            double error = d1f_x.copy().vdot(delta_x);
            if (pow(error, 2) / 2 < tol.get()) {
                converged = true;
                break;
            }

            double t = lineSearch.get().search(f.get(), d1f.get(), x0.get(), delta_x);
            sol.add(delta_x.mul(t));
        }
        return this;
    }

    private CholeskyDecomposition<Double> modifiedCholesky(Tensor<Double> A) {
        double beta = 0.001;

        // find minimum diagonal element

        double minac = A.get(0, 0);
        for (int i = 1; i < A.dim(0); i++) {
            minac = Math.min(minac, A.get(i, i));
        }

        // compute modifying constant
        double sigma = (minac > 0) ? 0 : -minac + beta;

        // update matrix
        Tensor<Double> Ac = A.copy();
        for (int i = 0; i < Ac.dim(0); i++) {
            Ac.setDouble(A.get(i, i) + sigma, i, i);
        }

        // compute Cholesky
        CholeskyDecomposition<Double> chol = Ac.cholesky();
        if (chol.isSPD()) {
            return chol;
        }

        while (true) {
            // update sigma
            sigma = max(100 * sigma, beta);
            // update matrix
            Tensor<Double> Acc = Ac.copy();
            for (int i = 0; i < Acc.dim(0); i++) {
                Acc.setDouble(Ac.get(i, i) + sigma, i, i);
            }

            // compute Cholesky
            CholeskyDecomposition<Double> chol2 = Acc.cholesky();
            if (chol2.isSPD()) {
                return chol2;
            }
        }
    }

    @Override
    public String toString() {
        return "solution: " + sol.toString() + "\n";
    }

    public List<Tensor<Double>> solutions() {
        return solutions;
    }

    public Tensor<Double> solution() {
        return sol;
    }

    @Override
    public boolean hasConverged() {
        return converged;
    }
}
