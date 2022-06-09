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

package rapaio.ml.model.rvm;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import rapaio.core.RandomSource;
import rapaio.core.SamplingTools;
import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarType;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ListParam;
import rapaio.ml.common.ParametricEquals;
import rapaio.ml.common.ValueParam;
import rapaio.ml.common.kernel.Kernel;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.ml.model.RegressionModel;
import rapaio.ml.model.RegressionResult;
import rapaio.ml.model.RunInfo;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.collection.IntArrays;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/13/21.
 */
public class RVMRegression extends RegressionModel<RVMRegression, RegressionResult, RVMRegression.RvmRunInfo> {

    public static RVMRegression newModel() {
        return new RVMRegression();
    }

    public enum Method {
        EVIDENCE_APPROXIMATION,
        FAST_TIPPING,
        ONLINE_PRUNING
    }


    @Serial
    private static final long serialVersionUID = 9165148257709665706L;

    /**
     * Feature factory providers.
     * <p>
     * Those providers are used to produce features learned by RVM model
     */
    public ListParam<FeatureProvider, RVMRegression> providers = new ListParam<>(this,
            List.of(new InterceptProvider(), new RBFProvider(VarDouble.wrap(1), 1)), "providers", (fp1, fp2) -> true);

    /**
     * Method used to fit model
     */
    public ValueParam<Method, RVMRegression> method = new ValueParam<>(this, Method.FAST_TIPPING, "method");

    /**
     * Fit threshold used in convergence criteria.
     */
    public ValueParam<Double, RVMRegression> fitThreshold = new ValueParam<>(this, 1e-10, "fitThreshold");

    /**
     * Fit threshold for setting an alpha weight's prior to infinity.
     */
    public ValueParam<Double, RVMRegression> alphaThreshold = new ValueParam<>(this, 1e9, "alphaThreshold");

    /**
     * Max number of iterations
     */
    public ValueParam<Integer, RVMRegression> maxIter = new ValueParam<>(this, 10_000, "maxIter");

    /**
     * Maximum number of failures for a feature, before it is pruned.
     */
    public ValueParam<Integer, RVMRegression> maxFailures = new ValueParam<>(this, 10_000, "maxFailures");

    /**
     * RVM regression feature. Features are produuced by {@link FeatureProvider} implementations.
     *
     * @param name       feature name for printing puurposes
     * @param trainIndex observation's index used to build feature, -1 if the feature is not based on any observation
     * @param xi         original training observation
     * @param phii       kernelized feature supplier {@code [k(x,xi)]^T}
     * @param kernel     kernel function with second argument provided by internal training observation
     */
    public record Feature(String name, int trainIndex, DVector xi, Supplier<DVector> phii, Function<DVector, Double> kernel) {
    }

    public interface FeatureProvider extends ParametricEquals<FeatureProvider> {
        Feature[] generateFeatures(DMatrix x);
    }

    /**
     * Feature provider for the intercept feature
     */
    public record InterceptProvider() implements FeatureProvider {

        @Override
        public boolean equalOnParams(FeatureProvider object) {
            return object instanceof InterceptProvider;
        }

        @Override
        public Feature[] generateFeatures(DMatrix x) {
            DVector mean = DVector.from(x.cols(), col -> x.mapCol(col).mean());
            return new Feature[] {new Feature("intercept", -1, mean, () -> DVector.fill(x.rows(), 1.0), v -> 1.0)};
        }
    }

    /**
     * RBF kernel feature provider.
     *
     * @param gammas possible values for gamma parameter (inverse of squared variance)
     * @param p      percentage of generated features, 1 for all, 0 for none
     */
    public record RBFProvider(VarDouble gammas, double p) implements FeatureProvider {

        @Override
        public boolean equalOnParams(FeatureProvider object) {
            if (object instanceof RBFProvider rbfProvider) {
                return gammas.deepEquals(rbfProvider.gammas) && p == rbfProvider.p;
            }
            return false;
        }

        public RBFProvider {
            if (p < 0 || p > 1) {
                throw new IllegalArgumentException("Percentage value p=%s is not in interval [0,1].".formatted(Format.floatFlex(p)));
            }
            if (gammas == null || gammas.size() == 0) {
                throw new IllegalArgumentException("Sigma vector cannot be empty.");
            }
        }

