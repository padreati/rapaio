package rapaio.math.linear.base;

import org.junit.jupiter.api.Test;
import rapaio.math.linear.DM;
import rapaio.math.linear.StandardDMTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/16/20.
 */
public class DMBaseTest extends StandardDMTest {

    @Override
    protected DM.Type type() {
        return DM.Type.BASE;
    }

    @Override
    protected DM generateSequential(int n, int m) {
        DMBase matrix = DMBase.empty(n, m);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix.set(i, j, i * m + j);
            }
        }
        return matrix;
    }

    @Override
    protected DM generateFill(int n, int m, double fill) {
        DMBase matrix = DMBase.empty(n, m);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix.set(i, j, fill);
            }
        }
        return matrix;
    }

    @Override
    protected DM generateIdentity(int n) {
        DMBase matrix = DMBase.empty(n, n);
        for (int i = 0; i < n; i++) {
            matrix.set(i, i, 1);
        }
        return matrix;
    }

    @Override
    protected DM generateWrap(double[][] values) {
        return DMBase.wrap(values);
    }

    @Override
    protected String className() {
        return "DMBase";
    }

    @Test
    void testBuilders() {
        DM m = DMBase.empty(10, 11);
        assertEquals(10, m.rowCount());
        assertEquals(11, m.colCount());

        for (int i = 0; i < m.rowCount(); i++) {
            for (int j = 0; j < m.colCount(); j++) {
                assertEquals(0, m.get(i, j));
            }
        }
    }
}
