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

package rapaio.ml.model.km;

import static java.lang.StrictMath.abs;
import static java.lang.StrictMath.pow;

import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import rapaio.core.param.ValueParam;
import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarType;
import rapaio.math.tensor.Shape;
import rapaio.math.tensor.Tensor;
import rapaio.math.tensor.Tensors;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.distance.Distance;
import rapaio.ml.common.distance.MinkowskiDistance;
import rapaio.ml.model.ClusteringModel;
import rapaio.ml.model.RunInfo;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * Minkowsky Weighted KMeans
 * <p>
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/27/17.
 */
public class MWKMeans extends ClusteringModel<MWKMeans, MWKMeansResult, RunInfo<MWKMeans>> {

    public static MWKMeans newMWKMeans() {
        return new MWKMeans();
    }

    private static final Logger LOGGER = Logger.getLogger(MWKMeans.class.getName());

    /**
     * Specifies the desired number of centroids.
     */
    public final ValueParam<Integer, MWKMeans> k = new ValueParam<>(this, 2, "k");

    /**
     * Power of Minkowski metric. Value should be greater than 1
     */
    public final ValueParam<Integer, MWKMeans> p = new ValueParam<>(this, 2, "p", v -> v >= 1);

    /**
     * Number of restarts when choosing the initial centroids.
     */
    public final ValueParam<Integer, MWKMeans> nstart = new ValueParam<>(this, 1, "nstart", v -> v >= 1);

    /**
     * Cluster initialization method.
     */
    public final ValueParam<KMClusterInit, MWKMeans> init = new ValueParam<>(this, KMClusterInit.Forgy, "init");

    /**
     * Subspace clustering flag. If true, a separate set of weights for each cluster will be used, otherwise
     * a global set of weights will be used.
     */
    public final ValueParam<Boolean, MWKMeans> subspace = new ValueParam<>(this, false, "subspace clustering flag");

    /**
     * Threshold value used as a criteria for assessing convergence.
     */
    public final ValueParam<Double, MWKMeans> eps = new ValueParam<>(this, 1e-10, "eps");

    // clustering artifacts

    private Tensor<Double> c;
    private Tensor<Double> weights;
    private VarDouble errors;

    private MWKMeans() {
    }

    @Override
    public String name() {
        return "MWKMeans";
    }

    @Override
    public MWKMeans newInstance() {
        return new MWKMeans().copyParameterValues(this);
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .inputs(1, Integer.MAX_VALUE, false, VarType.DOUBLE, VarType.INT, VarType.BINARY)
                .targets(0, 0, true);
    }

    public double distance(Tensor<Double> x, Tensor<Double> y, Tensor<Double> w, int p) {
        return pow(error(x, y, w, p), 1. / p);
    }

    private double error(Tensor<Double> x, Tensor<Double> y, Tensor<Double> w, int p) {
        return x.sub(y).mul_(w).apply_(v -> pow(abs(v), p)).sum();
    }

    private Tensor<Double> initializeCentroids(Random random, Tensor<Double> x) {

        Distance d = new MinkowskiDistance(p.get());
        Tensor<Double> bestCentroids = init.get().init(random, d, x, k.get());
        double bestError = computeError(x, bestCentroids);
        LOGGER.fine("Initialization of centroids round 1 computed error: " + bestError);

        // compute initial restarts if nstart is greater than 1
        // the best restart is kept as initial centroids

        if (nstart.get() > 1) {
            for (int i = 1; i < nstart.get(); i++) {
                Tensor<Double> nextCentroids = init.get().init(random, d, x, k.get());
                double nextError = computeError(x, nextCentroids);
                LOGGER.fine("Initialization of centroids, round %d, computed error: %f"
                        .formatted(i + 1, nextError));
                if (nextError < bestError) {
                    bestCentroids = nextCentroids;
                    bestError = nextError;
                    LOGGER.fine("Initial best error improved at: " + bestError + " at iteration " + (i + 1));
                }
            }
        }
        return bestCentroids;
    }