        public RBFProvider(VarDouble gammas) {
            this(gammas, 1.0);
        }

        @Override
        public Feature[] generateFeatures(DMatrix x) {
            int len = (int) (x.rows() * gammas.size() * p);
            int[] selection = SamplingTools.sampleWOR(x.rows() * gammas.size(), len);
            Feature[] factories = new Feature[selection.length];
            IntArrays.quickSort(selection);
            int pp = 0;
            for (int pos : selection) {
                int sigmaIndex = pos / x.rows();
                int rowIndex = pos % x.rows();
                DVector xrow = x.mapRowNew(rowIndex);
                double gamma = gammas.getDouble(sigmaIndex);
                RBFKernel kernel = new RBFKernel(gamma);
                factories[pp++] = new Feature(
                        String.format("%s, vector: %s, train trainIndex: %d", kernel.name(), xrow.toString(), rowIndex),
                        rowIndex,
                        xrow,
                        () -> DVector.from(x.rows(), r -> kernel.compute(xrow, x.mapRow(r))),
                        vector -> kernel.compute(vector, xrow)
                );
            }
            return factories;
        }

        @Override
        public String toString() {
            return "RBFProvider{gammas=[%s],p=%s}".formatted(
                    gammas.stream().map(s -> Format.floatFlex(s.getDouble())).collect(Collectors.joining(",")),
                    Format.floatFlex(p));
        }
    }

    /**
     * Generic kernel feature provider
     *
     * @param kernel kernel function
     * @param p      percentage of the generated features
     */
    public record KernelProvider(Kernel kernel, double p) implements FeatureProvider {

        public KernelProvider(Kernel kernel) {
            this(kernel, 1.0);
        }

        @Override
        public boolean equalOnParams(FeatureProvider object) {
            if (object instanceof KernelProvider kernelProvider) {
                return kernel.name().equals(kernelProvider.kernel.name()) && p == kernelProvider.p;
            }
            return false;
        }

        @Override
        public Feature[] generateFeatures(DMatrix x) {
            int len = Math.max(1, (int) (x.rows() * p));
            int[] selection = SamplingTools.sampleWOR(x.rows(), len);
            Feature[] factories = new Feature[selection.length];
            IntArrays.quickSort(selection);
            int pp = 0;
            for (int rowIndex : selection) {
                DVector xrow = x.mapRowNew(rowIndex);
                factories[pp++] = new Feature(
                        String.format("%s, vector: %s, train trainIndex: %d", kernel.name(), xrow.toString(), rowIndex),
                        rowIndex,
                        xrow,
                        () -> DVector.from(x.rows(), r -> kernel.compute(xrow, x.mapRow(r))),
                        vector -> kernel.compute(vector, xrow)
                );
            }
            return factories;
        }
    }

    public record RandomRBFProvider(VarDouble gammas, double p, Distribution noise) implements FeatureProvider {

        @Override
        public boolean equalOnParams(FeatureProvider object) {
            if (object instanceof RandomRBFProvider randomRBFProvider) {
                return gammas.deepEquals(randomRBFProvider.gammas) && p == randomRBFProvider.p && noise.name()
                        .equals(randomRBFProvider.noise.name());
            }
            return false;
        }

        @Override
        public Feature[] generateFeatures(DMatrix x) {
            int len = Math.max(1, (int) (gammas.size() * x.rows() * p));
            Feature[] factories = new Feature[len];
            for (int i = 0; i < len; i++) {
                factories[i] = nextFactory(x);
            }
            return factories;
        }

        public Feature nextFactory(DMatrix x) {
            double sigma = gammas.getDouble(RandomSource.nextInt(gammas.size()));
            RBFKernel kernel = new RBFKernel(sigma);
            DVector out = DVector.fill(x.cols(), 0);
            for (int j = 0; j < out.size(); j++) {
                out.set(j, x.get(RandomSource.nextInt(x.rows()), j) + noise.sampleNext());
            }
            return new Feature(
                    String.format("%s, trainIndex: %s", kernel.name(), out),
                    -1,
                    out,
                    () -> DVector.from(x.rows(), r -> kernel.compute(out, x.mapRow(r))),
                    vector -> kernel.compute(vector, out)
            );
        }
    }

    public static final class RvmRunInfo extends RunInfo<RVMRegression> {
        final boolean[] activeFlag;
        final DVector activeIndexes;
        final DVector alpha;
        final DVector theta;

