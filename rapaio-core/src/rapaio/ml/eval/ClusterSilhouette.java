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

package rapaio.ml.eval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import rapaio.core.param.ParamSet;
import rapaio.core.param.ValueParam;
import rapaio.core.tools.DistanceMatrix;
import rapaio.data.Var;
import rapaio.printer.Format;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * Computes cluster silhouette information based
 * on a distance matrix and a clustering assignment.
 * <p>
 * The assignment variable must contain non-missing numeric/index
 * integer values greater than 0. The number of clusters is considered
 * the greatest index value in the cluster assignment variable.
 * <p>
 * Distance matrix should have the same length as the number of clusters
 * considered.
 * <p>
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/13/17.
 */
public class ClusterSilhouette extends ParamSet<ClusterSilhouette> implements Printable {

    public static ClusterSilhouette newSilhouette() {
        return new ClusterSilhouette();
    }

    public static ClusterSilhouette from(Var assignment, DistanceMatrix dm, boolean similarity) {
        return new ClusterSilhouette()
                .assignment.set(assignment)
                .distanceMatrix.set(dm)
                .similarity.set(similarity);
    }

    /**
     * Cluster assignment
     */
    public final ValueParam<Var, ClusterSilhouette> assignment = new ValueParam<>(this, null,"cluster assignment", Objects::nonNull);

    /**
     * Distance matrix
     */
    public final ValueParam<DistanceMatrix, ClusterSilhouette> distanceMatrix = new ValueParam<>(this, null,"distance matrix", Objects::nonNull);

    /**
     * Similarity function (true) or disimilarity function (false)
     */
    public final ValueParam<Boolean, ClusterSilhouette> similarity = new ValueParam<>(this, null,"similarity", Objects::nonNull);

    // artifacts

    private double[] a; // score within cluster for each instance
    private double[] b; // score with neighbour cluster for each instance
    private double[] s; // silhouette score of each instance
    private int[] n; // neighbour cluster index

    private final Map<String, Integer> clusterIndex = new HashMap<>();
    private String[] clusterIds;
    private double[] clusterScores;
    private double overallScore;

    private List<Integer> clusterOrder;
    private List<List<Integer>> instanceOrder;

    public int getClusterCount() {
        return clusterIndex.size();
    }

    public String[] getClusterLabels() {
        return clusterIds;
    }

    public double[] getClusterScores() {
        return clusterScores;
    }

    public double[] getScores() {
        return s;
    }

    public double getAverageClusterScore() {
        return overallScore;
    }

    public List<Integer> getClusterOrder() {
        return clusterOrder;
    }

    public List<List<Integer>> getInstanceOrder() {
        return instanceOrder;
    }

    private int getCluster(int row) {
        return clusterIndex.get(assignment.get().getLabel(row));
    }