    @Override
    public MWKMeans coreFit(Frame df, Var w) {

        weights = subspace.get()
                ? Tensors.full(Shape.of(k.get(), inputNames.length), 1.0 / inputNames.length)
                : Tensors.full(Shape.of(1, inputNames.length), 1.0 / inputNames.length);
        LOGGER.finer("Initial weights: " + weights);
        errors = VarDouble.empty().name("errors");

        // initialize design matrix
        Tensor<Double> x = df.mapVars(inputNames).dtNew();

        Random random = getRandom();

        // initialize centroids
        c = initializeCentroids(random, x);

        // assign instances to centroids and compute error
        int[] assign = computeAssignmentAndError(x, true);

        int rounds = runs.get();
        while (rounds-- > 0) {

            weightsUpdate(x, assign);
            LOGGER.finer("Weights: " + weights.toString());
            recomputeCentroids(x, assign);
            assign = computeAssignmentAndError(x, true);

            if (runningHook != null) {
                learned = true;
                runningHook.get().accept(RunInfo.forClustering(this, runs.get() - rounds));
            }
            if (errors.size() > 1) {
                double previousError = errors.getDouble(errors.size() - 2);
                double lastError = errors.getDouble(errors.size() - 1);
                LOGGER.fine("prev error: " + previousError + ", current error: " + lastError + ", error improvement: " + (lastError
                        - previousError));
                if (previousError - lastError < eps.get() && lastError <= previousError) {
                    break;
                }
            }
        }
        learned = true;
        return this;
    }

    @Override
    public MWKMeansResult corePredict(Frame df, boolean withScores) {

        Tensor<Double> x = df.mapVars(inputNames).dtNew();
        int[] assign = computeAssignmentAndError(x, false);

        return MWKMeansResult.valueOf(this, df, VarInt.wrap(assign));
    }

    private double computeError(Tensor<Double> x, Tensor<Double> centroids) {
        return IntStream.range(0, x.dim(0)).parallel().mapToDouble(j -> {
            double d = Double.NaN;
            for (int c = 0; c < centroids.dim(0); c++) {
                Tensor<Double> w = subspace.get() ? weights.takesq(0, c) : weights.takesq(0, 0);
                double dd = error(x.takesq(0, j), centroids.takesq(0, c), w, p.get());
                d = Double.isNaN(d) ? dd : Math.min(dd, d);
            }
            if (Double.isNaN(d)) {
                throw new RuntimeException("Cluster could not be computed.");
            }
            return d;
        }).sum();
    }

    private int[] computeAssignmentAndError(Tensor<Double> x, boolean withErrors) {
        int[] assignment = new int[x.dim(0)];
        double totalError = 0.0;
        for (int i = 0; i < x.dim(0); i++) {
            double error = Double.NaN;
            int cluster = -1;
            for (int j = 0; j < c.dim(0); j++) {
                Tensor<Double> w = subspace.get() ? weights.takesq(0, j) : weights.takesq(0, 0);
                double currentError = error(x.takesq(0, i), c.takesq(0, j), w, p.get());
                if (!Double.isFinite(currentError)) {
                    continue;
                }
                if (Double.isNaN(error) || (currentError < error)) {
                    error = currentError;
                    cluster = j;
                }
            }
            if (cluster == -1) {
                LOGGER.severe("Cluster could not be found during assign to centroids.");
                throw new RuntimeException("Cluster could not be computed");
            }
            totalError += error;
            assignment[i] = cluster;
        }
        if (withErrors) {
            errors.addDouble(totalError);
        }
        return assignment;
    }

    /**
     * Computes indexes of all instances assigned to a centroid.
     */
    private int[] computeCentroidIndexes(int c, int[] assign) {
        int len = 0;
        for (int index : assign) {
            if (index == c) {
                len++;
            }
        }
        int[] set = new int[len];
        int pos = 0;
        for (int i = 0; i < assign.length; i++) {
            if (assign[i] == c) {
                set[pos++] = i;
            }
        }
        return set;
    }

    double error(Tensor<Double> x, double beta, double c) {
        return x.apply(v -> pow(abs(v - c), beta)).sum();
    }

    double derivative(Tensor<Double> x, double beta, double c) {
        double value = 0;
        for (int i = 0; i < x.size(); i++) {
            double v = x.getDouble(i);
            if (x.getDouble(i) < c) {
                value += pow(c - v, beta - 1);
            } else {
                value -= pow(v - c, beta - 1);
            }
        }
        return beta * value;
    }

    double findLeft(Tensor<Double> y, double beta, double c) {
        int left = 0;
        double min = error(y, beta, y.getDouble(left));
        for (int i = 1; i < y.size(); i++) {
            if (y.getDouble(i) >= c) {
                break;
            }
            double e = error(y, beta, y.getDouble(i));
            if (e < min) {
                left = i;
                min = e;
            }
        }
        return y.getDouble(left);
    }

