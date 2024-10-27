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

package rapaio.ml.model.rvm;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import rapaio.core.SamplingTools;
import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.core.param.ListParam;
import rapaio.core.param.ParametricEquals;
import rapaio.core.param.ValueParam;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarType;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.kernel.Kernel;
import rapaio.ml.common.kernel.RBFKernel;
import rapaio.ml.model.RegressionModel;
import rapaio.ml.model.RegressionResult;
import rapaio.ml.model.RunInfo;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;
import rapaio.util.collection.IntArrays;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/13/21.
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
    public final ListParam<FeatureProvider, RVMRegression> providers = new ListParam<>(this,
            List.of(new InterceptProvider(), new RBFProvider(VarDouble.wrap(1), 1)), "providers", (fp1, fp2) -> true);

    /**
     * Method used to fit model
     */
    public final ValueParam<Method, RVMRegression> method = new ValueParam<>(this, Method.FAST_TIPPING, "method");

    /**
     * Fit threshold used in convergence criteria.
     */
    public final ValueParam<Double, RVMRegression> fitThreshold = new ValueParam<>(this, 1e-10, "fitThreshold");

    /**
     * Fit threshold for setting an alpha weight's prior to infinity.
     */
    public final ValueParam<Double, RVMRegression> alphaThreshold = new ValueParam<>(this, 1e9, "alphaThreshold");

    /**
     * Max number of iterations
     */
    public final ValueParam<Integer, RVMRegression> maxIter = new ValueParam<>(this, 10_000, "maxIter");

    /**
     * Maximum number of failures for a feature, before it is pruned.
     */
    public final ValueParam<Integer, RVMRegression> maxFailures = new ValueParam<>(this, 10_000, "maxFailures");

    /**
     * RVM regression feature. Features are produced by {@link FeatureProvider} implementations.
     *
     * @param name       feature name for printing purposes
     * @param trainIndex observation's index used to build feature, -1 if the feature is not based on any observation
     * @param xi         original training observation
     * @param phii       kernelized feature supplier {@code [k(x,xi)]^T}
     * @param kernel     kernel function with second argument provided by internal training observation
     */
    public record Feature(String name, int trainIndex, Tensor<Double> xi, Supplier<Tensor<Double>> phii,
                          Function<Tensor<Double>, Double> kernel) {
    }

    public interface FeatureProvider extends ParametricEquals<FeatureProvider> {
        Feature[] generateFeatures(Random random, Tensor<Double> mx);
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
        public Feature[] generateFeatures(Random random, Tensor<Double> x) {
            return new Feature[] {new Feature("intercept", -1, x.mean(0), () -> Tensors.full(Shape.of(x.dim(0)), 1.0), v -> 1.0)};
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
        public Feature[] generateFeatures(Random random, Tensor<Double> x) {
            int len = (int) (x.dim(0) * gammas.size() * p);
            int[] selection = SamplingTools.sampleWOR(x.dim(0) * gammas.size(), len);
            Feature[] factories = new Feature[selection.length];
            IntArrays.quickSort(selection);
            int pp = 0;
            for (int pos : selection) {
                int sigmaIndex = pos / x.dim(0);
                int rowIndex = pos % x.dim(0);
                var xrow = x.takesq(0, rowIndex).copy();
                double gamma = gammas.getDouble(sigmaIndex);
                RBFKernel kernel = new RBFKernel(gamma);
                factories[pp++] = new Feature(
                        String.format("%s, vector: %s, train trainIndex: %d", kernel.name(), xrow.toString(), rowIndex),
                        rowIndex,
                        xrow,
                        () -> Tensors.zeros(Shape.of(x.dim(0))).apply_((r, _) -> kernel.compute(xrow, x.takesq(0, r))),
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
        public Feature[] generateFeatures(Random random, Tensor<Double> x) {
            int len = Math.max(1, (int) (x.dim(0) * p));
            int[] selection = SamplingTools.sampleWOR(x.dim(0), len);
            Feature[] factories = new Feature[selection.length];
            IntArrays.quickSort(selection);
            int pp = 0;
            for (int rowIndex : selection) {
                var xrow = x.takesq(0, rowIndex).copy();
                factories[pp++] = new Feature(
                        String.format("%s, vector: %s, train trainIndex: %d", kernel.name(), xrow.toString(), rowIndex),
                        rowIndex,
                        xrow,
                        () -> Tensors.zeros(Shape.of(x.dim(0))).apply_((r, _) -> kernel.compute(xrow, x.takesq(0, r))),
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
        public Feature[] generateFeatures(Random random, Tensor<Double> x) {
            int len = Math.max(1, (int) (gammas.size() * x.dim(0) * p));
            Feature[] factories = new Feature[len];
            for (int i = 0; i < len; i++) {
                factories[i] = nextFactory(random, x);
            }
            return factories;
        }

        public Feature nextFactory(Random random, Tensor<Double> x) {
            double sigma = gammas.getDouble(random.nextInt(gammas.size()));
            RBFKernel kernel = new RBFKernel(sigma);
            var out = Tensors.zeros(Shape.of(x.dim(1)));
            for (int j = 0; j < out.size(); j++) {
                out.setDouble(x.getDouble(random.nextInt(x.dim(0)), j) + noise.sampleNext(), j);
            }
            return new Feature(
                    String.format("%s, trainIndex: %s", kernel.name(), out),
                    -1,
                    out,
                    () -> Tensors.zeros(Shape.of(x.dim(0))).apply_((r, _) -> kernel.compute(out, x.takesq(0, r))),
                    vector -> kernel.compute(vector, out)
            );
        }
    }

    public static final class RvmRunInfo extends RunInfo<RVMRegression> {
        final boolean[] activeFlag;
        final Tensor<Double> activeIndexes;
        final Tensor<Double> alpha;
        final Tensor<Double> theta;

        private RvmRunInfo(RVMRegression model, int run, boolean[] activeFlag, Tensor<Double> activeIndexes, Tensor<Double> alpha,
                Tensor<Double> theta) {
            super(model, run);
            this.activeFlag = activeFlag;
            this.activeIndexes = activeIndexes;
            this.alpha = alpha;
            this.theta = theta;
        }
    }

    private int[] featureIndexes;
    private int[] trainingIndexes;
    private Tensor<Double> mrelevanceVectors;

    private Tensor<Double> vm;

    /**
     * Fitted covariance matrix.
     */
    private Tensor<Double> msigma;
    private Tensor<Double> valpha;
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

    public Tensor<Double> relevanceVectors() {
        return mrelevanceVectors;
    }

    public Tensor<Double> m() {
        return vm;
    }

    public Tensor<Double> sigma() {
        return msigma;
    }

    public double beta() {
        return beta;
    }

    public Tensor<Double> alpha() {
        return valpha;
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
        return hasLearned ? mrelevanceVectors.dim(0) : -1;
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        Random random = getRandom();
        Tensor<Double> mx = buildInput(df);
        Tensor<Double> vy = buildTarget(df);

        features = new ArrayList<>();
        for (FeatureProvider fp : providers.get()) {
            features.addAll(Arrays.asList(fp.generateFeatures(random, mx)));
        }
        methodImpl = switch (method.get()) {
            case EVIDENCE_APPROXIMATION -> new EvidenceApproximation(this, mx, vy);
            case FAST_TIPPING -> new FastTipping(this, mx, vy);
            case ONLINE_PRUNING -> new FastOnline(this, mx, vy);
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
    private Tensor<Double> buildInput(Frame df) {
        return df.mapVars(inputNames).tensor();
    }

    protected Tensor<Double> buildTarget(Frame df) {
        return df.rvar(targetNames[0]).tensor();
    }

    @Override
    protected RegressionResult corePredict(Frame df, boolean withResiduals, final double[] quantiles) {
        Tensor<Double> feat = buildInput(df);
        RegressionResult prediction = RegressionResult.build(this, df, withResiduals, quantiles);
        for (int i = 0; i < df.rowCount(); i++) {
            double pred = 0;
            for (int j = 0; j < vm.size(); j++) {
                pred += features.get(featureIndexes[j]).kernel.apply(feat.takesq(0, i)) * vm.getDouble(j);
            }
            prediction.prediction(firstTargetName()).setDouble(i, pred);
            if (quantiles != null && quantiles.length > 0) {
                var phi_m = Tensors.zeros(Shape.of(vm.size()));
                for (int j = 0; j < vm.size(); j++) {
                    phi_m.setDouble(features.get(featureIndexes[j]).kernel.apply(feat.takesq(0, i)), j);
                }
                double variance = 1.0 / beta + phi_m.stretch(0).mm(msigma).mv(phi_m).getDouble();
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
    public String toSummary(Printer printer, POpt<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(headerSummary());
        sb.append("\n");

        if (!hasLearned) {
            return sb.toString();
        }

        sb.append("> relevant vectors count: ").append(mrelevanceVectors.dim(0)).append("\n");
        sb.append("> relevant vector training indexes: [")
                .append(IntArrays.stream(trainingIndexes, 0, trainingIndexes.length).mapToObj(String::valueOf)
                        .collect(Collectors.joining(",")))
                .append("]\n");
        sb.append("> convergence: ").append(converged).append("\n> iterations: ").append(iterations).append("\n");

        sb.append("> mean: \n");
        sb.append(vm.toFullContent(options));
        sb.append(">alphas: \n");
        sb.append(valpha.toFullContent(options));
        sb.append("> beta: ").append(Format.floatFlex(beta)).append("\n");

        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POpt<?>... options) {
        return toSummary(printer, options) + "> sigma: \n" + msigma.toContent(options);
    }

    private static abstract class MethodImpl {

        protected final RVMRegression parent;
        public final Tensor<Double> x;
        public final Tensor<Double> y;

        protected MethodImpl(RVMRegression parent, Tensor<Double> x, Tensor<Double> y) {
            this.parent = parent;
            this.x = x;
            this.y = y;
        }

        public abstract boolean fit();
    }

    private static abstract class BaseAlgorithm extends MethodImpl {

        public int[] indexes;
        public Tensor<Double> phi;
        public Tensor<Double> m;
        public Tensor<Double> sigma;

        public Tensor<Double> alpha;
        public double beta;

        protected BaseAlgorithm(RVMRegression parent, Tensor<Double> x, Tensor<Double> y) {
            super(parent, x, y);
        }

        protected Tensor<Double> buildPhi() {
            Tensor<Double>[] vectors = new Tensor[parent.features.size()];
            for (int i = 0; i < parent.features.size(); i++) {
                vectors[i] = parent.features.get(i).phii.get();
            }
            return Tensors.stack(1, List.of(vectors));
        }

        protected boolean testConvergence(Tensor<Double> oldAlpha, Tensor<Double> alpha) {
            double delta = 0;
            for (int i = 0; i < alpha.size(); i++) {
                double new_value = alpha.getDouble(i);
                double old_value = oldAlpha.getDouble(i);
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

        public EvidenceApproximation(RVMRegression parent, Tensor<Double> x, Tensor<Double> y) {
            super(parent, x, y);
        }

        private Tensor<Double> phi_t_phi;
        private Tensor<Double> phi_t_y;

        private int n;
        private int fcount;

        public boolean fit() {
            n = x.dim(0);
            fcount = parent.features.size();
            phi = buildPhi();
            indexes = IntArrays.newSeq(0, parent.features.size());
            phi_t_phi = phi.t().mm(phi);
            phi_t_y = phi.t().mv(y);

            initializeAlphaBeta();
            for (int it = 1; it <= parent.maxIter.get(); it++) {

                // compute m and sigma
                var t = phi_t_phi.copy().mul_(beta);
                for (int i = 0; i < t.dim(0); i++) {
                    t.inc(alpha.getDouble(i), i, i);
                }

                try {
                    sigma = t.lu().inv();
                } catch (IllegalArgumentException ignored) {
                    sigma = t.qr().inv();
                }
                m = sigma.mv(phi_t_y).mul_(beta);

                // compute alpha and beta

                var gamma = Tensors.zeros(Shape.of(m.size())).apply_((i, _) -> 1 - alpha.getDouble(i) * sigma.getDouble(i, i));
                var oldAlpha = alpha.copy();

                // update alpha
                for (int i = 0; i < alpha.size(); i++) {
                    alpha.setDouble(gamma.getDouble(i) / (m.getDouble(i) * m.getDouble(i)), i);
                }
                // update sigma
                var deltaDiff = phi.mv(m).sub_(y);
                beta = (n - gamma.sum()) / (deltaDiff.inner(deltaDiff));

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
                if (alpha.getDouble(i) > parent.alphaThreshold.get()) {
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
            alpha = alpha.take(0, keep).copy();

            phi = phi.take(1, keep).copy();
            phi_t_phi = phi_t_phi.take(1, keep).take(0, keep).copy();
            phi_t_y = phi_t_y.take(0, keep).copy();

            var t = phi_t_phi.mul(beta);
            for (int i = 0; i < t.dim(0); i++) {
                t.inc(alpha.getDouble(i), i, i);
            }

            try {
                sigma = t.lu().inv();
            } catch (IllegalArgumentException ignored) {
                sigma = t.qr().inv();
            }

            m = sigma.mv(phi_t_y).mul_(beta);
        }

        private void initializeAlphaBeta() {
            beta = 1.0 / (y.var() * 0.1);
            alpha = Tensors.zeros(Shape.of(phi.dim(1))).apply_((row, _) -> Math.abs(parent.getRandom().nextDouble() / 10));
        }

        private void updateResults(RVMRegression parent, boolean convergent, int iterations) {
            parent.featureIndexes = indexes;
            parent.trainingIndexes =
                    IntStream.of(indexes).map(i -> parent.features.get(i).trainIndex).filter(i -> i >= 0).distinct().toArray();
            parent.mrelevanceVectors = Tensors.stack(1, IntStream.of(indexes)
                    .mapToObj(i -> parent.features.get(i).xi)
                    .toList());
            parent.vm = m.copy();
            parent.msigma = sigma.copy();
            parent.valpha = alpha.copy();
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

        private Tensor<Double> phi_hat;
        private Tensor<Double> phi_dot_y;

        public FastTipping(RVMRegression parent, Tensor<Double> x, Tensor<Double> y) {
            super(parent, x, y);
        }

        public boolean fit() {
            phi = buildPhi();
            phi_hat = phi.t().mm(phi);
            phi_dot_y = phi.t().mv(y);

            n = phi.dim(0);
            fcount = parent.features.size();
            ss = new double[fcount];
            qq = new double[fcount];
            s = new double[fcount];
            q = new double[fcount];

            initialize();
            computeSigmaAndMu();
            computeSQ();
            computeBeta();

            var old_alpha = alpha.copy();
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
                    if (Double.isInfinite(alpha.getDouble(i))) {
                        llDelta[i] = (qq[i] * qq[i] - ss[i]) / ss[i] + Math.log(ss[i] / (qq[i] * qq[i]));
                    } else {
                        double alpha_new = s[i] * s[i] / theta[i];
                        double delta_alpha = 1. / alpha_new - 1.0 / alpha.getDouble(i);
                        llDelta[i] = (qq[i] * qq[i]) / (ss[i] + 1. / delta_alpha) - Math.log1p(ss[i] * delta_alpha);
                    }
                } else {
                    if (Double.isFinite(alpha.getDouble(i))) {
                        llDelta[i] = qq[i] * qq[i] / (ss[i] - alpha.getDouble(i)) - Math.log(1 - ss[i] / alpha.getDouble(i));
                    }
                }
            }

            int i = 0;
            for (int j = 1; j < fcount; j++) {
                if (llDelta[j] > llDelta[i]) {
                    i = j;
                }
            }
            double alpha_i = alpha.getDouble(i);

            if (theta[i] > 0) {
                if (Double.isInfinite(alpha_i)) {
                    // add alpha_i to model
                    alpha.setDouble(s[i] * s[i] / theta[i], i);
                    indexes = addIndex(i);
                } else {
                    // alpha is in set, re-estimate alpha
                    alpha.setDouble(s[i] * s[i] / theta[i], i);
                }
            } else {
                if (Double.isFinite(alpha_i) && indexes.length > 1) {
                    alpha.setDouble(Double.POSITIVE_INFINITY, i);
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
            beta = 1.0 / (y.varc(1) * 0.1);
            alpha = Tensors.full(Shape.of(parent.features.size()), Double.POSITIVE_INFINITY);

            // select one alpha

            int best_index = 0;
            double best_projection = phi_dot_y.getDouble(0) / phi_hat.getDouble(0, 0);

            for (int i = 1; i < parent.features.size(); i++) {
                double projection = phi_dot_y.getDouble(i) / phi_hat.getDouble(i, i);
                if (projection >= best_projection) {
                    best_projection = projection;
                    best_index = i;
                }
            }
            indexes = new int[] {best_index};

            alpha.setDouble(phi_hat.getDouble(best_index, best_index) / (best_projection - 1.0 / beta), best_index);
        }

        private void computeSigmaAndMu() {

            Tensor<Double> m_sigma_inv = phi_hat.take(0, indexes).take(1, indexes).copy().mul_(beta);
            for (int i = 0; i < indexes.length; i++) {
                m_sigma_inv.inc(alpha.getDouble(indexes[i]), i, i);
            }
            sigma = m_sigma_inv.qr().inv();
            m = sigma.mv(phi_dot_y.take(0, indexes)).mul_(beta);
        }

        void computeSQ() {
            for (int i = 0; i < fcount; i++) {
                Tensor<Double> left = phi_hat.takesq(1, i).take(0, indexes);
                Tensor<Double> right = phi_dot_y.take(0, indexes);
                ss[i] = beta * phi_hat.getDouble(i, i) - beta * beta * left.stretch(0).mm(sigma).mv(left).getDouble(0);
                qq[i] = beta * phi_dot_y.getDouble(i) - beta * beta * left.stretch(0).mm(sigma).mv(right).getDouble(0);
            }

            for (int i = 0; i < fcount; i++) {
                double alpha_i = alpha.getDouble(i);
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
            Tensor<Double> gamma =
                    Tensors.zeros(Shape.of(m.size())).apply_((i, _) -> 1 - alpha.getDouble(indexes[i]) * sigma.getDouble(i, i));
            Tensor<Double> pruned_phi = phi.take(1, indexes);
            Tensor<Double> delta = pruned_phi.mv(m).sub_(y);
            beta = (n - gamma.sum()) / delta.inner(delta);
        }

        private void updateResults(RVMRegression parent, boolean convergent, int iterations) {
            parent.featureIndexes = indexes;
            parent.trainingIndexes =
                    IntStream.of(indexes).map(i -> parent.features.get(i).trainIndex).filter(i -> i >= 0).distinct().toArray();
            parent.mrelevanceVectors = Tensors.stack(1, IntStream.of(indexes)
                    .mapToObj(i -> parent.features.get(i).xi)
                    .toList());
            parent.vm = m.copy();
            parent.msigma = sigma.copy();
            parent.valpha = alpha.take(0, indexes).copy();
            parent.beta = beta;
            parent.converged = convergent;
            parent.iterations = iterations;
        }
    }

    private static final class FastOnline extends MethodImpl {

        private int n;
        private int fcount;
        private double beta;
        private Tensor<Double> sigma;
        private Tensor<Double> m;

        private Tensor<Double> phiHat;

        private Tensor<Double> phiiDotPhii;
        private Tensor<Double> phiiDotY;
        private double yTy;
        private Tensor<Double> ss;
        private Tensor<Double> qq;
        private Tensor<Double> s;
        private Tensor<Double> q;
        private Tensor<Double> alpha;

        private final List<Integer> candidates = new LinkedList<>();
        private int[] fails;

        // cached for all vectors

        // cached for activeFlag vectors only
        private final ArrayList<ActiveFeature> active = new ArrayList<>();

        private final PhiCache cache = new PhiCache();

        public FastOnline(RVMRegression parent, Tensor<Double> x, Tensor<Double> y) {
            super(parent, x, y);
        }

        public boolean fit() {
            initialize();

            n = x.dim(0);

            computeSigmaAndMu();
            computeSQ();
            computeBeta();

            if (parent.runningHook.get() != null) {
                boolean[] a = new boolean[fcount];
                for (ActiveFeature ac : active) {
                    a[ac.index] = true;
                }
                Tensor<Double> activeIndexes = Tensors.zeros(Shape.of(active.size())).apply_((i, _) -> (double) active.get(i).index);
                parent.runningHook.get().accept(new RvmRunInfo(parent, 0, a,
                        activeIndexes, alpha.copy(), q.copy().apply(x -> x * x).sub(s)));
            }

            Tensor<Double> old_alpha = alpha.copy();
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
                    Tensor<Double> activeIndexes = Tensors.zeros(Shape.of(active.size())).apply_((i, _) -> (double) active.get(i).index);
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
            phiiDotPhii = Tensors.full(Shape.of(fcount), Double.NaN);
            phiiDotY = Tensors.full(Shape.of(fcount), Double.NaN);
            yTy = y.inner(y);

            ss = Tensors.zeros(Shape.of(fcount));
            qq = Tensors.zeros(Shape.of(fcount));
            s = Tensors.zeros(Shape.of(fcount));
            q = Tensors.zeros(Shape.of(fcount));
            alpha = Tensors.full(Shape.of(fcount), Double.POSITIVE_INFINITY);

            beta = 1.0 / (y.varc(1) * 0.1);

            // select one alpha

            int bestIndex = 0;
            Tensor<Double> bestVector = parent.features.get(0).phii.get();
            phiiDotPhii.setDouble(bestVector.inner(bestVector), 0);
            phiiDotY.setDouble(bestVector.inner(y), 0);
            double bestProjection = phiiDotY.getDouble(0) / phiiDotPhii.getDouble(0);

            for (int i = 1; i < fcount; i++) {
                Tensor<Double> phii = parent.features.get(i).phii.get();
                phiiDotPhii.setDouble(phii.inner(phii), i);
                phiiDotY.setDouble(phii.inner(y), i);
                double projection = phiiDotY.getDouble(i) / phiiDotPhii.getDouble(i);
                if (projection >= bestProjection) {
                    bestIndex = i;
                    bestVector = phii;
                    bestProjection = projection;
                }
            }
            active.add(new ActiveFeature(bestIndex, bestVector));
            alpha.setDouble(phiiDotPhii.getDouble(bestIndex) / (bestProjection - 1.0 / beta), bestIndex);

            // initial phi_hat, dimension 1x1 with value computed already in artifacts
            phiHat = Tensors.full(Shape.of(1, 1), phiiDotPhii.getDouble(bestIndex));
        }

        private void computeSigmaAndMu() {

            Tensor<Double> m_sigma_inv = phiHat.mul(beta);
            for (int i = 0; i < active.size(); i++) {
                m_sigma_inv.incDouble(alpha.getDouble(active.get(i).index), i, i);
            }
            sigma = m_sigma_inv.cholesky().solve(Tensors.eye(active.size()));
            m = sigma.mv(computePhiDotY().mul(beta));
        }

        private Tensor<Double> computePhiiDotPhi(int i) {

            // first check if it is activeFlag, since if it is activeFlag the values are already in phi_hat
            if (Double.isFinite(alpha.getDouble(i))) {
                for (int j = 0; j < active.size(); j++) {
                    if (i == active.get(j).index) {
                        return phiHat.takesq(1, j);
                    }
                }
            }

            // if not activeFlag then try to complete the vector from cache

            Tensor<Double> v = Tensors.full(Shape.of(active.size()), Double.NaN);
            boolean full = true;
            for (int j = 0; j < active.size(); j++) {
                ActiveFeature a = active.get(j);
                double value = cache.get(i, a.index);
                if (Double.isNaN(value)) {
                    full = false;
                } else {
                    v.setDouble(value, j);
                }
            }

            // if not full from cache, then regenerate the vector and fill missing values, do that also in cache
            if (!full) {
                Tensor<Double> phii = parent.features.get(i).phii.get();
                for (int j = 0; j < v.size(); j++) {
                    if (Double.isNaN(v.getDouble(j))) {
                        double value = active.get(j).vector.inner(phii);
                        v.setDouble(value, j);
                        cache.store(i, active.get(j).index, value);
                    }
                }
            }

            return v;
        }

        private Tensor<Double> computePhiDotY() {
            double[] v = new double[active.size()];
            int pos = 0;
            for (var a : active) {
                v[pos++] = phiiDotY.getDouble(a.index);
            }
            return Tensors.stride(v);
        }

        void computeSQ() {
            Tensor<Double> right = computePhiDotY();
            for (int i : candidates) {
                Tensor<Double> left = computePhiiDotPhi(i);
                Tensor<Double> sigmaDotLeft = sigma.mv(left);
                ss.setDouble(beta * phiiDotPhii.getDouble(i) - beta * beta * sigmaDotLeft.inner(left), i);
                qq.setDouble(beta * phiiDotY.getDouble(i) - beta * beta * sigmaDotLeft.inner(right), i);

                double alpha_i = alpha.getDouble(i);
                if (Double.isInfinite(alpha_i)) {
                    s.setDouble(ss.getDouble(i), i);
                    q.setDouble(qq.getDouble(i), i);
                } else {
                    s.setDouble(alpha_i * ss.getDouble(i) / (alpha_i - ss.getDouble(i)), i);
                    q.setDouble(alpha_i * qq.getDouble(i) / (alpha_i - ss.getDouble(i)), i);
                }
            }
        }

        private void computeBeta() {
            double gammaSum = 0;
            for (int i = 0; i < active.size(); i++) {
                gammaSum += alpha.getDouble(active.get(i).index) * sigma.getDouble(i, i);
            }
            double low = yTy - 2 * m.inner(computePhiDotY()) + m.stretch(0).mm(phiHat).mv(m).getDouble();
            beta = (n - active.size() + gammaSum) / low;
        }

        private void updateBestVector() {

            // compute likelihood criteria

            int bestIndex = 0;
            double bestTheta = Double.NaN;
            double bestDelta = Double.NaN;

            List<Integer> toRemove = new LinkedList<>();
            for (int i : candidates) {
                double theta = q.getDouble(i) * q.getDouble(i) - s.getDouble(i);
                double delta = Double.NEGATIVE_INFINITY;
                if (theta > 0) {
                    if (Double.isInfinite(alpha.getDouble(i))) {
                        delta = (qq.getDouble(i) * qq.getDouble(i) - ss.getDouble(i)) / ss.getDouble(i) + Math.log(
                                ss.getDouble(i) / (qq.getDouble(i) * qq.getDouble(i)));
                    } else {
                        double alpha_new = s.getDouble(i) * s.getDouble(i) / theta;
                        double delta_alpha = 1. / alpha_new - 1.0 / alpha.getDouble(i);
                        delta = (qq.getDouble(i) * qq.getDouble(i)) / (ss.getDouble(i) + 1. / delta_alpha) - Math.log1p(
                                ss.getDouble(i) * delta_alpha);
                    }
                } else {
                    if (Double.isFinite(alpha.getDouble(i))) {
                        delta = qq.getDouble(i) * qq.getDouble(i) / (ss.getDouble(i) - alpha.getDouble(i)) - Math.log(
                                1 - ss.getDouble(i) / alpha.getDouble(i));
                    }
                    fails[i]++;
                    if (fails[i] >= parent.maxFailures.get()) {
                        toRemove.add(i);
                    }
                }
                delta *= 1 + Math.pow(phiiDotY.getDouble(i), 0.7);
                if (Double.isNaN(bestDelta) || delta > bestDelta) {
                    bestDelta = delta;
                    bestTheta = theta;
                    bestIndex = i;
                }
            }
            candidates.removeAll(toRemove);

            double alpha_i = alpha.getDouble(bestIndex);
            double _alpha = s.getDouble(bestIndex) * s.getDouble(bestIndex) / bestTheta;

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
            alpha.setDouble(_alpha, index);

            // add activeFlag feature to the trainIndex

            Tensor<Double> phii = parent.features.get(index).phii.get();
            active.add(new ActiveFeature(index, phii));

            // adjust phiHat by adding a new row and column

            Tensor<Double> fill = Tensors.full(Shape.of(phiHat.dim(0) + 1, phiHat.dim(1) + 1), Double.NaN);
            phiHat.copyTo(fill.narrow(0, true, 0, phiHat.dim(0)).narrow(1, true, 0, phiHat.dim(1)));
            phiHat = fill;

            // fill the remaining entries from cache
            boolean full = true;
            for (int i = 0; i < phiHat.dim(0); i++) {
                double value = cache.get(index, active.get(i).index);
                if (Double.isNaN(value)) {
                    full = false;
                } else {
                    phiHat.setDouble(value, phiHat.dim(0) - 1, i);
                    phiHat.setDouble(value, i, phiHat.dim(1) - 1);
                }
            }

            // if not completed from cache
            if (!full) {
                for (int i = 0; i < phiHat.dim(0); i++) {
                    double value = phiHat.getDouble(phiHat.dim(0) - 1, i);
                    if (Double.isNaN(value)) {
                        value = active.get(i).vector.inner(phii);
                        phiHat.setDouble(value, phiHat.dim(0) - 1, i);
                        phiHat.setDouble(value, i, phiHat.dim(1) - 1);
                        cache.store(index, active.get(i).index, value);
                    }
                }
            }
        }

        private void updateActiveFeature(int index, double _alpha) {
            alpha.setDouble(_alpha, index);
        }

        private void removeActiveFeature(int index) {
            alpha.setDouble(Double.POSITIVE_INFINITY, index);

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

            // this copy is required?
            phiHat = phiHat.remove(0, pos).remove(1, pos).copy();
        }

        private boolean testConvergence(Tensor<Double> old_alpha) {
//                In step 11, we must judge if we have attained a local maximum of the marginal likelihood. We
//                terminate when the changes in log α in Step 6 for all basis functions in the model are smaller than
//                10 −6 and all other θ i ≤ 0.
            double delta = 0.0;
            for (int i = 0; i < alpha.size(); i++) {
                double new_value = alpha.getDouble(i);
                double old_value = old_alpha.getDouble(i);
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
            parent.mrelevanceVectors =
                    Tensors.stack(1, active.stream().mapToInt(a -> a.index).mapToObj(i -> parent.features.get(i).xi).toList());
            parent.vm = m.copy();
            parent.msigma = sigma.copy();
            parent.valpha = alpha.take(0, parent.featureIndexes).copy();
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

        private record ActiveFeature(int index, Tensor<Double> vector) {
        }
    }
}
