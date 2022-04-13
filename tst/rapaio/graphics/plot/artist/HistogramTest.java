package rapaio.graphics.plot.artist;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.bins;
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
import rapaio.graphics.plot.artist.AbstractArtistTest;
import rapaio.image.ImageTools;

public class HistogramTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testHistogram() throws IOException {
        Var x = df.rvar(0).name("x");

        GridLayer grid = new GridLayer(2, 2);

        grid.add(hist(x, -10, -2, bins(30)).xLim(-10,-2));
        grid.add(hist(x));

        grid.add(hist(x, bins(40), horizontal(true)).yLim(-10,-2));
        grid.add(hist(x, bins(40), horizontal(true), prob(true)).yLim(-10,-2).xLim(0, 0.02));

        assertTest(grid, "hist-test");
    }
}
