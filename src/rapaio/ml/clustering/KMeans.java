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

package rapaio.ml.clustering;

import rapaio.core.RandomSource;
import rapaio.core.stat.Mean;
import rapaio.data.*;
import rapaio.data.filter.Filters;
import rapaio.ml.common.distance.Distance;
import rapaio.ml.common.distance.KMeansInitMethod;
import rapaio.printer.Printable;
import rapaio.printer.Summary;
import rapaio.sys.WS;
import rapaio.util.Pair;
import rapaio.util.Tag;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static rapaio.core.CoreTools.*;

/**
 * KMeans clustering algorithm
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class KMeans implements Printable {

    private int k = 2;
    private int nstart = 1;
    private int runs = Integer.MAX_VALUE;
    private Tag<KMeansInitMethod> init = KMeansInitMethod.FORGY;
    private Distance distance = Distance.EUCLIDEAN;
    private BiConsumer<KMeans, Integer> runningHook = null;
    private Frame summary;
    private double eps = 1e-20;
    private boolean learned = false;
    private boolean debug = false;

    // clustering artifacts

    private String[] inputs;
    private Frame centroids;
    private IdxVar arrows;
    private NumVar errors;

    // summary artifacts

    private NumVar summaryAllDist;

    public KMeans withNStart(int nstart) {
        if (nstart <= 0) {
            throw new IllegalArgumentException("nstart value should be greater than 1");
        }
        this.nstart = nstart;
        return this;
    }

    public KMeans withK(int k) {
        this.k = k;
        return this;
    }

    public KMeans withInit(Tag<KMeansInitMethod> init) {
        this.init = init;
        return this;
    }

    public KMeans withEps(double eps) {
        this.eps = eps;
        return this;
    }

    public KMeans withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    public KMeans withDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    public final KMeans withRunningHook(BiConsumer<KMeans, Integer> hook) {
        runningHook = hook;
        return this;
    }

    public void cluster(Frame df, String... varNames) {
        validate(df, varNames);

        inputs = VRange.of(varNames).parseVarNames(df).toArray(new String[0]);

        Frame bestCentroids = init.get().init(df, inputs, k);
        double bestError = computeError(df, bestCentroids);

        if (debug) {
            WS.println("Initial starting clusters done, initial computed error is: " + bestError);
        }

        // compute initial restarts if nstart is greater than 1
        // the best restart is kept as initial centroids

        if (debug) {
            WS.println("nstart > 1, searching for alternate starting points");
        }
        if (nstart > 1) {
            for (int i = 1; i < nstart; i++) {
                Frame nextCentroids = init.get().init(df, inputs, k);
                double nextError = computeError(df, nextCentroids);
                if (nextError < bestError) {
                    bestCentroids = nextCentroids;
                    bestError = nextError;
                    if (debug) {
                        WS.println("Initial best error improved at: " + bestError + " at iteration " + (i + 1));
                    }
                }
            }
        }

        centroids = bestCentroids;

        arrows = IdxVar.fill(df.rowCount(), -1);
        errors = NumVar.empty().withName("errors");

        assignToCentroids(df);
        repairEmptyClusters(df);

        int rounds = runs;
        while (rounds-- > 0) {
            recomputeCentroids(df);
            assignToCentroids(df);
            repairEmptyClusters(df);

            if (runningHook != null) {
                buildSummary(df);
                learned = true;
                runningHook.accept(this, runs - rounds);
            }
            int erc = errors.rowCount();
            if (erc > 1 && debug) {
                WS.println("prev error: " + errors.value(erc - 2) +
                        ", current error: " + errors.value(erc - 1) +
                        ", error improvement: " + (errors.value(erc - 2) - errors.value(erc - 1))
                );
            }
            if (erc > 1 && errors.value(erc - 2) - errors.value(erc - 1) < eps && errors.value(erc - 1) <= errors.value(erc - 2)) {
                break;
            }
        }
        buildSummary(df);
        learned = true;
    }

    private void validate(Frame df, String... varNames) {
        List<String> nameList = VRange.of(varNames).parseVarNames(df);
        for (String varName : nameList) {
            if (!df.rvar(varName).type().isNumeric())
                throw new IllegalArgumentException("all matched vars must be numeric: check var " + varName);
            if (df.rvar(varName).stream().complete().count() != df.rowCount()) {
                throw new IllegalArgumentException("all matched vars must have non-missing values: check var " + varName);
            }
        }
    }

    private double computeError(Frame df, Frame centroids) {
        return IntStream.range(0, df.rowCount()).parallel().mapToDouble(j -> {
            double d = Double.NaN;
            for (int c = 0; c < centroids.rowCount(); c++) {
                double dd = distance.compute(df, j, centroids, c, inputs)._2;
                if (!Double.isFinite(dd)) continue;
                d = Double.isNaN(d) ? dd : Math.min(dd, d);
            }
            if (Double.isNaN(d)) {
                throw new RuntimeException("Cluster could not be computed.");
            }
            return d;
        }).sum();
    }

    private void assignToCentroids(Frame df) {
        double[] rowErrors = new double[df.rowCount()];
        for (int i = 0; i < df.rowCount(); i++) {
            double d = Double.NaN;
            double err = Double.NaN;
            int cluster = -1;
            for (int j = 0; j < centroids.rowCount(); j++) {
                Pair<Double, Double> comp = distance.compute(df, i, centroids, j, inputs);
                double dd = comp._1;
                if (!Double.isFinite(dd)) continue;
                if (!Double.isNaN(d)) {
                    if (dd < d) {
                        d = dd;
                        err = comp._2;
                        cluster = j;
                    }
                } else {
                    d = dd;
                    err = comp._2;
                    cluster = j;
                }
            }
            if (cluster == -1) {
                throw new RuntimeException("cluster could not be computed");
            }
            rowErrors[i] = err;
            arrows.setIndex(i, cluster);
        }

        double totalError = 0.0;
        for (double error : rowErrors) {
            totalError += error;
        }
        errors.addValue(totalError);
    }

    private void recomputeCentroids(Frame df) {

        // we compute mean for each feature separately
        for (String input : inputs) {
            // collect values for each cluster in mean, for a given input feature
            Var[] means = IntStream.range(0, k).boxed()
                    .map(i -> NumVar.empty())
                    .toArray(NumVar[]::new);
            for (int i = 0; i < df.rowCount(); i++) {
                means[arrows.index(i)].addValue(df.value(i, input));
            }
            for (int i = 0; i < k; i++) {
                centroids.setValue(i, input, Mean.from(means[i]).value());
            }
        }
    }

    private void repairEmptyClusters(Frame df) {
        // check for empty clusters, if any is found then
        // select random points to be new clusters, different than
        // existing clusters

        int[] clusterCount = new int[centroids.rowCount()];
        arrows.stream().mapToInt().forEach(arrow -> clusterCount[arrow]++);
        if (Arrays.stream(clusterCount).filter(count -> count == 0).count() > 0) {

            // first find all empty clusters

            HashSet<Integer> emptyCentroids = new HashSet<>();
            for (int i = 0; i < clusterCount.length; i++) {
                if (clusterCount[i] == 0)
                    emptyCentroids.add(i);
            }

            // replace each empty cluster

            Iterator<Integer> it = emptyCentroids.iterator();
            while (it.hasNext()) {
                int next = it.next();
                while (true) {
                    int selection = RandomSource.nextInt(df.rowCount());
                    boolean found = true;

                    // check if it does not collide with existent valid clusters

                    for (int i = 0; i < centroids.rowCount(); i++) {
                        if (emptyCentroids.contains(i))
                            continue;
                        if (checkIfEqual(centroids, i, df, next)) {
                            found = false;
                            break;
                        }
                    }
                    if (!found) continue;

                    // we found a valid centroid, it will be assigned

                    for (String input : inputs) {
                        centroids.setValue(next, input, df.value(selection, input));
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

            recomputeCentroids(df);
        }
    }

    private boolean checkIfEqual(Frame centroids, int c, Frame df, int i) {
        int count = 0;
        for (String input : inputs) {
            if (centroids.value(c, input) == df.value(i, input)) {
                count++;
            }
        }
        return count == inputs.length;
    }

    public Var clusterAssignment() {
        return arrows;
    }

    public Frame centroids() {
        return centroids;
    }

    public NumVar runningErrors() {
        return errors.solidCopy();
    }

    public double error() {
        return errors.rowCount() == 0 ? Double.NaN : errors.value(errors.rowCount() - 1);
    }

    private void buildSummary(Frame df) {

        IdxVar summaryId = IdxVar.seq(1, centroids.rowCount()).withName("ID");
        IdxVar summaryCount = IdxVar.fill(centroids.rowCount(), 0).withName("count");
        NumVar summaryMean = NumVar.fill(centroids.rowCount(), 0).withName("mean");
        NumVar summaryVar = NumVar.fill(centroids.rowCount(), 0).withName("var");
        NumVar summaryVarP = NumVar.fill(centroids.rowCount(), 0).withName("var/total");
        NumVar summarySd = NumVar.fill(centroids.rowCount(), 0).withName("sd");

        summaryAllDist = NumVar.empty().withName("all dist");

        Map<Integer, NumVar> errors = new HashMap<>();

        for (int i = 0; i < df.rowCount(); i++) {
            double d = distance.compute(centroids, arrows.index(i), df, i, inputs)._2;
            if (!errors.containsKey(arrows.index(i)))
                errors.put(arrows.index(i), NumVar.empty());
            errors.get(arrows.index(i)).addValue(d);
            summaryAllDist.addValue(d);
        }
        double tvar = variance(summaryAllDist).value();
        for (Map.Entry<Integer, NumVar> e : errors.entrySet()) {
            summaryCount.setIndex(e.getKey(), e.getValue().rowCount());
            summaryMean.setValue(e.getKey(), mean(e.getValue()).value());
            double v = variance(e.getValue()).value();
            summaryVar.setValue(e.getKey(), v);
            summaryVarP.setValue(e.getKey(), v / tvar);
            summarySd.setValue(e.getKey(), Math.sqrt(v));
        }

        summary = SolidFrame.byVars(summaryId, summaryCount, summaryMean, summaryVar, summaryVarP, summarySd);
    }

    @Override
    public String summary() {

        StringBuilder sb = new StringBuilder();
        sb.append("KMeans clustering model\n");
        sb.append("=======================\n");
        sb.append("\n");
        sb.append("Parameters: \n");
        sb.append("> K = ").append(k).append("\n");
        sb.append("> init = ").append(init.name()).append("\n");
        sb.append("> distance = ").append(distance.name()).append("\n");
        sb.append("> eps = ").append(eps).append("\n");
        sb.append("> debug = ").append(debug).append("\n");
        sb.append("\n");

        sb.append("Learned clusters\n");
        sb.append("----------------\n");

        if (!learned) {
            sb.append("KMeans did not clustered anything yet!\n");
        } else {
            sb.append("Overall: \n");
            sb.append("> count: ").append(summaryAllDist.rowCount()).append("\n");
            sb.append("> mean: ").append(WS.formatFlex(mean(summaryAllDist).value())).append("\n");
            sb.append("> var: ").append(WS.formatFlex(variance(summaryAllDist).value())).append("\n");
            sb.append("> sd: ").append(WS.formatFlex(variance(summaryAllDist).sdValue())).append("\n");
            sb.append("\n");

            sb.append("Per cluster: \n");
            sb.append(Summary.headString(false, Filters.refSort(summary, summary.rvar("count").refComparator(false))));
        }

        return sb.toString();
    }
}
