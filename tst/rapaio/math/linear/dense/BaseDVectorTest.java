package rapaio.math.linear.dense;

import org.junit.jupiter.api.Test;
import rapaio.data.VarDouble;
import rapaio.math.linear.DVector;
import rapaio.math.linear.StandardDVectorTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/9/20.
 */
public class BaseDVectorTest extends StandardDVectorTest {

    @Override
    public DVector generateWrap(double[] values) {
        return BaseDVector.wrap(values);
    }

    @Override
    public DVector generateFill(int size, double fill) {
        double[] array = new double[size];
        for (int i = 0; i < size; i++) {
            array[i] = fill;
        }
        return BaseDVector.wrap(array);
    }

    @Override
    public DVector generateZeros(int size) {
        return BaseDVector.wrap(new double[size]);
    }

    @Override
    public String className() {
        return "BaseDVector";
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
