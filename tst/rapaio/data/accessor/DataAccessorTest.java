package rapaio.data.accessor;

import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarLong;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/18/18.
 */
public class DataAccessorTest {

    private static final double TOL = 1e-20;

    @Before
    public void setUp() {
        RandomSource.setSeed(1234);
    }

    @Test
    public void testVarDoubleDataAccessor() {
        VarDouble x = VarDouble.from(100, RandomSource::nextDouble);
        VarDoubleDataAccessor accessor = x.getDataAccessor();

        assertTrue(Double.isNaN(accessor.getMissingValue()));

        double[] data = accessor.getData();
        for (int i = 0; i < accessor.getRowCount(); i++) {
            data[i] = 1.;
        }
        accessor.setData(data);
        accessor.setRowCount(10);

        assertEquals(10, x.rowCount());
        for (int i = 0; i < x.rowCount(); i++) {
            assertEquals(1.0, x.getDouble(i), TOL);
        }
    }

    @Test
    public void testVarLongDataAccessor() {
        VarLong x = VarLong.from(100, () -> (long) RandomSource.nextDouble() * 100);
        VarLongDataAccessor accessor = x.getDataAccessor();
        assertEquals(Long.MIN_VALUE, accessor.getMissingValue());

        long[] data = accessor.getData();
        int rows = accessor.getRowCount();
        for (int i = 0; i < rows; i++) {
            data[i] = 10L;
        }
        accessor.setData(data);
        accessor.setRowCount(10);

        assertEquals(10, x.rowCount());
        for (int i = 0; i < 10; i++) {
            assertEquals(10L, x.getLong(i));
        }
    }

    @Test
    public void testVarIntDataAccessor() {
        VarInt x = VarInt.from(100, row -> (int) RandomSource.nextDouble() * 100);
        VarIntDataAccessor accessor = x.getDataAccessor();
        assertEquals(Integer.MIN_VALUE, accessor.getMissingValue());

        int[] data = accessor.getData();
        int rows = accessor.getRowCount();
        for (int i = 0; i < rows; i++) {
            data[i] = 10;
        }
        accessor.setData(data);
        accessor.setRowCount(10);

        assertEquals(10, x.rowCount());
        for (int i = 0; i < 10; i++) {
            assertEquals(10L, x.getInt(i));
        }
    }
}
