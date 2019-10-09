package rapaio.ml.common.predicate;

import org.junit.Test;
import rapaio.core.*;
import rapaio.data.*;
import rapaio.data.stream.*;
import rapaio.math.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/21/17.
 */
public class RowPredicateTest {

    private boolean test(FSpot s, RowPredicate rp) {
        return rp.test(s.row(), s.frame());
    }

    @Test
    public void testNumPredicates() {

        Frame df = SolidFrame.byVars(VarDouble.from(50, MTools::sqrt).withName("x"));

        // test basic numeric predicates

        RowPredicate rp1 = RowPredicate.numLessEqual("x", 4);
        RowPredicate rp2 = RowPredicate.numLess("x", 4);
        RowPredicate rp3 = RowPredicate.numGreater("x", 4);
        RowPredicate rp4 = RowPredicate.numGreaterEqual("x", 4);

        assertEquals(17, df.stream().filter(s -> test(s, rp1)).count());
        assertEquals(16, df.stream().filter(s -> test(s, rp2)).count());
        assertEquals(33, df.stream().filter(s -> test(s, rp3)).count());
        assertEquals(34, df.stream().filter(s -> test(s, rp4)).count());

        // add some missing values, and test the count sum to be correct

        RandomSource.setSeed(123);

        for (int row : SamplingTools.sampleWOR(50, 10)) {
            df.setMissing(row, "x");
        }

        assertEquals(40, df.stream().filter(s -> test(s, rp1)).count() + df.stream().filter(s -> test(s, rp3)).count());
        assertEquals(40, df.stream().filter(s -> test(s, rp2)).count() + df.stream().filter(s -> test(s, rp4)).count());

        // set all missing to be sure we have 0 passes

        for (int i = 0; i < df.rowCount(); i++) {
            df.setMissing(i, "x");
        }

        assertEquals(0, df.stream().filter(s -> test(s, rp1)).count());
        assertEquals(0, df.stream().filter(s -> test(s, rp2)).count());
        assertEquals(0, df.stream().filter(s -> test(s, rp3)).count());
        assertEquals(0, df.stream().filter(s -> test(s, rp4)).count());

        // now check the names

        assertEquals("x <= 4", rp1.toString());
        assertEquals("x < 4", rp2.toString());
        assertEquals("x > 4", rp3.toString());
        assertEquals("x >= 4", rp4.toString());
    }

    @Test
    public void testBinaryPredicates() {

        int[] values = SamplingTools.sampleWR(2, 100);
        SolidFrame df = SolidFrame.byVars(VarBinary.from(values.length, row -> values[row] == 1).withName("x"));

        assertEquals(100, df.stream().filter(s -> RowPredicate.binEqual("x", true).test(s.row(), s.frame())).count()
                + df.stream().filter(s -> RowPredicate.binEqual("x", false).test(s.row(), s.frame())).count());
    }
}
