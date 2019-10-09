package rapaio.data.filter.var;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rapaio.data.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/28/18.
 */
public class VToDoubleTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testToDouble() {
        Var num1 = VarDouble.wrap(1.0, 2.0, 1.2, Double.NaN, 3.0, Double.NaN, 3.2);
        Var nom1 = VarNominal.copy("1", "2", "1.2", "?", "3", "?", "3.2");
        Var nom2 = VarNominal.copy("1", "2", "1.2", "mimi", "3", "lulu", "3.2");
        Var idx1 = VarInt.copy(1, 2, 3, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 4);
        Var bin1 = VarBinary.copy(1, 0, 1, -1, 1, -1, 0);

        // by default transformer

        assertTrue(VarDouble.wrap(1, 2, 1.2, Double.NaN, 3, Double.NaN, 3.2)
                .deepEquals(num1.fapply(VToDouble.byDefault())));

        assertTrue(VarDouble.wrap(1, 2, 1.2, Double.NaN, 3, Double.NaN, 3.2)
                .deepEquals(nom1.fapply(VToDouble.byDefault())));

        assertTrue(VarDouble.wrap(1, 2, 1.2, Double.NaN, 3, Double.NaN, 3.2)
                .deepEquals(nom2.fapply(VToDouble.byDefault())));

        assertTrue(VarDouble.wrap(1, 2, 3, Double.NaN, 3, Double.NaN, 4)
                .deepEquals(idx1.fapply(VToDouble.byDefault())));

        assertTrue(VarDouble.wrap(1, 0, 1, Double.NaN, 1, Double.NaN, 0)
                .deepEquals(bin1.fapply(VToDouble.byDefault())));

        // by spot transformer

        assertTrue(VarDouble.wrap(1, 1, 1, 0, 1, 0, 1)
                .deepEquals(num1.fapply(VToDouble.bySpot(s -> s.isMissing() ? 0.0 : 1.0))));

        // by value transformer

        assertTrue(VarDouble.wrap(1, 2, 1.2, Double.NaN, 3, Double.NaN, 3.2)
                .deepEquals(num1.fapply(VToDouble.byValue(x -> x))));

        // by index transformer

        assertTrue(VarDouble.wrap(1, 2, 3, Double.NaN, 3, Double.NaN, 4)
                .deepEquals(idx1.fapply(VToDouble.byInt(x -> x == Integer.MIN_VALUE ? Double.NaN : x))));

        // by label transformer

        assertTrue(num1.deepEquals(nom1.fapply(VToDouble.byLabel(txt -> txt.equals("?") ? Double.NaN : Double.parseDouble(txt)))));
    }

    @Test
    public void testUnsupportedLongToDouble() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Variable type: long is not supported.");
        VarLong.wrap(1, 2, 3).fapply(VToDouble.byDefault());
    }
}
