/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

import rapaio.math.MathTools;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.decomposition.DoubleCholeskyDecomposition;
import rapaio.util.param.ParamSet;
import rapaio.util.param.ValueParam;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/21/20.
 */
public class BinaryLogisticIRLS extends ParamSet<BinaryLogisticIRLS> {

    @Serial
    private static final long serialVersionUID = -1351523770434554322L;

    /**
     * Threshold value used to assess convergence of a solution
     */
    public final ValueParam<Double, BinaryLogisticIRLS> eps = new ValueParam<>(this, 1e-20, "eps");

    /**
     * Maximum number of iterations
     */
    public final ValueParam<Integer, BinaryLogisticIRLS> maxIter = new ValueParam<>(this, 10, "maxIter");

    /**
     * L2 regularization penalty
     */
    public final ValueParam<Double, BinaryLogisticIRLS> lambdap = new ValueParam<>(this, 0.0, "lambda");

    public final ValueParam<DMatrix, BinaryLogisticIRLS> xp = new ValueParam<>(this, null, "x");

    public final ValueParam<DVector, BinaryLogisticIRLS> yp = new ValueParam<>(this, null, "y");

    /**
     * Initial weights
     */
    public final ValueParam<DVector, BinaryLogisticIRLS> w0 = new ValueParam<>(this, null, "w0");

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

    public BinaryLogisticIRLS.Result fit() {

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

            DVector wnew = iterate(w, x, y, lambda, p, np);

            p = x.dot(wnew).apply(MathTools::logistic);
            np = p.applyNew(v -> 1 - v);
            double nll = negativeLogLikelihood(y, ny, wnew, lambda, p, np);

            double nll_delta = nll - nlls.get(nlls.size() - 1);
            if (it > 1 && (abs(nll_delta / nll) <= eps.get() /*|| nll_delta > 0*/)) {
                return new BinaryLogisticIRLS.Result(nlls, ws, true);
            }
            ws.add(wnew);
            nlls.add(nll);
            w = wnew;
        }
        return new BinaryLogisticIRLS.Result(nlls, ws, false);
    }

    private double negativeLogLikelihood(DVector y, DVector ny, DVector w, double lambda, DVector p, DVector np) {
        DVector logp = p.cutNew(1e-6, Double.NaN).log();
        DVector lognp = np.cutNew(1e-6, Double.NaN).log();

        return -logp.dot(y) - lognp.dot(ny) + lambda * w.norm(2) / 2;
    }

    private DVector iterate(DVector w, DMatrix x, DVector y, double lambda, DVector p, DVector np) {

        // p(1-p) diag from p diag
        DVector pvar = p.mulNew(np).cut(1e-6, Double.NaN);

        // H = X^t * I{p(1-p)} * X + I_lambda
        DMatrix xta = x.t().mulNew(pvar, 0);
        DMatrix h = xta.dot(x);
        if (lambda > 0) {
            h.add(DMatrix.diagonal(DVector.fill(h.rows(), lambda)));
        }

        // z = Xw + I{p(1-p)}^{-1} (y-p)
        DVector z = x.dot(w).add(y.subNew(p).div(pvar));
        DVector right = xta.dot(z);

        // solve IRLS
        DoubleCholeskyDecomposition chol = h.cholesky();
        if (chol.isSPD()) {
            return chol.solve(right);
        } else {
            return h.qr().solve(right);
        }
    }
}
