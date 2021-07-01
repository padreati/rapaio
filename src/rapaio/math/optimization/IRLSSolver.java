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

package rapaio.math.optimization;

import rapaio.data.VarDouble;
import rapaio.math.MathTools;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.decomposition.QRDecomposition;
import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/12/17.
 * <p>
 * Implemented using as reference: "Iterative Reweighted Least Squares  - C. Sidney Burrus"
 */
public final class IRLSSolver extends ParamSet<IRLSSolver> implements Solver {

    public static IRLSSolver newMinimizer() {
        return new IRLSSolver();
    }

    public static IRLSSolver newMinimizerM1(double p) {
        return new IRLSSolver()
                .method.set(Method.IRLS1M)
                .p.set(p)
                .k.set((p >= 2) ? 1.5 : 0.8);
    }

    public static IRLSSolver newMinimizerM0(double p) {
        return new IRLSSolver()
                .method.set(Method.IRLS0M)
                .p.set(p)
                .k.set((p >= 2) ? 1.5 : 0.8);

    }

    @Serial
    private static final long serialVersionUID = 3887827896160000986L;

    public enum Method {
        IRLS0M,
        IRLS1M
    }

    public final ValueParam<Method, IRLSSolver> method = new ValueParam<>(this, Method.IRLS1M,
            "method", "method used to find the solution");

    /**
     * p norm of the loss function to minimize
     */
    public final ValueParam<Double, IRLSSolver> p = new ValueParam<>(this, 2.0,
            "p", "p norm of the loss function.");

    /**
     * Design matrix
     */
    public final ValueParam<DMatrix, IRLSSolver> m = new ValueParam<>(this, null,
            "m", "design matrix");

    /**
     * Output vector
     */
    public final ValueParam<DVector, IRLSSolver> b = new ValueParam<>(this, null,
            "b", "output vector");

    /**
     * Maximum number of iterations
     */
    public final ValueParam<Integer, IRLSSolver> maxIt = new ValueParam<>(this, 10,
            "maxIt", "maximum number of iterations.");

    /**
     * Convergence threshold
     */
    public final ValueParam<Double, IRLSSolver> eps = new ValueParam<>(this, 1e-12,
            "eps", "convergence threshold");

    /**
     * Homotopy parameter.
     * For `2 < p < infinity` we have `k in [1.01, 2].
     * For `0 < p < 2` we have `k in [0.7, 0.9]`.
     */
    public final ValueParam<Double, IRLSSolver> k = new ValueParam<>(this, 1.5,
            "k", "Homotopy parameter");

    private DVector solution;
    private List<DVector> solutions;
    private VarDouble errors;
    private boolean converged;

    private IRLSSolver() {
    }

    @Override
    public DVector solution() {
        return solution;
    }

    @Override
    public List<DVector> solutions() {
        return solutions;
    }

    @Override
    public VarDouble errors() {
        return errors;
    }

    @Override
    public boolean hasConverged() {
        return converged;
    }

    @Override
    public IRLSSolver compute() {
        switch (method.get()) {
            case IRLS0M -> new IRLS0M(this).compute().pushSolution();
            case IRLS1M -> new IRLS1M(this).compute().pushSolution();
        }
        return this;
    }

    private static abstract class MethodImpl {

        // parameters
        protected final IRLSSolver parent;
        protected final DMatrix A;
        protected final DVector b;
        protected double p;
        protected double k;
        protected int maxIt;
        protected final double tol;

        // solution
        protected DVector solution;
        protected List<DVector> solutions = new ArrayList<>();
        protected VarDouble errors = VarDouble.empty().name("errors");
        protected boolean converged;

        public MethodImpl(IRLSSolver parent) {
            this.parent = parent;
            this.A = parent.m.get();
            this.b = parent.b.get();
            this.p = parent.p.get();
            this.k = parent.k.get();
            this.maxIt = parent.maxIt.get();
            this.tol = parent.eps.get();

            if (p >= 2) {
                k = MathTools.cut(k, 1.01, 2);
            }
            if (p < 2) {
                k = MathTools.cut(k, 0.5, 0.99);
            }
        }

        public abstract MethodImpl compute();

        public void pushSolution() {
            parent.converged = this.converged;
            parent.solution = solution.copy();
            parent.solutions = new ArrayList<>(solutions);
            parent.errors = errors.copy();
        }
    }

    /**
     * Implements algorithm m0 from "Iterative Reweighted Least Squares  - C. Sidney Burrus"
     */
    private static class IRLS0M extends MethodImpl {

        public IRLS0M(IRLSSolver parent) {
            super(parent);
        }

        @Override
        public MethodImpl compute() {
            converged = false;
            // initial L2 solution
            DVector x = QRDecomposition.from(A).solve(b.asMatrix()).mapCol(0);
            for (int it = 0; it < maxIt; it++) {
                // error vector
                DVector e = A.dot(x).sub(b);
                errors.addDouble(e.norm(p));

                // error weights for IRLS
                DVector w = e.apply(v -> (p == 1) ? 1 / Math.max(1e-10, abs(v)) : pow(abs(v), (p - 2) / 2));
                // normalize weight vector
                w.div(w.sum());
                // square w
                w.mult(w);
                // weighted L2 solution
                DMatrix w2a = w.diagDot(A);
                DMatrix A1 = w2a.t().dot(A);
                DVector b1 = w2a.t().dot(b);

                x = QRDecomposition.from(A1).solve(b1.asMatrix()).mapCol(0);
                solutions.add(x);

                // break if the improvement is then tolerance
                if (it > 0 && Math.abs(errors.getDouble(errors.size() - 2) - e.norm(p)) < tol) {
                    converged = true;
                    break;
                }
            }
            solution = x;
            return this;
        }
    }

    /**
     * Implements algorithm m0 from "Iterative Reweighted Least Squares  - C. Sidney Burrus"
     */
    private static class IRLS1M extends MethodImpl {

        public IRLS1M(IRLSSolver parent) {
            super(parent);
        }

        @Override
        public MethodImpl compute() {
            converged = false;
            // initial homotopy value
            double pk = 2;
            // initial L2 solution

            DVector x = QRDecomposition.from(A).solve(b.asMatrix()).mapCol(0);
            for (int it = 0; it < maxIt; it++) {
                pk = (p >= 2) ? Math.min(p, k * pk) : Math.max(p, k * pk);
                // error vector
                DVector e = A.dot(x).sub(b);
                // error weights for IRLS
                double pkk = pk;
                DVector w = e.copy().apply(v -> (pkk == 1) ? 1 / Math.max(1e-10, abs(v)) : pow(abs(v), (pkk - 2)));
                // normalize weight vector
                w.div(w.sum());
                // weighted L2 solution
                DMatrix w2a = w.diagDot(A);
                DMatrix A1 = w2a.t().dot(A);
                DVector b1 = w2a.t().dot(b);
                DVector x1 = QRDecomposition.from(A1).solve(b1.asMatrix()).mapCol(0);
                // Newton's parameter
                double q = 1.0 / (pk - 1);
                double nn;
                if (p > 2) {
                    // partial update for p>2
                    x = x1.mult(q).add(x.mult(1 - q));
                    nn = p;
                } else {
                    // no partial update for p<=2
                    x = x1;
                    nn = 2;
                }
                solutions.add(x);
                errors.addDouble(e.norm(nn));
                // break if the improvement is less then tolerance
                if (it > 0 && Math.abs(errors.getDouble(errors.size() - 2) - errors.getDouble(errors.size() - 1)) < tol) {
                    converged = true;
                    break;
                }
            }
            solution = x;
            return this;
        }
    }
}
