package rapaio.datasets;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rapaio.data.Frame;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DatasetsTest {

    public DatasetsTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testIrisDataset() throws IOException {
        Frame df = Datasets.loadIrisDataset();

        assertNotNull(df);
        assertEquals(5, df.getColCount());
        assertEquals(150, df.getRowCount());

        final String[] names = new String[]{"sepal-length", "sepal-width", "petal-length", "petal-width", "class"};
        assertArrayEquals(names, df.getColNames());

        int nas = 0;
        for (int i = 0; i < df.getColCount(); i++) {
            for (int j = 0; j < df.getRowCount(); j++) {
                if (df.getCol(i).isMissing(j)) {
                    nas++;
                }
            }
        }
        assertEquals(0, nas);
    }
}