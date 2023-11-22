/*
 *
 *  * Apache License
 *  * Version 2.0, January 2004
 *  * http://www.apache.org/licenses/
 *  *
 *  * Copyright 2013 - 2022 Aurelian Tutuianu
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package rapaio.ml.eval;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import rapaio.core.tools.DistanceMatrix;
import rapaio.data.Var;
import rapaio.data.VarInt;

public class ClusterSilhouetteTest {

    @Test
    public void paperTest() {
        /*
        This test reproduces the results from original paper:
        "Silhouettes: a graphical aid to the interpretation and validation of cluster analysis - Peter J. ROUSSEEUW"
         */
        String[] names = new String[] {"BEL", "BRA", "CHI", "CUB", "EGY", "FRA", "IND", "ISR", "USA", "USS", "YUG", "ZAI"};
        DistanceMatrix dm = DistanceMatrix.empty(names);

        double[] values = new double[] {
                5.58,
                7.00, 6.50,
                7.08, 7.00, 3.83,
                4.83, 5.08, 8.17, 5.83,
                2.17, 5.75, 6.67, 6.92, 4.92,
                6.42, 5.00, 5.58, 6.00, 4.67, 6.42,
                3.42, 5.50, 6.42, 6.42, 5.00, 3.92, 6.17,
                2.50, 4.92, 6.25, 7.33, 4.50, 2.25, 6.33, 2.75,
                6.08, 6.67, 4.25, 2.67, 6.00, 6.17, 6.17, 6.92, 6.17,
                5.25, 6.83, 4.50, 3.75, 5.75, 5.42, 6.08, 5.83, 6.67, 3.67,
                4.75, 3.00, 6.08, 6.67, 5.00, 5.58, 4.83, 6.17, 5.67, 6.50, 6.92
        };
        int pos = 0;
        for (int i = 1; i < names.length; i++) {
            for (int j = 0; j < i; j++) {
                dm.set(i, j, values[pos++]);
            }
        }

        Var asgn1 = VarInt.wrap(1, 1, 2, 2, 1, 1, 2, 1, 1, 2, 2, 1);
        Var asgn2 = VarInt.wrap(1, 2, 3, 3, 1, 1, 2, 1, 1, 3, 3, 2);

        ClusterSilhouette cs1 = ClusterSilhouette.from(asgn1,dm,false).compute();

        assertEquals(2, cs1.getClusterCount());
        assertArrayEquals(new String[] {"1", "2"}, cs1.getClusterLabels());
        assertArrayEquals(new double[] {0.30, 0.26}, cs1.getClusterScores(), 1e-2);
        assertEquals(0.28, cs1.getAverageClusterScore(), 1e-2);
        assertArrayEquals(new double[]{0.39, 0.22, 0.33, 0.40, 0.20, 0.35, -0.04, 0.30, 0.43, 0.34, 0.26, 0.19}, cs1.getScores(), 1e-2);

        assertEquals("ClusterSilhouette{clusters:2, overall score: 0.2796659}", cs1.toString());
        assertEquals("""
Cluster silhouette summary
==========================

Cluster 1 has average silhouette width: 0.2963655459588149
Cluster 2 has average silhouette width: 0.25628640863559815

                """, cs1.toSummary());
        assertEquals("""
Cluster silhouette summary
==========================

Cluster 1 has average silhouette width: 0.2963655459588149
Cluster 2 has average silhouette width: 0.25628640863559815

                """, cs1.toContent());
        assertEquals("""
                Cluster silhouette summary
                ==========================

                1 2  0.43 USA\s
                1 2  0.39 BEL\s
                1 2  0.35 FRA\s
                1 2  0.30 ISR\s
                1 2  0.22 BRA\s
                1 2  0.20 EGY\s
                1 2  0.19 ZAI\s

                2 1  0.40 CUB\s
                2 1  0.34 USS\s
                2 1  0.33 CHI\s
                2 1  0.26 YUG\s
                2 1 -0.04 IND\s


                Cluster 1 has average silhouette width: 0.2963655459588149
                Cluster 2 has average silhouette width: 0.25628640863559815

                                """, cs1.toFullContent());

        ClusterSilhouette cs2 = ClusterSilhouette.from(asgn2, dm, false).compute();

        assertEquals(3, cs2.getClusterCount());
        assertArrayEquals(new String[] {"1", "2", "3"}, cs2.getClusterLabels());
        assertArrayEquals(new double[] {0.34, 0.24, 0.38}, cs2.getClusterScores(), 1e-2);
        assertEquals(0.33, cs2.getAverageClusterScore(), 1e-2);
        assertArrayEquals(new double[]{0.42, 0.25, 0.31, 0.48, 0.02, 0.44, 0.17, 0.37, 0.47, 0.44, 0.31, 0.28}, cs2.getScores(), 1e-2);

        assertEquals("ClusterSilhouette{clusters:3, overall score: 0.3301021}", cs2.toString());
        assertEquals("""
Cluster silhouette summary
==========================

Cluster 3 has average silhouette width: 0.38401001090035614
Cluster 1 has average silhouette width: 0.34321867615612356
Cluster 2 has average silhouette width: 0.23636384893740545

                """, cs2.toSummary());
        assertEquals("""
Cluster silhouette summary
==========================

Cluster 3 has average silhouette width: 0.38401001090035614
Cluster 1 has average silhouette width: 0.34321867615612356
Cluster 2 has average silhouette width: 0.23636384893740545

                """, cs2.toContent());
        assertEquals("""
                Cluster silhouette summary
                ==========================

                3 2  0.48 CUB\s
                3 1  0.44 USS\s
                3 1  0.31 YUG\s
                3 2  0.31 CHI\s

                1 2  0.47 USA\s
                1 2  0.44 FRA\s
                1 2  0.42 BEL\s
                1 2  0.37 ISR\s
                1 2  0.02 EGY\s

                2 1  0.28 ZAI\s
                2 1  0.25 BRA\s
                2 3  0.17 IND\s


                Cluster 3 has average silhouette width: 0.38401001090035614
                Cluster 1 has average silhouette width: 0.34321867615612356
                Cluster 2 has average silhouette width: 0.23636384893740545

                                """, cs2.toFullContent());
    }

    @Test
    void validationTest() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                ClusterSilhouette.from(VarInt.wrap(1), DistanceMatrix.empty(2), true).compute());
        assertEquals("Assignment and distance matrix sizes does not match.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () ->
                ClusterSilhouette.from(VarInt.wrap(VarInt.MISSING_VALUE, 1), DistanceMatrix.empty(2), true).compute());
        assertEquals("Assignment variable contains missing data", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () ->
                ClusterSilhouette.from(VarInt.fill(1, 0), DistanceMatrix.empty(1), true).compute());
        assertEquals("Silhouettes cannot be computed for a single cluster.", ex.getMessage());
    }
}
