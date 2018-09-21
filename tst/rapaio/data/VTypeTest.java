package rapaio.data;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/14/18.
 */
public class VTypeTest {

    private VType[] types = new VType[]{
            VType.BOOLEAN, VType.INT, VType.LONG, VType.DOUBLE, VType.NOMINAL, VType.TEXT};

    @Test
    public void testNewInstance() {

        VarDouble varDouble = VarDouble.empty();
        VarLong varLong = VarLong.empty();
        VarInt varInt = VarInt.empty();
        VarBoolean varBoolean = VarBoolean.empty();
        VarNominal varNominal = VarNominal.empty();
        VarText varText = VarText.empty();

        assertTrue(varDouble.deepEquals(varDouble.type().newInstance()));
        assertTrue(varLong.deepEquals(varLong.type().newInstance()));
        assertTrue(varInt.deepEquals(varInt.type().newInstance()));
        assertTrue(varBoolean.deepEquals(varBoolean.type().newInstance()));
        assertTrue(varNominal.deepEquals(varNominal.type().newInstance()));
        assertTrue(varText.deepEquals(varText.type().newInstance()));
    }

    @Test
    public void testIsCategory() {
        boolean[] numeric = new boolean[] {true, true, false, true, false, false};
        boolean[] binary = new boolean[] {true, false, false, false, false, false};
        boolean[] nominal = new boolean[]{false, false, false, false, true, false};
        String[] code = new String[]{"binary", "int", "long", "double", "nominal", "text"};

        for (int i = 0; i < types.length; i++) {
            assertEquals(numeric[i], types[i].isNumeric());
            assertEquals(binary[i], types[i].isBinary());
            assertEquals(nominal[i], types[i].isNominal());
            assertEquals(code[i], types[i].code());
        }
    }
}
