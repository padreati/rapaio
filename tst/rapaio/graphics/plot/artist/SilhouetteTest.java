package rapaio.graphics.plot.artist;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.horizontal;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.tools.DistanceMatrix;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.VarInt;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.artist.AbstractArtistTest;
import rapaio.image.ImageTools;
import rapaio.ml.eval.ClusterSilhouette;
import rapaio.ml.model.km.KMCluster;
import rapaio.ml.model.km.KMClusterResult;

public class SilhouetteTest extends AbstractArtistTest {

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testSilhouette() throws IOException {
        Frame df = Datasets.loadIrisDataset().removeVars("class");

        KMCluster kMeans = KMCluster.newKMeans().k.set(2).method.set(KMCluster.KMeans);
        kMeans.fit(df);
        KMClusterResult prediction = kMeans.predict(df);
        VarInt assignment = prediction.getAssignment();

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
