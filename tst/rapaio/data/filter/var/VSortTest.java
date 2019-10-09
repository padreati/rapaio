package rapaio.data.filter.var;

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/28/18.
 */
public class VSortTest {

    @Test
    public void testSortNominal() {
        RandomSource.setSeed(1);
        Var x1 = VarNominal.copy("z", "q", "a", "b", "d", "c");
        Var x2 = x1.fapply(VSort.asc());
        for (int i = 0; i < x2.rowCount() - 1; i++) {
            assertTrue(x2.getLabel(i).compareTo(x2.getLabel(i + 1)) <= 0);
        }
        Var x3 = x1.fapply(VSort.asc(false));
        for (int i = 0; i < x3.rowCount() - 1; i++) {
            assertTrue(x3.getLabel(i).compareTo(x3.getLabel(i + 1)) >= 0);
        }
    }

    @Test
    public void testSortNumeric() {
        RandomSource.setSeed(1);
        Var x1 = VarDouble.copy(7, 5, 1, 2, 5, 4);
        Var x2 = x1.fapply(VSort.asc());
        for (int i = 0; i < x2.rowCount() - 1; i++) {
            assertTrue(Double.compare(x2.getDouble(i), x2.getDouble(i + 1)) <= 0);
        }
        Var x3 = x1.fapply(VSort.asc(false));
        for (int i = 0; i < x3.rowCount() - 1; i++) {
            assertTrue(Double.compare(x3.getDouble(i), x3.getDouble(i + 1)) >= 0);
        }
    }
}
