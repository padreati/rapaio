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
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.GridLayer;
import rapaio.image.ImageTools;
import rapaio.ml.eval.metric.ROC;

public class ROCCurveTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testRocCurve() throws IOException {

        ROC roc = ROC.from(df.rvar(0), df.rvar("class"), 2);
        Figure fig = rocCurve(roc);
        assertTest(fig, "roc-test");
    }
}
