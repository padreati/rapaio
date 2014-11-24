/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.ml.refactor.ml_experiment.clustering;

import rapaio.core.sample.Sampling;
import rapaio.core.stat.Mean;
import rapaio.data.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public class KMeansClustering {

    private int k = 2;
    private int runs = Integer.MAX_VALUE;
    private StartMethod startMethod = StartMethod.Forgy;
    private Distance distance = Distance.Euclidean;

    // clustering artifacts

    private List<String> targets;
    private Frame centroids;
    private int[] arrows;

    public KMeansClustering withK(int k) {
        this.k = k;
        return this;
    }

    public KMeansClustering withRuns(int runs) {
        this.runs = runs;
        return this;
    }

    public Var getClusterAssignement() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            names.add("c" + (i + 1));
        }
        Var var = Nominal.newEmpty(arrows.length, names);
        for (int i = 0; i < arrows.length; i++) {
            var.setIndex(i, arrows[i] + 1);
        }
        return var;
    }

    public void cluster(Frame df, String varNames) {
        validate(df, varNames);
        targets = new VarRange(varNames).parseVarNames(df);
        centroids = SolidFrame.newMatrix(k, targets.toArray(new String[targets.size()]));
        arrows = new int[df.rowCount()];

        startMethod.init(df, centroids);
        assignToCentroid(df);

        int rounds = runs;
        while (rounds-- > 0) {
            refineCentroid(df);
            boolean progress = assignToCentroid(df);
            if (!progress) break;
        }
    }

    private boolean assignToCentroid(Frame df) {
        boolean changed = false;
        for (int i = 0; i < df.rowCount(); i++) {
            double d = Double.NaN;
            int cluster = -1;
            for (int j = 0; j < centroids.rowCount(); j++) {
                double dd = distance.distance(df, i, centroids, j, targets);
                if (!Double.isFinite(dd)) continue;
                if (Double.isFinite(d)) {
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
            if (arrows[i] != cluster) {
                changed = true;
            }
            arrows[i] = cluster;
        }
        return changed;
    }

    private void refineCentroid(Frame df) {
        Var[] means = new Var[k];
        for (int i = 0; i < k; i++) {
            means[i] = Numeric.newFill(df.rowCount(), 0);
        }
        for (int i = 0; i < targets.size(); i++) {
            for (int j = 0; j < k; j++) {
                means[j].clear();
            }
            for (int j = 0; j < df.rowCount(); j++) {
                means[arrows[j]].addValue(df.value(j, targets.get(i)));
            }
            for (int j = 0; j < k; j++) {
                centroids.setValue(j, targets.get(i), new Mean(means[j]).value());
            }
        }
    }


    private void validate(Frame df, String varNames) {
        List<String> nameList = new VarRange(varNames).parseVarNames(df);
        for (String varName : nameList) {
            if (!df.var(varName).type().isNumeric())
                throw new IllegalArgumentException("all matched vars must be numeric: check var " + varName);
            if (df.var(varName).stream().complete().count() != df.rowCount()) {
                throw new IllegalArgumentException("all matched vars must have non-missing values: check var " + varName);
            }
        }
    }

    public static enum StartMethod {
        Forgy {
            @Override
            public void init(Frame df, Frame centroids) {
                int[] indexes = Sampling.sampleWOR(centroids.rowCount(), df.rowCount());
                for (int i = 0; i < indexes.length; i++) {
                    for (int j = 0; j < centroids.varNames().length; j++) {
                        centroids.setValue(i, j, df.value(indexes[i], centroids.varNames()[j]));
                    }
                }

            }
        }, RandomPartition {
            @Override
            public void init(Frame df, Frame centroids) {
                throw new NotImplementedException();
            }
        };

        public abstract void init(Frame df, Frame centroids);
    }

    public static enum Distance {
        Euclidean {
            @Override
            public double distance(Frame s, int sRow, Frame t, int tRow, List<String> varNames) {
                double total = 0;
                for (String varName : varNames) {
                    total += Math.pow(s.value(sRow, varName) - t.value(tRow, varName), 2);
                }
                return Math.sqrt(total);
            }
        };

        public abstract double distance(Frame s, int sRow, Frame t, int tRow, List<String> varNames);
    }
}
