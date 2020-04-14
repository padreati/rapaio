package rapaio.ml.classifier.linear.binarylogistic;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.CholeskyDecomposition;
import rapaio.math.linear.dense.QRDecomposition;
import rapaio.math.linear.dense.SolidDMatrix;

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
    private int maxIter = 10;
    @Builder.Default
    private double lambda = 0.0;
    @NonNull
    private final DMatrix x;
    @NonNull
    private final DVector y;
    @NonNull
    private final DVector w0;

    @Builder
    @Getter
    @ToString
    public static class Result {

        @NonNull
        private final DVector w;

        @NonNull
        private final List<Double> nll;

        @NonNull
        private final List<DVector> ws;

        private final boolean converged;
    }

    public Result fit() {
        int it = 0;
        // current solution
        DVector w = w0.copy();
        ArrayList<DVector> ws = new ArrayList<>();
        ws.add(w);
        List<Double> nlls = new ArrayList<>();
        nlls.add(negativeLogLikelihood(x, y, w));

        while (it++ < maxIter) {
            DVector wnew = iterate(w);
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

        // compute delta weights first as (X^t I_{p(1-p)} X)^{-1} X^t (y-p)

        // Xw dot product
        DVector xw = x.dot(w);

        // p diag = 1/(1+exp(-Xw))
        DVector p = xw.copy().apply(value -> cut(1. / (1. + exp(-value))));

        // p(1-p) diag from p diag
        DVector pvars = p.copy().apply(value -> value * (1 - value));

        // Xt(p(1-p)
        DMatrix xpvar = x.copy();
        for (int i = 0; i < xpvar.rowCount(); i++) {
            double pvar = pvars.get(i);
            for (int j = 0; j < xpvar.colCount(); j++) {
                xpvar.set(i, j, xpvar.get(i, j) * pvar);
            }
        }

        // X^t * I(p(1-p))^T * X
        DMatrix mA = xpvar.t().dot(x);

        DMatrix invA;
        CholeskyDecomposition chol = CholeskyDecomposition.from(mA);
        if (chol.isSPD()) {
            invA = chol.solve(SolidDMatrix.identity(w.size()));
        } else {
            QRDecomposition qr = QRDecomposition.from(mA);
            invA = qr.solve(SolidDMatrix.identity(w.size()));
        }

        // z = Wx - I(p(1-p))^{-1}(y-p)
        DVector delta = invA.dot(x.t().dot(y.copy().minus(p)));


        // otherwise we fall in QR decomposition
        return delta.plus(w);
    }
}
