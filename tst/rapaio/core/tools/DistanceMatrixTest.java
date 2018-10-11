package rapaio.core.tools;

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.math.linear.RM;
import rapaio.math.linear.dense.SolidRM;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/13/17.
 */
public class DistanceMatrixTest {

    private static final double TOL = 1e-20;

    @Test
    public void basicDistanceMatrix() throws IOException, URISyntaxException {

        RandomSource.setSeed(1234);

        Normal normal = Normal.of(0, 10);

        // generate symmetric matrix
        RM sym = SolidRM.empty(4, 4);
        for (int i = 0; i < 4; i++) {
            for (int j = i; j < 4; j++) {
                double next = normal.sampleNext();
                sym.set(i, j, next);
                sym.set(j, i, next);
            }
        }

        // build a distance matrix and copy values from sym
        String[] names = new String[] {"1", "2", "3", "4"};
        DistanceMatrix d = DistanceMatrix.empty(names);
        for (int i = 0; i < 4; i++) {
            for (int j = i; j < 4; j++) {
                d.set(i, j, sym.get(i, j));
            }
        }

        // now compare
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                assertEquals(sym.get(i, j), d.get(i, j), TOL);
            }
        }
        assertEquals(names.length, d.length());
        for (int i = 0; i < d.length(); i++) {
            assertEquals(names[i], d.name(i));
        }
    }
}
