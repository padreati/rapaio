package rapaio.core;

import org.junit.Test;
import rapaio.data.Frame;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static rapaio.core.BaseMath.sqrt;
import static rapaio.core.BaseStat.variance;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class VarianceTest extends CoreStatTestUtil {

    public VarianceTest() throws IOException {
    }

    @Test
    public void testRReferenceVariance() {
        Frame df = getDataFrame();
        assertEquals(Double.valueOf("1.0012615815492349469"), sqrt(variance(df.getCol(0)).value()), 1e-12);
    }
}
