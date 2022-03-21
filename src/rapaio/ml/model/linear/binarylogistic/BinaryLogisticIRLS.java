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

import static java.lang.Math.exp;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.decomposition.CholeskyDecomposition;
import rapaio.math.linear.decomposition.QRDecomposition;
import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;

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
    public final ValueParam<Double, BinaryLogisticIRLS> lambda = new ValueParam<>(this, 0.0, "lambda");

    public final ValueParam<DMatrix, BinaryLogisticIRLS> x = new ValueParam<>(this, null, "x");

    public final ValueParam<DVector, BinaryLogisticIRLS> y = new ValueParam<>(this, null, "y");

    /**
     * Initial weights
     */
    public final ValueParam<DVector, BinaryLogisticIRLS> w0 = new ValueParam<>(this,null, "w0");

    public record Result(List<Double> nlls, List<DVector> ws, boolean converged) {

        public DVector getW() {
            if (ws.size() > 0) {
                return ws.get(ws.size() - 1);
            }
            return null;
        }

        public double getNll() {
            if (nlls.size() > 1) {
                return nlls.get(nlls.size() - 1);
            }
            return Double.NaN;
        }
    }

    public Result fit() {
        int it = 0;
        // current solution
        DVector w = w0.get().copy();
        ArrayList<DVector> ws = new ArrayList<>();
        ws.add(w);
        List<Double> nlls = new ArrayList<>();
        nlls.add(negativeLogLikelihood(x.get(), y.get(), w));

        while (it++ < maxIter.get()) {
            DVector wnew = iterate(w);
            double nll = negativeLogLikelihood(x.get(), y.get(), wnew);

            double nll_delta = nll - nlls.get(nlls.size() - 1);
            if (it > 1 && (Math.abs(nll_delta) <= eps.get())) {
                return new Result(nlls, ws, true);
            }
            ws.add(wnew);
            nlls.add(nll);
            w = wnew;
        }
        return new Result(nlls, ws, false);
    }

    private double negativeLogLikelihood(DMatrix x, DVector y, DVector w) {
        return -x.dot(w)
                .apply((i, v) -> (y.get(i) == 1) ? (1. / (1. + exp(-v))) : (1 - 1. / (1. + exp(-v))))
                .apply(this::cut)
                .apply(Math::log)
                .nansum() / x.rowCount();
    }

    private double cut(double value) {
        return Math.min(1 - 1e-12, value);
    }

    private DVector iterate(DVector w) {

        // Xw dot product
        DVector xw = x.get().dot(w);

        // p diag = 1/(1+exp(-Xw))
        DVector p = xw.copy().apply(value -> cut(1. / (1. + exp(-value))));

        // p(1-p) diag from p diag
        DVector pvars = p.copy().apply(value -> value * (1 - value));

        // z = Wx - I(p(1-p))^{-1}(y-p)
        DVector z = xw.add(y.get().copy().sub(p).div(pvars));

        // Xt(p(1-p)
        DMatrix xpvar = x.get().copy();
        for (int i = 0; i < xpvar.rowCount(); i++) {
            double pvar = pvars.get(i);
            for (int j = 0; j < xpvar.colCount(); j++) {
                xpvar.set(i, j, xpvar.get(i, j) * pvar);
            }
        }

        // XI(p(1-p))^T * X
        DMatrix mA = xpvar.t().dot(x.get());

        // for L2 regularization we inflate main diagonal
        if (lambda.get() != 0) {
            for (int i = 0; i < mA.rowCount(); i++) {
                mA.inc(i, i, lambda.get());
            }
        }

        //XI(p(1-p))^T * X * z
        DMatrix b = xpvar.t().dot(z.asMatrix());

        CholeskyDecomposition chol = CholeskyDecomposition.from(mA);
        if (chol.isSPD()) {
            // if we have a symmetric positive definite matrix we solve it with Cholesky
            return chol.solve(b).mapColNew(0);
        }
        // otherwise we fall in QR decomposition
        return QRDecomposition.from(mA).solve(b).mapColNew(0);
    }
}
