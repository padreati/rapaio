package rapaio.core;

import org.junit.Test;
import rapaio.core.stat.Mean;
import rapaio.data.Frame;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class MeanTest extends CoreStatTestUtil {

    public MeanTest() throws IOException {
    }

    @Test
    public void testRReferenceMean() {
        Frame df = getDataFrame();
        assertEquals(Double.valueOf("999.98132402093892779"), new Mean(df.getCol(0)).getValue(), 1e-12);
    }
}
