package rapaio.data.filter.var;

import org.junit.Test;
import rapaio.core.*;
import rapaio.data.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/28/18.
 */
public class VRefSortTest {

    @Test
    public void testSortRef() {
        RandomSource.setSeed(1);
        Var x1 = VarNominal.copy("z", "q", "a", "b", "d", "c");
        Var x2 = VarDouble.copy(7, 6, 1, 2, 5, 4);
        Var x3 = x2.copy().fapply(VRefSort.from());
        Var x4 = x1.copy().fapply(VRefSort.from());
        for (int i = 0; i < x3.rowCount() - 1; i++) {
            assertTrue(Double.compare(x3.getDouble(i), x3.getDouble(i + 1)) <= 0);
        }
        for (int i = 0; i < x4.rowCount() - 1; i++) {
            assertTrue(x4.getLabel(i).compareTo(x4.getLabel(i + 1)) <= 0);
        }

        // TODO test aggregate comparators
    }
}
