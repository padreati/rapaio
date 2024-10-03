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

package rapaio.graphics.plot.artist;

import static rapaio.graphics.Plotter.*;
import static rapaio.graphics.opt.GOpts.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.tools.DistanceMatrix;
import rapaio.data.Frame;
import rapaio.data.VarInt;
import rapaio.datasets.Datasets;
import rapaio.printer.ImageTools;
import rapaio.ml.eval.ClusterSilhouette;
import rapaio.ml.model.km.KMCluster;
import rapaio.ml.model.km.KMClusterResult;

public class SilhouetteTest extends AbstractArtistTest {

    @BeforeEach
    void setUp() throws Exception {
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testSilhouette() throws IOException {
        Frame df = Datasets.loadIrisDataset().removeVars("class");

        KMCluster kMeans = KMCluster.newKMeans().k.set(2).method.set(KMCluster.KMeans);
        kMeans.fit(df);
        KMClusterResult prediction = kMeans.predict(df);
        VarInt assignment = prediction.assignment();

        DistanceMatrix dm = DistanceMatrix.empty(df.rowCount()).fill((i, j) -> {
            double sum = 0;
            for (int k = 0; k < df.varCount(); k++) {
                double delta = df.getDouble(i, k) - df.getDouble(j, k);
                sum += delta * delta;
            }
            return Math.sqrt(sum);
        });
        ClusterSilhouette silhouette = ClusterSilhouette.from(assignment, dm, false).compute();

        assertTest(silhouette(silhouette, horizontal(true)), "silhouette-test");
    }
}
