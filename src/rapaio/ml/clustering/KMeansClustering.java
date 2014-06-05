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

package rapaio.ml.clustering;

import rapaio.core.ColRange;
import rapaio.core.sample.DiscreteSampling;
import rapaio.data.Frame;
import rapaio.data.Frames;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
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

    public void cluster(Frame df, String varNames) {
        validate(df, varNames);
        targets = new ColRange(varNames).parseColumnNames(df);
        centroids = Frames.newMatrix(k, targets.toArray(new String[targets.size()]));
        arrows = new int[df.rowCount()];

        startMethod.init(df, centroids);

        int rounds = runs;
        while (rounds-- > 0) {
            assignToCentroid();
            boolean progress = refineCentroid();
            if (!progress) break;
        }
    }

    private void assignToCentroid() {

    }

    private boolean refineCentroid() {
        return false;
    }


    private void validate(Frame df, String varNames) {
        List<String> nameList = new ColRange(varNames).parseColumnNames(df);
        for (String varName : nameList) {
            if (!df.col(varName).type().isNumeric())
                throw new IllegalArgumentException("all matched vars must be numeric: check var " + varName);
            if (df.col(varName).stream().complete().count() != df.rowCount()) {
                throw new IllegalArgumentException("all matched vars must have non-missing values: check var " + varName);
            }
        }
    }

    public static enum StartMethod {
        Forgy {
            @Override
            public void init(Frame df, Frame centroids) {
                int[] indexes = new DiscreteSampling().sampleWOR(centroids.rowCount(), df.rowCount());
                for (int i = 0; i < indexes.length; i++) {
                    for (int j = 0; j < centroids.colNames().length; j++) {
                        centroids.setValue(i, j, df.value(indexes[i], centroids.colNames()[j]));
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
