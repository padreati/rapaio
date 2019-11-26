package rapaio.data;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/14/18.
 */
public class VTypeTest {

    private VType[] types = new VType[]{
            VType.BINARY, VType.INT, VType.LONG, VType.DOUBLE, VType.NOMINAL, VType.STRING};

    @Test
    public void testNewInstance() {

        VarDouble varDouble = VarDouble.empty();
        VarLong varLong = VarLong.empty();
        VarInt varInt = VarInt.empty();
        VarBinary varBinary = VarBinary.empty();
        VarNominal varNominal = VarNominal.empty();
        VarString varString = VarString.empty();

        assertTrue(varDouble.deepEquals(varDouble.type().newInstance()));
        assertTrue(varLong.deepEquals(varLong.type().newInstance()));
        assertTrue(varInt.deepEquals(varInt.type().newInstance()));
        assertTrue(varBinary.deepEquals(varBinary.type().newInstance()));
        assertTrue(varNominal.deepEquals(varNominal.type().newInstance()));
        assertTrue(varString.deepEquals(varString.type().newInstance()));
    }

    @Test
    public void testIsCategory() {
        boolean[] numeric = new boolean[] {true, true, false, true, false, false};
        boolean[] binary = new boolean[] {true, false, false, false, false, false};
        boolean[] nominal = new boolean[]{false, false, false, false, true, false};
        String[] code = new String[]{"bin", "int", "long", "dbl", "nom", "str"};

        for (int i = 0; i < types.length; i++) {
            assertEquals(numeric[i], types[i].isNumeric());
            assertEquals(binary[i], types[i].isBinary());
            assertEquals(nominal[i], types[i].isNominal());
            assertEquals(code[i], types[i].code());
        }
    }
}
