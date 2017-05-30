/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static rapaio.core.CoreTools.*;

/**
 * KMeans clusterization algorithm
 *
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class KMeans implements Printable {

    private int k = 2;
    private int runs = Integer.MAX_VALUE;
    private Tag<KMeansInitMethod> init = KMeansInitMethod.FORGY;
    private Tag<Distance> distance = Distance.EUCLIDEAN;
    private Consumer<KMeans> runningHook = null;
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


    public KMeans withK(int k) {
        this.k = k;
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

    public final KMeans withRunningHook(Consumer<KMeans> hook) {
        runningHook = hook;
        return this;
    }

    public void cluster(Frame df, String... varNames) {
        validate(df, varNames);

        inputs = VRange.of(varNames).parseVarNames(df).stream().toArray(String[]::new);
        centroids = init.get().init(df, inputs, k);
        arrows = new int[df.getRowCount()];
        errors = NumericVar.empty().withName("errors");
        clusterErrors = new HashMap<>();
        IndexVar.seq(k).stream().forEach(c -> clusterErrors.put(c.getIndex(), NumericVar.empty().withName("c" + (c.getIndex() + 1) + "_errors")));

        assignToCentroids(df);

        int rounds = runs;
        while (rounds-- > 0) {
            recomputeCentroids(df);
            assignToCentroids(df);
            if (runningHook != null) {
                runningHook.accept(this);
            }
            int erc = errors.getRowCount();
            if (erc > 1 && Math.abs(errors.getValue(erc - 1) - errors.getValue(erc - 2)) < eps) {
                break;
            }
        }
        buildSummary(df);
        learned = true;
    }

    private void validate(Frame df, String... varNames) {
        List<String> nameList = VRange.of(varNames).parseVarNames(df);
        for (String varName : nameList) {
            if (!df.getVar(varName).getType().isNumeric())
                throw new IllegalArgumentException("all matched vars must be numeric: check var " + varName);
            if (df.getVar(varName).stream().complete().count() != df.getRowCount()) {
                throw new IllegalArgumentException("all matched vars must have non-missing values: check var " + varName);
            }
        }
    }


    private void assignToCentroids(Frame df) {
        if (debug) WS.println("assignToCentroids called ..");
        double totalError = 0.0;
        double[] err = new double[centroids.getRowCount()];
        List<Pair<Integer, Double>> pairs = IntStream.range(0, df.getRowCount()).parallel().boxed().map(i -> {
            double d = Double.NaN;
            int cluster = -1;
            for (int j = 0; j < centroids.getRowCount(); j++) {
                double dd = distance.get().distance(df, i, centroids, j, inputs);
                if (!Double.isFinite(dd)) continue;
                if (Double.isNaN(dd)) continue;
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
        Var[] means = IntStream.range(0, k).boxed().map(i -> NumericVar.fill(df.getRowCount(), 0)).toArray(NumericVar[]::new);
        for (String input : inputs) {
            for (int j = 0; j < k; j++) {
                means[j].clear();
            }
            for (int j = 0; j < df.getRowCount(); j++) {
                means[arrows[j]].addValue(df.getValue(j, input));
            }
            for (int j = 0; j < k; j++) {
                if (means[j].getRowCount() == 0)
                    continue;
                double mean = Mean.from(means[j]).getValue();
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

    public NumericVar getRunningErrors() {
        return errors.solidCopy();
    }

    public double getError() {
        return errors.getRowCount() == 0 ? Double.NaN : errors.getValue(errors.getRowCount() - 1);
    }

    public NumericVar getRunningClusterError(int c) {
        if (c >= k)
            throw new IllegalArgumentException("cluster " + c + " does not exists");
        return clusterErrors.get(c);
    }

    public double getClusterError(int c) {
        if (c >= k)
            throw new IllegalArgumentException("cluster " + c + " does not exists");
        return clusterErrors.get(c).getValue(clusterErrors.get(c).getRowCount() - 1);
    }

    private void buildSummary(Frame df) {
        IndexVar summaryId = IndexVar.seq(1, centroids.getRowCount() + 1).withName("ID");
        IndexVar summaryCount = IndexVar.fill(centroids.getRowCount(), 0).withName("count");
        NumericVar summaryMean = NumericVar.fill(centroids.getRowCount(), 0).withName("mean");
        NumericVar summaryVar = NumericVar.fill(centroids.getRowCount(), 0).withName("var");
        NumericVar summaryVarP = NumericVar.fill(centroids.getRowCount(), 0).withName("var/total");
        NumericVar summarySd = NumericVar.fill(centroids.getRowCount(), 0).withName("sd");

        summaryAllDist = NumericVar.empty().withName("all dist");

        Map<Integer, NumericVar> distances = new HashMap<>();

        for (int i = 0; i < df.getRowCount(); i++) {
            double d = distance.get().distance(centroids, arrows[i], df, i, inputs);
            if (!distances.containsKey(arrows[i]))
                distances.put(arrows[i], NumericVar.empty());
            distances.get(arrows[i]).addValue(d);
            summaryAllDist.addValue(d);
        }
        double tvar = variance(summaryAllDist).getValue();
        for (Map.Entry<Integer, NumericVar> e : distances.entrySet()) {
            summaryCount.setIndex(e.getKey(), e.getValue().getRowCount());
            summaryMean.setValue(e.getKey(), mean(e.getValue()).getValue());
            double v = variance(e.getValue()).getValue();
            summaryVar.setValue(e.getKey(), v);
            summaryVarP.setValue(e.getKey(), v / tvar);
            summarySd.setValue(e.getKey(), Math.sqrt(v));
        }

        summary = SolidFrame.byVars(summaryId, summaryCount, summaryMean, summaryVar, summaryVarP, summarySd);
    }

    @Override
    public String getSummary() {
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
            sb.append("> count: ").append(summaryAllDist.getRowCount()).append("\n");
            sb.append("> mean: ").append(WS.formatFlex(mean(summaryAllDist).getValue())).append("\n");
            sb.append("> var: ").append(WS.formatFlex(variance(summaryAllDist).getValue())).append("\n");
            sb.append("> sd: ").append(WS.formatFlex(variance(summaryAllDist).sdValue())).append("\n");
            sb.append("\n");

            sb.append("Per cluster: \n");
            sb.append(Summary.headString(Filters.refSort(summary, summary.getVar("count").refComparator(false))));
        }

        return sb.toString();
    }
}
