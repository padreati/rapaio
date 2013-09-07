package rapaio.core;

import org.junit.Test;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static rapaio.core.BaseMath.sqrt;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class VarianceTest extends CoreStatTestUtil {

    public VarianceTest() throws IOException {
    }

    @Test
    public void testRReferenceVariance() {
        Frame df = getDataFrame();
        assertEquals(Double.valueOf("1.0012615815492349469"), sqrt(new Variance(df.getCol(0)).getValue()), 1e-12);
    }
}
