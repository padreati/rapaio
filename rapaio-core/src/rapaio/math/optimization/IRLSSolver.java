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

package rapaio.math.optimization;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import rapaio.core.param.ParamSet;
import rapaio.core.param.ValueParam;
import rapaio.data.VarDouble;
import rapaio.math.MathTools;
import rapaio.math.tensor.Tensor;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/12/17.
 * <p>
 * Implemented using as reference: "Iterative Reweighted Least Squares  - c. Sidney Burrus"
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

    /**
     * Method used to find the solution.
     */
    public final ValueParam<Method, IRLSSolver> method = new ValueParam<>(this, Method.IRLS1M, "method");

    /**
     * p norm of the loss function to minimize
     */
    public final ValueParam<Double, IRLSSolver> p = new ValueParam<>(this, 2.0, "p");

    /**
     * Design matrix
     */
    public final ValueParam<Tensor<Double>, IRLSSolver> m = new ValueParam<>(this, null, "m");

    /**
     * Output vector
     */
    public final ValueParam<Tensor<Double>, IRLSSolver> b = new ValueParam<>(this, null, "b");

    /**
     * Maximum number of iterations
     */
    public final ValueParam<Integer, IRLSSolver> maxIt = new ValueParam<>(this, 10, "maxIt");

    /**
     * Convergence threshold
     */
    public final ValueParam<Double, IRLSSolver> eps = new ValueParam<>(this, 1e-12, "eps");

    /**
     * Homotopy parameter.
     * For `2 < p < infinity` we have `k in [1.01, 2].
     * For `0 < p < 2` we have `k in [0.7, 0.9]`.
     */
    public final ValueParam<Double, IRLSSolver> k = new ValueParam<>(this, 1.5, "k");

    private Tensor<Double> solution;
    private List<Tensor<Double>> solutions;
    private VarDouble errors;
    private boolean converged;

    private IRLSSolver() {
    }

    @Override
    public Tensor<Double> solution() {
        return solution;
    }

    @Override
    public List<Tensor<Double>> solutions() {
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
        protected final Tensor<Double> A;
        protected final Tensor<Double> b;
        protected final double p;
        protected double k;
        protected final int maxIt;
        protected final double tol;

        // solution
        protected Tensor<Double> solution;
        protected final List<Tensor<Double>> solutions = new ArrayList<>();
        protected final VarDouble errors = VarDouble.empty().name("errors");
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
     * Implements algorithm m0 from "Iterative Reweighted Least Squares  - c. Sidney Burrus"
     */
    private static class IRLS0M extends MethodImpl {

        public IRLS0M(IRLSSolver parent) {
            super(parent);
        }

        @Override
        public MethodImpl compute() {
            converged = false;
            // initial L2 solution
            Tensor<Double> x = A.qr().solve(b);
            for (int it = 0; it < maxIt; it++) {
                // error vector
                Tensor<Double> e = A.mv(x).sub_(b);
                errors.addDouble(e.norm(p));

                // error weights for IRLS
                Tensor<Double> w = e.apply(v -> (p == 1) ? 1 / Math.max(tol, abs(v)) : pow(abs(v), (p - 2) / 2));
                // normalize weight vector
                w.div(w.sum());
                // square w
                w.mul(w);
                // weighted L2 solution

                Tensor<Double> w2a = A.mul(w.stretch(1));
                Tensor<Double> A1 = w2a.t().mm(A);
                Tensor<Double> b1 = w2a.t().mv(b);

                try {
                    x = A1.qr().solve(b1);
                } catch (RuntimeException ignored) {
                    converged = false;
                    break;
                }
                solutions.add(x);

                // break if the improvement is then tolerance
                if (it > 0) {
                    double previousError = errors.getDouble(errors.size() - 2);
                    double currentError = errors.getDouble(errors.size() - 1);
                    if (Math.abs(previousError - currentError) < tol) {
                        converged = true;
                        break;
                    }
                }
            }
            solution = x;
            return this;
        }
    }

    /**
     * Implements algorithm m0 from "Iterative Reweighted Least Squares  - c. Sidney Burrus"
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

            Tensor<Double> x = A.qr().solve(b);
            for (int it = 0; it < maxIt; it++) {
                pk = (p >= 2) ? Math.min(p, k * pk) : Math.max(p, k * pk);
                // error vector
                Tensor<Double> e = A.mv(x).sub_(b);
                // error weights for IRLS
                double pkk = pk;
                Tensor<Double> w = e.apply(v -> (pkk == 1) ? 1 / Math.max(1e-10, abs(v)) : pow(abs(v), (pkk - 2)));
                // normalize weight vector
                w.div(w.sum());
                // weighted L2 solution
                Tensor<Double> w2a = A.mul(w.stretch(1));
                Tensor<Double> A1 = w2a.t().mm(A);
                Tensor<Double> b1 = w2a.t().mv(b);
                Tensor<Double> x1 = A1.qr().solve(b1);
                // Newton's parameter
                double q = 1.0 / (pk - 1);
                double nn;
                if (p > 2) {
                    // partial update for p>2
                    x = x1.mul(q).add(x.mul(1 - q));
                    nn = p;
                } else {
                    // no partial update for p<=2
                    x = x1;
                    nn = 2;
                }
                solutions.add(x);
                errors.addDouble(e.norm(nn));
                // break if the improvement is less then tolerance
                if (it > 0 && abs(errors.getDouble(errors.size() - 2) - errors.getDouble(errors.size() - 1)) < tol) {
                    converged = true;
                    break;
                }
            }
            solution = x;
            return this;
        }
    }
}
