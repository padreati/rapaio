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

package rapaio.ml.clustering.kmeans;

import rapaio.core.RandomSource;
import rapaio.core.SamplingTools;
import rapaio.math.linear.DMatrix;
import rapaio.util.collection.DoubleArrays;
import rapaio.util.collection.IntArrays;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Function which produces initial centroids for KMeans algorithm
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/23/15.
 */
public enum KMeansInit implements Serializable {

    Forgy {
        public DMatrix init(KMeans.Space space, DMatrix m, int k) {
            return m.mapRows(SamplingTools.sampleWOR(m.rowCount(), k)).copy();
        }
    },
    PlusPlus {
        @Override
        public DMatrix init(KMeans.Space space, DMatrix m, int k) {

            int[] rows = IntArrays.newSeq(0, m.rowCount());
            int[] centroids = IntArrays.newFill(k, -1);

            centroids[0] = RandomSource.nextInt(m.rowCount());
            Set<Integer> ids = new HashSet<>();//new IntOpenHashSet();
            ids.add(centroids[0]);

            double[] p = new double[m.rowCount()];
            for (int i = 1; i < k; i++) {
                // fill weights with 0
                Arrays.fill(p, 0);
                // assign weights to the minimum distance to center
                for (int j = 0; j < m.rowCount(); j++) {
                    if (ids.contains(j)) {
                        continue;
                    }
                    p[j] = space.distance(m.mapRow(centroids[0]), m.mapRow(j));
                    for (int l = 1; l < i; l++) {
                        p[j] = Math.min(p[j], space.distance(m.mapRow(centroids[l]), m.mapRow(j)));
                    }
                }
                // normalize the weights
                double sum = DoubleArrays.sum(p, 0, p.length);
                DoubleArrays.div(p, 0, sum, p.length);

                int next = SamplingTools.sampleWeightedWR(1, p)[0];
                centroids[i] = next;
                ids.add(next);
            }

            return m.mapRows(centroids).copy();
        }
    };

    public abstract DMatrix init(KMeans.Space space, DMatrix m, int k);
}
