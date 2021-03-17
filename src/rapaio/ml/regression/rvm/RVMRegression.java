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

package rapaio.ml.regression.rvm;

import lombok.Getter;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.SOrder;
import rapaio.math.linear.decomposition.LUDecomposition;
import rapaio.math.linear.dense.DMatrixStripe;
import rapaio.math.linear.dense.DVectorDense;
import rapaio.ml.classifier.svm.kernel.Kernel;
import rapaio.ml.classifier.svm.kernel.LinearKernel;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.ml.regression.AbstractRegressionModel;
import rapaio.ml.regression.RegressionResult;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.NotImplementedException;
import rapaio.util.collection.IntArrays;

import java.util.List;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/13/21.
 */
public class RVMRegression extends AbstractRegressionModel<RVMRegression, RegressionResult> {

    public static RVMRegression newModel() {
        return new RVMRegression();
    }

    public enum Method {
        EVIDENCE_APPROXIMATION,
        FAST_TIPPING
    }

    private static final long serialVersionUID = 9165148257709665706L;

    public ValueParam<Kernel, RVMRegression> kernel = new ValueParam<>(this, new LinearKernel(),
            "kernel", "Kernel function used to build the design matrix.");

    public ValueParam<Boolean, RVMRegression> intercept = new ValueParam<>(this, true,
            "intercept", "If we add an intercept to features or not.");

    public ValueParam<Method, RVMRegression> method = new ValueParam<>(this, Method.EVIDENCE_APPROXIMATION,
            "method", "Method used to fit the model.");

    public ValueParam<Double, RVMRegression> fitThreshold = new ValueParam<>(this, 1e-9,
            "fitThreshold", "Fit threshold used in convergence criteria.");

    public ValueParam<Double, RVMRegression> alphaThreshold = new ValueParam<>(this, 1e6,
            "aalphaThreshold", "Fit threshold for setting an alpha weight's prior to infinity.");

    public ValueParam<Integer, RVMRegression> maxIter = new ValueParam<>(this, 1000,
            "maxIter", "Max number of iterations");

    // solution
    @Getter
    private int[] indexes;
    private DVector m;
    private DMatrix sigma;
    private DMatrix x;
    private double beta;
    private boolean converged;
    private int iterations;

    private RVMRegression() {
    }

    /**
     * Creates a new regression instance with the same parameters as the original.
     * The fitted model and other artifacts are not replicated.
     *
     * @return new parametrized instance
     */
    @Override
    public RVMRegression newInstance() {
        return new RVMRegression().copyParameterValues(this);
    }

    /**
     * @return regression model name
     */
    @Override
    public String name() {
        return "RVMRegression";
    }

    /**
     * Describes the learning algorithm
     *
     * @return capabilities of the learning algorithm
     */
    @Override
    public Capabilities capabilities() {
        return Capabilities.builder()
                .allowMissingInputValues(false)
                .allowMissingTargetValues(false)
                .inputTypes(List.of(VType.DOUBLE, VType.INT, VType.BINARY))
                .targetType(VType.DOUBLE)
                .maxInputCount(Integer.MAX_VALUE)
                .minInputCount(1)
                .maxTargetCount(1)
                .minTargetCount(1)
                .build();
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        switch (method.get()) {
            case EVIDENCE_APPROXIMATION:
                return new EvidenceApproximation(this, df).fit();
            case FAST_TIPPING:
            default:
                throw new NotImplementedException("The method selected for fitting RVMRegresion is not implemented.");
        }
    }

    private DMatrix buildFeatures(Frame df) {
        int inputLen = inputNames.length;
        int offset = (intercept.get() ? 1 : 0);
        DMatrix m = DMatrixStripe.empty(SOrder.R, df.rowCount(), inputLen + offset);
        for (int i = 0; i < df.rowCount(); i++) {
            if (intercept.get()) {
                m.set(i, 0, 1.0);
            }
            for (int j = 0; j < inputNames.length; j++) {
                m.set(i, j + offset, df.getDouble(i, inputNames[j]));
            }
        }
        return m;
    }

