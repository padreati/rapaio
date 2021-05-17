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

import rapaio.core.RandomSource;
import rapaio.core.correlation.CorrPearson;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarType;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.MType;
import rapaio.math.linear.decomposition.CholeskyDecomposition;
import rapaio.math.linear.decomposition.LUDecomposition;
import rapaio.math.linear.decomposition.QRDecomposition;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.ml.common.kernel.Kernel;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.ml.regression.AbstractRegressionModel;
import rapaio.ml.regression.RegressionResult;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.NotImplementedException;
import rapaio.util.collection.IntArrays;

import java.io.Serial;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/13/21.
 */
public class RVMRegression extends AbstractRegressionModel<RVMRegression, RegressionResult> {

    public static RVMRegression newModel() {
        return new RVMRegression();
    }

    public enum Method {
        EVIDENCE_APPROXIMATION,
        FAST_TIPPING,
        EXPERIMENT_ADAPTIVE_RBF,
        EXPERIMENT_FAST_TRUNCATED
    }

    @Serial
    private static final long serialVersionUID = 9165148257709665706L;

    public ValueParam<Kernel, RVMRegression> kernel = new ValueParam<>(this, new RBFKernel(1),
            "kernel", "Kernel function used to build the design matrix.");

    public ValueParam<Boolean, RVMRegression> intercept = new ValueParam<>(this, true,
            "intercept", "If we add an intercept to features or not.");

    public ValueParam<Method, RVMRegression> method = new ValueParam<>(this, Method.EVIDENCE_APPROXIMATION,
            "method", "Method used to fit the model.");

    public ValueParam<Double, RVMRegression> fitThreshold = new ValueParam<>(this, Math.exp(10e-10),
            "fitThreshold", "Fit threshold used in convergence criteria.");

    public ValueParam<Double, RVMRegression> alphaThreshold = new ValueParam<>(this, 1e6,
            "alphaThreshold", "Fit threshold for setting an alpha weight's prior to infinity.");

    public ValueParam<Integer, RVMRegression> maxIter = new ValueParam<>(this, 1000,
            "maxIter", "Max number of iterations");

    private int[] indexes;

    private DVector m;

    /**
     * Fitted covariance matrix.
     */
    private DMatrix sigma;
    private DMatrix x;
    private DVector alpha;
    private DVector rbfBeta;
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

    public int[] getIndexes() {
        return indexes;
    }

    public DVector getM() {
        return m;
    }

    public DMatrix getSigma() {
        return sigma;
    }

    public DVector getRbfBeta() {
        return rbfBeta;
    }

    public double getBeta() {
        return beta;
    }

    public int getIterations() {
        return iterations;
    }

    /**
     * Describes the learning algorithm
     *
     * @return capabilities of the learning algorithm
     */
    @Override
    public Capabilities capabilities() {
        return new Capabilities(
                1, Integer.MAX_VALUE, List.of(VarType.DOUBLE, VarType.INT, VarType.BINARY), false,
                1, 1, List.of(VarType.DOUBLE), false);
    }

    /**
     * @return Numbers of fitted relevant vectors count, -1 if model is not fitted.
     */
    public int getVectorCount() {
        return hasLearned ? indexes.length : -1;
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        return switch (method.get()) {
            case EVIDENCE_APPROXIMATION -> new EvidenceApproximation(this, df).fit();
            case FAST_TIPPING -> new FastTipping(this, df).fit();
            case EXPERIMENT_ADAPTIVE_RBF -> new AdaptiveRBF(this, df).fit();
            case EXPERIMENT_FAST_TRUNCATED -> new FastTruncated(this, df).fit();
            default -> throw new NotImplementedException("The method selected for fitting RVMRegression is not implemented.");
        };
    }

