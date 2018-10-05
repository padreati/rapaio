package rapaio.data.filter.var;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarLong;
import rapaio.data.VarNominal;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/28/18.
 */
public class VToIntTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testToInt() {
        Var num1 = VarDouble.wrap(1.0, 2.0, 1.2, Double.NaN, 3.0, Double.NaN, 3.2);
        Var nom1 = VarNominal.copy("1", "2", "1.2", "?", "3", "?", "4");
        Var nom2 = VarNominal.copy("1", "2", "1.2", "mimi", "3", "lulu", "3.2");
        Var idx1 = VarInt.copy(1, 2, 3, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 4);
        Var bin1 = VarBinary.copy(1, 0, 1, -1, 1, -1, 0);

        // by default transformer

        Assert.assertTrue(VarInt.wrap(1, 2, 1, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 3)
                .deepEquals(num1.fapply(VToInt.byDefault())));

        Assert.assertTrue(VarInt.wrap(1, 2, Integer.MIN_VALUE, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 4)
                .deepEquals(nom1.fapply(VToInt.byDefault())));

        Assert.assertTrue(VarInt.wrap(1, 2, Integer.MIN_VALUE, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, Integer.MIN_VALUE)
                .deepEquals(nom2.fapply(VToInt.byDefault())));

        Assert.assertTrue(VarInt.wrap(1, 2, 3, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 4)
                .deepEquals(idx1.fapply(VToInt.byDefault())));

        Assert.assertTrue(VarInt.wrap(1, 0, 1, Integer.MIN_VALUE, 1, Integer.MIN_VALUE, 0)
                .deepEquals(bin1.fapply(VToInt.byDefault())));

        // by spot transformer

        Assert.assertTrue(VarInt.wrap(1, 1, 1, 0, 1, 0, 1)
                .deepEquals(num1.fapply(VToInt.bySpot(s -> s.isMissing() ? 0 : 1))));

        // by value transformer

        Assert.assertTrue(VarInt.wrap(1, 2, 1, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 3)
                .deepEquals(num1.fapply(VToInt.fromDouble(x -> Double.isNaN(x) ? Integer.MIN_VALUE : Double.valueOf(x).intValue()))));

        // by index transformer

        Assert.assertTrue(VarInt.wrap(1, 2, 3, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 4)
                .deepEquals(idx1.fapply(VToInt.fromInt(x -> x))));

        // by label transformer

        Assert.assertTrue(VarInt.wrap(1, 2, Integer.MIN_VALUE, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 4)
                .deepEquals(nom1.fapply(VToInt.byLabel(txt -> {
                    if (txt.equals("?"))
                        return Integer.MIN_VALUE;
                    try {
                        return Integer.parseInt(txt);
                    } catch (NumberFormatException e) {
                        return Integer.MIN_VALUE;
                    }
                }))));
    }

    @Test
    public void testUnsupportedLongToDouble() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Variable type: long is not supported.");
        VarLong.wrap(1, 2, 3).fapply(VToInt.byDefault());
    }
}
