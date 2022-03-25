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

package rapaio.ml.model.km;

import static rapaio.math.MathTools.*;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import rapaio.core.stat.Quantiles;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarType;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.ml.common.distance.Distance;
import rapaio.ml.common.distance.MinkowskiDistance;
import rapaio.ml.model.ClusteringModel;
import rapaio.ml.model.RunInfo;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

/**
 * Minkowsky Weighted KMeans
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/27/17.
 */
public class MWKMeans extends ClusteringModel<MWKMeans, MWKMeansResult, RunInfo<MWKMeans>> {

    public static MWKMeans newMWKMeans() {
        return new MWKMeans();
    }

    private static final Logger LOGGER = Logger.getLogger(MWKMeans.class.getName());

    /**
     * Specifies the desired number of centroids.
     */
    public ValueParam<Integer, MWKMeans> k = new ValueParam<>(this, 2, "k");

    /**
     * Power of Minkowski metric. Value should be greater than 1
     */
    public ValueParam<Double, MWKMeans> p = new ValueParam<>(this, 2.0, "p", v -> Double.isFinite(v) && v >= 1);

    /**
     * Number of restarts when choosing the initial centroids.
     */
    public ValueParam<Integer, MWKMeans> nstart = new ValueParam<>(this, 1, "nstart", v -> v >= 1);

    /**
     * Cluster initialization method.
     */
    public ValueParam<KMClusterInit, MWKMeans> init = new ValueParam<>(this, KMClusterInit.Forgy, "init");

    /**
     * Subspace clustering flag. If true, a separate set of weights for each cluster will be used, otherwise
     * a global set of weights will be used.
     */
    public ValueParam<Boolean, MWKMeans> subspace = new ValueParam<>(this, false, "subspace clustering flag");

    /**
     * Threshold value used as a criteria for assessing convergence.
     */
    public ValueParam<Double, MWKMeans> eps = new ValueParam<>(this, 1e-10, "eps");

    // clustering artifacts