    double findRight(Tensor<Double> y, double beta, double c) {
        int right = y.size() - 1;
        double min = error(y, beta, y.getDouble(right));
        for (int i = right - 1; i >= 0; i--) {
            if (y.getDouble(i) <= c) {
                break;
            }
            double e = error(y, beta, y.getDouble(i));
            if (e < min) {
                right = i;
                min = e;
            }
        }
        return y.getDouble(right);
    }

    double findMinimum(Tensor<Double> y, double beta) {
        Tensor<Double> errors = y.apply(v -> error(y, beta, v));
        double c0 = y.getDouble(errors.argmin());
        double left = findLeft(y, beta, c0);
        double right = findRight(y, beta, c0);

        double c;
        while (true) {
            // perhaps this should be put as parameter
            if (right - left <= eps.get()) {
                c = left;
                break;
            }
            double mid = left + (right - left) / 2;
            if (mid <= left || mid >= right) {
                // for numerical reasons we break
                c = left;
                break;
            }
            double d = derivative(y, beta, mid);
            if (d > 0) {
                right = mid;
            } else {
                left = mid;
            }
        }
        return c;
    }

    private void recomputeCentroids(Tensor<Double> x, int[] assign) {
        for (int i = 0; i < k.get(); i++) {
            int[] indexes = computeCentroidIndexes(i, assign);
            Tensor<Double> xc = x.take(0, indexes);
            for (int j = 0; j < x.dim(1); j++) {
                Tensor<Double> ykj = xc.takesq(1, j).copy();
                if (p.get() > 1) {
                    c.setDouble(findMinimum(ykj, p.get()), i, j);
                } else {
                    c.setDouble(Quantiles.of(ykj.dv(), 0.5).values()[0], i, j);
                }
            }
        }
    }

    private void weightsUpdate(Tensor<Double> x, int[] assign) {
        if (subspace.get()) {
            subspaceWeightsUpdate(x, assign);
        } else {
            globalWeightsUpdate(x, assign);
        }
    }

    private void subspaceWeightsUpdate(Tensor<Double> x, int[] assign) {
        Tensor<Double> dm = c.take(0, assign).sub(x).apply_(v -> pow(abs(v), p.get()));

        Tensor<Double> dv = Tensors.zeros(Shape.of(k.get(), dm.dim(1)));
        for (int i = 0; i < dm.dim(0); i++) {
            for (int j = 0; j < dm.dim(1); j++) {
                dv.incDouble(dm.getDouble(i, j), assign[i], j);
            }
        }
        // avoid division by 0
        dv.clamp_(1e-10, Double.NaN);

        double invPow = p.get() > 1 ? 1. / (p.get() - 1) : 0;

        for (int i = 0; i < dv.dim(0); i++) {
            for (int j = 0; j < dv.dim(1); j++) {
                double v = 0;
                for (int l = 0; l < dv.dim(1); l++) {
                    v += pow(dv.getDouble(i, j) / dv.getDouble(i, l), invPow);
                }
                weights.setDouble(1 / v, i, j);
            }
        }
    }

    private void globalWeightsUpdate(Tensor<Double> x, int[] assign) {
        Tensor<Double> dv = c.take(0, assign).sub(x).apply_(v -> pow(abs(v), p.get())).sum(0);
        // avoid division by 0
        dv.clamp_(1e-10, Double.NaN);

        double invPow = p.get() > 1 ? 1. / (p.get() - 1) : 0;
        for (int i = 0; i < dv.size(); i++) {
            double v = 0;
            for (int j = 0; j < dv.size(); j++) {
                v += pow(dv.getDouble(i) / dv.getDouble(j), invPow);
            }
            weights.setDouble(1 / v, 0, i);
        }
    }

    public Tensor<Double> getCentroidsMatrix() {
        return c;
    }

    public Tensor<Double> getWeightsMatrix() {
        return weights;
    }

    public double getError() {
        return errors.size() == 0 ? Double.NaN : errors.getDouble(errors.size() - 1);
    }

    public VarDouble getErrors() {
        return errors.copy();
    }

    @Override
    public String toString() {
        return fullName() + ", fitted=" + hasLearned();
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(fullName()).append("\n");
        sb.append("Model fitted=").append(hasLearned()).append("\n");
        if (learned) {
            sb.append("Inertia:").append(getError()).append("\n");
            sb.append("Iterations:").append(errors.size()).append("\n");
            sb.append("Clusters:").append(c.dim(0)).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POpt<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(toSummary(printer, options));
        if (hasLearned()) {
            sb.append("Centroids:\n");
            sb.append(c.toFullContent(printer, options));
            sb.append("Weights:\n");
            sb.append(weights.toFullContent(printer, options));
        }
        return sb.toString();
    }
}
