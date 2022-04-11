package rapaio.graphics;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.alpha;
import static rapaio.sys.With.bins;
import static rapaio.sys.With.fill;
import static rapaio.sys.With.horizontal;
import static rapaio.sys.With.prob;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.GridLayer;
import rapaio.graphics.plot.Plot;
import rapaio.image.ImageTools;

public class Histogram2DTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testHistogram2D() throws IOException {

        Var x = df.rvar(0).copy().name("x");
        Var y = df.rvar(1).copy().name("y");

        Plot plot = hist2d(x, y, fill(2), bins(20)).points(x, y, alpha(0.3f));
        assertTest(plot, "hist2d-test");
    }
}