    public ClusterSilhouette compute() {
        if (assignment.get().size() != distanceMatrix.get().length()) {
            throw new IllegalArgumentException("Assignment and distance matrix sizes does not match.");
        }

        for (int i = 0; i < assignment.get().size(); i++) {
            if (assignment.get().isMissing(i)) {
                throw new IllegalArgumentException("Assignment variable contains missing data");
            }
            String clusterId = assignment.get().getLabel(i);
            if (clusterIndex.containsKey(clusterId)) {
                continue;
            }
            clusterIndex.put(clusterId, clusterIndex.size());
        }
        clusterIds = new String[clusterIndex.size()];
        for (Map.Entry<String, Integer> entry : clusterIndex.entrySet()) {
            clusterIds[entry.getValue()] = entry.getKey();
        }

        int clusters = clusterIds.length;
        if (clusters == 1) {
            throw new IllegalArgumentException("Silhouettes cannot be computed for a single cluster.");
        }

        // compute individual a and b vectors

        int rows = distanceMatrix.get().length();

        a = new double[rows];
        b = new double[rows];
        n = new int[rows];

        Arrays.fill(b, Double.NaN);


        for (int row = 0; row < rows; row++) {
            double[] sum = new double[clusters];
            int[] count = new int[clusters];

            for (int i = 0; i < rows; i++) {
                if (i == row) {
                    continue;
                }
                int cluster = getCluster(i);
                count[cluster]++;
                sum[cluster] += distanceMatrix.get().get(row, i);
            }

            int cluster = getCluster(row);
            a[row] = count[cluster] == 0 ? 0 : sum[cluster] / count[cluster];
            for (int i = 0; i < clusters; i++) {
                if (i == cluster || count[i] == 0) {
                    continue;
                }
                if (Double.isNaN(b[row])) {
                    b[row] = sum[i] / count[i];
                    n[row] = i;
                } else {
                    if (similarity.get()) {
                        if ((sum[i] / count[i]) > b[row]) {
                            b[row] = sum[i] / count[i];
                            n[row] = i;
                        }
                    } else {
                        if ((sum[i] / count[i]) < b[row]) {
                            b[row] = sum[i] / count[i];
                            n[row] = i;
                        }
                    }
                }
            }
        }

        // compute individual silhouettes

        s = new double[rows];
        for (int i = 0; i < rows; i++) {
            s[i] = (b[i] - a[i]) / Math.max(a[i], b[i]);
        }

        // compute cluster score averages and overall average score

        int[] count = new int[clusters];
        int tcount = 0;
        double tsum = 0;
        double[] sum = new double[clusters];
        for (int i = 0; i < rows; i++) {
            count[getCluster(i)]++;
            sum[getCluster(i)] += s[i];
            tcount++;
            tsum += s[i];
        }
        clusterScores = new double[clusters];
        for (int i = 0; i < clusters; i++) {
            clusterScores[i] = count[i] == 0 ? 0 : sum[i] / count[i];
        }
        overallScore = tsum / tcount;

        // build cluster order

        clusterOrder = new ArrayList<>();
        for (int i = 0; i < clusters; i++) {
            clusterOrder.add(i);
        }
        clusterOrder.sort((o1, o2) -> -1 * Double.compare(clusterScores[o1], clusterScores[o2]));

        // build instance order

        instanceOrder = new ArrayList<>();
        for (int cluster : clusterOrder) {
            List<Integer> instances = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                if (getCluster(i) == cluster) {
                    instances.add(i);
                }
            }
            instances.sort((o1, o2) -> -1 * Double.compare(s[o1], s[o2]));
            instanceOrder.add(instances);
        }
        return this;
    }

    @Override
    public String toString() {
        return "ClusterSilhouette{clusters:" + getClusterCount() + ", "
                + "overall score: " + Format.floatFlex(getAverageClusterScore()) + "}";
    }

    @Override
    public String toSummary(Printer printer, POpt<?>... options) {
        StringBuilder sb = new StringBuilder();
        addHeader(sb);
        addClusterAverages(sb);
        return sb.toString();
    }

    private void addHeader(StringBuilder sb) {
        sb.append("Cluster silhouette summary\n");
        sb.append("==========================\n");
        sb.append("\n");
    }

    private void addClusterAverages(StringBuilder sb) {
        for (int cluster : clusterOrder) {
            sb.append("Cluster ")
                    .append(clusterIds[cluster])
                    .append(" has average silhouette width: ")
                    .append(clusterScores[cluster])
                    .append("\n");
        }
        sb.append("\n");
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        return toSummary(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POpt<?>... options) {
        StringBuilder sb = new StringBuilder();
        addHeader(sb);
        addInstanceDetails(sb);
        addClusterAverages(sb);
        return sb.toString();
    }

    private void addInstanceDetails(StringBuilder sb) {
        for (int i = 0; i < clusterOrder.size(); i++) {
            int cluster = clusterOrder.get(i);
            for (int row : instanceOrder.get(i)) {
                sb.append(clusterIds[cluster]).append(" ");
                sb.append(clusterIds[n[row]]).append(" ");
                sb.append(String.format("%5.2f ", s[row]));
                sb.append(distanceMatrix.get().name(row)).append(" ");
                sb.append("\n");
            }
            sb.append("\n");
        }
        sb.append("\n");
    }
}
