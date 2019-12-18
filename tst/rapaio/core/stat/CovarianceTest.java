package rapaio.core.stat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/18.
 */
public class CovarianceTest {

    private static final double TOL = 1e-12;

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    void testDouble() {
        VarDouble x = VarDouble.from(100, row -> row % 7 == 0 ? Double.NaN : RandomSource.nextDouble());
        VarDouble y = VarDouble.from(100, row -> row % 7 == 0 ? Double.NaN : RandomSource.nextDouble());
        double mu1 = Mean.of(x).value();
        double mu2 = Mean.of(y).value();

        double cs = 0;
        double s1 = 0.0;
        double s2 = 0.0;
        double count = 0.0;
        for (int i = 0; i < x.rowCount(); i++) {
            if (x.isMissing(i)) {
                continue;
            }
            count++;
            s1 += Math.pow(x.getDouble(i) - mu1, 2);
            s2 += Math.pow(y.getDouble(i) - mu2, 2);
            cs += (x.getDouble(i) - mu1) * (y.getDouble(i) - mu2);
        }
        Covariance cov = Covariance.of(x, y);
        assertEquals(cs / (count - 1), cov.value(), TOL);

        assertEquals("> cov[?,?]\n" +
                "total rows: 100 (complete: 85, missing: 15)\n" +
                "covariance: -0.0050385\n", cov.toSummary());
    }

    @Test
    void testEmpty() {
        assertEquals(Double.NaN, Covariance.of(VarDouble.empty(10), VarDouble.empty(10)).value(), TOL);
    }
}
