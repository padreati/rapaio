package rapaio.graphics;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.bins;
import static rapaio.sys.With.fill;
import static rapaio.sys.With.horizontal;
import static rapaio.sys.With.prob;
import static rapaio.sys.With.sz;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.GridLayer;
import rapaio.image.ImageTools;

public class GridLayerTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testGridLayer() throws IOException {

        Var x = df.rvar(0).name("x");
        Var y = df.rvar(1).name("y");

        Figure fig = gridLayer(3, 3)
                .add(0, 0, 2, 2, points(x, y, sz(2)))
                .add(2, 1, 2, 1, hist2d(x, y, fill(2)))
                .add(lines(x))
                .add(hist(x, bins(20)))
                .add(hist(y, bins(20)));

        assertTest(fig, "grid-test");
    }
}
