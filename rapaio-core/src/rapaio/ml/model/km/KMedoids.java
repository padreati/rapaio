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

import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import rapaio.core.SamplingTools;
import rapaio.core.param.ValueParam;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.math.tensor.Tensor;
import rapaio.ml.common.distance.Distance;
import rapaio.ml.common.distance.Manhattan;
import rapaio.ml.model.ClusteringModel;
import rapaio.ml.model.ClusteringResult;
import rapaio.ml.model.RunInfo;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.collection.IntArrays;

/**
 * KMedoids clustering algorithms. Implemented methods are alternate and PAM. Fast PAM has to be implemented.
 *
 * <ul>
 * <li>ALTERNATE</li> method implemented according with the description presented in
 * "A simple and fast algorithm for K-medoids clustering - Hae-Sang Park, Chi-Hyuck Jun"
 * <li>PAM</li> method implemented according with the description presented in "https://www.cs.umb.edu/cs738/pam1.pdf"
 * <li>FAST_PAM</li> method implemented according with description presented in
 * "Fast and eager k-medoids clustering:  runtime improvement of the PAM, CLARA, and CLARANS algorithms"
 * </ul>
 */
public class KMedoids extends ClusteringModel<KMedoids, ClusteringResult<KMedoids>, RunInfo<KMedoids>> {

    public static KMedoids newAlternateModel(int k) {
        return new KMedoids().method.set(Method.ALTERNATE).k.set(k);
    }

    public static KMedoids newPAMModel(int k) {
        return new KMedoids().method.set(Method.PAM).k.set(k);
    }

    private static final Logger LOGGER = Logger.getLogger(KMedoids.class.getName());

    /**
     * Method used to fit KMedoids algorithm.
     */
    public enum Method {
        ALTERNATE,
        PAM
    }

    public final ValueParam<Method, KMedoids> method = new ValueParam<>(this, Method.PAM, "method");
    public final ValueParam<Integer, KMedoids> k = new ValueParam<>(this, 1, "k");
    public final ValueParam<Distance, KMedoids> distance = new ValueParam<>(this, new Manhattan(), "distance");
    public final ValueParam<Integer, KMedoids> maxIt = new ValueParam<>(this, 1000, "maxIt");

    private Tensor<Double> c;
    private VarDouble errors;

    private KMedoids() {
    }

    @Override
    public KMedoids newInstance() {
        return new KMedoids().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "KMedoids";
    }

    public Tensor<Double> getCentroidsMatrix() {
        return c;
    }

    @Override
    public KMedoids coreFit(Frame df, Var weights) {
        Tensor<Double> x = df.mapVars(inputNames).dtNew();
        if (k.get() > x.dim(0)) {
            throw new IllegalArgumentException(
                    "Number of clusters %d bigger than number of instances %d.".formatted(k.get(), x.dim(0)));
        }

        if (method.get() == Method.ALTERNATE) {
            coreFitAlternate(x);
        } else {
            coreFitPAM(x);
        }
        return this;
    }

    void coreFitAlternate(Tensor<Double> x) {
        LOGGER.fine("Starting core fit for alternate method.");
        LOGGER.finest("Initialize centroids as random instances.");
        int[] centroidIndexes = SamplingTools.sampleWOR(new Random(seed.get()), x.dim(0), k.get());
        LOGGER.finest("medoid indexes: " + Arrays.stream(centroidIndexes).mapToObj(String::valueOf).collect(Collectors.joining(",")));
        c = x.take(0, centroidIndexes);

        LOGGER.finest("Initialize a cache for training purposes");
        DistanceCache cache = new DistanceCache(x.dim(0), distance.get());

        LOGGER.finest("Assign instances to centroids.");
        int[] assign = computeAssignment(x, centroidIndexes, cache);

        LOGGER.finest("Compute errors and store them for assessment.");
        double error = computeError(x, assign, centroidIndexes, cache);
        LOGGER.finest("Computed error: %.6f".formatted(error));
        errors = VarDouble.empty().name("errors");
        errors.addDouble(error);


        for (int i = 0; i < maxIt.get(); i++) {
            LOGGER.fine("Iteration %d".formatted(i + 1));
            // try to find the best swap
            LOGGER.finest("candidate medoid indexes: " + Arrays.stream(centroidIndexes)
                    .mapToObj(String::valueOf).collect(Collectors.joining(",")));
            int[] nextCentroidIndexes = alternateSwap(x, centroidIndexes, assign, cache);
            int[] nextAssign = computeAssignment(x, nextCentroidIndexes, cache);
            double nextError = computeError(x, nextAssign, centroidIndexes, cache);
            LOGGER.finest("candidate medoid errors: %.6f".formatted(nextError));

            if (nextError < error) {
                errors.addDouble(nextError);
                error = nextError;
                LOGGER.finest("medoid indexes: " + Arrays.stream(centroidIndexes)
                        .mapToObj(String::valueOf).collect(Collectors.joining(",")));
                centroidIndexes = nextCentroidIndexes;
                assign = nextAssign;
                c = x.take(0, centroidIndexes);
                continue;
            }

            LOGGER.fine("No improvements detected, stop iterations.");
            return;
        }
    }

