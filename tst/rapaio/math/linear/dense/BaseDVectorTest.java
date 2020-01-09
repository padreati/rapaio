package rapaio.math.linear.dense;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.util.collection.DoubleArrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/9/20.
 */
public class BaseDVectorTest {

    private static final double TOL = 1e-15;
    private Normal normal;
    private double[] values;
    private BaseDVector x;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
        normal = Normal.std();
        values = DoubleArrays.newFrom(0, 100, row -> normal.sampleNext());
        x = new BaseDVector(100, values);
    }

    @Test
    void testBuilders() {
        DVector vector = BaseDVector.wrap(values);

        assertEquals(100, vector.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(values[i], vector.get(i), TOL);
        }

        DVector copy = vector.copy();
        for (int i = 0; i < copy.size(); i++) {
            copy.set(i, 10);
            assertEquals(10, copy.get(i));
        }
    }

    @Test
    void testStream() {
        double[] streamValues = x.valueStream().toArray();
        assertArrayEquals(values, streamValues, TOL);
    }

    @Test
    void testToVarDouble() {
        VarDouble v = x.asVarDouble();
        assertEquals(100, v.rowCount());
        for (int i = 0; i < 100; i++) {
            assertEquals(values[i], v.getDouble(i), TOL);
        }
    }

}
