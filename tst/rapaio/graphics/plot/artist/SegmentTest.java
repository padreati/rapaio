package rapaio.graphics.plot.artist;

import static rapaio.sys.With.fill;
import static rapaio.sys.With.lwd;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.artist.AbstractArtistTest;
import rapaio.image.ImageTools;

public class SegmentTest extends AbstractArtistTest {

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
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
