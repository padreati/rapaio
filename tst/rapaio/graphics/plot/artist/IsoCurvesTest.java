package rapaio.graphics.plot.artist;

import static java.lang.StrictMath.pow;

import static rapaio.graphics.Plotter.*;
import static rapaio.graphics.opt.Palette.bicolor;
import static rapaio.graphics.opt.Palette.hue;
import static rapaio.sys.With.palette;

import java.io.IOException;
import java.util.function.BiFunction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.tools.GridData;
import rapaio.data.VarDouble;
import rapaio.graphics.opt.NColor;
import rapaio.graphics.opt.Palette;
import rapaio.graphics.plot.GridLayer;
import rapaio.image.ImageTools;

public class IsoCurvesTest extends AbstractArtistTest {

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testIsoCurves() throws IOException {

        GridLayer grid = new GridLayer(2, 2);

        BiFunction<Double, Double, Double> fun = (x, y) -> pow(x * x + y - 11, 2) + pow(x + y * y - 7, 2);

        GridData gd = GridData.fromFunction(fun, -3, 3, -3, 3, 256);
        int levelCount = 30;
        double[] p = VarDouble.seq(0, 1, 1. / levelCount).elements();
        double[] levels = gd.quantiles(p);

        grid.add(isoCurves(gd, levels, palette(bicolor(NColor.darkred, NColor.dodgerblue, gd.minValue(), gd.maxValue()))));
        grid.add(isoBands(gd, levels, palette(hue(0, 240, gd.minValue(), gd.maxValue()))));

        grid.add(isoLines(gd, levels, palette(hue())));
        grid.add(isoCurves(gd, levels, palette(hue())).xLim(-2,2).yLim(-4, 4));

        assertTest(grid, "isocurves-test");
    }
}
