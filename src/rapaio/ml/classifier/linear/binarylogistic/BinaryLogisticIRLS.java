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

package rapaio.ml.classifier.linear.binarylogistic;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import rapaio.math.linear.DM;
import rapaio.math.linear.DV;
import rapaio.math.linear.decomposition.CholeskyDecomposition;
import rapaio.math.linear.decomposition.QRDecomposition;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.exp;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/21/20.
 */
@Builder(setterPrefix = "with")
@Getter
public class BinaryLogisticIRLS {

    @Builder.Default
    private final double eps = 1e-20;
    @Builder.Default
    private final int maxIter = 10;
    @Builder.Default
    private final double lambda = 0.0;
    @NonNull
    private final DM x;
    @NonNull
    private final DV y;
    @NonNull
    private final DV w0;

    @Builder(setterPrefix = "with")
    @Getter
    @ToString
    public static class Result {

        public DV getW() {
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

        @NonNull
        private final List<Double> nlls;

        @NonNull
        private final List<DV> ws;

        private final boolean converged;
    }

    public Result fit() {
        int it = 0;
        // current solution
        DV w = w0.copy();
        ArrayList<DV> ws = new ArrayList<>();
        ws.add(w);
        List<Double> nlls = new ArrayList<>();
        nlls.add(negativeLogLikelihood(x, y, w));

        while (it++ < maxIter) {
            DV wnew = iterate(w);
            double nll = negativeLogLikelihood(x, y, wnew);

            double nll_delta = nll - nlls.get(nlls.size() - 1);
            if (it > 1 && (Math.abs(nll_delta) <= eps)) {
                return Result.builder()
                        .withConverged(true)
                        .withNlls(nlls)
                        .withWs(ws)
                        .build();
            }
            ws.add(wnew);
            nlls.add(nll);
            w = wnew;
        }
        return Result.builder()
                .withWs(ws)
                .withNlls(nlls)
                .withConverged(false)
                .build();
    }

    private double negativeLogLikelihood(DM x, DV y, DV w) {
        return -x.dot(w)
                .apply((i, v) -> (y.get(i) == 1) ? (1. / (1. + exp(-v))) : (1 - 1. / (1. + exp(-v))))
                .apply(this::cut)
                .apply(Math::log)
                .nansum() / x.rowCount();
    }

    private double cut(double value) {
        return Math.min(1 - 1e-12, value);
    }

    private DV iterate(DV w) {

        // Xw dot product
        DV xw = x.dot(w);

        // p diag = 1/(1+exp(-Xw))
        DV p = xw.copy().apply(value -> cut(1. / (1. + exp(-value))));

        // p(1-p) diag from p diag
        DV pvars = p.copy().apply(value -> value * (1 - value));

        // z = Wx - I(p(1-p))^{-1}(y-p)
        DV z = xw.add(y.copy().sub(p).div(pvars));

        // Xt(p(1-p)
        DM xpvar = x.copy();
        for (int i = 0; i < xpvar.rowCount(); i++) {
            double pvar = pvars.get(i);
            for (int j = 0; j < xpvar.colCount(); j++) {
                xpvar.set(i, j, xpvar.get(i, j) * pvar);
            }
        }

        // XI(p(1-p))^T * X
        DM mA = xpvar.t().dot(x);

        // for L2 regularization we inflate main diagonal
        if (lambda != 0) {
            for (int i = 0; i < mA.rowCount(); i++) {
                mA.inc(i, i, lambda);
            }
        }

        //XI(p(1-p))^T * X * z
        DM b = xpvar.t().dot(z.asMatrix());

        CholeskyDecomposition chol = CholeskyDecomposition.from(mA);
        if (chol.isSPD()) {
            // if we have a symmetric positive definite matrix we solve it with Cholesky
            return chol.solve(b).mapCol(0).copy();
        }
        // otherwise we fall in QR decomposition
        return QRDecomposition.from(mA).solve(b).mapCol(0).copy();
    }
}