    /**
     * Builds features from the data frame as a matrix with observations on rows
     * and features on columns. An optional intercept is added as the first columns,
     * depending on the value of the parameter {@link #intercept}.
     *
     * @param df source data frame
     * @return matrix of features
     */
    private DMatrix buildFeatures(Frame df) {
        int inputLen = inputNames.length;
        int offset = (intercept.get() ? 1 : 0);
        DMatrix m = DMatrix.empty(MType.RDENSE, df.rowCount(), inputLen + offset);
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
            if (method.get() != Method.EXPERIMENT_ADAPTIVE_RBF) {
                for (int j = 0; j < m.size(); j++) {
                    pred += kernel.get().compute(feat.mapRow(i), x.mapRow(j)) * m.get(j);
                }
            } else {
                for (int j = 0; j < m.size(); j++) {
                    pred += AdaptiveRBF.rbf(rbfBeta.get(j), feat.mapRow(i), x.mapRow(j)) * m.get(j);
                }
            }
            prediction.prediction(firstTargetName()).setDouble(i, pred);
            if (quantiles != null && quantiles.length > 0) {
                DVector phi_m = DVector.zeros(m.size());
                if (method.get() != Method.EXPERIMENT_ADAPTIVE_RBF) {
                    for (int j = 0; j < m.size(); j++) {
                        phi_m.set(j, kernel.get().compute(feat.mapRow(i), x.mapRow(j)));
                    }
                } else {
                    for (int j = 0; j < m.size(); j++) {
                        phi_m.set(j, AdaptiveRBF.rbf(rbfBeta.get(j), feat.mapRow(i), x.mapRow(j)));
                    }
                }
                double variance = 1.0 / beta + phi_m.asMatrix().t().dot(sigma).dot(phi_m).get(0);
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
        sb.append(fullName());
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

        sb.append("> relevant vectors count: ").append(indexes.length).append("\n");
        sb.append("> relevant vector indexes: [")
                .append(IntArrays.stream(indexes, 0, indexes.length).mapToObj(String::valueOf).collect(Collectors.joining(",")))
                .append("]");
        sb.append("> convergence: ").append(converged).append(", iterations: ").append(iterations).append("\n");

        sb.append("> mean: \n");
        sb.append(m.toFullContent(options));
        sb.append(">alphas: \n");
        sb.append(alpha.toFullContent(options));
        sb.append("> beta: ").append(Format.floatFlex(beta)).append("\n");

        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(toSummary(printer, options));

        sb.append("> sigma: \n");
        sb.append(sigma.toContent(options));

        sb.append("> x: \n");
        sb.append(x.toContent(options));

        return sb.toString();
    }

    private static abstract class BaseAlgorithm {

        protected final RVMRegression parent;
        protected final Frame df;

        public int[] indexes;
        public DMatrix x;
        public DMatrix phi;
        public DVector y;
        public DVector m;
        public DMatrix sigma;

        public DVector alpha;
        public double beta;

        protected BaseAlgorithm(RVMRegression parent, Frame df) {
            this.parent = parent;
            this.df = df;
        }

        protected DMatrix buildPhi() {
            int n = x.rowCount();
            DMatrix m = DMatrix.empty(MType.CDENSE, n, n);
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

        protected DVector buildTarget(Frame df) {
            DVector t = DVector.zeros(df.rowCount());
            int index = df.varIndex(parent.targetNames[0]);
            for (int i = 0; i < df.rowCount(); i++) {
                t.set(i, df.getDouble(i, index));
            }
            return t;
        }
    }

    private static final class EvidenceApproximation extends BaseAlgorithm {

        public EvidenceApproximation(RVMRegression parent, Frame df) {
            super(parent, df);
        }

        public DMatrix phi_t_phi;
        public DVector phi_t_y;

        public boolean fit() {
            x = parent.buildFeatures(df);
            phi = buildPhi();
            y = buildTarget(df);
            indexes = IntArrays.newSeq(0, phi.colCount());
            phi_t_phi = phi.t().dot(phi);
            phi_t_y = phi.t().dot(y);

            initializeAlphaBeta();

            for (int it = 1; it <= parent.maxIter.get(); it++) {

                // compute m and sigma
                DMatrix t = phi_t_phi.copy().mult(beta);
                for (int i = 0; i < t.rowCount(); i++) {
                    t.inc(i, i, alpha.get(i));
                }

                try {
                    sigma = LUDecomposition.from(t).solve(DMatrix.identity(t.rowCount()));
                } catch (IllegalArgumentException ignored) {
                    sigma = QRDecomposition.from(t).solve(DMatrix.identity(t.rowCount()));
                }
                m = sigma.dot(phi_t_y).mult(beta);

                // compute alpha and beta

                DVector gamma = DVector.from(m.size(), i -> 1 - alpha.get(i) * sigma.get(i, i));

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
            phi_t_phi = phi_t_phi.mapCols(keep).mapRows(keep).copy();
            phi_t_y = phi_t_y.mapCopy(keep);

            y = y.mapCopy(keep);
            m = m.mapCopy(keep);
            sigma = sigma.mapCols(keep).mapRows(keep).copy();
            alpha = alpha.mapCopy(keep);
        }

        private void initializeAlphaBeta() {
            beta = 1.0 / (y.variance() * 0.1);
            alpha = DVector.from(phi.colCount(), row -> RandomSource.nextDouble() / 10);
        }

        protected void updateResults(RVMRegression parent, boolean convergent, int iterations) {
            this.parent.indexes = indexes;
            this.parent.m = m.copy();
            this.parent.sigma = sigma.copy();
            this.parent.x = x.mapRows(indexes);
            this.parent.alpha = alpha.copy();
            this.parent.beta = beta;
            this.parent.converged = convergent;
            this.parent.iterations = iterations;
        }
    }

    private static final class FastTipping extends BaseAlgorithm {

        private int n;
        private double[] ss;
        private double[] qq;
        private double[] s;
        private double[] q;

        private DMatrix phi_hat;
        private DVector phi_dot_y;

        public FastTipping(RVMRegression parent, Frame df) {
            super(parent, df);
        }

        public boolean fit() {
            x = parent.buildFeatures(df);
            phi = buildPhi();
            y = buildTarget(df);
            phi_hat = phi.t().dot(phi);
            phi_dot_y = phi.t().dot(y);

            n = phi.rowCount();
            ss = new double[n];
            qq = new double[n];
            s = new double[n];
            q = new double[n];

            initialize();
            computeSigmaAndMu();
            computeSQ();
            computeBeta();

            DVector old_alpha = alpha.copy();
            for (int it = 1; it <= parent.maxIter.get(); it++) {

                updateBestVector();
                computeSigmaAndMu();
                computeSQ();
                computeBeta();

                if (testConvergence(old_alpha)) {
                    updateResults(parent, true, it);
                    return true;
                }
                old_alpha = alpha.copy();
            }

            updateResults(parent, false, parent.maxIter.get());
            return true;
        }

        private void updateBestVector() {

            double[] theta = new double[n];
            double[] llDelta = new double[n];

            for (int i = 0; i < n; i++) {
                theta[i] = q[i] * q[i] - s[i];
                if (theta[i] > 0) {
                    if (Double.isInfinite(alpha.get(i))) {
                        llDelta[i] = (qq[i] * qq[i] - ss[i]) / ss[i] + Math.log(ss[i] / (qq[i] * qq[i]));
                    } else {
                        double alpha_new = s[i] * s[i] / theta[i];
                        double delta_alpha = 1. / alpha_new - 1.0 / alpha.get(i);
                        llDelta[i] = (qq[i] * qq[i]) / (ss[i] + 1. / delta_alpha) - Math.log1p(ss[i] * delta_alpha);
                    }
                } else {
                    if (Double.isFinite(alpha.get(i))) {
                        llDelta[i] = qq[i] * qq[i] / (ss[i] - alpha.get(i)) - Math.log(1 - ss[i] / alpha.get(i));
                    }
                }
            }

            int i = 0;
            for (int j = 1; j < n; j++) {
                if (llDelta[j] > llDelta[i]) {
                    i = j;
                }
            }
            double alpha_i = alpha.get(i);

            if (theta[i] > 0) {
                if (Double.isInfinite(alpha_i)) {
                    // add alpha_i to model
                    alpha.set(i, s[i] * s[i] / theta[i]);
                    indexes = addIndex(indexes, i);
                } else {
                    // alpha is in set, re-estimate alpha
                    alpha.set(i, s[i] * s[i] / theta[i]);
                }
            } else {
                if (Double.isFinite(alpha_i) && indexes.length > 1) {
                    alpha.set(i, Double.POSITIVE_INFINITY);
                    indexes = removeIndex(indexes, i);
                }
            }

        }

        private boolean testConvergence(DVector old_alpha) {
            /*
                In step 11, we must judge if we have attained a local maximum of the marginal likelihood. We
                terminate when the changes in log α in Step 6 for all basis functions in the model are smaller than
                10 −6 and all other θ i ≤ 0.
             */
            double delta = 0.0;
            for (int i = 0; i < alpha.size(); i++) {
                double new_value = alpha.get(i);
                double old_value = old_alpha.get(i);
                if (Double.isInfinite(new_value) && Double.isInfinite(old_value)) {
                    continue;
                }
                if (Double.isInfinite(new_value) || Double.isInfinite(old_value)) {
                    return false;
                }
                delta += Math.abs(old_value - new_value);
            }
            return delta < parent.fitThreshold.get();
        }

        private int[] addIndex(int[] original, int i) {
            int[] copy = new int[indexes.length + 1];
            System.arraycopy(indexes, 0, copy, 0, indexes.length);
            copy[indexes.length] = i;
            return copy;
        }

        private int[] removeIndex(int[] original, int j) {
            int[] copy = new int[original.length - 1];
            int pos = 0;
            for (int k : original) {
                if (k != j) {
                    copy[pos++] = k;
                }
            }
            return copy;
        }

        private void initialize() {
            beta = 1.0 / (y.variance() * 0.1);
            alpha = DVector.fill(phi.rowCount(), Double.POSITIVE_INFINITY);

            // select one alpha

            int best_index = 0;
            double best_projection = phi_dot_y.get(0) / phi_hat.get(0, 0);

            for (int i = 1; i < phi.colCount(); i++) {
                double projection = phi_dot_y.get(i) / phi_hat.get(i, i);
                if (projection >= best_projection) {
                    best_projection = projection;
                    best_index = i;
                }
            }
            indexes = new int[]{best_index};

            alpha.set(best_index, phi_hat.get(best_index, best_index) / (best_projection - 1.0 / beta));
        }

        private void computeSigmaAndMu() {

            DMatrix m_sigma_inv = phi_hat.mapRows(indexes).mapCols(indexes).copy().mult(beta);
            for (int i = 0; i < indexes.length; i++) {
                m_sigma_inv.inc(i, i, alpha.get(indexes[i]));
            }
            sigma = QRDecomposition.from(m_sigma_inv).solve(DMatrix.identity(indexes.length));
            m = sigma.dot(phi_dot_y.mapCopy(indexes)).mult(beta);
        }

        void computeSQ() {
            for (int i = 0; i < n; i++) {
                DVector left = phi_hat.mapRow(i).mapCopy(indexes);
                DVector right = phi_dot_y.mapCopy(indexes);
                ss[i] = beta * phi_hat.get(i, i) - beta * beta * sigma.dot(left).dot(left);
                qq[i] = beta * phi_dot_y.get(i) - beta * beta * sigma.dot(left).dot(right);
            }

            for (int i = 0; i < n; i++) {
                double alpha_i = alpha.get(i);
                if (Double.isInfinite(alpha_i)) {
                    s[i] = ss[i];
                    q[i] = qq[i];
                } else {
                    s[i] = alpha_i * ss[i] / (alpha_i - ss[i]);
                    q[i] = alpha_i * qq[i] / (alpha_i - ss[i]);
                }
            }
        }

        private void computeBeta() {
            DVector gamma = DVector.from(m.size(), i -> 1 - alpha.get(indexes[i]) * sigma.get(i, i));
            DMatrix pruned_phi = phi.mapCols(indexes);
            DVector delta = pruned_phi.dot(m).sub(y);
            beta = (n - gamma.sum()) / delta.dot(delta);
        }

        protected void updateResults(RVMRegression parent, boolean convergent, int iterations) {
            this.parent.indexes = indexes;
            this.parent.m = m.copy();
            this.parent.sigma = sigma.copy();
            this.parent.x = x.mapRows(indexes);
            this.parent.alpha = alpha.mapCopy(indexes);
            this.parent.beta = beta;
            this.parent.converged = convergent;
            this.parent.iterations = iterations;
        }
    }

    private static final class AdaptiveRBF extends BaseAlgorithm {

        private static final double ksigma_init = 2;
        private static final double rbfBetaDelta = 1e-3;

        private static final double min_delta_threshold = 1e-100;
        private static DVector rbfBeta;

        public static double rbf(double beta, DVector u, DVector v) {
            double sum = 0.0;
            for (int i = 0; i < u.size(); i++) {
                double delta = u.get(i) - v.get(i);
                sum += delta * delta;
            }
            return Math.exp(beta * sum);
        }

        private int n;
        private double[] ss;
        private double[] qq;
        private double[] s;
        private double[] q;

        private DMatrix phi_hat;

        public AdaptiveRBF(RVMRegression parent, Frame df) {
            super(parent, df);
        }

        public boolean fit() {
            x = parent.buildFeatures(df);
            n = x.rowCount();
            rbfBeta = DVector.fill(n, -1.0 / (2.0 * ksigma_init * ksigma_init));

            phi = buildPhi();
            y = buildTarget(df);
            phi_hat = phi.t().dot(phi);

            ss = new double[n];
            qq = new double[n];
            s = new double[n];
            q = new double[n];

            initialize();
            computeSigmaAndMu();
            computeSQ();
            computeBeta();

            DVector old_alpha = alpha.copy();
            for (int it = 1; it <= parent.maxIter.get(); it++) {

                updateBestVector();
                computeSigmaAndMu();
                computeSQ();
                computeBeta();

                updateBestRbfBeta();

                if (testConvergence(old_alpha)) {
                    updateResults(parent, true, it);
                    return true;
                }
                old_alpha = alpha.copy();
            }

            updateResults(parent, false, parent.maxIter.get());
            return true;
        }

        protected DMatrix buildPhi() {
            int n = x.rowCount();
            return DMatrix.fill(MType.CDENSE, n, n, (i, j) -> rbf(rbfBeta.get(j), x.mapRow(i), x.mapRow(j)));
        }

        private void updateBestVector() {

            double[] theta = new double[n];
            double[] llDelta = new double[n];

            for (int i = 0; i < n; i++) {
                theta[i] = q[i] * q[i] - s[i];
                if (theta[i] > 0) {
                    if (Double.isInfinite(alpha.get(i))) {
                        llDelta[i] = (qq[i] * qq[i] - ss[i]) / ss[i] + Math.log(ss[i] / (qq[i] * qq[i]));
                    } else {
                        double alpha_new = s[i] * s[i] / theta[i];
                        double delta_alpha = 1. / alpha_new - 1.0 / alpha.get(i);
                        llDelta[i] = (qq[i] * qq[i]) / (ss[i] + 1. / delta_alpha) - Math.log1p(ss[i] * delta_alpha);
                    }
                } else {
                    if (Double.isFinite(alpha.get(i))) {
                        llDelta[i] = qq[i] * qq[i] / (ss[i] - alpha.get(i)) - Math.log(1 - ss[i] / alpha.get(i));
                    }
                }
            }

            int i = 0;
            for (int j = 1; j < n; j++) {
                if (llDelta[j] > llDelta[i]) {
                    i = j;
                }
            }
            double alpha_i = alpha.get(i);

            if (theta[i] > 0) {
                if (Double.isInfinite(alpha_i)) {
                    // add alpha_i to model
                    alpha.set(i, s[i] * s[i] / theta[i]);
                    indexes = addIndex(indexes, i);
                } else {
                    // alpha is in set, re-estimate alpha
                    alpha.set(i, s[i] * s[i] / theta[i]);
                }
            } else {
                if (Double.isFinite(alpha_i) && indexes.length > 1) {
                    alpha.set(i, Double.POSITIVE_INFINITY);
                    indexes = removeIndex(indexes, i);
                }
            }
        }

        private void updateBestRbfBeta() {

            for (int t = 0; t < 1000; t++) {


                double[] ll_current = new double[indexes.length];
                double[] ll_delta_plus = new double[indexes.length];
                double[] ll_delta_minus = new double[indexes.length];

                DMatrix phi_active = phi.mapCols(indexes).t();
                DVector right = phi_active.dot(y);

                double delta = rbfBetaDelta;
                for (int i = 0; i < indexes.length; i++) {
                    final int ii = i;
                    DVector phi_m = phi.mapCol(indexes[i]);
                    ll_current[i] = compute_ll(phi_active, right, indexes[i], phi_m);

                    DVector phi_m_plus = DVector.from(n, j -> rbf(rbfBeta.get(indexes[ii]) + delta, x.mapRow(indexes[ii]), x.mapRow(j)));
                    ll_delta_plus[i] = compute_ll(phi_active, right, indexes[i], phi_m_plus);
                    if (!Double.isFinite(ll_delta_plus[i]) || rbfBeta.get(indexes[ii]) + delta >= 0) {
                        ll_delta_plus[i] = 0;
                    }

                    DVector phi_m_minus = DVector.from(n, j -> rbf(rbfBeta.get(indexes[ii]) - delta, x.mapRow(indexes[ii]), x.mapRow(j)));
                    ll_delta_minus[i] = compute_ll(phi_active, right, indexes[i], phi_m_minus);
                    if (!Double.isFinite(ll_delta_minus[i]) || rbfBeta.get(indexes[ii]) - delta >= 0) {
                        ll_delta_minus[i] = 0;
                    }
                }

                int best_plus = 0;
                double best_delta_plus = ll_delta_plus[0] - ll_current[0];
                int best_minus = 0;
                double best_delta_minus = ll_delta_minus[0] - ll_current[0];
                for (int i = 1; i < indexes.length; i++) {
                    if (ll_delta_plus[i] - ll_current[i] > best_delta_plus) {
                        best_delta_plus = ll_delta_plus[i] - ll_current[i];
                        best_plus = i;
                    }
                    if (ll_delta_minus[i] - ll_current[i] > best_delta_minus) {
                        best_delta_minus = ll_delta_minus[i] - ll_current[i];
                        best_minus = i;
                    }
                }

                if (best_delta_plus > best_delta_minus) {
                    if (best_delta_plus > min_delta_threshold) {
                        System.out.println("index " + indexes[best_plus] + " +");
                        int ii = best_plus;
                        DVector phi_m_plus = DVector.from(n, j -> rbf(rbfBeta.get(indexes[ii]) + delta, x.mapRow(indexes[ii]), x.mapRow(j)));
                        for (int i = 0; i < n; i++) {
                            phi.set(i, indexes[ii], phi_m_plus.get(i));
                        }
                        rbfBeta.inc(indexes[ii], delta);
                    } else {
                        int ii = best_minus;
                        System.out.println("index " + indexes[best_minus] + " -");
                        DVector phi_m_minus = DVector.from(n, j -> rbf(rbfBeta.get(indexes[ii]) - delta, x.mapRow(indexes[ii]), x.mapRow(j)));
                        for (int i = 0; i < n; i++) {
                            phi.set(i, indexes[ii], phi_m_minus.get(i));
                        }
                        rbfBeta.inc(indexes[ii], -delta);
                    }
                    phi_hat = phi.t().dot(phi);

                    computeSigmaAndMu();
                    computeSQ();
                    computeBeta();
                }
            }

        }

        private double compute_ll(DMatrix phi_active, DVector right, int index, DVector phi_m) {
            DVector left = phi_active.dot(phi_m);
            double ssi = beta * phi_m.dot(phi_m) - beta * beta * sigma.dot(left).dot(left);
            double qqi = beta * phi_m.dot(y) - beta * beta * sigma.dot(left).dot(right);
            double alpha_i = alpha.get(index);
            double si, qi;
            if (Double.isInfinite(alpha_i)) {
                si = ssi;
                qi = qqi;
            } else {
                si = alpha_i * ssi / (alpha_i - ssi);
                qi = alpha_i * qqi / (alpha_i - ssi);
            }

            return 0.5 * (Math.log(alpha_i) - Math.log(alpha_i + si)) + qi * qi / (alpha_i + si);
        }

        private boolean testConvergence(DVector old_alpha) {
            /*
                In step 11, we must judge if we have attained a local maximum of the marginal likelihood. We
                terminate when the changes in log α in Step 6 for all basis functions in the model are smaller than
                10 −6 and all other θ i ≤ 0.
             */
            double delta = 0.0;
            for (int i = 0; i < alpha.size(); i++) {
                double new_value = alpha.get(i);
                double old_value = old_alpha.get(i);
                if (Double.isInfinite(new_value) && Double.isInfinite(old_value)) {
                    continue;
                }
                if (Double.isInfinite(new_value) || Double.isInfinite(old_value)) {
                    return false;
                }
                delta += Math.abs(old_value - new_value);
            }
            return delta < parent.fitThreshold.get();
        }

        private int[] addIndex(int[] original, int i) {
            int[] copy = new int[indexes.length + 1];
            System.arraycopy(indexes, 0, copy, 0, indexes.length);
            copy[indexes.length] = i;
            return copy;
        }

        private int[] removeIndex(int[] original, int j) {
            int[] copy = new int[original.length - 1];
            int pos = 0;
            for (int k : original) {
                if (k != j) {
                    copy[pos++] = k;
                }
            }
            return copy;
        }

        private void initialize() {
            beta = 1.0 / (y.variance() * 0.1);
            alpha = DVector.fill(phi.colCount(), Double.POSITIVE_INFINITY);

            // select one alpha

            int best_index = 0;

            DVector phi_m = phi.mapCol(0);
            double best_projection = phi_m.dot(y) / phi_m.dot(phi_m);

            for (int i = 1; i < phi.colCount(); i++) {
                phi_m = phi.mapCol(i);
                double projection = phi_m.dot(y) / phi_m.dot(phi_m);
                if (projection >= best_projection) {
                    best_projection = projection;
                    best_index = i;
                }
            }
            indexes = new int[]{best_index};

            phi_m = phi.mapCol(best_index);
            alpha.set(best_index, phi_m.dot(phi_m) / (best_projection - 1.0 / beta));
        }

        private void computeSigmaAndMu() {

            DMatrix m_sigma_inv = phi_hat.mapCols(indexes).mapRows(indexes).copy().mult(beta);
            for (int i = 0; i < indexes.length; i++) {
                m_sigma_inv.inc(i, i, alpha.get(indexes[i]));
            }
            try {
                sigma = CholeskyDecomposition.from(m_sigma_inv).solve(DMatrix.identity(indexes.length));
            } catch (IllegalArgumentException ex) {
                sigma = QRDecomposition.from(m_sigma_inv).solve(DMatrix.identity(indexes.length));
            }
            m = sigma.dot(phi.mapCols(indexes).t().dot(y)).mult(beta);
        }

        void computeSQ() {
            for (int i = 0; i < n; i++) {
                DVector phi_m = phi.mapCol(i);
                DMatrix phi_active = phi.mapCols(indexes).t();
                DVector left = phi_active.dot(phi_m);
                DVector right = phi_active.dot(y);
                ss[i] = beta * phi_m.dot(phi_m) - beta * beta * sigma.dot(left).dot(left);
                qq[i] = beta * phi_m.dot(y) - beta * beta * sigma.dot(left).dot(right);
                double alpha_i = alpha.get(i);
                if (Double.isInfinite(alpha_i)) {
                    s[i] = ss[i];
                    q[i] = qq[i];
                } else {
                    s[i] = alpha_i * ss[i] / (alpha_i - ss[i]);
                    q[i] = alpha_i * qq[i] / (alpha_i - ss[i]);
                }
            }
        }

        private void computeBeta() {
            DMatrix pruned_phi = phi.mapCols(indexes);
            DVector gamma = DVector.from(m.size(), i -> 1 - alpha.get(indexes[i]) * sigma.get(i, i));
            DVector delta = pruned_phi.dot(m).sub(y);
            beta = (n - gamma.sum()) / delta.dot(delta);
        }

        protected void updateResults(RVMRegression parent, boolean convergent, int iterations) {
            this.parent.indexes = indexes;
            this.parent.m = m.copy();
            this.parent.sigma = sigma.copy();
            this.parent.x = x.mapRows(indexes);
            this.parent.alpha = alpha.mapCopy(indexes);
            this.parent.rbfBeta = rbfBeta.mapCopy(indexes);
            this.parent.beta = beta;
            this.parent.converged = convergent;
            this.parent.iterations = iterations;
        }
    }

    private static final class FastTruncated extends BaseAlgorithm {

        private int n;
        private int[] active;
        private double[] ss;
        private double[] qq;
        private double[] s;
        private double[] q;

        private DMatrix phi_hat;
        private DVector phi_dot_y;

        public FastTruncated(RVMRegression parent, Frame df) {
            super(parent, df);
        }

        public boolean fit() {
            x = parent.buildFeatures(df);
            phi = buildPhi();
            n = phi.rowCount();
            y = buildTarget(df);
            truncateFeatures();
            phi_hat = phi.t().dot(phi);
            phi_dot_y = phi.t().dot(y);

            ss = new double[n];
            qq = new double[n];
            s = new double[n];
            q = new double[n];

            initialize();
            computeSigmaAndMu();
            computeSQ();
            computeBeta();

            DVector old_alpha = alpha.copy();
            for (int it = 1; it <= parent.maxIter.get(); it++) {

                updateBestVector();
                computeSigmaAndMu();
                computeSQ();
                if (it % 2 == 0) {
                    computeBeta();
                }

                if (testConvergence(old_alpha)) {
                    updateResults(parent, true, it);
                    return true;
                }
                old_alpha = alpha.copy();
            }

            updateResults(parent, false, parent.maxIter.get());
            return true;
        }

        private void truncateFeatures() {
            active = new int[n];
            int activeLen = 0;

            int[] vectors = IntArrays.newSeq(0, n);

            final double[] correlations = new double[n];
            IntStream.range(0, n).parallel().forEach(
                    i -> correlations[i] = Math.abs(CorrPearson.of(y.asVarDouble(), phi.mapCol(i).asVarDouble()).singleValue()));

            double threshold = Quantiles.of(VarDouble.wrap(correlations), 0.9).values()[0];
            for (int i = 0; i < n; i++) {
                if (correlations[i] > threshold) {
                    active[activeLen++] = i;
                }
            }
            active = Arrays.copyOf(active, activeLen);
        }

        private static class RecursiveTruncate extends RecursiveAction {

            private final int pos;
            private final double[] correlations;

            @Serial
            private static final long serialVersionUID = 5360432925103729246L;

            private RecursiveTruncate(int pos, double[] correlations) {
                this.pos = pos;
                this.correlations = correlations;
            }

            @Override
            protected void compute() {
            }
        }

        private void updateBestVector() {

            double[] theta = new double[n];
            double[] llDelta = new double[n];

            for (int i : active) {
                theta[i] = q[i] * q[i] - s[i];
                if (theta[i] > 0) {
                    if (Double.isInfinite(alpha.get(i))) {
                        llDelta[i] = (qq[i] * qq[i] - ss[i]) / ss[i] + Math.log(ss[i] / (qq[i] * qq[i]));
                    } else {
                        double alpha_new = s[i] * s[i] / theta[i];
                        double delta_alpha = 1. / alpha_new - 1.0 / alpha.get(i);
                        llDelta[i] = (qq[i] * qq[i]) / (ss[i] + 1. / delta_alpha) - Math.log1p(ss[i] * delta_alpha);
                    }
                } else {
                    if (Double.isFinite(alpha.get(i))) {
                        llDelta[i] = qq[i] * qq[i] / (ss[i] - alpha.get(i)) - Math.log(1 - ss[i] / alpha.get(i));
                    }
                }
            }

            int i = active[0];
            for (int j = 1; j < active.length; j++) {
                if (llDelta[active[j]] > llDelta[i]) {
                    i = active[j];
                }
            }
            double alpha_i = alpha.get(i);

            if (theta[i] > 0) {
                if (Double.isInfinite(alpha_i)) {
                    // add alpha_i to model
                    alpha.set(i, s[i] * s[i] / theta[i]);
                    indexes = addIndex(indexes, i);
                } else {
                    // alpha is in set, re-estimate alpha
                    alpha.set(i, s[i] * s[i] / theta[i]);
                }
            } else {
                if (Double.isFinite(alpha_i) && indexes.length > 1) {
                    alpha.set(i, Double.POSITIVE_INFINITY);
                    indexes = removeIndex(indexes, i);
                }
            }

        }

        private boolean testConvergence(DVector old_alpha) {
            /*
                In step 11, we must judge if we have attained a local maximum of the marginal likelihood. We
                terminate when the changes in log α in Step 6 for all basis functions in the model are smaller than
                10 −6 and all other θ i ≤ 0.
             */
            double delta = 0.0;
            for (int i = 0; i < alpha.size(); i++) {
                double new_value = alpha.get(i);
                double old_value = old_alpha.get(i);
                if (Double.isInfinite(new_value) && Double.isInfinite(old_value)) {
                    continue;
                }
                if (Double.isInfinite(new_value) || Double.isInfinite(old_value)) {
                    return false;
                }
                delta += Math.abs(old_value - new_value);
            }
            return delta < parent.fitThreshold.get();
        }

        private int[] addIndex(int[] original, int i) {
            int[] copy = new int[indexes.length + 1];
            System.arraycopy(indexes, 0, copy, 0, indexes.length);
            copy[indexes.length] = i;
            return copy;
        }

        private int[] removeIndex(int[] original, int j) {
            int[] copy = new int[original.length - 1];
            int pos = 0;
            for (int k : original) {
                if (k != j) {
                    copy[pos++] = k;
                }
            }
            return copy;
        }

        private void initialize() {
            beta = 1.0 / (y.variance() * 0.1);
            alpha = DVector.fill(phi.rowCount(), Double.POSITIVE_INFINITY);

            // select one alpha

            int best_index = -1;
            double best_projection = Double.NaN;

            for (int i : active) {
                double projection = phi_dot_y.get(i) / phi_hat.get(i, i);
                if (best_index < 0 || projection >= best_projection) {
                    best_projection = projection;
                    best_index = i;
                }
            }
            indexes = new int[]{best_index};
            alpha.set(best_index, phi_hat.get(best_index, best_index) / (best_projection - 1.0 / beta));
        }

        private void computeSigmaAndMu() {

            DMatrix m_sigma_inv = phi_hat.mapRows(indexes).mapCols(indexes).copy().mult(beta);
            for (int i = 0; i < indexes.length; i++) {
                m_sigma_inv.inc(i, i, alpha.get(indexes[i]));
            }
            sigma = QRDecomposition.from(m_sigma_inv).solve(DMatrix.identity(indexes.length));
            m = sigma.dot(phi_dot_y.mapCopy(indexes)).mult(beta);
        }

        void computeSQ() {
            for (int i : active) {
                DVector phi_m = phi.mapCol(i);
                DMatrix phi_active = phi.mapCols(indexes).t();
                DVector left = phi_hat.mapRow(i).mapCopy(indexes);
                ss[i] = beta * phi_hat.get(i, i) - beta * beta * sigma.dot(left).dot(left);
                qq[i] = beta * phi_dot_y.get(i) - beta * beta * sigma.dot(left).dot(phi_dot_y.mapCopy(indexes));
            }

            for (int i : active) {
                double alpha_i = alpha.get(i);
                if (Double.isInfinite(alpha_i)) {
                    s[i] = ss[i];
                    q[i] = qq[i];
                } else {
                    s[i] = alpha_i * ss[i] / (alpha_i - ss[i]);
                    q[i] = alpha_i * qq[i] / (alpha_i - ss[i]);
                }
            }
        }

        private void computeBeta() {
            DMatrix pruned_phi = phi.mapCols(indexes);
            DVector gamma = DVector.from(m.size(), i -> 1 - alpha.get(indexes[i]) * sigma.get(i, i));
            DVector delta = pruned_phi.dot(m).sub(y);
            beta = (n - gamma.sum()) / delta.dot(delta);
        }

        protected void updateResults(RVMRegression parent, boolean convergent, int iterations) {
            this.parent.indexes = indexes;
            this.parent.m = m.copy();
            this.parent.sigma = sigma.copy();
            this.parent.x = x.mapRows(indexes);
            this.parent.alpha = alpha.mapCopy(indexes);
            this.parent.beta = beta;
            this.parent.converged = convergent;
            this.parent.iterations = iterations;
        }
    }
}