    /**
     * Finds new centroids, one from each cluster which will replace the medoid from that cluster if reduces the within cost.
     *
     * @param x      data matrix
     * @param cint   current indexes of the medoids
     * @param assign current assignment
     * @return new indexes for medoids
     */
    int[] alternateSwap(Tensor<Double> x, int[] cint, int[] assign, DistanceCache cache) {
        // each candidate for medoid will search only inside it's cluster

        HashMap<Integer, List<Integer>> clusters = new HashMap<>();
        for (int i = 0; i < assign.length; i++) {
            clusters.computeIfAbsent(assign[i], c -> new ArrayList<>()).add(i);
        }

        int[] newClusters = IntArrays.copy(cint);
        for (int i = 0; i < cint.length; i++) {

            int c = cint[i];
            List<Integer> cluster = clusters.get(c);
            if (cluster == null) {
                continue;
            }

            int bestCandidate = c;
            double bestError = errorWithinCluster(x, c, cluster, cache);

            for (int v : cluster) {
                if (v == c) {
                    continue;
                }
                double error = errorWithinCluster(x, v, cluster, cache);
                if (error < bestError) {
                    bestError = error;
                    bestCandidate = v;
                }
            }

            if (bestCandidate != c) {
                newClusters[i] = bestCandidate;
            }
        }
        return newClusters;
    }

    double errorWithinCluster(Tensor<Double> x, int c, List<Integer> cluster, DistanceCache cache) {
        if (cluster == null) {
            return 0.0;
        }
        double error = 0.0;
        Tensor<Double> cv = x.takesq(0, c);
        for (int i : cluster) {
            error += cache.get(c, i, cv, x.takesq(0, i));
        }
        return error;
    }

    /**
     * Computes assignment of instances to medoids by choosing for each
     * instance the closest medoid.
     *
     * @param x     instance matrix
     * @param cint  medoid indexes
     * @param cache cache used to speed up distance computations
     * @return vector of assignments which contains for each instance identified by it's index the index of the closest medoid
     */
    int[] computeAssignment(Tensor<Double> x, int[] cint, DistanceCache cache) {
        int[] assign = new int[x.dim(0)];
        for (int i = 0; i < assign.length; i++) {
            int min = 0;
            double dj = cache.get(i, cint[0], x.takesq(0, i), x.takesq(0, cint[0]));
            for (int j = 1; j < cint.length; j++) {
                double d = cache.get(i, cint[j], x.takesq(0, i), x.takesq(0, cint[j]));
                if (dj > d) {
                    dj = d;
                    min = j;
                }
            }
            assign[i] = min;
        }
        return assign;
    }

    /**
     * Computes the error produced by the given assignment of medoids.
     *
     * @param x      data instance matrix
     * @param assign assignment evaluated
     * @param cint   centroid indexes
     * @param cache  distance cache used to speed up computation
     * @return error for the given assignment and centroid configuration
     */
    double computeError(Tensor<Double> x, int[] assign, int[] cint, DistanceCache cache) {
        double error = 0;
        for (int i = 0; i < assign.length; i++) {
            error += cache.get(i, cint[assign[i]], x.takesq(0, i), x.takesq(0, cint[assign[i]]));
        }
        return error;
    }

