package rapaio.data.ops;

import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/11/19.
 */
public class VarOpTest {

    private static final double TOLERANCE = 1e-12;
    private Normal normal = Normal.std();

    @Before
    public void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    public void varDoubleSortedTest() {

        VarDouble x = VarDouble.from(100, row -> row % 4 == 0 ? Double.NaN : normal.sampleNext());
        VarDouble apply1 = x.copy().op().apply(v -> v + 1);
        VarDouble apply2 = x.op().capply(v -> v + 1);
        VarDouble apply3 = x.copy().op().plus(1);
        VarDouble apply4 = x.copy().op().plus(VarDouble.fill(100, 1));

        assertTrue(apply1.deepEquals(apply2));
        assertTrue(apply1.deepEquals(apply3));
        assertTrue(apply1.deepEquals(apply4));

        double sum1 = x.op().sum();
        double sum2 = x.copy().op().sort(true).op().sum();
        double sum3 = x.copy().op().sort(false).op().avg() * 75;
        double sum4 = x.copy().op().sort(x.refComparator()).op().sum();
        double sum5 = x.mapRows(x.op().sortedCompleteRows()).op().sum();
        double sum6 = x.mapRows(x.op().sortedCompleteRows(false)).op().sum();
        double sum7 = x.mapRows(x.op().sortedRows()).op().sum();
        double sum8 = x.mapRows(x.op().sortedRows(false)).op().sum();

        assertEquals(sum1, sum2, TOLERANCE);
        assertEquals(sum1, sum3, TOLERANCE);
        assertEquals(sum1, sum4, TOLERANCE);
        assertEquals(sum1, sum5, TOLERANCE);
        assertEquals(sum1, sum6, TOLERANCE);
        assertEquals(sum1, sum7, TOLERANCE);
        assertEquals(sum1, sum8, TOLERANCE);
    }

    @Test
    public void varIntSortedTest() {

        Var x = VarInt.from(100, row -> row % 4 == 0 ? VarInt.MISSING_VALUE : RandomSource.nextInt(100));
        Var apply1 = x.copy().op().apply(v -> v + 1);
        Var apply3 = x.copy().op().plus(1);
        Var apply4 = x.copy().op().plus(VarDouble.fill(100, 1));

        assertTrue(apply1.deepEquals(apply3));
        assertTrue(apply1.deepEquals(apply4));

        double sum1 = x.op().sum();
        double sum2 = x.copy().op().sort(true).op().sum();
        double sum3 = x.copy().op().sort(false).op().avg() * 75;
        double sum4 = x.copy().op().sort(x.refComparator()).op().sum();
        double sum5 = x.mapRows(x.op().sortedCompleteRows()).op().sum();
        double sum6 = x.mapRows(x.op().sortedCompleteRows(false)).op().sum();
        double sum7 = x.mapRows(x.op().sortedRows()).op().sum();
        double sum8 = x.mapRows(x.op().sortedRows(false)).op().sum();

        assertEquals(sum1, sum2, TOLERANCE);
        assertEquals(sum1, sum3, TOLERANCE);
        assertEquals(sum1, sum4, TOLERANCE);
        assertEquals(sum1, sum5, TOLERANCE);
        assertEquals(sum1, sum6, TOLERANCE);
        assertEquals(sum1, sum7, TOLERANCE);
        assertEquals(sum1, sum8, TOLERANCE);
    }
}
