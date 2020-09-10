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

package rapaio.ml.clustering.kmeans;

import lombok.Getter;
import rapaio.core.RandomSource;
import rapaio.core.stat.Mean;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Unique;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.math.linear.DM;
import rapaio.math.linear.DV;
import rapaio.math.linear.dense.DMStripe;
import rapaio.ml.clustering.AbstractClusteringModel;
import rapaio.ml.clustering.ClusteringModel;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.util.collection.IntArrays;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * KMeans clustering algorithm
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class KMeans extends AbstractClusteringModel<KMeans, KMeansResult> {

    public static KMeans newModel() {
        return new KMeans();
    }

    private static final long serialVersionUID = -1046184364541391871L;

    public final ValueParam<Integer, KMeans> k = new ValueParam<>(this, 2, "k", "number of clusters");
    public final ValueParam<Integer, KMeans> nstart = new ValueParam<>(this, 1, "nstart", "Number of restarts", n -> n != null && n > 0);
    public final ValueParam<KMeansInit, KMeans> init = new ValueParam<>(this, KMeansInit.Forgy,
            "init", "Initialization algorithm");
    public final ValueParam<Double, KMeans> eps = new ValueParam<>(this, 1e-20, "eps", "Tolerance for convergence measures");

    // clustering artifacts

    private DM c;
    @Getter
    private Frame centroids;
    @Getter
    private VarDouble errors;

    @Override
    public ClusteringModel newInstance() {
        return new KMeans().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "KMeans";
    }

    public double getInertia() {
        return errors.rowCount() == 0 ? Double.NaN : errors.getDouble(errors.rowCount() - 1);
    }

    public DM getCentroidsMatrix() {
        return c;
    }

    @Override
    public Capabilities capabilities() {
        return Capabilities.builder()
                .allowMissingInputValues(true)
                .allowMissingTargetValues(true)
                .inputTypes(List.of(VType.DOUBLE, VType.INT, VType.BINARY))
                .minInputCount(1).maxInputCount(10_000)
                .minTargetCount(0).maxTargetCount(0)
                .targetTypes(List.of())
                .build();
    }

    public static double distance(DV v1, DV v2) {
        int len = Math.min(v1.size(), v2.size());
        double sum = 0.0;
        for (int i = 0; i < len; i++) {
            double d = v1.get(i) - v2.get(i);
            sum += d * d;
        }
        return Math.sqrt(sum);
    }

    @Override
    public ClusteringModel coreFit(Frame initialDf, Var weights) {

        DM m = DMStripe.copy(initialDf);
        c = initializeClusters(m);

        int[] assignment = IntArrays.newFill(m.rowCount(), -1);
        errors = VarDouble.empty().withName("errors");

        assignToCentroids(m, assignment);
        repairEmptyClusters(m, assignment);

        int rounds = runs.get();
        while (rounds-- > 0) {
            recomputeCentroids(m, assignment);
            assignToCentroids(m, assignment);
            repairEmptyClusters(m, assignment);

            if (runningHook != null) {
                learned = true;
                runningHook.get().accept(this, runs.get() - rounds);
            }
            int erc = errors.rowCount();
            if (erc > 1 && errors.getDouble(erc - 2) - errors.getDouble(erc - 1) < eps.get() && errors.getDouble(erc - 1) <= errors.getDouble(erc - 2)) {
                break;
            }
        }
        centroids = SolidFrame.matrix(c, inputNames);
        learned = true;
        return this;
    }

    private DM initializeClusters(DM m) {
        DM bestCentroids = init.get().init(m, k.get());
        double bestError = computeInitError(m, bestCentroids);

        // compute initial restarts if nstart is greater than 1
        // the best restart is kept as initial centroids

        for (int i = 1; i < nstart.get(); i++) {
            DM nextCentroids = init.get().init(m, k.get());
            double nextError = computeInitError(m, nextCentroids);
            if (nextError < bestError) {
                bestCentroids = nextCentroids;
                bestError = nextError;
            }
        }
        return bestCentroids;
    }

    private double computeInitError(DM m, DM c) {
        double sum = 0;
        for (int i = 0; i < m.rowCount(); i++) {
            DV mrow = m.mapRow(i);
            double d = distance(mrow, c.mapRow(0));
            for (int j = 1; j < c.rowCount(); j++) {
                d = Math.min(d, distance(mrow, c.mapRow(j)));
            }
            sum += Math.pow(d, 2);
        }
        return sum;
    }

    private void assignToCentroids(DM m, int[] assignment) {
        double totalError = 0.0;
        for (int i = 0; i < m.rowCount(); i++) {
            DV row = m.mapRow(i);
            double d = distance(row, c.mapRow(0));
            int cluster = 0;
            for (int j = 1; j < c.rowCount(); j++) {
                double dd = distance(row, c.mapRow(j));
                if (dd < d) {
                    d = dd;
                    cluster = j;
                }
            }
            totalError += d * d;
            assignment[i] = cluster;
        }
        errors.addDouble(totalError);
    }

    private void recomputeCentroids(DM m, int[] assignment) {

        // we compute mean for each feature separately
        for (int j = 0; j < m.colCount(); j++) {
            // collect values for each cluster in mean, for a given input feature
            Var[] means = IntStream.range(0, k.get()).boxed()
                    .map(i -> VarDouble.empty())
                    .toArray(VarDouble[]::new);
            for (int i = 0; i < m.rowCount(); i++) {
                means[assignment[i]].addDouble(m.get(i, j));
            }
            for (int i = 0; i < k.get(); i++) {
                c.set(i, j, Mean.of(means[i]).value());
            }
        }
    }

    private void repairEmptyClusters(DM df, int[] assignment) {
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
                    if (emptyCentroids.contains(i))
                        continue;
                    if (!checkIfEqual(c, i, df, next)) {
                        found = true;
                        break;
                    }
                }
                if (!found) continue;

                // we found a valid centroid, it will be assigned

                for (int j = 0; j < c.colCount(); j++) {
                    c.set(next, j, df.get(selection, j));
                }
                break;
            }

            // remove centroid from empty set

            it.remove();
        }

        // rebuit errors and cluster assignement
        // if empty clusters happens again, then that is it, we did our best
        // the stopping criterion is given by a bound on error or a
        // maximum iteration

        recomputeCentroids(df, assignment);
    }

    private boolean checkIfEqual(DM centroids, int c, DM df, int i) {
        int count = 0;
        for (int j = 0; j < centroids.colCount(); j++) {
            if (centroids.get(c, j) == df.get(i, j)) {
                count++;
            }
        }
        return count == inputNames.length;
    }

    @Override
    public KMeansResult corePredict(Frame df, boolean withScores) {
        int[] assignment = IntArrays.newFill(df.rowCount(), -1);
        DM m = DMStripe.copy(df);
        assignToCentroids(m, assignment);
        return KMeansResult.valueOf(this, df, VarInt.wrap(assignment));
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
            sb.append("Inertia:").append(getInertia()).append("\n");
            sb.append("Iterations:").append(errors.rowCount()).append("\n");
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
