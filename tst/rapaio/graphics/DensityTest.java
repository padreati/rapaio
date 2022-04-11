package rapaio.graphics;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.alpha;
import static rapaio.sys.With.bins;
import static rapaio.sys.With.color;
import static rapaio.sys.With.fill;
import static rapaio.sys.With.horizontal;
import static rapaio.sys.With.lwd;
import static rapaio.sys.With.prob;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.empirical.KFuncGaussian;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.GridLayer;
import rapaio.image.ImageTools;

public class DensityTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testDensity() throws IOException {

        Var x = df.rvar(0).mapRows(Mapping.range(200));
        var up = densityLine(x, new KFuncGaussian(), lwd(30), alpha(0.1f), color(2));
        for (int i = 10; i < 150; i += 5) {
            up.densityLine(x, i / 300.0);
        }
        up.densityLine(x, lwd(2), color(1));

        var down = densityLine(df.rvar(0), fill(13));
        assertTest(gridLayer(1, 2).add(up).add(down), "density-test");
    }
}
