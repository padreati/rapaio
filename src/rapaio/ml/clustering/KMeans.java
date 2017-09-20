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

import rapaio.core.stat.Mean;
import rapaio.data.*;
import rapaio.data.filter.Filters;
import rapaio.ml.common.distance.Distance;
import rapaio.ml.common.distance.KMeansInitMethod;
import rapaio.printer.Printable;
import rapaio.sys.WS;
import rapaio.util.Pair;
import rapaio.util.Tag;
import rapaio.printer.Summary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
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
    private int[] arrows;
    private NumericVar errors;
    private Map<Integer, NumericVar> clusterErrors;

    // summary artifacts

    private NumericVar summaryAllDist;

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

        inputs = VRange.of(varNames).parseVarNames(df).stream().toArray(String[]::new);

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
                    WS.println("Initial best error improved at: " + bestError + " at iteration " + (i + 1));
                }
            }
        }

        centroids = bestCentroids;

        arrows = new int[df.rowCount()];
        errors = NumericVar.empty().withName("errors");
        clusterErrors = new HashMap<>();
        IndexVar.seq(k).stream().forEach(c -> clusterErrors.put(c.getIndex(), NumericVar.empty().withName("c" + (c.getIndex() + 1) + "_errors")));

        assignToCentroids(df);

        int rounds = runs;
        while (rounds-- > 0) {
            recomputeCentroids(df);
            assignToCentroids(df);

            if (runningHook != null) {
                buildSummary(df);
                learned = true;
                runningHook.accept(this, runs - rounds);
            }
            int erc = errors.rowCount();
            if (erc > 1 && debug) {
                WS.println("prev error: " + errors.value(erc - 2) +
                        ", current error: " + errors.value(erc - 1) +
                        ", error diff: " + (errors.value(erc - 2) - errors.value(erc - 1))
                );
            }
            if (erc > 1 && Math.abs(errors.value(erc - 1) - errors.value(erc - 2)) < eps) {
                break;
            }
        }
        buildSummary(df);
        learned = true;
    }

    private void validate(Frame df, String... varNames) {
        List<String> nameList = VRange.of(varNames).parseVarNames(df);
        for (String varName : nameList) {
            if (!df.var(varName).type().isNumeric())
                throw new IllegalArgumentException("all matched vars must be numeric: check var " + varName);
            if (df.var(varName).stream().complete().count() != df.rowCount()) {
                throw new IllegalArgumentException("all matched vars must have non-missing values: check var " + varName);
            }
        }
    }

    private double computeError(Frame df, Frame centroids) {
        return IntStream.range(0, df.rowCount()).parallel().mapToDouble(i -> {
            double d = Double.NaN;
            for (int j = 0; j < centroids.rowCount(); j++) {
                double dd = distance.distance(df, i, centroids, j, inputs);
                if (!Double.isFinite(dd)) continue;
                d = Double.isNaN(d) ? dd : Math.min(dd, d);
            }
            if (Double.isNaN(d)) {
                throw new RuntimeException("cluster could not be computed");
            }
            return d;
        }).sum();
    }

    private void assignToCentroids(Frame df) {
        double totalError = 0.0;
        double[] err = new double[centroids.rowCount()];
        List<Pair<Integer, Double>> pairs = IntStream.range(0, df.rowCount()).parallel().boxed().map(i -> {
            double d = Double.NaN;
            int cluster = -1;
            for (int j = 0; j < centroids.rowCount(); j++) {
                double dd = distance.distance(df, i, centroids, j, inputs);
                if (!Double.isFinite(dd)) continue;
                if (!Double.isNaN(d)) {
                    if (dd < d) {
                        d = dd;
                        cluster = j;
                    }
                } else {
                    d = dd;
                    cluster = j;
                }
            }
            if (cluster == -1) {
                throw new RuntimeException("cluster could not be computed");
            }
            double error = Math.pow(d, 2);
            arrows[i] = cluster;
            return Pair.from(cluster, error);
        }).collect(toList());
        for (Pair<Integer, Double> p : pairs) {
            totalError += p._2;
            err[p._1] += p._2;
        }
        for (int i = 0; i < err.length; i++) {
            clusterErrors.get(i).addValue(err[i]);
        }
        errors.addValue(totalError);
    }

    private void recomputeCentroids(Frame df) {
        if (debug) WS.println("recomputeCentroids called ..");
        Var[] means = IntStream.range(0, k).boxed().map(i -> NumericVar.fill(df.rowCount(), 0)).toArray(NumericVar[]::new);
        for (String input : inputs) {
            for (int j = 0; j < k; j++) {
                means[j].clear();
            }
            for (int j = 0; j < df.rowCount(); j++) {
                means[arrows[j]].addValue(df.value(j, input));
            }
            for (int j = 0; j < k; j++) {
                if (means[j].rowCount() == 0)
                    continue;
                double mean = Mean.from(means[j]).value();
                centroids.setValue(j, input, mean);
            }
        }
    }

    public Var getClusterAssignment() {
        Var var = IndexVar.empty(arrows.length);
        for (int i = 0; i < arrows.length; i++) {
            var.setIndex(i, arrows[i] + 1);
        }
        return var;
    }

    public Frame centroids() {
        return centroids;
    }

    public NumericVar runningErrors() {
        return errors.solidCopy();
    }

    public double error() {
        return errors.rowCount() == 0 ? Double.NaN : errors.value(errors.rowCount() - 1);
    }

    public NumericVar runningClusterError(int c) {
        if (c >= k)
            throw new IllegalArgumentException("cluster " + c + " does not exists");
        return clusterErrors.get(c);
    }

    public double clusterError(int c) {
        if (c >= k)
            throw new IllegalArgumentException("cluster " + c + " does not exists");
        return clusterErrors.get(c).value(clusterErrors.get(c).rowCount() - 1);
    }

    private void buildSummary(Frame df) {
        IndexVar summaryId = IndexVar.seq(1, centroids.rowCount()).withName("ID");
        IndexVar summaryCount = IndexVar.fill(centroids.rowCount(), 0).withName("count");
        NumericVar summaryMean = NumericVar.fill(centroids.rowCount(), 0).withName("mean");
        NumericVar summaryVar = NumericVar.fill(centroids.rowCount(), 0).withName("var");
        NumericVar summaryVarP = NumericVar.fill(centroids.rowCount(), 0).withName("var/total");
        NumericVar summarySd = NumericVar.fill(centroids.rowCount(), 0).withName("sd");

        summaryAllDist = NumericVar.empty().withName("all dist");

        Map<Integer, NumericVar> distances = new HashMap<>();

        for (int i = 0; i < df.rowCount(); i++) {
            double d = distance.distance(centroids, arrows[i], df, i, inputs);
            if (!distances.containsKey(arrows[i]))
                distances.put(arrows[i], NumericVar.empty());
            distances.get(arrows[i]).addValue(d);
            summaryAllDist.addValue(d);
        }
        double tvar = variance(summaryAllDist).value();
        for (Map.Entry<Integer, NumericVar> e : distances.entrySet()) {
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
            sb.append(Summary.headString(false, Filters.refSort(summary, summary.var("count").refComparator(false))));
        }

        return sb.toString();
    }
}
