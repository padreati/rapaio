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
import rapaio.math.linear.dense.DMStripe;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.exp;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/21/20.
 */
@Builder(setterPrefix = "with")
@Getter
public class BinaryLogisticNewton {

    @Builder.Default
    private final double eps = 1e-100;
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

    @Builder
    @Getter
    @ToString
    public static class Result {

        @NonNull
        private final DV w;

        @NonNull
        private final List<Double> nll;

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
            if (it > 1 && (Math.abs(nll_delta) <= eps /*|| nll_delta > 0*/)) {
                return Result.builder()
                        .w(w)
                        .converged(true)
                        .nll(nlls)
                        .ws(ws)
                        .build();
            }
            ws.add(wnew);
            nlls.add(nll);
            w = wnew;
        }
        return Result.builder()
                .ws(ws)
                .nll(nlls)
                .converged(false)
                .w(w)
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

        // compute delta weights first as (X^t I_{p(1-p)} X)^{-1} X^t (y-p)

        // Xw dot product
        DV xw = x.dot(w);

        // p diag = 1/(1+exp(-Xw))
        DV p = xw.copy().apply(value -> cut(1. / (1. + exp(-value))));

        // p(1-p) diag from p diag
        DV pvars = p.copy().apply(value -> value * (1 - value));

        // Xt(p(1-p)
        DM xpvar = x.copy();
        for (int i = 0; i < xpvar.rowCount(); i++) {
            double pvar = pvars.get(i);
            for (int j = 0; j < xpvar.colCount(); j++) {
                xpvar.set(i, j, xpvar.get(i, j) * pvar);
            }
        }

        // X^t * I(p(1-p))^T * X
        DM mA = xpvar.t().dot(x);

        DM invA;
        CholeskyDecomposition chol = CholeskyDecomposition.from(mA);
        if (chol.isSPD()) {
            invA = chol.solve(DMStripe.identity(w.size()));
        } else {
            QRDecomposition qr = QRDecomposition.from(mA);
            invA = qr.solve(rapaio.math.linear.dense.DMStripe.identity(w.size()));
        }

        // z = Wx - I(p(1-p))^{-1}(y-p)
        DV delta = invA.dot(x.t().dot(y.copy().sub(p)));


        // otherwise we fall in QR decomposition
        return delta.add(w);
    }
}