        private RvmRunInfo(RVMRegression model, int run, boolean[] activeFlag, DVector activeIndexes, DVector alpha, DVector theta) {
            super(model, run);
            this.activeFlag = activeFlag;
            this.activeIndexes = activeIndexes;
            this.alpha = alpha;
            this.theta = theta;
        }
    }

    private int[] featureIndexes;
    private int[] trainingIndexes;
    private DMatrix relevanceVectors;

    private DVector m;

    /**
     * Fitted covariance matrix.
     */
    private DMatrix sigma;
    private DVector alpha;
    private double beta;
    private boolean converged;
    private int iterations;

    private MethodImpl methodImpl;
    private List<Feature> features;

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

    public int[] featureIndexes() {
        return featureIndexes;
    }

    public String[] featureNames() {
        return IntStream.of(featureIndexes).mapToObj(i -> features.get(i).name).toArray(String[]::new);
    }

    public Feature[] features() {
        return IntStream.of(featureIndexes).mapToObj(i -> features.get(i)).toArray(Feature[]::new);
    }

    public int[] trainingIndexes() {
        return trainingIndexes;
    }

    public DMatrix relevanceVectors() {
        return relevanceVectors;
    }

    public DVector m() {
        return m;
    }

    public DMatrix sigma() {
        return sigma;
    }

    public double beta() {
        return beta;
    }

    public DVector alpha() {
        return alpha;
    }

    public int iterations() {
        return iterations;
    }

