package rapaio.data.filter.frame;

import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.VRange;
import rapaio.data.VType;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/4/18.
 */
public class FOneHotEncodingTest {

    private static final double TOL = 1e-20;

    @Before
    public void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    public void testDouble() {
        Frame df = FFilterTestUtil.allDoubleNominal(100, 2, 2);

        Frame f1 = df.fapply(FOneHotEncoding.on(VRange.onlyTypes(VType.DOUBLE)));
        assertTrue(f1.deepEquals(df));

        Frame f2 = df.apply(FOneHotEncoding.on("v1,v2"));
        assertTrue(f2.deepEquals(df));
    }

    @Test
    public void testNominal() {
        Frame df = FFilterTestUtil.allDoubleNominal(100, 2, 2).mapVars(VRange.of(2));

        List<String> levels = df.rvar(0).levels();

        Frame f1 = df.fapply(FOneHotEncoding.on(false, true, "all"));
        assertEquals(levels.size(), f1.varCount());

        for (int i = 0; i < levels.size(); i++) {
            assertTrue(f1.varName(i).contains(levels.get(i)));
        }

        for (int i = 0; i < f1.rowCount(); i++) {
            double sum = 0;
            for (int j = 0; j < f1.varCount(); j++) {
                sum += f1.getDouble(i, j);
            }
            assertEquals(1.0, sum, TOL);
        }

        Frame f2 = df.fapply(FOneHotEncoding.on(true, false, VRange.all()).newInstance());
        assertEquals(levels.size()-2, f2.varCount());

        for (int i = 2; i < levels.size(); i++) {
            assertTrue(f2.varName(i-2).contains(levels.get(i)));
        }
    }
}
