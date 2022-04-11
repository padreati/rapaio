package rapaio.graphics;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.bins;
import static rapaio.sys.With.fill;
import static rapaio.sys.With.horizontal;
import static rapaio.sys.With.lwd;
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

public class SegmentTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testSegment() throws IOException {
        Plot plot = new Plot();
        plot.xLim(0, 1);
        plot.yLim(0, 1);

        plot.segmentLine(0.1, 0.1, 0.7, 0.7, fill(1));
        plot.segmentArrow(0.1, 0.9, 0.9, 0.1, fill(2), lwd(6));

        assertTest(plot, "segment-test");
    }
}
