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

package rapaio.ml.model.km;

import java.io.Serial;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

import rapaio.core.param.ValueParam;
import rapaio.core.stat.Mean;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Unique;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarType;
import rapaio.data.transform.VarSort;
import rapaio.math.narrays.NArray;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.distance.Distance;
import rapaio.ml.common.distance.EuclideanDistance;
import rapaio.ml.common.distance.Manhattan;
import rapaio.ml.model.ClusteringModel;
import rapaio.ml.model.RunInfo;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;
import rapaio.util.collection.IntArrays;

/**
 * KMeans clustering algorithm.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KMCluster extends ClusteringModel<KMCluster, KMClusterResult, RunInfo<KMCluster>> {

    public static KMCluster newKMeans() {
        return new KMCluster().method.set(KMeans);
    }

    public static KMCluster newKMedians() {
        return new KMCluster().method.set(KMedians);
    }

    public interface Method {

        Distance distance();

        void recomputeCentroids(int k, NArray<Double> c, NArray<Double> instances, int[] assignment);
    }

    public static final Method KMeans = new Method() {

        @Override
        public Distance distance() {
            return new EuclideanDistance();
        }

        public void recomputeCentroids(int k, NArray<Double> c, NArray<Double> instances, int[] assignment) {

            // we compute mean for each feature separately
            for (int j = 0; j < instances.dim(1); j++) {
                // collect values for each cluster in mean, for a given input feature
                Var[] means = IntStream.range(0, k).mapToObj(i -> VarDouble.empty()).toArray(VarDouble[]::new);
                for (int i = 0; i < instances.dim(0); i++) {
                    means[assignment[i]].addDouble(instances.getDouble(i, j));
                }
                for (int i = 0; i < k; i++) {
                    c.setDouble(Mean.of(means[i]).value(), i, j);
                }
            }
        }

        @Override
        public String toString() {
            return "KMeans";
        }
    };

    public static final Method KMedians = new Method() {

        @Override
        public Distance distance() {
            return new Manhattan();
        }

        public void recomputeCentroids(int k, NArray<Double> c, NArray<Double> instances, int[] assignment) {

            // we compute mean for each feature separately
            for (int j = 0; j < instances.dim(1); j++) {
                // collect values for each cluster in mean, for a given input feature
                Var[] means = IntStream.range(0, k).mapToObj(i -> VarDouble.empty()).toArray(VarDouble[]::new);
                for (int i = 0; i < instances.dim(0); i++) {
                    means[assignment[i]].addDouble(instances.getDouble(i, j));
                }
                for (int i = 0; i < k; i++) {
                    Var sorted = means[i].fapply(VarSort.ascending());
                    c.setDouble(sorted.getDouble(sorted.size() / 2), i, j);
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

    /**
     * Number of clusters.
     */
    public final ValueParam<Integer, KMCluster> k = new ValueParam<>(this, null, "k", Objects::nonNull);

    /**
     * Number of restarts at initialization of the centroids.
     */
    public final ValueParam<Integer, KMCluster> nstart = new ValueParam<>(this, 1, "nstart", n -> n != null && n > 0);

    /**
     * Cluster initialization algorithm.
     */
    public final ValueParam<KMClusterInit, KMCluster> init = new ValueParam<>(this, KMClusterInit.Forgy, "init");

    /**
     * Method: kmeans, kmedians.
     */
    public final ValueParam<Method, KMCluster> method = new ValueParam<>(this, null, "method", Objects::nonNull);

    /**
     * Tolerance for convergence criteria.
     */
    public final ValueParam<Double, KMCluster> eps = new ValueParam<>(this, 1e-20, "eps");

    // clustering artifacts

    private NArray<Double> c;
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

    public NArray<Double> getCentroidsMatrix() {
        return c;
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .inputs(1, 10_000, true, VarType.DOUBLE, VarType.INT, VarType.BINARY)
                .targets(0, 0, true);
    }

    @Override
    public KMCluster coreFit(Frame initialDf, Var weights) {

        Random random = getRandom();
        NArray<Double> m = initialDf.tensor();
        c = initializeClusters(random, m);

        int[] assignment = IntArrays.newFill(m.dim(0), -1);
        errors = VarDouble.empty().name("errors");

        assignToCentroids(m, assignment, true);
        repairEmptyClusters(random, m, assignment);

        int rounds = runs.get();
        while (rounds-- > 0) {
            method.get().recomputeCentroids(k.get(), c, m, assignment);
            assignToCentroids(m, assignment, true);
            repairEmptyClusters(random, m, assignment);

            if (runningHook != null) {
                learned = true;
                runningHook.get().accept(RunInfo.forClustering(this, runs.get() - rounds));
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

    private NArray<Double> initializeClusters(Random random, NArray<Double> m) {
        NArray<Double> bestCentroids = init.get().init(random, method.get().distance(), m, k.get());
        double bestError = computeInitError(m, bestCentroids);

        // compute initial restarts if nstart is greater than 1
        // the best restart is kept as initial centroids

        for (int i = 1; i < nstart.get(); i++) {
            NArray<Double> nextCentroids = init.get().init(random, method.get().distance(), m, k.get());
            double nextError = computeInitError(m, nextCentroids);
            if (nextError < bestError) {
                bestCentroids = nextCentroids;
                bestError = nextError;
            }
        }
        return bestCentroids;
    }

    private double computeInitError(NArray<Double> m, NArray<Double> centroids) {
        double sum = 0;
        for (int i = 0; i < m.dim(0); i++) {
            NArray<Double> mrow = m.takesq(0, i);
            int cluster = findClosestCentroid(mrow, centroids);
            sum += method.get().distance().reduced(centroids.takesq(0, cluster), mrow);
        }
        return sum;
    }

    private void assignToCentroids(NArray<Double> m, int[] assignment, boolean withErrors) {
        double totalError = 0.0;
        for (int i = 0; i < m.dim(0); i++) {
            NArray<Double> row = m.takesq(0, i);
            int cluster = findClosestCentroid(row, c);
            if (withErrors) {
                totalError += method.get().distance().reduced(c.takesq(0, cluster), row);
            }
            assignment[i] = cluster;
        }
        if (withErrors) {
            errors.addDouble(totalError);
        }
    }

    private int findClosestCentroid(NArray<Double> mrow, NArray<Double> centroids) {
        int cluster = 0;
        double d = method.get().distance().compute(mrow, centroids.takesq(0, 0));
        for (int j = 1; j < centroids.dim(0); j++) {
            double dd = method.get().distance().compute(mrow, centroids.takesq(0, j));
            if (d > dd) {
                d = dd;
                cluster = j;
            }
        }
        return cluster;
    }

    private void repairEmptyClusters(Random random, NArray<Double> df, int[] assignment) {
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
                int selection = random.nextInt(df.dim(0));
                boolean found = false;

                // check if it does not collide with existent valid clusters

                for (int i = 0; i < c.dim(0); i++) {
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

                for (int j = 0; j < c.dim(1); j++) {
                    c.setDouble(df.getDouble(selection, j), next, j);
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

    private boolean checkIfEqual(NArray<Double> centroids, int c, NArray<Double> df, int i) {
        int count = 0;
        for (int j = 0; j < centroids.dim(1); j++) {
            if (centroids.getDouble(c, j) == df.getDouble(i, j)) {
                count++;
            }
        }
        return count == inputNames.length;
    }

    @Override
    public KMClusterResult corePredict(Frame df, boolean withScores) {
        int[] assignment = IntArrays.newFill(df.rowCount(), -1);
        NArray<Double> m = df.tensor();
        assignToCentroids(m, assignment, false);
        return KMClusterResult.valueOf(this, df, VarInt.wrap(assignment));
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
            sb.append("Learned clusters:").append(centroids.rowCount()).append("\n");
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
            sb.append(getCentroids().toFullContent(printer, options));
        }
        return sb.toString();
    }
}
