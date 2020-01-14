package rapaio.math.linear;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.math.linear.dense.BaseDMatrix;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/14/20.
 */
public class AbstractDMatrixTest {

    private static final double TOL = 1e-15;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    private DMatrix generate(int n, int m) {
        BaseDMatrix matrix = BaseDMatrix.empty(n, m);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                matrix.set(i, j, i * m + j);
            }
        }
        return matrix;
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

    @Test
    void testMapRow() {
        DMatrix m = generate(10, 11);
        DVector vector1 = m.mapRow(3);
        DVector vector2 = m.mapRowCopy(3);
        for (int i = 0; i < vector1.size(); i++) {
            assertEquals(33.0 + i, vector1.get(i), TOL);
            assertEquals(33.0 + i, vector2.get(i), TOL);
        }
    }

    @Test
    void testMapRows() {
        DMatrix m = generate(10, 11);

        DMatrix view = m.mapRows(3, 4);
        DMatrix copy = m.mapRowsCopy(3, 4);

        for (int i = 0; i < view.rowCount(); i++) {
            for (int j = 0; j < view.colCount(); j++) {
                assertEquals(view.get(i, j), copy.get(i, j), TOL);
            }
        }

        view.set(0, 0, 100);
        copy.set(0, 0, 200);

        assertEquals(m.get(3, 0), view.get(0, 0), TOL);
        assertEquals(100, m.get(3, 0), TOL);
        assertEquals(200, copy.get(0, 0), TOL);
    }

    @Test
    void testRangeRows() {
        DMatrix m = generate(10, 11);

        DMatrix view = m.rangeRows(3, 5);
        DMatrix copy = m.rangeRowsCopy(3, 5);

        for (int i = 0; i < view.rowCount(); i++) {
            for (int j = 0; j < view.colCount(); j++) {
                assertEquals(view.get(i, j), copy.get(i, j), TOL);
            }
        }

        view.set(0, 0, 100);
        copy.set(0, 0, 200);

        assertEquals(m.get(3, 0), view.get(0, 0), TOL);
        assertEquals(100, m.get(3, 0), TOL);
        assertEquals(200, copy.get(0, 0), TOL);
    }

    @Test
    void testRemoveRows() {
        DMatrix m = generate(10, 11);

        DMatrix view = m.removeRows(0, 1, 2, 4, 6, 7, 8, 9, 10);
        DMatrix copy = m.removeRowsCopy(0, 1, 2, 4, 6, 7, 8, 9, 10);

        for (int i = 0; i < view.rowCount(); i++) {
            for (int j = 0; j < view.colCount(); j++) {
                assertEquals(view.get(i, j), copy.get(i, j), TOL);
            }
        }

        view.set(0, 0, 100);
        copy.set(0, 0, 200);

        assertEquals(m.get(3, 0), view.get(0, 0), TOL);
        assertEquals(100, m.get(3, 0), TOL);
        assertEquals(200, copy.get(0, 0), TOL);
    }

    @Test
    void testMapCol() {
        DMatrix m = generate(10, 11);
        DVector vector1 = m.mapCol(3);
        DVector vector2 = m.mapColCopy(3);
        for (int i = 0; i < vector1.size(); i++) {
            assertEquals(3.0 + i * 11, vector1.get(i), TOL);
            assertEquals(3.0 + i * 11, vector2.get(i), TOL);
        }
    }

    @Test
    void testMapCols() {
        DMatrix m = generate(10, 11);

        DMatrix view = m.mapCols(3, 4);
        DMatrix copy = m.mapColsCopy(3, 4);

        for (int i = 0; i < view.rowCount(); i++) {
            for (int j = 0; j < view.colCount(); j++) {
                assertEquals(view.get(i, j), copy.get(i, j), TOL);
            }
        }

        view.set(0, 0, 100);
        copy.set(0, 0, 200);

        assertEquals(m.get(0, 3), view.get(0, 0), TOL);
        assertEquals(100, m.get(0, 3), TOL);
        assertEquals(200, copy.get(0, 0), TOL);
    }

    @Test
    void testRangeCols() {
        DMatrix m = generate(10, 11);

        DMatrix view = m.rangeCols(3, 5);
        DMatrix copy = m.rangeColsCopy(3, 5);

        for (int i = 0; i < view.rowCount(); i++) {
            for (int j = 0; j < view.colCount(); j++) {
                assertEquals(view.get(i, j), copy.get(i, j), TOL);
            }
        }

        view.set(0, 0, 100);
        copy.set(0, 0, 200);

        assertEquals(m.get(0, 3), view.get(0, 0), TOL);
        assertEquals(100, m.get(0, 3), TOL);
        assertEquals(200, copy.get(0, 0), TOL);
    }

    @Test
    void testRemoveCols() {
        DMatrix m = generate(10, 11);

        DMatrix view = m.removeCols(0, 1, 2, 4, 6, 7, 8, 9, 10);
        DMatrix copy = m.removeColsCopy(0, 1, 2, 4, 6, 7, 8, 9, 10);

        for (int i = 0; i < view.rowCount(); i++) {
            for (int j = 0; j < view.colCount(); j++) {
                assertEquals(view.get(i, j), copy.get(i, j), TOL);
            }
        }

        view.set(0, 0, 100);
        copy.set(0, 0, 200);

        assertEquals(m.get(0, 3), view.get(0, 0), TOL);
        assertEquals(100, m.get(0, 3), TOL);
        assertEquals(200, copy.get(0, 0), TOL);
    }
}
