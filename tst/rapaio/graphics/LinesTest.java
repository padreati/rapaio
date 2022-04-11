package rapaio.graphics;

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
import rapaio.data.filter.VApply;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.GridLayer;
import rapaio.image.ImageTools;

public class LinesTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testLines() throws IOException {

        Var x = df.rvar(0).fapply(VApply.onDouble(Math::log1p)).name("x").stream().complete().toMappedVar();
        Figure fig = gridLayer(1, 2)
                .add(lines(x))
                .add(lines(x).yLim(-2, -1));
        assertTest(fig, "lines-test");
    }
}
