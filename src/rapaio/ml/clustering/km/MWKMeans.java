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

import static rapaio.math.MathTools.*;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.IntStream;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarType;
import rapaio.data.filter.FRefSort;
import rapaio.math.linear.Algebra;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.ml.clustering.ClusteringHookInfo;
import rapaio.ml.clustering.ClusteringModel;
import rapaio.ml.clustering.ClusteringResult;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.ValueParam;
import rapaio.ml.common.distance.Distance;
import rapaio.ml.common.distance.MinkowskiDistance;
import rapaio.printer.Format;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

/**
 * Minkowsky Weighted KMeans
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/27/17.
 */
public class MWKMeans extends ClusteringModel<MWKMeans, ClusteringResult, ClusteringHookInfo> {

    private static final Logger LOGGER = Logger.getLogger(MWKMeans.class.getName());

    /**
     * Specifies the desired number of centroids
     */
    public ValueParam<Integer, MWKMeans> k = new ValueParam<>(this, 2, "k");

    /**
     * Power of Minkowski metric. Value should be greater than 1
     */
    public ValueParam<Double, MWKMeans> p = new ValueParam<>(this, 2.0, "p", v -> Double.isFinite(v) && v >= 1);

    /**
     * Number of restarts when choosing the initial centroids
     */
    public ValueParam<Integer, MWKMeans> nstart = new ValueParam<>(this, 1, "nstart", v -> v >= 1);

    /**
     * Initialization method
     */
    public ValueParam<KMClusterInit, MWKMeans> init = new ValueParam<>(this, KMClusterInit.Forgy, "init");

    /**
     * Threshold value used as a criteria for assessing convergence
     */
    public ValueParam<Double, MWKMeans> eps = new ValueParam<>(this, 1e-20, "eps");

    private Frame summary;

    // clustering artifacts

    private DMatrix x;
    private DMatrix c;
    private DMatrix weights;
    private int[] assign;
    private VarDouble errors;

    // summary artifacts

    private VarDouble summaryAllDist;

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

    private double distance(DVector x, DVector y, DVector w, double p) {
        return pow(error(x, y, w, p), 1 / p);
    }

    private double error(DVector x, DVector y, DVector w, double p) {
        return x.sub(y, Algebra.copy()).mul(w).apply(v -> pow(abs(v), p)).sum();
    }

