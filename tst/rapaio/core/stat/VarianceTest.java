package rapaio.core.stat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/18.
 */
public class VarianceTest {

    private static final double TOL = 1e-12;

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    void testDouble() {
        VarDouble x = VarDouble.from(100, row -> row % 7 == 0 ? Double.NaN : RandomSource.nextDouble());
        double mu = Mean.of(x).value();
        double s = 0.0;
        double count = 0.0;
        for (int i = 0; i < x.rowCount(); i++) {
            if (x.isMissing(i)) {
                continue;
            }
            count++;
            s += Math.pow(x.getDouble(i) - mu, 2);
        }
        Variance var = Variance.of(x);
        assertEquals(s / (count - 1), var.value(), TOL);
        assertEquals(s / count, var.biasedValue(), TOL);
        assertEquals(Math.sqrt(s / count), var.biasedSdValue(), TOL);

        assertEquals("> variance[?]\n" +
                "total rows: 100 (complete: 85, missing: 15)\n" +
                "variance: 0.0894799\n" +
                "sd: 0.2991319\n", var.toSummary());
    }

    @Test
    void testEmpty() {
        assertEquals(Double.NaN, Variance.of(VarDouble.empty(10)).value(), TOL);
    }
}
