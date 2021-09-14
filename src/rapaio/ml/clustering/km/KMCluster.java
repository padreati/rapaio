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

package rapaio.ml.clustering.km;

import rapaio.core.RandomSource;
import rapaio.core.stat.Mean;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Unique;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarType;
import rapaio.data.filter.VSort;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.clustering.ClusteringModel;
import rapaio.ml.clustering.DefaultHookInfo;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.collection.IntArrays;

import java.io.Serial;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * KMeans clustering algorithm
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KMCluster extends ClusteringModel<KMCluster, KMClusterResult, DefaultHookInfo> {

    public static KMCluster newKMeans() {
        return new KMCluster()
                .method.set(KMeans);
    }

    public static KMCluster newKMedians() {
        return new KMCluster()
                .method.set(KMedians);
    }

    public interface Method {

        /**
         * Computes the distance between two observation vectors
         *
         * @param u first vector
         * @param v second vector
         * @return distance between vectors
         */
        double distance(DVector u, DVector v);

        /**
         * Computes the error contribution of the observation v
         * when assigned to cluster with centroid c
         *
         * @param c cluster centroid
         * @param v observation vector
         * @return error contributed by this observation
         */
        double observationError(DVector c, DVector v);

        void recomputeCentroids(int k, DMatrix c, DMatrix instances, int[] assignment);
    }

    public static Method KMeans = new Method() {

        @Override
        public double distance(DVector u, DVector v) {
            double distance = 0;
            for (int i = 0; i < u.size(); i++) {
                double delta = u.get(i) - v.get(i);
                distance += delta * delta;
            }
            return Math.sqrt(distance);
        }

        @Override
        public double observationError(DVector c, DVector v) {
            double distance = 0;
            for (int i = 0; i < c.size(); i++) {
                double delta = c.get(i) - v.get(i);
                distance += delta * delta;
            }
            return distance;
        }

        public void recomputeCentroids(int k, DMatrix c, DMatrix instances, int[] assignment) {

            // we compute mean for each feature separately
            for (int j = 0; j < instances.colCount(); j++) {
                // collect values for each cluster in mean, for a given input feature
                Var[] means = IntStream.range(0, k).mapToObj(i -> VarDouble.empty()).toArray(VarDouble[]::new);
                for (int i = 0; i < instances.rowCount(); i++) {
                    means[assignment[i]].addDouble(instances.get(i, j));
                }
                for (int i = 0; i < k; i++) {
                    c.set(i, j, Mean.of(means[i]).value());
                }
            }
        }

        @Override
        public String toString() {
            return "KMeans";
        }
    };

    public static Method KMedians = new Method() {

        @Override
        public double distance(DVector u, DVector v) {
            double distance = 0;
            for (int i = 0; i < u.size(); i++) {
                distance += Math.abs(u.get(i) - v.get(i));
            }
            return distance;
        }

        @Override
        public double observationError(DVector u, DVector v) {
            return distance(u, v);
        }

        public void recomputeCentroids(int k, DMatrix c, DMatrix instances, int[] assignment) {

            // we compute mean for each feature separately
            for (int j = 0; j < instances.colCount(); j++) {
                // collect values for each cluster in mean, for a given input feature
                Var[] means = IntStream.range(0, k).mapToObj(i -> VarDouble.empty()).toArray(VarDouble[]::new);
                for (int i = 0; i < instances.rowCount(); i++) {
                    means[assignment[i]].addDouble(instances.get(i, j));
                }
                for (int i = 0; i < k; i++) {
                    Var sorted = means[i].apply(VSort.ascending());
                    c.set(i, j, sorted.getDouble(sorted.size() / 2));
                }
            }
        }

        @Override
        public String toString() {
            return "KMedians";
        }
    };

    @Serial
    private static final long serialVersionUID = -1046184364541391871L;

    public final ValueParam<Integer, KMCluster> k = new ValueParam<>(this, null, "k", "number of clusters", Objects::nonNull);
    public final ValueParam<Integer, KMCluster> nstart = new ValueParam<>(this, 1, "nstart", "Number of restarts", n -> n != null && n > 0);
    public final ValueParam<KMClusterInit, KMCluster> init =
            new ValueParam<>(this, KMClusterInit.Forgy, "init", "Initialization algorithm");
    public final ValueParam<Method, KMCluster> method = new ValueParam<>(this, null, "method", "method: kmeans, kmedians", Objects::nonNull);
    public final ValueParam<Double, KMCluster> eps = new ValueParam<>(this, 1e-20, "eps", "Tolerance for convergence measures");

    // clustering artifacts

    private DMatrix c;
    private Frame centroids;
    private VarDouble errors;

    @Override
    public KMCluster newInstance() {
        return new KMCluster().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "KMCluster";
    }

    public Frame getCentroids() {
        return centroids;
    }

    public VarDouble getErrors() {
        return errors;
    }

    public double getError() {
        return errors.size() == 0 ? Double.NaN : errors.getDouble(errors.size() - 1);
    }

    public DMatrix getCentroidsMatrix() {
        return c;
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities(1, 10_000,
                List.of(VarType.DOUBLE, VarType.INT, VarType.BINARY), true,
                0, 0, List.of(), true);
    }

    @Override
    public KMCluster coreFit(Frame initialDf, Var weights) {

        DMatrix m = DMatrix.copy(initialDf);
        c = initializeClusters(m);

        int[] assignment = IntArrays.newFill(m.rowCount(), -1);
        errors = VarDouble.empty().name("errors");

        assignToCentroids(m, assignment, true);
        repairEmptyClusters(m, assignment);

        int rounds = runs.get();
        while (rounds-- > 0) {
            method.get().recomputeCentroids(k.get(), c, m, assignment);
            assignToCentroids(m, assignment, true);
            repairEmptyClusters(m, assignment);

            if (runningHook != null) {
                learned = true;
                runningHook.get().accept(new DefaultHookInfo(this, runs.get() - rounds));
            }
            int erc = errors.size();
            if (erc > 1 && errors.getDouble(erc - 2) - errors.getDouble(erc - 1) < eps.get()
                    && errors.getDouble(erc - 1) <= errors.getDouble(erc - 2)) {
                break;
            }
        }
        centroids = SolidFrame.matrix(c, inputNames);
        learned = true;
        return this;
    }

    private DMatrix initializeClusters(DMatrix m) {
        DMatrix bestCentroids = init.get().init(method.get(), m, k.get());
        double bestError = computeInitError(m, bestCentroids);

        // compute initial restarts if nstart is greater than 1
        // the best restart is kept as initial centroids

        for (int i = 1; i < nstart.get(); i++) {
            DMatrix nextCentroids = init.get().init(method.get(), m, k.get());
            double nextError = computeInitError(m, nextCentroids);
            if (nextError < bestError) {
                bestCentroids = nextCentroids;
                bestError = nextError;
            }
        }
        return bestCentroids;
    }

    private double computeInitError(DMatrix m, DMatrix centroids) {
        double sum = 0;
        for (int i = 0; i < m.rowCount(); i++) {
            DVector mrow = m.mapRow(i);
            int cluster = findClosestCentroid(mrow, centroids);
            sum += method.get().observationError(centroids.mapRow(cluster), mrow);
        }
        return sum;
    }

    private void assignToCentroids(DMatrix m, int[] assignment, boolean withErrors) {
        double totalError = 0.0;
        for (int i = 0; i < m.rowCount(); i++) {
            DVector row = m.mapRow(i);
            int cluster = findClosestCentroid(row, c);
            if (withErrors) {
                totalError += method.get().observationError(c.mapRow(cluster), row);
            }
            assignment[i] = cluster;
        }
        if (withErrors) {
            errors.addDouble(totalError);
        }
    }

    private int findClosestCentroid(DVector mrow, DMatrix centroids) {
        int cluster = 0;
        double d = method.get().distance(mrow, centroids.mapRow(0));
        for (int j = 1; j < centroids.rowCount(); j++) {
            double dd = method.get().distance(mrow, centroids.mapRow(j));
            if (d > dd) {
                d = dd;
                cluster = j;
            }
        }
        return cluster;
    }

    private void repairEmptyClusters(DMatrix df, int[] assignment) {
        // check for empty clusters, if any is found then
        // select random points to be new clusters, different than
        // existing clusters

        var unique = Unique.ofInt(VarInt.wrap(assignment));
        if (unique.uniqueCount() == k.get()) {
            return;
        }

        HashSet<Integer> emptyCentroids = new HashSet<>();
        for (int i = 0; i < unique.uniqueCount(); i++) {
            if (unique.rowList(i).isEmpty()) {
                emptyCentroids.add(unique.uniqueValue(i));
            }
        }

        // replace each empty cluster

        Iterator<Integer> it = emptyCentroids.iterator();
        while (it.hasNext()) {
            int next = it.next();
            while (true) {
                int selection = RandomSource.nextInt(df.rowCount());
                boolean found = false;

                // check if it does not collide with existent valid clusters

                for (int i = 0; i < c.rowCount(); i++) {
                    if (emptyCentroids.contains(i)) {
                        continue;
                    }
                    if (!checkIfEqual(c, i, df, next)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }

                // we found a valid centroid, it will be assigned

                for (int j = 0; j < c.colCount(); j++) {
                    c.set(next, j, df.get(selection, j));
                }
                break;
            }

            // remove centroid from empty set

            it.remove();
        }

        // rebuilt errors and cluster assignment
        // if empty clusters happens again, then that is it, we did our best
        // the stopping criterion is given by a bound on error or a
        // maximum iteration

        method.get().recomputeCentroids(k.get(), c, df, assignment);
    }

    private boolean checkIfEqual(DMatrix centroids, int c, DMatrix df, int i) {
        int count = 0;
        for (int j = 0; j < centroids.colCount(); j++) {
            if (centroids.get(c, j) == df.get(i, j)) {
                count++;
            }
        }
        return count == inputNames.length;
    }

    @Override
    public KMClusterResult corePredict(Frame df, boolean withScores) {
        int[] assignment = IntArrays.newFill(df.rowCount(), -1);
        DMatrix m = DMatrix.copy(df);
        assignToCentroids(m, assignment, false);
        return KMClusterResult.valueOf(this, df, VarInt.wrap(assignment));
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
            sb.append("Learned clusters:").append(centroids.rowCount()).append("\n");
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
            sb.append(getCentroids().toFullContent(printer, options));
        }
        return sb.toString();
    }
}
