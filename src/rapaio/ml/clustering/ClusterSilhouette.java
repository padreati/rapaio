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

import rapaio.core.tools.DistanceMatrix;
import rapaio.data.Var;
import rapaio.printer.Printable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/13/17.
 */
public class ClusterSilhouette implements Printable {

    public static ClusterSilhouette from(Var asgn, DistanceMatrix d, boolean similarity) {
        return new ClusterSilhouette(asgn, d, similarity);
    }

    private final Var assignment;
    private final DistanceMatrix d;
    private final boolean similarity; // true is similarity, false if distance

    private boolean debug = false;

    // artifacts

    private double[] a; // score within cluster for each instance
    private double[] b; // score with neighbour cluster for each instance
    private double[] s; // silhouette score of each instance
    private int[] n; // neighbour cluster index

    private Map<String, Integer> clusterIndex = new HashMap<>();
    private String[] clusterIds;
    private double[] clusterScore;
    private double overallScore;

    private List<Integer> clusterOrder;
    private List<List<Integer>> instanceOrder;

    private ClusterSilhouette(Var assignment, DistanceMatrix d, boolean similarity) {
        this.assignment = assignment;
        this.d = d;
        this.similarity = similarity;

        compute();
    }

    public ClusterSilhouette withDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    private int getCluster(int row) {
        return clusterIndex.get(assignment.getLabel(row));
    }

    private void compute() {
        int len = d.length();

        for (int i = 0; i < assignment.rowCount(); i++) {
            if (assignment.isMissing(i)) {
                throw new IllegalArgumentException("Assignment variable contains missing data");
            }
            String clusterId = assignment.getLabel(i);
            if (clusterIndex.containsKey(clusterId))
                continue;
            clusterIndex.put(clusterId, clusterIndex.size());
        }
        clusterIds = new String[clusterIndex.size()];
        for (Map.Entry<String, Integer> entry : clusterIndex.entrySet()) {
            clusterIds[entry.getValue()] = entry.getKey();
        }

        int clusters = clusterIds.length;
        if (clusters == 1) {
            throw new IllegalArgumentException("Silhouettes cannot be computed for a signle cluster.");
        }

        // compute individual a and b vectors

        int rows = d.length();

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
                sum[cluster] += d.get(row, i);
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
                    if (similarity) {
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
        clusterScore = new double[clusters];
        for (int i = 0; i < clusters; i++) {
            clusterScore[i] = count[i] == 0 ? 0 : sum[i] / count[i];
        }
        overallScore = tsum / tcount;

        // build cluster order

        clusterOrder = new ArrayList<>();
        for (int i = 0; i < clusters; i++) {
            clusterOrder.add(i);
        }
        clusterOrder.sort((o1, o2) -> -1 * Double.compare(clusterScore[o1], clusterScore[o2]));

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
    }

    @Override
    public String summary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Cluster silhouette summary\n");
        sb.append("==========================\n");
        sb.append("\n");

        if (debug) {
            for (int i = 0; i < clusterOrder.size(); i++) {
                int cluster = clusterOrder.get(i);
                for (int row : instanceOrder.get(i)) {
                    sb.append(clusterIds[cluster]).append(" ");
                    sb.append(clusterIds[n[row]]).append(" ");
                    sb.append(String.format("%.2f ", s[row]));
                    sb.append(d.name(row)).append(" ");
                    sb.append("\n");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        for (int i = 0; i < clusterOrder.size(); i++) {
            int cluster = clusterOrder.get(i);
            sb.append("Cluster ").append(clusterIds[cluster]).append(" has average silhouette width: ").append(clusterScore[cluster]).append("\n");
        }
        sb.append("\n");

        return sb.toString();
    }
}