    @Override
    protected RegressionResult corePredict(Frame df, boolean withResiduals, final double[] quantiles) {
        DMatrix feat = buildFeatures(df);
        RegressionResult prediction = RegressionResult.build(this, df, withResiduals, quantiles);
        for (int i = 0; i < df.rowCount(); i++) {
            double pred = 0;
            for (int j = 0; j < m.size(); j++) {
                pred += kernel.get().compute(feat.mapRow(i), x.mapRow(j)) * m.get(j);
            }
            prediction.prediction(firstTargetName()).setDouble(i, pred);
            if (quantiles != null && quantiles.length > 0) {
                DVector phii = DVectorDense.zeros(m.size());
                for (int j = 0; j < m.size(); j++) {
                    phii.set(j, kernel.get().compute(feat.mapRow(i), x.mapRow(j)));
                }
                double variance = 1.0 / beta + phii.asMatrix().t().dot(sigma).dot(phii).get(0);
                Normal normal = Normal.of(pred, Math.sqrt(variance));
                for (int j = 0; j < quantiles.length; j++) {
                    double q = normal.quantile(quantiles[j]);
                    prediction.firstPredictionQuantiles()[j].setDouble(i, q);
                }
            }
        }
        prediction.buildComplete();
        return prediction;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb = sb.append(fullName());
        sb.append("; fitted=").append(hasLearned);
        if (hasLearned) {
            sb.append(", rvm count=").append(indexes.length);
        }
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(headerSummary());
        sb.append("\n");

        if (!hasLearned) {
            return sb.toString();
        }

        sb.append("> relevance vectors count: ").append(indexes.length).append("\n");
        sb.append("> convengence: ").append(converged).append(", iterations: ").append(iterations).append("\n");

        sb.append("> mean: \n");
        sb.append(m.toFullContent(options));
        sb.append("> beta: ").append(Format.floatFlex(beta)).append("\n");

        sb.append("> sigma: \n");
        sb.append(sigma.toContent(options));

        sb.append("> x: \n");
        sb.append(x.toContent(options));

        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }

    private static final class EvidenceApproximation {

        private final RVMRegression parent;
        private final Frame df;

        public DMatrix x;
        public DMatrix phi;
        public DMatrix phiTphi;
        public DVector phiTy;
        public DVector y;

        int[] indexes;

        public DVector m;
        public DMatrix sigma;

        public DVector alpha;
        public double beta;

        public EvidenceApproximation(RVMRegression parent, Frame df) {
            this.parent = parent;
            this.df = df;
        }

        public boolean fit() {
            x = buildX(df);
            phi = buildPhi(df);
            indexes = IntArrays.newSeq(0, phi.colCount());
            phiTphi = phi.t().dot(phi);
            y = buildTarget(df);
            phiTy = phi.t().dot(y);

            initializeAlpaBeta();

            for (int it = 1; it <= parent.maxIter.get(); it++) {

                // compute m and sigma
                DMatrix t = phiTphi.copy().mult(beta);
                for (int i = 0; i < t.rowCount(); i++) {
                    t.inc(i, i, alpha.get(i));
                }

                sigma = LUDecomposition.from(t).solve(DMatrixStripe.identity(t.rowCount()));
                m = sigma.dot(phiTy).mult(beta);

                // compute alpha and beta

                DVector gamma = DVectorDense.from(m.size(), i -> 1 - alpha.get(i) * sigma.get(i, i));

                DVector oldAlpha = alpha.copy();

                // update alpha
                for (int i = 0; i < alpha.size(); i++) {
                    alpha.set(i, gamma.get(i) / (m.get(i) * m.get(i)));
                }
                // update sigma
                beta = (alpha.size() - gamma.sum()) / Math.pow(phi.dot(m).sub(y).norm(2), 2);

                pruneAlphas();

                if (testConvergence(oldAlpha, alpha)) {
                    updateResults(parent, true, it);
                    return true;
                }
            }
            updateResults(parent, false, parent.maxIter.get());
            return true;
        }

