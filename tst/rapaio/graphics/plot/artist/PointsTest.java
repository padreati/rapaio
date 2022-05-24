package rapaio.graphics.plot.artist;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.color;
import static rapaio.sys.With.fill;
import static rapaio.sys.With.palette;
import static rapaio.sys.With.pch;
import static rapaio.sys.With.sz;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.datasets.Datasets;
import rapaio.graphics.Figure;
import rapaio.graphics.opt.Palette;
import rapaio.image.ImageTools;

public class PointsTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(1000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testPoints() throws IOException {

        Var x = df.rvar(0).dv().add(11).log1p().dv();
        Var y = df.rvar(1).dv().add(11).log1p().dv();
        Var h = VarDouble.from(x.size(), row -> Math.pow(Math.hypot(x.getDouble(row), y.getDouble(row)), 1.5));

        h.printSummary();

        Figure fig = gridLayer(2, 2)
                .add(points(x))
                .add(points(x, y, pch(2), fill(2), color(1)))
                .add(points(x, y, pch(2), fill(h), sz(4), palette(Palette.hue(0, 240, h.dv().min(), h.dv().max()))))
                .add(points(x, pch(2), fill(y), sz(3), palette(Palette.hue(0, 120, y.dv().min(), y.dv().max()))));
        assertTest(fig, "points-test");
    }
}
