package rapaio.graphics.plot.artist;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.color;
import static rapaio.sys.With.fill;
import static rapaio.sys.With.pch;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.filter.VApply;
import rapaio.datasets.Datasets;
import rapaio.graphics.Figure;
import rapaio.graphics.plot.artist.AbstractArtistTest;
import rapaio.image.ImageTools;

public class PointsTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testPoints() throws IOException {

        Var x = df.rvar(0).fapply(VApply.onDouble(Math::log1p)).name("x");
        Var y = df.rvar(1).fapply(VApply.onDouble(Math::log1p)).name("y");

        Figure fig = gridLayer(1, 2)
                .add(points(x))
                .add(points(x, y, pch(2), fill(2), color(1)).xLim(-3, -1));
        assertTest(fig, "points-test");
    }
}