        private void updateResults(RVMRegression parent, boolean convergent, int iterations) {
            this.parent.indexes = indexes;
            this.parent.m = m.copy();
            this.parent.sigma = sigma.copy();
            this.parent.x = x.mapRows(indexes);
            this.parent.beta = beta;
            this.parent.converged = convergent;
            this.parent.iterations = iterations;
        }

        private boolean testConvergence(DVector oldAlpha, DVector alpha) {
            double total = 0;
            for (int i = 0; i < alpha.size(); i++) {
                total += Math.abs(oldAlpha.get(i) - alpha.get(i));
            }
            return total < parent.fitThreshold.get();
        }

        private void pruneAlphas() {

            // select relevant vectors

            boolean[] pruningFlag = new boolean[indexes.length];
            int pruningCount = 0;
            for (int i = 0; i < pruningFlag.length; i++) {
                if (alpha.get(i) > parent.alphaThreshold.get()) {
                    // we assume it goes to infinity, thus we prune the entry
                    pruningFlag[i] = true;
                    pruningCount++;
                }
            }

            if (pruningCount == 0) {
                // if there are no vectors to eliminate
                return;
            }

            if (pruningCount == pruningFlag.length) {
                // something went bad, we can't eliminate all of them
                throw new RuntimeException("All vectors pruned.");
            }

            // recreate the index
            int[] newIndex = new int[indexes.length - pruningCount];
            int[] keep = new int[indexes.length - pruningCount];
            int pos = 0;
            for (int i = 0; i < indexes.length; i++) {
                if (!pruningFlag[i]) {
                    newIndex[pos] = indexes[i];
                    keep[pos] = i;
                    pos++;
                }
            }
            indexes = newIndex;

            phi = phi.mapCols(keep).mapRows(keep).copy();
            phiTphi = phiTphi.mapCols(keep).mapRows(keep).copy();
            phiTy = phiTy.mapCopy(keep);

            y = y.mapCopy(keep);
            m = m.mapCopy(keep);
            sigma = sigma.mapCols(keep).mapRows(keep).copy();
            alpha = alpha.mapCopy(keep);
        }

        private DMatrix buildPhi(Frame df) {
            int n = x.rowCount();
            DMatrix m = DMatrixStripe.empty(SOrder.R, n, n);
            Kernel k = parent.kernel.get();
            for (int i = 0; i < n; i++) {
                m.set(i, i, k.compute(x.mapRow(i), x.mapRow(i)));
                for (int j = i + 1; j < n; j++) {
                    double value = k.compute(x.mapRow(i), x.mapRow(j));
                    m.set(i, j, value);
                    m.set(j, i, value);
                }
            }
            return m;
        }

        private DMatrix buildX(Frame df) {
            int inputLen = parent.inputNames.length;
            int offset = (parent.intercept.get() ? 1 : 0);
            DMatrix m = DMatrixStripe.empty(SOrder.R, df.rowCount(), inputLen + offset);
            for (int i = 0; i < df.rowCount(); i++) {
                if (parent.intercept.get()) {
                    m.set(i, 0, 1.0);
                }
                for (int j = 0; j < inputLen; j++) {
                    m.set(i, j + offset, df.getDouble(i, parent.inputNames[j]));
                }
            }
            return m;
        }

        private DVector buildTarget(Frame df) {
            DVector t = DVectorDense.zeros(df.rowCount());
            int index = df.varIndex(parent.targetNames[0]);
            for (int i = 0; i < df.rowCount(); i++) {
                t.set(i, df.getDouble(i, index));
            }
            return t;
        }

        private void initializeAlpaBeta() {
            beta = 1.0 / (y.variance() * 0.1);
            alpha = DVectorDense.from(phi.colCount(), row -> RandomSource.nextDouble() / 10);
        }
    }

}