    private DMatrix c;
    private DMatrix weights;
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
        return new Capabilities(1, Integer.MAX_VALUE, List.of(VarType.DOUBLE, VarType.INT, VarType.BINARY), false, 0, 0, List.of(), true);
    }

    public double distance(DVector x, DVector y, DVector w, double p) {
        return pow(error(x, y, w, p), 1 / p);
    }

    private double error(DVector x, DVector y, DVector w, double p) {
        return x.subNew(y).mul(w).apply(v -> pow(abs(v), p)).sum();
    }

    private DMatrix initializeCentroids(DMatrix x) {

        Distance d = new MinkowskiDistance(p.get());
        DMatrix bestCentroids = init.get().init(d, x, k.get());
        double bestError = computeError(x, bestCentroids);
        LOGGER.fine("Initialization of centroids round 1 computed error: " + bestError);

        // compute initial restarts if nstart is greater than 1
        // the best restart is kept as initial centroids

        if (nstart.get() > 1) {
            for (int i = 1; i < nstart.get(); i++) {
                DMatrix nextCentroids = init.get().init(d, x, k.get());
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
                ? DMatrix.fill(k.get(), inputNames.length, 1.0 / inputNames.length)
                : DMatrix.fill(1, inputNames.length, 1.0 / inputNames.length);
        LOGGER.finer("Initial weights: " + weights);
        errors = VarDouble.empty().name("errors");

        // initialize design matrix
        DMatrix x = DMatrix.copy(df.mapVars(inputNames));

        // initialize centroids
        c = initializeCentroids(x);

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

        DMatrix x = DMatrix.copy(df.mapVars(inputNames));
        int[] assign = computeAssignmentAndError(x, false);

        return MWKMeansResult.valueOf(this, df, VarInt.wrap(assign));
    }

    private double computeError(DMatrix x, DMatrix centroids) {
        return IntStream.range(0, x.rows()).parallel().mapToDouble(j -> {
            double d = Double.NaN;
            for (int c = 0; c < centroids.rows(); c++) {
                DVector w = subspace.get() ? weights.mapRow(c) : weights.mapRow(0);
                double dd = error(x.mapRow(j), centroids.mapRow(c), w, p.get());
                d = Double.isNaN(d) ? dd : Math.min(dd, d);
            }
            if (Double.isNaN(d)) {
                throw new RuntimeException("Cluster could not be computed.");
            }
            return d;
        }).sum();
    }

    private int[] computeAssignmentAndError(DMatrix x, boolean withErrors) {
        int[] assignment = new int[x.rows()];
        double totalError = 0.0;
        for (int i = 0; i < x.rows(); i++) {
            double error = Double.NaN;
            int cluster = -1;
            for (int j = 0; j < c.rows(); j++) {
                DVector w = subspace.get() ? weights.mapRow(j) : weights.mapRow(0);
                double currentError = error(x.mapRow(i), c.mapRow(j), w, p.get());
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

    double error(DVector x, double beta, double c) {
        return x.applyNew(v -> pow(abs(v - c), beta)).sum();
    }

    double derivative(DVector x, double beta, double c) {
        double value = 0;
        for (int i = 0; i < x.size(); i++) {
            double v = x.get(i);
            if (x.get(i) < c) {
                value += pow(c - v, beta - 1);
            } else {
                value -= pow(v - c, beta - 1);
            }
        }
        return beta * value;
    }

    double findLeft(DVector y, double beta, double c) {
        int left = 0;
        double min = error(y, beta, y.get(left));
        for (int i = 1; i < y.size(); i++) {
            if (y.get(i) >= c) {
                break;
            }
            double e = error(y, beta, y.get(i));
            if (e < min) {
                left = i;
                min = e;
            }
        }
        return y.get(left);
    }

    double findRight(DVector y, double beta, double c) {
        int right = y.size() - 1;
        double min = error(y, beta, y.get(right));
        for (int i = right - 1; i >= 0; i--) {
            if (y.get(i) <= c) {
                break;
            }
            double e = error(y, beta, y.get(i));
            if (e < min) {
                right = i;
                min = e;
            }
        }
        return y.get(right);
    }

    double findMinimum(DVector y, double beta) {
        DVector errors = y.applyNew(v -> error(y, beta, v));
        double c0 = y.get(errors.argmin());
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

    private void recomputeCentroids(DMatrix x, int[] assign) {
        for (int i = 0; i < k.get(); i++) {
            int[] indexes = computeCentroidIndexes(i, assign);
            DMatrix xc = x.mapRows(indexes);
            for (int j = 0; j < x.cols(); j++) {
                DVector ykj = xc.mapColNew(j);
                if (p.get() > 1) {
                    c.set(i, j, findMinimum(ykj, p.get()));
                } else {
                    c.set(i, j, Quantiles.of(ykj.dv(), 0.5).values()[0]);
                }
            }
        }
    }

    private void weightsUpdate(DMatrix x, int[] assign) {
        if (subspace.get()) {
            subspaceWeightsUpdate(x, assign);
        } else {
            globalWeightsUpdate(x, assign);
        }
    }

    private void subspaceWeightsUpdate(DMatrix x, int[] assign) {
        DMatrix dm = c.mapRows(assign).subNew(x).apply(v -> pow(abs(v), p.get()));

        DMatrix dv = DMatrix.empty(k.get(), dm.cols());
        for (int i = 0; i < dm.rows(); i++) {
            for (int j = 0; j < dm.cols(); j++) {
                dv.inc(assign[i], j, dm.get(i, j));
            }
        }
        // avoid division by 0
        dv.apply(v -> max(v, 1e-10));

        double invPow = p.get() > 1 ? 1 / (p.get() - 1) : 0;

        for (int i = 0; i < dv.rows(); i++) {
            for (int j = 0; j < dv.cols(); j++) {
                double v = 0;
                for (int l = 0; l < dv.cols(); l++) {
                    v += pow(dv.get(i, j) / dv.get(i, l), invPow);
                }
                weights.set(i, j, 1 / v);
            }
        }
    }

    private void globalWeightsUpdate(DMatrix x, int[] assign) {
        DVector dv = c.mapRows(assign).subNew(x).apply(v -> pow(abs(v), p.get())).sum(0);
        // avoid division by 0
        dv.apply(v -> max(v, 1e-10));

        double invPow = p.get() > 1 ? 1 / (p.get() - 1) : 0;
        for (int i = 0; i < dv.size(); i++) {
            double v = 0;
            for (int j = 0; j < dv.size(); j++) {
                v += pow(dv.get(i) / dv.get(j), invPow);
            }
            weights.set(0, i, 1 / v);
        }
    }

    public DMatrix getCentroidsMatrix() {
        return c;
    }

    public DMatrix getWeightsMatrix() {
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
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append(fullName()).append("\n");
        sb.append("Model fitted=").append(hasLearned()).append("\n");
        if (learned) {
            sb.append("Inertia:").append(getError()).append("\n");
            sb.append("Iterations:").append(errors.size()).append("\n");
            sb.append("Clusters:").append(c.rows()).append("\n");
        }
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
        if (hasLearned()) {
            sb.append("Centroids:\n");
            sb.append(c.toFullContent(printer, options));
            sb.append("Weights:\n");
            sb.append(weights.toFullContent(printer, options));
        }
        return sb.toString();
    }
}
