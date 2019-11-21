package rapaio.data.filter;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.VType;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/3/18.
 */
public class FRetainTypesTest {

    @Test
    public void testAll() {

        Frame src = FFilterTestUtil.allDoubleNominal(100, 5, 3);

        Frame allInt = src.fapply(FRetainTypes.on(VType.INT));
        Frame allNom = src.fapply(FRetainTypes.on(VType.NOMINAL));
        Frame allDouble = src.fapply(FRetainTypes.on(VType.DOUBLE).newInstance());
        Frame all = src.fapply(FRetainTypes.on(VType.DOUBLE, VType.NOMINAL));

        assertEquals(100, allInt.rowCount());
        assertEquals(0, allInt.varCount());

        assertEquals(3, allNom.varCount());
        assertEquals(100, allNom.rowCount());

        assertEquals(5, allDouble.varCount());
        assertEquals(100, allDouble.rowCount());

        assertTrue(all.deepEquals(src));
    }
}