    void coreFitPAM(Tensor<Double> x) {
        LOGGER.fine("Starting core fit for PAM method.");

        LOGGER.finest("Initialize a cache for training purposes");
        DistanceCache cache = new DistanceCache(x.dim(0), distance.get());

        // array which stores the distance to the closest centroid
        double[] dv = DoubleArrays.newFill(x.dim(0), Double.NaN);
        // array which stores the distance to the second closest centroid
        double[] ev = DoubleArrays.newFill(x.dim(0), Double.NaN);

        LOGGER.finest("Initialize centroids as random instances.");
        int[] centroidIndexes = initializePAM(x, dv, ev, cache);

        LOGGER.finest("medoid indexes: " + Arrays.stream(centroidIndexes)
                .mapToObj(String::valueOf).collect(Collectors.joining(",")));
        c = x.take(0, centroidIndexes);

        LOGGER.finest("Assign instances to centroids.");
        int[] assign = computeAssignment(x, centroidIndexes, cache);

        LOGGER.finest("Compute errors and store them for assessment.");
        double error = computeError(x, assign, centroidIndexes, cache);
        LOGGER.finest("Computed error: %.6f".formatted(error));
        errors = VarDouble.empty().name("errors");
        errors.addDouble(error);


        for (int i = 0; i < maxIt.get(); i++) {
            LOGGER.fine("Iteration %d".formatted(i + 1));
            // try to find the best swap
            LOGGER.finest("candidate medoid indexes: " + Arrays.stream(centroidIndexes)
                    .mapToObj(String::valueOf).collect(Collectors.joining(",")));
            int[] best = pamSwap(x, centroidIndexes, dv, ev, assign, cache);
            int bestBefore = best[0];
            int bestAfter = best[1];

            // next centroid indexes
            int[] nextCentroidIndexes = Arrays.copyOf(centroidIndexes, centroidIndexes.length);
            for (int j = 0; j < nextCentroidIndexes.length; j++) {
                if (nextCentroidIndexes[j] == bestBefore) {
                    nextCentroidIndexes[j] = bestAfter;
                }
            }

            int[] nextAssign = computeAssignment(x, nextCentroidIndexes, cache);
            double nextError = computeError(x, nextAssign, centroidIndexes, cache);
            LOGGER.finest("candidate medoid errors: %.6f".formatted(nextError));

            if (nextError < error) {
                errors.addDouble(nextError);
                error = nextError;

                LOGGER.finest(
                        "medoid indexes: " + Arrays.stream(centroidIndexes).mapToObj(String::valueOf).collect(Collectors.joining(",")));
                centroidIndexes = nextCentroidIndexes;
                assign = nextAssign;
                c = x.take(0, centroidIndexes);

                // update closest distance vectors
                updateAllClosest(x, centroidIndexes, dv, ev, cache);

                continue;
            }

            LOGGER.fine("No improvements detected, stop iterations.");
            return;
        }
    }

    int[] initializePAM(Tensor<Double> x, double[] dv, double[] ev, DistanceCache cache) {
        Set<Integer> centroidSet = new HashSet<>();
        int[] centroidIndexes = new int[k.get()];

        // first index which minimize the whole distance
        centroidIndexes[0] = peekFirstCentroid(x, cache);
        centroidSet.add(centroidIndexes[0]);
        for (int j = 0; j < x.dim(0); j++) {
            dv[j] = cache.get(centroidIndexes[0], j, x.takesq(0, centroidIndexes[0]), x.takesq(0, j));
        }
        for (int t = 1; t < k.get(); t++) {
            int next = peekNextCentroid(x, centroidSet, dv, cache);
            centroidIndexes[t] = next;
            centroidSet.add(next);

            updateNewClosest(x, next, dv, ev, cache);
        }

        return centroidIndexes;
    }

    void updateNewClosest(Tensor<Double> x, int next, double[] dv, double[] ev, DistanceCache cache) {
        for (int i = 0; i < x.dim(0); i++) {
            double d = cache.get(i, next, x.takesq(0, i), x.takesq(0, next));
            if (Double.isNaN(dv[i]) || d < dv[i]) {
                ev[i] = dv[i];
                dv[i] = d;
            } else {
                if (Double.isNaN(ev[i]) || d < ev[i]) {
                    ev[i] = d;
                }
            }
        }
    }

    void updateAllClosest(Tensor<Double> x, int[] centroidIndexes, double[] dv, double[] ev, DistanceCache cache) {
        Arrays.fill(dv, Double.NaN);
        Arrays.fill(ev, Double.NaN);
        for (int i = 0; i < x.dim(0); i++) {
            for (int c : centroidIndexes) {
                double d = cache.get(i, c, x.takesq(0, i), x.takesq(0, c));
                if (Double.isNaN(dv[i]) || d < dv[i]) {
                    ev[i] = dv[i];
                    dv[i] = d;
                } else {
                    if (Double.isNaN(ev[i]) || d < ev[i]) {
                        ev[i] = d;
                    }
                }
            }
        }
    }

