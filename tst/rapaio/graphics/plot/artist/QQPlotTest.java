package rapaio.graphics.plot.artist;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.fill;
import static rapaio.sys.With.pch;

import java.awt.Color;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.AbstractArtistTest;
import rapaio.image.ImageTools;

public class QQPlotTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testQQPlot() throws IOException {
        Var x = df.rvar(2);
        Distribution normal = Normal.of(Mean.of(x).value(), Variance.of(x).sdValue());
        Plot plot = qqplot(x, normal, pch(2), fill(3))
                .vLine(0, fill(Color.GRAY))
                .hLine(0, fill(Color.GRAY));

        assertTest(plot, "qqplot-test");
    }
}
