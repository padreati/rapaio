package rapaio.graphics;

import static java.lang.StrictMath.sqrt;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.With.color;
import static rapaio.sys.With.fill;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.tools.DistanceMatrix;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.GridLayer;
import rapaio.image.ImageTools;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.DVector;
import rapaio.math.linear.dense.DMatrixDenseC;

public class MatrixTest extends AbstractArtistTest {

    private Frame df;

    @BeforeEach
    void setUp() throws Exception {
        RandomSource.setSeed(1234);
        df = Datasets.loadLifeScience().mapRows(Mapping.range(2000));
        ImageTools.setBestRenderingHints();
    }

    @Test
    void testMatrix() throws IOException {
        GridLayer grid = new GridLayer(2, 2);

        int n = 6;

        DMatrix random = DMatrix.random(n, n);
        DVector mean = random.mean(0);
        DVector sd = random.sd(0).mul(sqrt(n-1));

        random.sub(mean, 0).div(sd, 0);

        DMatrix cov = random.t().dot(random).roundValues(15);

        DistanceMatrix dm = DistanceMatrix.empty(n).fill(cov::get);
        grid.add(matrix(cov));
        grid.add(corrGram(dm));

        grid.add(matrix(cov, color(0)));
        grid.add(matrix(DMatrixDenseC.random(60, 80)));

        assertTest(grid, "matrix-test");
    }
}
