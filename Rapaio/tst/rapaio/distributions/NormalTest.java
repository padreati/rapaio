package rapaio.distributions;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.io.CsvPersistence;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static rapaio.core.BaseFilters.toValue;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class NormalTest {

    private static final double ERROR = 1e-9;
    private Frame df;

    public NormalTest() throws IOException {
        CsvPersistence persistence = new CsvPersistence();
        persistence.setHeader(false);
        persistence.setColSeparator(' ');
        df = persistence.read("stdnorm", this.getClass().getResourceAsStream("standard_normal.csv"));
        df = toValue(df);
    }

    @Test
    public void testStandardQuantile() {
        Normal d = new Normal();
        double step = 0.0001;
        double q = 0;
        int pos = 0;
        while (true) {
            if (pos == 0) {
                assertEquals(Double.NEGATIVE_INFINITY, d.quantile(q), ERROR);
                q += step;
                pos++;
                continue;
            }
            if (pos == 10000) {
                assertEquals(Double.POSITIVE_INFINITY, d.quantile(1.), ERROR);
                q += step;
                pos++;
                break;
            }
            assertEquals(df.getValue(pos, 0), d.quantile(q), ERROR);
            assertEquals(q, d.cdf(d.quantile(q)), ERROR);
            assertEquals(df.getValue(pos, 1), d.pdf(q), ERROR);
            q += step;
            pos++;
        }
    }

    @Test
    public void testExceptions() {
        Normal dist = new Normal();
        try {
            dist.cdf(Double.NaN);
            assertFalse(true);
        } catch (IllegalArgumentException ex) {
        }
        try {
            dist.cdf(Double.NEGATIVE_INFINITY);
            assertFalse(true);
        } catch (IllegalArgumentException ex) {
        }
        try {
            dist.cdf(Double.POSITIVE_INFINITY);
            assertFalse(true);
        } catch (IllegalArgumentException ex) {
        }
        try {
            dist.quantile(-1);
            assertFalse(true);
        } catch (IllegalArgumentException ex) {
        }
    }

    @Test
    public void testAttributes() {
        Normal distr = new Normal(1, 1);
        assertEquals(1., distr.getMu(), ERROR);
        assertEquals(1., distr.getVar(), ERROR);
    }
}