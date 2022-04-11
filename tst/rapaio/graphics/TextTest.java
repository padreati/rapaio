package rapaio.graphics;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.HALIGN_CENTER;
import static rapaio.sys.With.HALIGN_LEFT;
import static rapaio.sys.With.HALIGN_RIGHT;
import static rapaio.sys.With.bins;
import static rapaio.sys.With.color;
import static rapaio.sys.With.hAlign;
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
import rapaio.image.ImageTools;

public class TextTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testText() throws IOException {
        var plot = plot().xLim(0, 1).yLim(0, 1);
        plot.text(0.1, 0.9, "Ana\nAre\nMere", hAlign(HALIGN_LEFT));
        plot.text(0.5, 0.9, "Ana\nAre\nMere", hAlign(HALIGN_CENTER), color(2));
        plot.text(0.8, 0.9, "Ana\nAre\nMere", hAlign(HALIGN_RIGHT), color(4));

        assertTest(plot, "text-test");
    }
}