    private DMatrix initialize() {

        Distance d = new MinkowskiDistance(p.get());
        DMatrix bestCentroids = init.get().init(d, x, k.get());
        double bestError = computeError(x, bestCentroids);
        LOGGER.fine("Initial starting clusters done, initial computed error is: " + bestError);

        // compute initial restarts if nstart is greater than 1
        // the best restart is kept as initial centroids

        LOGGER.fine("nstart > 1, searching for alternate starting points");
        if (nstart.get() > 1) {
            for (int i = 1; i < nstart.get(); i++) {
                DMatrix nextCentroids = init.get().init(d, x, k.get());
                double nextError = computeError(x, nextCentroids);
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
        errors = VarDouble.empty().name("errors");

        // initialize design matrix
        x = DMatrix.copy(df.mapVars(inputNames));

        // initialize centroids
        c = initialize();
        // initialize weights
        weights = DMatrix.fill(k.get(), inputNames.length, 1.0 / k.get());
        // assign to centroids and compute error
        assign = assignToCentroids();
//        repairEmptyClusters();

        int rounds = runs.get();
        while (rounds-- > 0) {
            recomputeCentroids();
            assignToCentroids();
            weightsUpdate(df);
//            repairEmptyClusters();

            if (runningHook != null) {
                buildSummary(df);
                learned = true;
                runningHook.get().accept(new ClusteringHookInfo(this, runs.get() - rounds));
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
        buildSummary(df);
        learned = true;
        return this;
    }

    @Override
    public ClusteringResult corePredict(Frame df, boolean withScores) {
        return null;
    }

    private double computeError(DMatrix x, DMatrix centroids) {
        return IntStream.range(0, x.rowCount()).parallel().mapToDouble(j -> {
            double d = Double.NaN;
            for (int c = 0; c < centroids.rowCount(); c++) {
                double dd = error(x.mapRow(j), centroids.mapRow(c), weights.mapRow(c), p.get());
                d = Double.isNaN(d) ? dd : Math.min(dd, d);
            }
            if (Double.isNaN(d)) {
                throw new RuntimeException("Cluster could not be computed.");
            }
            return d;
        }).sum();
    }

    private int[] assignToCentroids() {
        int[] assignment = new int[x.rowCount()];
        double totalError = 0.0;
        for (int i = 0; i < x.rowCount(); i++) {
            double error = Double.NaN;
            int cluster = -1;
            for (int j = 0; j < c.rowCount(); j++) {
                double currentError = error(x.mapRow(i), c.mapRow(j), weights.mapRow(j), p.get());
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
        errors.addDouble(totalError);
        return assignment;
    }

    /**
     * Computes indexes of all instances assigned to a centroid.
     */
    private int[] computeCentroidIndexes(int c) {
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

    private void recomputeCentroids() {
        for (int i = 0; i < k.get(); i++) {
            int[] indexes = computeCentroidIndexes(i);
            DMatrix xc = x.mapRows(indexes);

        }
    }

    private void weightsUpdate(Frame df) {
        // TODO fix
        DMatrix d = DMatrix.fill(k.get(), inputNames.length, 0.0);
        for (int i = 0; i < df.rowCount(); i++) {
            for (int j = 0; j < inputNames.length; j++) {
                int c = assign[i];
                double value = pow(abs(x.get(i, j) - this.c.get(c, j)), p.get());
                d.set(c, j, d.get(c, j) + value);
            }
        }

        // revert values

        for (int i = 0; i < d.rowCount(); i++) {
            for (int j = 0; j < d.colCount(); j++) {
                double dist = Math.max(d.get(i, j), 1e-20);
                d.set(i, j, Math.pow(1.0 / dist, 1.0 / (p.get() - 1)));
            }
        }

        // compute normalizing sums

        DVector rv = DVector.fill(k.get(), 0.0);
        for (int i = 0; i < d.rowCount(); i++) {
            double sum = 0.0;
            for (int j = 0; j < d.colCount(); j++) {
                sum += d.get(i, j);
            }
            rv.set(i, sum);
        }

        // update weights

        for (int i = 0; i < weights.rowCount(); i++) {
            for (int j = 0; j < weights.colCount(); j++) {
                weights.set(i, j, d.get(i, j) / rv.get(i));
            }
        }
    }

    private void repairEmptyClusters() {
        // check for empty clusters, if any is found then
        // select random points to be new clusters, different than
        // existing clusters

        /*
        int[] clusterCount = new int[c.rowCount()];
        assign.stream().mapToInt().forEach(arrow -> clusterCount[arrow]++);
        if (Arrays.stream(clusterCount).filter(count -> count == 0).count() > 0) {

            // first find all empty clusters

            IntOpenHashSet emptyCentroids = new IntOpenHashSet();
            for (int i = 0; i < clusterCount.length; i++) {
                if (clusterCount[i] == 0) {
                    emptyCentroids.add(i);
                }
            }

            // replace each empty cluster

            Iterator<Integer> it = emptyCentroids.iterator();
            while (it.hasNext()) {
                int next = it.next();
                while (true) {
                    int selection = RandomSource.nextInt(df.rowCount());
                    boolean found = true;

                    // check if it does not collide with existent valid clusters

                    for (int i = 0; i < c.rowCount(); i++) {
                        if (emptyCentroids.contains(i)) {
                            continue;
                        }
                        if (checkIfEqual(c, i, df, next)) {
                            found = false;
                            break;
                        }
                    }
                    if (!found) {
                        continue;
                    }

                    // we found a valid centroid, it will be assigned

                    for (String input : inputs) {
                        c.setDouble(next, input, df.getDouble(selection, input));
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

            recomputeCentroids();
        }

         */
    }

    private boolean checkIfEqual(Frame centroids, int c, Frame df, int i) {
        /*
        int count = 0;
        for (String input : inputs) {
            if (centroids.getDouble(c, input) == df.getDouble(i, input)) {
                count++;
            }
        }
        return count == inputs.length;

         */
        return false;
    }

    public int[] clusterAssignment() {
        return assign;
    }

    public DMatrix centroids() {
        return c;
    }

    public VarDouble runningErrors() {
        return errors.copy();
    }

    public double error() {
        return errors.size() == 0 ? Double.NaN : errors.getDouble(errors.size() - 1);
    }

    private void buildSummary(Frame df) {
        /*
        VarInt summaryId = VarInt.seq(1, c.rowCount()).name("ID");
        VarInt summaryCount = VarInt.fill(c.rowCount(), 0).name("count");
        VarDouble summaryMean = VarDouble.fill(c.rowCount(), 0).name("mean");
        VarDouble summaryVar = VarDouble.fill(c.rowCount(), 0).name("var");
        VarDouble summaryVarP = VarDouble.fill(c.rowCount(), 0).name("var/total");
        VarDouble summarySd = VarDouble.fill(c.rowCount(), 0).name("sd");

        summaryAllDist = VarDouble.empty().name("all dist");

        Map<Integer, VarDouble> errors = new HashMap<>();

        for (int i = 0; i < df.rowCount(); i++) {
            double d = distance(df, i, c, assign[i], weights).v2;
            if (!errors.containsKey(assign.getInt(i))) {
                errors.put(assign.getInt(i), VarDouble.empty());
            }
            errors.get(assign.getInt(i)).addDouble(d);
            summaryAllDist.addDouble(d);
        }
        double tvar = Variance.of(summaryAllDist).value();
        for (Map.Entry<Integer, VarDouble> e : errors.entrySet()) {
            summaryCount.setInt(e.getKey(), e.getValue().size());
            summaryMean.setDouble(e.getKey(), Mean.of(e.getValue()).value());
            double v = Variance.of(e.getValue()).value();
            summaryVar.setDouble(e.getKey(), v);
            summaryVarP.setDouble(e.getKey(), v / tvar);
            summarySd.setDouble(e.getKey(), Math.sqrt(v));
        }

        summary = SolidFrame.byVars(summaryId, summaryCount, summaryMean, summaryVar, summaryVarP, summarySd);

         */
    }


    @Override
    public String toSummary(Printer printer, POption<?>... options) {

        StringBuilder sb = new StringBuilder();
        sb.append("MinkowskiWeightedKMeans clustering model\n");
        sb.append("=======================\n");
        sb.append("\n");
        sb.append("Parameters: \n");
        sb.append("> K = ").append(k).append("\n");
        sb.append("> p = ").append(p).append("\n");
        sb.append("> init = ").append(init.name()).append("\n");
        sb.append("> distance = Minkowski[p=").append(p).append("]\n");
        sb.append("> eps = ").append(eps).append("\n");
        sb.append("\n");

        sb.append("Learned clusters\n");
        sb.append("----------------\n");

        if (!learned) {
            sb.append("MinkowskyWeightedKMeans did not clustered anything yet!\n");
        } else {
            sb.append("Overall: \n");
            sb.append("> count: ").append(summaryAllDist.size()).append("\n");
            sb.append("> mean: ").append(Format.floatFlex(Mean.of(summaryAllDist).value())).append("\n");
            sb.append("> var: ").append(Format.floatFlex(Variance.of(summaryAllDist).value())).append("\n");
            sb.append("> sd: ").append(Format.floatFlex(Variance.of(summaryAllDist).sdValue())).append("\n");
            sb.append("\n");

            sb.append("Per cluster: \n");
            sb.append(summary.fapply(FRefSort.by(summary.rvar("count").refComparator(false))).toFullContent(printer, options));
            sb.append("\n");
            sb.append("Cluster weights:\n");
            sb.append(weights.toFullContent(printer, options));
            sb.append("\n");
        }

        return sb.toString();
    }
}
