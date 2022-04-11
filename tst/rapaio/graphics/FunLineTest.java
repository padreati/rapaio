package rapaio.graphics;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.color;
import static rapaio.sys.With.points;

import java.awt.Color;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.Plot;
import rapaio.image.ImageTools;

public class FunLineTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testFunLine() throws IOException {

        Plot plot = funLine(x -> x * x, color(1))
                .funLine(Math::log1p, color(2))
                .funLine(x -> Math.sin(Math.pow(x, 3)) + 5, color(3), points(10_000))
                .hLine(5, color(Color.LIGHT_GRAY))
                .xLim(0, 10)
                .yLim(0, 10);

        assertTest(plot, "funline-test");
    }
}
