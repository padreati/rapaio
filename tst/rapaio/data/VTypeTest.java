package rapaio.data;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/14/18.
 */
public class VTypeTest {

    @Test
    public void testNewInstance() {

        VarDouble varDouble = VarDouble.empty();
        VarLong varLong = VarLong.empty();
        VarInt varInt = VarInt.empty();
        VarBoolean varBoolean = VarBoolean.empty();
        VarNominal varNominal = VarNominal.empty();
        VarOrdinal varOrdinal = VarOrdinal.empty();
        VarText varText = VarText.empty();

        assertTrue(varDouble.deepEquals(varDouble.type().newInstance()));
        assertTrue(varLong.deepEquals(varLong.type().newInstance()));
        assertTrue(varInt.deepEquals(varInt.type().newInstance()));
        assertTrue(varBoolean.deepEquals(varBoolean.type().newInstance()));
        assertTrue(varNominal.deepEquals(varNominal.type().newInstance()));
        assertTrue(varOrdinal.deepEquals(varOrdinal.type().newInstance()));
        assertTrue(varText.deepEquals(varText.type().newInstance()));
    }
}
