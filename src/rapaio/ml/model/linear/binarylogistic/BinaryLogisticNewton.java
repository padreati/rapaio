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

package rapaio.ml.model.linear.binarylogistic;

import static java.lang.StrictMath.abs;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import rapaio.math.MathTools;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.decomposition.DoubleCholeskyDecomposition;
import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/21/20.
 */
public class BinaryLogisticNewton extends ParamSet<BinaryLogisticNewton> {

    @Serial
    private static final long serialVersionUID = 4772367031535421893L;

    /**
     * Tolerance threshold used to assess convergence of a solution
     */
    public final ValueParam<Double, BinaryLogisticNewton> eps = new ValueParam<>(this, 1e-100, "eps");

    /**
     * Maximum number of iterations
     */
    public final ValueParam<Integer, BinaryLogisticNewton> maxIter = new ValueParam<>(this, 10, "maxIter");

    /**
     * L2 regularization penalty
     */
    public final ValueParam<Double, BinaryLogisticNewton> lambdap = new ValueParam<>(this, 0.0, "lambda");

    /**
     * Input matrix
     */
    public final ValueParam<DMatrix, BinaryLogisticNewton> xp = new ValueParam<>(this, null, "x");

    /**
     * Target vector
     */
    public final ValueParam<DVector, BinaryLogisticNewton> yp = new ValueParam<>(this, null, "y");

    /**
     * Initial values for weights
     */
    public final ValueParam<DVector, BinaryLogisticNewton> w0 = new ValueParam<>(this, null, "w0");

    public record Result(List<Double> nlls, List<DVector> ws, boolean converged) {
        public DVector w() {
            if (!ws.isEmpty()) {
                return ws.get(ws.size() - 1);
            }
            return DVector.zeros(0);
        }

        public double nll() {
            if (nlls.size() > 1) {
                return nlls.get(nlls.size() - 1);
            }
            return Double.NaN;
        }
    }

    public BinaryLogisticNewton.Result fit() {

        DMatrix x = xp.get();
        DVector y = yp.get();
        DVector ny = DVector.ones(y.size()).sub(y);
        DVector w = w0.get();
        double lambda = lambdap.get();
        DVector p = x.dot(w).apply(MathTools::logistic);
        DVector np = p.applyNew(v -> 1 - v);

        int it = 0;
        // current solution
        ArrayList<DVector> ws = new ArrayList<>();
        ws.add(w);
        List<Double> nlls = new ArrayList<>();
        nlls.add(negativeLogLikelihood(y, ny, w, lambda, p, np));

        while (it++ < maxIter.get()) {

            DVector wnew = iterate(w, x, y, ny, lambda, p, np);

            p = x.dot(wnew).apply(MathTools::logistic);
            np = p.applyNew(v -> 1 - v);
            double nll = negativeLogLikelihood(y, ny, wnew, lambda, p, np);

            double nll_delta = nll - nlls.get(nlls.size() - 1);
            if (it > 1 && (abs(nll_delta / nll) <= eps.get() /*|| nll_delta > 0*/)) {
                return new BinaryLogisticNewton.Result(nlls, ws, true);
            }
            ws.add(wnew);
            nlls.add(nll);
            w = wnew;
        }
        return new BinaryLogisticNewton.Result(nlls, ws, false);
    }

    private double negativeLogLikelihood(DVector y, DVector ny, DVector w, double lambda, DVector p, DVector np) {
        DVector logp = p.applyNew(this::cut).apply(StrictMath::log);
        DVector lognp = np.applyNew(this::cut).apply(StrictMath::log);

        return -logp.dot(y) - lognp.dot(ny) + lambda * w.norm(2) / 2;
    }

    private double cut(double value) {
        return Math.max(1e-6, value);
    }

    private DVector iterate(DVector w, DMatrix x, DVector y, DVector ny, double lambda, DVector p, DVector np) {

        // p(1-p) diag from p diag
        DVector pvar = p.mulNew(np).apply(this::cut);

        // H = X^t * I{p(1-p)} * X + I_lambda
        DMatrix xta = x.t().mulNew(pvar, 0);
        DMatrix h = xta.dot(x);
        if (lambda > 0) {
            h.add(DMatrix.diagonal(DVector.fill(h.rows(), lambda)));
        }

        // ng = -g = X^t (y - p)
        DVector ng = x.t().dot(y.subNew(p));

        // solve IRLS
        DoubleCholeskyDecomposition chol = h.cholesky();
        DVector d;
        if (chol.isSPD()) {
            d = chol.solve(ng);
        } else {
            d = h.qr().solve(ng);
        }

        // linbe search which could be improved or at least standardized

        double factor = 1.0;
        double decrease = 0.9;
        DVector wc = w.addMulNew(factor, d);
        DVector pp = x.dot(wc).apply(MathTools::logistic);
        DVector nnp = pp.applyNew(v -> 1 - v);

        double nll = negativeLogLikelihood(y, ny, wc, lambda, pp, nnp);

        while (true) {
            var wcnew = w.addMulNew(factor * decrease, d);
            pp = x.dot(wcnew).apply(MathTools::logistic);
            nnp = pp.applyNew(v -> 1 - v);
            double nllNew = negativeLogLikelihood(y, ny, wc, lambda, pp, nnp);
            if (nllNew < nll) {
                factor *= decrease;
                nll = nllNew;
                wc = wcnew;
            } else {
                return wc;
            }
        }
    }
}
