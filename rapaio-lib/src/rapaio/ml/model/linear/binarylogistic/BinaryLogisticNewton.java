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

package rapaio.ml.model.linear.binarylogistic;

import static java.lang.StrictMath.abs;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import rapaio.core.param.ParamSet;
import rapaio.core.param.ValueParam;
import rapaio.darray.DArray;
import rapaio.darray.DArrays;
import rapaio.darray.Shape;
import rapaio.darray.matrix.CholeskyDecomposition;
import rapaio.math.MathTools;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/21/20.
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
    public final ValueParam<DArray<Double>, BinaryLogisticNewton> xp = new ValueParam<>(this, null, "x");

    /**
     * Target vector
     */
    public final ValueParam<DArray<Double>, BinaryLogisticNewton> yp = new ValueParam<>(this, null, "y");

    /**
     * Initial values for weights
     */
    public final ValueParam<DArray<Double>, BinaryLogisticNewton> w0 = new ValueParam<>(this, null, "w0");

    public record Result(List<Double> nlls, List<DArray<Double>> ws, boolean converged) {
        public DArray<Double> w() {
            if (!ws.isEmpty()) {
                return ws.getLast();
            }
            return DArrays.scalar(Double.NaN);
        }

        public double nll() {
            if (nlls.size() > 1) {
                return nlls.getLast();
            }
            return Double.NaN;
        }
    }

    public BinaryLogisticNewton.Result fit() {

        var mx = xp.get();
        var vy = yp.get();
        var vny = DArrays.full(Shape.of(vy.size()), 1.).sub_(vy);
        var vw = w0.get();
        double lambda = lambdap.get();
        var vp = mx.mv(vw).apply_(MathTools::logistic);
        var vnp = vp.apply(v -> 1 - v);

        int it = 0;
        // current solution
        List<DArray<Double>> ws = new ArrayList<>();
        ws.add(vw);
        List<Double> nlls = new ArrayList<>();
        nlls.add(negativeLogLikelihood(vy, vny, vw, lambda, vp, vnp));

        while (it++ < maxIter.get()) {

            DArray<Double> wnew = iterate(vw, mx, vy, vny, lambda, vp, vnp);

            vp = mx.mv(wnew).apply_(MathTools::logistic);
            vnp = vp.apply(v -> 1 - v);
            double nll = negativeLogLikelihood(vy, vny, wnew, lambda, vp, vnp);

            double nll_delta = nll - nlls.getLast();
            if (it > 1 && (abs(nll_delta / nll) <= eps.get() /*|| nll_delta > 0*/)) {
                return new BinaryLogisticNewton.Result(nlls, ws, true);
            }
            ws.add(wnew);
            nlls.add(nll);
            vw = wnew;
        }
        return new BinaryLogisticNewton.Result(nlls, ws, false);
    }

    private double negativeLogLikelihood(DArray<Double> y, DArray<Double> ny, DArray<Double> w, double lambda, DArray<Double> p,
            DArray<Double> np) {
        DArray<Double> logp = p.clamp(1e-6, Double.NaN).apply_(StrictMath::log);
        DArray<Double> lognp = np.clamp(1e-6, Double.NaN).apply(StrictMath::log);

        return -logp.inner(y) - lognp.inner(ny) + lambda * w.norm(2.) / 2;
    }

    private DArray<Double> iterate(DArray<Double> vw, DArray<Double> mx, DArray<Double> vy, DArray<Double> vny, double lambda,
            DArray<Double> vp, DArray<Double> vnp) {

        // p(1-p) diag from p diag
        DArray<Double> pvar = vp.mul(vnp).clamp_(1e-6, Double.NaN);

        // H = X^t * I{p(1-p)} * X + I_lambda
        DArray<Double> xta = mx.t().mul(pvar.stretch(0).expand(0, mx.t().dim(0)));
        DArray<Double> h = xta.mm(mx);
        if (lambda > 0) {
            for (int i = 0; i < h.dim(0); i++) {
                h.incDouble(lambda, i, i);
            }
        }

        // ng = -g = X^t (y - p)
        DArray<Double> ng = mx.t().mv(vy.sub(vp));

        // solve IRLS
        CholeskyDecomposition<Double> chol = h.cholesky();
        DArray<Double> d;
        if (chol.isSPD()) {
            d = chol.solve(ng);
        } else {
            d = h.qr().solve(ng);
        }

        // line search which could be improved or at least standardized

        double factor = 1.0;
        double decrease = 0.9;
        DArray<Double> wc = vw.fma(factor, d);
        DArray<Double> pp = mx.mv(wc).apply_(MathTools::logistic);
        DArray<Double> nnp = pp.apply(v -> 1 - v);

        double nll = negativeLogLikelihood(vy, vny, wc, lambda, pp, nnp);

        while (true) {
            var wcnew = vw.fma(factor * decrease, d);
            pp = mx.mv(wcnew).apply_(MathTools::logistic);
            nnp = pp.apply(v -> 1 - v);
            double nllNew = negativeLogLikelihood(vy, vny, wc, lambda, pp, nnp);
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
