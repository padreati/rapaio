package rapaio.ml.classifier.linear.binarylogistic;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.CholeskyDecomposition;
import rapaio.math.linear.dense.QRDecomposition;

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
    private int maxIter = 10;
    @Builder.Default
    private double lambda = 0.0;
    @NonNull
    private final DMatrix x;
    @NonNull
    private final DVector y;
    @NonNull
    private final DVector w0;

    @Builder(setterPrefix = "with")
    @Getter
    @ToString
    public static class Result {

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

        @NonNull
        private final List<Double> nlls;

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
        DVector xw = x.dot(w);

        // p diag = 1/(1+exp(-Xw))
        DVector p = xw.copy().apply(value -> cut(1. / (1. + exp(-value))));

        // p(1-p) diag from p diag
        DVector pvars = p.copy().apply(value -> value * (1 - value));

        // z = Wx - I(p(1-p))^{-1}(y-p)
        DVector z = xw.plus(y.copy().minus(p).div(pvars));

        // Xt(p(1-p)
        DMatrix xpvar = x.copy();
        for (int i = 0; i < xpvar.rowCount(); i++) {
            double pvar = pvars.get(i);
            for (int j = 0; j < xpvar.colCount(); j++) {
                xpvar.set(i, j, xpvar.get(i, j) * pvar);
            }
        }

        // XI(p(1-p))^T * X
        DMatrix mA = xpvar.t().dot(x);

        // for L2 regularization we inflate main diagonal
        if (lambda != 0) {
            for (int i = 0; i < mA.rowCount(); i++) {
                mA.increment(i, i, lambda);
            }
        }

        //XI(p(1-p))^T * X * z
        DMatrix b = xpvar.t().dot(z.asMatrix());

        CholeskyDecomposition chol = CholeskyDecomposition.from(mA);
        if (chol.isSPD()) {
            // if we have a symmetric positive definite matrix we solve it with Cholesky
            return chol.solve(b).mapCol(0).copy();
        }
        // otherwise we fall in QR decomposition
        return QRDecomposition.from(mA).solve(b).mapCol(0).copy();
    }
}
