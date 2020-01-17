package rapaio.math.linear.dense;

import org.junit.jupiter.api.Test;
import rapaio.math.linear.DMatrix;
import rapaio.math.linear.StandardDMatrixTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/16/20.
 */
public class BaseDMatrixTest extends StandardDMatrixTest {

    @Override
    protected DMatrix generateSequential(int n, int m) {
        BaseDMatrix matrix = BaseDMatrix.empty(n, m);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix.set(i, j, i * m + j);
            }
        }
        return matrix;
    }

    @Override
    protected DMatrix generateFill(int n, int m, double fill) {
        BaseDMatrix matrix = BaseDMatrix.empty(n, m);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix.set(i, j, fill);
            }
        }
        return matrix;
    }

    @Override
    protected DMatrix generateIdentity(int n) {
        BaseDMatrix matrix = BaseDMatrix.empty(n, n);
        for (int i = 0; i < n; i++) {
            matrix.set(i, i, 1);
        }
        return matrix;
    }

    @Override
    protected DMatrix generateWrap(double[][] values) {
        return BaseDMatrix.wrap(values);
    }

    @Override
    protected String className() {
        return "BaseDMatrix";
    }

    @Test
    void testBuilders() {
        DMatrix m = BaseDMatrix.empty(10, 11);
        assertEquals(10, m.rowCount());
        assertEquals(11, m.colCount());

        for (int i = 0; i < m.rowCount(); i++) {
            for (int j = 0; j < m.colCount(); j++) {
                assertEquals(0, m.get(i, j));
            }
        }
    }
}