    /**
     * Describes the learning algorithm
     *
     * @return capabilities of the learning algorithm
     */
    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .inputs(1, Integer.MAX_VALUE, false, VarType.DOUBLE, VarType.INT, VarType.BINARY)
                .targets(1, 1, false, VarType.DOUBLE);
    }

    /**
     * @return Numbers of fitted relevant vectors count, -1 if model is not fitted.
     */
    public int rvCount() {
        return hasLearned ? relevanceVectors.rows() : -1;
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {

        DMatrix x = buildInput(df);
        DVector y = buildTarget(df);

        features = new ArrayList<>();
        for (FeatureProvider fp : providers.get()) {
            features.addAll(Arrays.asList(fp.generateFeatures(x)));
        }
        methodImpl = switch (method.get()) {
            case EVIDENCE_APPROXIMATION -> new EvidenceApproximation(this, x, y);
            case FAST_TIPPING -> new FastTipping(this, x, y);
            case ONLINE_PRUNING -> new FastOnline(this, x, y);
        };

        return methodImpl.fit();
    }

    /**
     * Builds features from the data frame as a matrix with observations on rows
     * and features on columns.
     *
     * @param df source data frame
     * @return matrix of features
     */
    private DMatrix buildInput(Frame df) {
        return DMatrix.copy(df.mapVars(inputNames));
    }

    protected DVector buildTarget(Frame df) {
        return df.rvar(targetNames[0]).dv().denseCopy();
    }

    @Override
    protected RegressionResult corePredict(Frame df, boolean withResiduals, final double[] quantiles) {
        DMatrix feat = buildInput(df);
        RegressionResult prediction = RegressionResult.build(this, df, withResiduals, quantiles);
        for (int i = 0; i < df.rowCount(); i++) {
            double pred = 0;
            for (int j = 0; j < m.size(); j++) {
                pred += features.get(featureIndexes[j]).kernel.apply(feat.mapRow(i)) * m.get(j);
            }
            prediction.prediction(firstTargetName()).setDouble(i, pred);
            if (quantiles != null && quantiles.length > 0) {
                DVector phi_m = DVector.zeros(m.size());
                for (int j = 0; j < m.size(); j++) {
                    phi_m.set(j, features.get(featureIndexes[j]).kernel.apply(feat.mapRow(i)));
                }
                double variance = 1.0 / beta + sigma.dot(phi_m).dot(phi_m);
                Normal normal = Normal.of(pred, Math.sqrt(variance));
                for (int j = 0; j < quantiles.length; j++) {
                    double q = normal.quantile(quantiles[j]);
                    prediction.firstQuantiles()[j].setDouble(i, q);
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
            sb.append(", rvm count=").append(trainingIndexes.length);
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

        sb.append("> relevant vectors count: ").append(relevanceVectors.rows()).append("\n");
        sb.append("> relevant vector training indexes: [")
                .append(IntArrays.stream(trainingIndexes, 0, trainingIndexes.length).mapToObj(String::valueOf)
                        .collect(Collectors.joining(",")))
                .append("]\n");
        sb.append("> convergence: ").append(converged).append("\n> iterations: ").append(iterations).append("\n");

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
        return toSummary(printer, options)
                + "> sigma: \n"
                + sigma.toContent(options);
    }

    private static abstract class MethodImpl {

        protected final RVMRegression parent;
        public final DMatrix x;
        public final DVector y;

        protected MethodImpl(RVMRegression parent, DMatrix x, DVector y) {
            this.parent = parent;
            this.x = x;
            this.y = y;
        }

        public abstract boolean fit();
    }

    private static abstract class BaseAlgorithm extends MethodImpl {

        public int[] indexes;
        public DMatrix phi;
        public DVector m;
        public DMatrix sigma;

        public DVector alpha;
        public double beta;

        protected BaseAlgorithm(RVMRegression parent, DMatrix x, DVector y) {
            super(parent, x, y);
        }

        protected DMatrix buildPhi() {
            DVector[] vectors = new DVector[parent.features.size()];
            for (int i = 0; i < parent.features.size(); i++) {
                vectors[i] = parent.features.get(i).phii.get();
            }
            return DMatrix.copy(false, vectors);
        }

        protected boolean testConvergence(DVector oldAlpha, DVector alpha) {
            double delta = 0;
            for (int i = 0; i < alpha.size(); i++) {
                double new_value = alpha.get(i);
                double old_value = oldAlpha.get(i);
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
    }

    private static final class EvidenceApproximation extends BaseAlgorithm {

        public EvidenceApproximation(RVMRegression parent, DMatrix x, DVector y) {
            super(parent, x, y);
        }

        private DMatrix phi_t_phi;
        private DVector phi_t_y;

        private int n;
        private int fcount;

        public boolean fit() {
            n = x.rows();
            fcount = parent.features.size();
            phi = buildPhi();
            indexes = IntArrays.newSeq(0, parent.features.size());
            phi_t_phi = phi.t().dot(phi);
            phi_t_y = phi.t().dot(y);

            initializeAlphaBeta();
            for (int it = 1; it <= parent.maxIter.get(); it++) {

                // compute m and sigma
                DMatrix t = phi_t_phi.copy().mul(beta);
                for (int i = 0; i < t.rows(); i++) {
                    t.inc(i, i, alpha.get(i));
                }

                try {
                    sigma = t.lu().inv();
                } catch (IllegalArgumentException ignored) {
                    sigma = t.qr().inv();
                }
                m = sigma.dot(phi_t_y).mul(beta);

                // compute alpha and beta

                DVector gamma = DVector.from(m.size(), i -> 1 - alpha.get(i) * sigma.get(i, i));

                DVector oldAlpha = alpha.copy();

                // update alpha
                for (int i = 0; i < alpha.size(); i++) {
                    alpha.set(i, gamma.get(i) / (m.get(i) * m.get(i)));
                }
                // update sigma
                DVector deltaDiff = phi.dot(m).sub(y);
                beta = (n - gamma.sum()) / (deltaDiff.dot(deltaDiff));

                pruneAlphas();

                if (testConvergence(oldAlpha, alpha)) {
                    updateResults(parent, true, it);
                    return true;
                }
            }
            updateResults(parent, false, parent.maxIter.get());
            return true;
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

            // recreate the trainIndex
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
            alpha = alpha.mapNew(keep);

            phi = phi.mapCols(keep).copy();
            phi_t_phi = phi_t_phi.mapCols(keep).mapRows(keep).copy();
            phi_t_y = phi_t_y.mapNew(keep);

            DMatrix t = phi_t_phi.copy().mul(beta);
            for (int i = 0; i < t.rows(); i++) {
                t.inc(i, i, alpha.get(i));
            }

            try {
                sigma = t.lu().inv();
            } catch (IllegalArgumentException ignored) {
                sigma = t.qr().inv();
            }

            m = sigma.dot(phi_t_y).mul(beta);
        }

        private void initializeAlphaBeta() {
            beta = 1.0 / (y.variance() * 0.1);
            alpha = DVector.from(phi.cols(), row -> Math.abs(RandomSource.nextDouble() / 10));
        }

        private void updateResults(RVMRegression parent, boolean convergent, int iterations) {
            parent.featureIndexes = indexes;
            parent.trainingIndexes =
                    IntStream.of(indexes).map(i -> parent.features.get(i).trainIndex).filter(i -> i >= 0).distinct().toArray();
            parent.relevanceVectors =
                    DMatrix.copy(true, IntStream.of(indexes).mapToObj(i -> parent.features.get(i).xi).toArray(DVector[]::new));
            parent.m = m.copy();
            parent.sigma = sigma.copy();
            parent.alpha = alpha.copy();
            parent.beta = beta;
            parent.converged = convergent;
            parent.iterations = iterations;
        }
    }

    private static final class FastTipping extends BaseAlgorithm {

        private int n;
        private int fcount;
        private double[] ss;
        private double[] qq;
        private double[] s;
        private double[] q;

        private DMatrix phi_hat;
        private DVector phi_dot_y;

        public FastTipping(RVMRegression parent, DMatrix x, DVector y) {
            super(parent, x, y);
        }

        public boolean fit() {
            phi = buildPhi();
            phi_hat = phi.t().dot(phi);
            phi_dot_y = phi.t().dot(y);

            n = phi.rows();
            fcount = parent.features.size();
            ss = new double[fcount];
            qq = new double[fcount];
            s = new double[fcount];
            q = new double[fcount];

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

                if (testConvergence(old_alpha, alpha)) {
                    updateResults(parent, true, it);
                    return true;
                }
                old_alpha = alpha.copy();
            }

            updateResults(parent, false, parent.maxIter.get());
            return true;
        }

        private void updateBestVector() {

            double[] theta = new double[fcount];
            double[] llDelta = new double[fcount];

            for (int i = 0; i < fcount; i++) {
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
            for (int j = 1; j < fcount; j++) {
                if (llDelta[j] > llDelta[i]) {
                    i = j;
                }
            }
            double alpha_i = alpha.get(i);

            if (theta[i] > 0) {
                if (Double.isInfinite(alpha_i)) {
                    // add alpha_i to model
                    alpha.set(i, s[i] * s[i] / theta[i]);
                    indexes = addIndex(i);
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

        private int[] addIndex(int i) {
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
            alpha = DVector.fill(parent.features.size(), Double.POSITIVE_INFINITY);

            // select one alpha

            int best_index = 0;
            double best_projection = phi_dot_y.get(0) / phi_hat.get(0, 0);

            for (int i = 1; i < parent.features.size(); i++) {
                double projection = phi_dot_y.get(i) / phi_hat.get(i, i);
                if (projection >= best_projection) {
                    best_projection = projection;
                    best_index = i;
                }
            }
            indexes = new int[] {best_index};

            alpha.set(best_index, phi_hat.get(best_index, best_index) / (best_projection - 1.0 / beta));
        }

        private void computeSigmaAndMu() {

            DMatrix m_sigma_inv = phi_hat.mapRows(indexes).mapCols(indexes).copy().mul(beta);
            for (int i = 0; i < indexes.length; i++) {
                m_sigma_inv.inc(i, i, alpha.get(indexes[i]));
            }
            sigma = m_sigma_inv.qr().inv();
            m = sigma.dot(phi_dot_y.mapNew(indexes)).mul(beta);
        }

        void computeSQ() {
            for (int i = 0; i < fcount; i++) {
                DVector left = phi_hat.mapCol(i).mapNew(indexes);
                DVector right = phi_dot_y.mapNew(indexes);
                ss[i] = beta * phi_hat.get(i, i) - beta * beta * left.dotBilinear(sigma);
                qq[i] = beta * phi_dot_y.get(i) - beta * beta * left.dotBilinear(sigma, right);
            }

            for (int i = 0; i < fcount; i++) {
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

        private void updateResults(RVMRegression parent, boolean convergent, int iterations) {
            parent.featureIndexes = indexes;
            parent.trainingIndexes =
                    IntStream.of(indexes).map(i -> parent.features.get(i).trainIndex).filter(i -> i >= 0).distinct().toArray();
            parent.relevanceVectors =
                    DMatrix.copy(true, IntStream.of(indexes).mapToObj(i -> parent.features.get(i).xi).toArray(DVector[]::new));
            parent.m = m.copy();
            parent.sigma = sigma.copy();
            parent.alpha = alpha.mapNew(indexes);
            parent.beta = beta;
            parent.converged = convergent;
            parent.iterations = iterations;
        }
    }

    private static final class FastOnline extends MethodImpl {

        private int n;
        private int fcount;
        private double beta;
        private DMatrix sigma;
        private DVector m;

        private DMatrix phiHat;

        private DVector phiiDotPhii;
        private DVector phiiDotY;
        private double yTy;
        private DVector ss;
        private DVector qq;
        private DVector s;
        private DVector q;
        private DVector alpha;

        private final List<Integer> candidates = new LinkedList<>();
        private int[] fails;

        // cached for all vectors

        // cached for activeFlag vectors only
        private final ArrayList<ActiveFeature> active = new ArrayList<>();

        private final PhiCache cache = new PhiCache();

        public FastOnline(RVMRegression parent, DMatrix x, DVector y) {
            super(parent, x, y);
        }

        public boolean fit() {
            initialize();

            n = x.rows();

            computeSigmaAndMu();
            computeSQ();
            computeBeta();

            if (parent.runningHook.get() != null) {
                boolean[] a = new boolean[fcount];
                for (ActiveFeature ac : active) {
                    a[ac.index] = true;
                }
                DVector activeIndexes = DVector.from(active.size(), i -> active.get(i).index);
                parent.runningHook.get().accept(new RvmRunInfo(parent, 0, a,
                        activeIndexes, alpha.copy(), q.copy().apply(x -> x * x).sub(s)));
            }

            DVector old_alpha = alpha.copy();
            for (int it = 1; it <= parent.maxIter.get(); it++) {

                updateBestVector();
                computeSigmaAndMu();
                computeSQ();
                computeBeta();

                if (parent.runningHook.get() != null) {
                    boolean[] a = new boolean[fcount];
                    for (ActiveFeature ac : active) {
                        a[ac.index] = true;
                    }
                    DVector activeIndexes = DVector.from(active.size(), i -> active.get(i).index);
                    parent.runningHook.get().accept(new RvmRunInfo(parent, it, a,
                            activeIndexes, alpha.copy(), q.copy().apply(x -> x * x).sub(s).apply(Math::log1p)));
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

        private void initialize() {

            fcount = parent.features.size();
            for (int i = 0; i < fcount; i++) {
                candidates.add(i);
            }

            fails = IntArrays.newFill(fcount, 0);

            // initialize raw features
            phiiDotPhii = DVector.fill(fcount, Double.NaN);
            phiiDotY = DVector.fill(fcount, Double.NaN);
            yTy = y.dot(y);

            ss = DVector.fill(fcount, 0);
            qq = DVector.fill(fcount, 0);
            s = DVector.fill(fcount, 0);
            q = DVector.fill(fcount, 0);
            alpha = DVector.fill(fcount, Double.POSITIVE_INFINITY);

            beta = 1.0 / (y.variance() * 0.1);

            // select one alpha

            int bestIndex = 0;
            DVector bestVector = parent.features.get(0).phii.get();
            phiiDotPhii.set(0, bestVector.dot(bestVector));
            phiiDotY.set(0, bestVector.dot(y));
            double bestProjection = phiiDotY.get(0) / phiiDotPhii.get(0);

            for (int i = 1; i < fcount; i++) {
                DVector phii = parent.features.get(i).phii.get();
                phiiDotPhii.set(i, phii.dot(phii));
                phiiDotY.set(i, phii.dot(y));
                double projection = phiiDotY.get(i) / phiiDotPhii.get(i);
                if (projection >= bestProjection) {
                    bestIndex = i;
                    bestVector = phii;
                    bestProjection = projection;
                }
            }
            active.add(new ActiveFeature(bestIndex, bestVector));
            alpha.set(bestIndex, phiiDotPhii.get(bestIndex) / (bestProjection - 1.0 / beta));

            // initial phi_hat, dimension 1x1 with value computed already in artifacts
            phiHat = DMatrix.fill(1, 1, phiiDotPhii.get(bestIndex));
        }

        private void computeSigmaAndMu() {

            DMatrix m_sigma_inv = phiHat.copy().mul(beta);
            for (int i = 0; i < active.size(); i++) {
                m_sigma_inv.inc(i, i, alpha.get(active.get(i).index));
            }
            sigma = m_sigma_inv.cholesky().solve(DMatrix.eye(active.size()));
            m = sigma.dot(computePhiDotY().mul(beta));
        }

        private DVector computePhiiDotPhi(int i) {

            // first check if it is activeFlag, since if it is activeFlag the values are already in phi_hat
            if (Double.isFinite(alpha.get(i))) {
                for (int j = 0; j < active.size(); j++) {
                    if (i == active.get(j).index) {
                        return phiHat.mapCol(j);
                    }
                }
            }

            // if not activeFlag then try to complete the vector from cache

            DVector v = DVector.fill(active.size(), Double.NaN);
            boolean full = true;
            for (int j = 0; j < active.size(); j++) {
                ActiveFeature a = active.get(j);
                double value = cache.get(i, a.index);
                if (Double.isNaN(value)) {
                    full = false;
                } else {
                    v.set(j, value);
                }
            }

            // if not full from cache, then regenerate the vector and fill missing values, do that also in cache
            if (!full) {
                DVector phii = parent.features.get(i).phii.get();
                for (int j = 0; j < v.size(); j++) {
                    if (Double.isNaN(v.get(j))) {
                        double value = active.get(j).vector.dot(phii);
                        v.set(j, value);
                        cache.store(i, active.get(j).index, value);
                    }
                }
            }

            return v;
        }

        private DVector computePhiDotY() {
            double[] v = new double[active.size()];
            int pos = 0;
            for (var a : active) {
                v[pos++] = phiiDotY.get(a.index);
            }
            return DVector.wrap(v);
        }

        void computeSQ() {
            DVector right = computePhiDotY();
            for (int i : candidates) {
                DVector left = computePhiiDotPhi(i);
                DVector sigmaDotLeft = sigma.dot(left);
                ss.set(i, beta * phiiDotPhii.get(i) - beta * beta * sigmaDotLeft.dot(left));
                qq.set(i, beta * phiiDotY.get(i) - beta * beta * sigmaDotLeft.dot(right));

                double alpha_i = alpha.get(i);
                if (Double.isInfinite(alpha_i)) {
                    s.set(i, ss.get(i));
                    q.set(i, qq.get(i));
                } else {
                    s.set(i, alpha_i * ss.get(i) / (alpha_i - ss.get(i)));
                    q.set(i, alpha_i * qq.get(i) / (alpha_i - ss.get(i)));
                }
            }
        }

        private void computeBeta() {
            double gammaSum = 0;
            for (int i = 0; i < active.size(); i++) {
                gammaSum += alpha.get(active.get(i).index) * sigma.get(i, i);
            }
            double low = yTy - 2 * m.dot(computePhiDotY()) + phiHat.dot(m).dot(m);
            beta = (n - active.size() + gammaSum) / low;
        }

        private void updateBestVector() {

            // compute likelihood criteria

            int bestIndex = 0;
            double bestTheta = Double.NaN;
            double bestDelta = Double.NaN;

            List<Integer> toRemove = new LinkedList<>();
            for (int i : candidates) {
                double theta = q.get(i) * q.get(i) - s.get(i);
                double delta = Double.NEGATIVE_INFINITY;
                if (theta > 0) {
                    if (Double.isInfinite(alpha.get(i))) {
                        delta = (qq.get(i) * qq.get(i) - ss.get(i)) / ss.get(i) + Math.log(ss.get(i) / (qq.get(i) * qq.get(i)));
                    } else {
                        double alpha_new = s.get(i) * s.get(i) / theta;
                        double delta_alpha = 1. / alpha_new - 1.0 / alpha.get(i);
                        delta = (qq.get(i) * qq.get(i)) / (ss.get(i) + 1. / delta_alpha) - Math.log1p(ss.get(i) * delta_alpha);
                    }
                } else {
                    if (Double.isFinite(alpha.get(i))) {
                        delta = qq.get(i) * qq.get(i) / (ss.get(i) - alpha.get(i)) - Math.log(1 - ss.get(i) / alpha.get(i));
                    }
                    fails[i]++;
                    if (fails[i] >= parent.maxFailures.get()) {
                        toRemove.add(i);
                    }
                }
                delta *= 1 + Math.pow(phiiDotY.get(i), 0.7);
                if (Double.isNaN(bestDelta) || delta > bestDelta) {
                    bestDelta = delta;
                    bestTheta = theta;
                    bestIndex = i;
                }
            }
            candidates.removeAll(toRemove);

            double alpha_i = alpha.get(bestIndex);
            double _alpha = s.get(bestIndex) * s.get(bestIndex) / bestTheta;

            if (bestTheta > 0) {
                if (Double.isInfinite(alpha_i)) {
                    // add alpha_i to model
                    addActiveFeature(bestIndex, _alpha);
                } else {
                    // alpha is in set, re-estimate alpha
                    updateActiveFeature(bestIndex, _alpha);
                }
            } else {
                if (Double.isFinite(alpha_i) && active.size() > 1) {
                    removeActiveFeature(bestIndex);
                }
            }
        }

        private void addActiveFeature(int index, double _alpha) {
            alpha.set(index, _alpha);

            // add activeFlag feature to the trainIndex

            DVector phii = parent.features.get(index).phii.get();
            active.add(new ActiveFeature(index, phii));

            // adjust phiHat by adding a new row and column

            phiHat = phiHat.resizeCopy(phiHat.rows() + 1, phiHat.cols() + 1, Double.NaN);

            // fill the remaining entries from cache
            boolean full = true;
            for (int i = 0; i < phiHat.rows(); i++) {
                double value = cache.get(index, active.get(i).index);
                if (Double.isNaN(value)) {
                    full = false;
                } else {
                    phiHat.set(phiHat.rows() - 1, i, value);
                    phiHat.set(i, phiHat.cols() - 1, value);
                }
            }

            // if not completed from cache
            if (!full) {
                for (int i = 0; i < phiHat.rows(); i++) {
                    double value = phiHat.get(phiHat.rows() - 1, i);
                    if (Double.isNaN(value)) {
                        value = active.get(i).vector.dot(phii);
                        phiHat.set(phiHat.rows() - 1, i, value);
                        phiHat.set(i, phiHat.cols() - 1, value);
                        cache.store(index, active.get(i).index, value);
                    }
                }
            }
        }

        private void updateActiveFeature(int index, double _alpha) {
            alpha.set(index, _alpha);
        }

        private void removeActiveFeature(int index) {
            alpha.set(index, Double.POSITIVE_INFINITY);

            // find position of activeFlag feature to be removed
            int pos = -1;
            for (int i = 0; i < active.size(); i++) {
                if (active.get(i).index == index) {
                    pos = i;
                    break;
                }
            }

            // if pos == -1 it means we want to remove a feature which is not activeFlag
            if (pos == -1) {
                throw new IllegalStateException("Try to remove activeFlag feature with trainIndex: " + index);
            }

            // now remove from activeFlag features
            active.remove(pos);

            // adjust phiHat

            phiHat = phiHat.removeRows(pos).removeColsNew(pos);
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

        private void updateResults(RVMRegression parent, boolean convergent, int iterations) {
            parent.featureIndexes = active.stream().mapToInt(a -> a.index).toArray();
            parent.trainingIndexes =
                    active.stream()
                            .mapToInt(a -> a.index)
                            .map(i -> parent.features.get(i).trainIndex)
                            .filter(i -> i >= 0)
                            .distinct()
                            .toArray();
            parent.relevanceVectors = DMatrix.copy(true, active
                    .stream().mapToInt(a -> a.index)
                    .mapToObj(i -> parent.features.get(i).xi)
                    .toArray(DVector[]::new));
            parent.m = m.copy();
            parent.sigma = sigma.copy();
            parent.alpha = alpha.mapNew(parent.featureIndexes);
            parent.beta = beta;
            parent.converged = convergent;
            parent.iterations = iterations;
        }

        public static final class PhiCache {

            private final HashMap<Long, Double> cache = new HashMap<>(10_000, 0.5f);

            public double get(int i, int j) {
                long pos = (i >= j) ? ((long) i << 32) | j : ((long) j << 32) | i;
                return cache.getOrDefault(pos, Double.NaN);
            }

            public void store(int i, int j, double value) {
                long pos = (i >= j) ? ((long) i << 32) | j : ((long) j << 32) | i;
                cache.putIfAbsent(pos, value);
            }
        }

        private record ActiveFeature(int index, DVector vector) {
        }
    }
}