    /**
     * Peek next centroid as the instance which decrease the objective function most.
     *
     * @param x           instances
     * @param centroidSet set of existent centroids
     * @param dv          values from the closest centroid
     * @param cache       cache of computed values
     * @return best centroid
     */
    int peekNextCentroid(Tensor<Double> x, Set<Integer> centroidSet, double[] dv, DistanceCache cache) {
        int next = -1;
        double nextG = Double.NaN;
        for (int i = 0; i < x.dim(0); i++) {
            if (centroidSet.contains(i)) {
                continue;
            }
            double g = 0.0;
            for (int j = 0; j < x.dim(0); j++) {
                if (i == j || centroidSet.contains(j)) {
                    continue;
                }
                g += max(0, dv[j] - cache.get(i, j, x.takesq(0, i), x.takesq(0, j)));
            }
            if (Double.isNaN(nextG) || nextG < g) {
                nextG = g;
                next = i;
            }
        }
        return next;
    }

    /**
     * Peek the first centroid as the instance which minimize the sum of distances from itself to all other instances.
     *
     * @param x     instance matrix
     * @param cache cache of distances
     * @return best centroid candidate
     */
    int peekFirstCentroid(Tensor<Double> x, DistanceCache cache) {
        int i = 0;
        double error = distanceFromCluster(x, i, cache);
        for (int j = 0; j < x.dim(0); j++) {
            double nextError = distanceFromCluster(x, j, cache);
            if (nextError < error) {
                error = nextError;
                i = j;
            }
        }
        return i;
    }

    double distanceFromCluster(Tensor<Double> x, int c, DistanceCache cache) {
        double total = 0;
        for (int i = 0; i < x.dim(0); i++) {
            total += cache.get(i, c, x.takesq(0, i), x.takesq(0, c));
        }
        return total;
    }

    int[] pamSwap(Tensor<Double> x, int[] centroidIndexes, double[] dv, double[] ev, int[] assign, DistanceCache cache) {

        int bestAfter = -1;
        int bestBefore = -1;
        double bestReduction = Double.NaN;

        for (int i : centroidIndexes) {
            for (int h = 0; h < x.dim(0); h++) {
                if (assign[h] == h) {
                    continue;
                }

                double reduction = 0.0;
                for (int j = 0; j < x.dim(0); j++) {
                    if (j == h) {
                        continue;
                    }
                    double dji = cache.get(i, j, x.takesq(0, i), x.takesq(0, j));
                    if (dji > dv[j]) {
                        reduction += min(0, cache.get(j, h, x.takesq(0, j), x.takesq(0, h)) - dv[j]);
                    } else {
                        reduction += min(cache.get(j, h, x.takesq(0, j), x.takesq(0, h)), ev[j]) - dv[j];
                    }
                }
                if (Double.isNaN(bestReduction) || bestReduction > reduction) {
                    bestAfter = h;
                    bestBefore = i;
                    bestReduction = reduction;
                }
            }
        }
        return new int[] {bestBefore, bestAfter};
    }

    @Override
    public ClusteringResult<KMedoids> corePredict(Frame df, boolean withScores) {
        Tensor<Double> x = df.mapVars(inputNames).dtNew();
        int[] assign = new int[x.dim(0)];
        for (int i = 0; i < assign.length; i++) {
            int min = 0;
            Tensor<Double> xi = x.takesq(0, i);
            double dj = distance.get().compute(xi, c.takesq(0, 0));
            for (int j = 1; j < c.dim(0); j++) {
                double d = distance.get().compute(xi, c.takesq(0, j));
                if (dj > d) {
                    dj = d;
                    min = j;
                }
            }
            assign[i] = min;
        }
        return new ClusteringResult<>(this, df, VarInt.wrap(assign));
    }

    /**
     * Cache implementation for distances.
     * This cache is not optimal for memory since it uses {@code n^2} memory instead of
     * {@code n(n+1)/2}. However, it avoids using a hash map or equivalent since that would waste
     * a lot of additional memory and induce memory fragmentation.
     */
    static final class DistanceCache {


        private final int len;
        private final double[] values;
        private final Distance distance;

        public DistanceCache(int len, Distance distance) {
            this.len = len;
            this.distance = distance;
            this.values = DoubleArrays.newFill(len * len, Double.NaN);
        }

        public double get(int i, int j, Tensor<Double> vi, Tensor<Double> vj) {
            double cached = getCache(i, j);
            if (!Double.isNaN(cached)) {
                return cached;
            }
            double d = distance.compute(vi, vj);
            setCache(i, j, d);
            return d;
        }

        private double getCache(int i, int j) {
            if (i > j) {
                return getCache(j, i);
            }
            return values[i * len + j];
        }

        private void setCache(int i, int j, double value) {
            values[(i > j) ? (j * len + i) : (i * len + j)] = value;
        }
    }

    @Override
    public String toString() {
        return fullName() + ", fitted=" + hasLearned();
    }
}
