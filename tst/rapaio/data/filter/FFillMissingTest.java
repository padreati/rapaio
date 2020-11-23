package rapaio.data.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/4/18.
 */
public class FFillMissingTest {

    private Frame df;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
        df = SolidFrame.byVars(
                VarDouble.wrap(1, VarDouble.MISSING_VALUE, 3, 4).name("a"),
                VarInt.wrap(1, 2, VarInt.MISSING_VALUE, 4).name("b"),
                VarNominal.copy("1", "2", "3", VarNominal.MISSING_VALUE).name("c")
        );
    }

    @Test
    void testDouble() {
        var copy = df.copy().fapply(FFillMissing.onDouble(10, VRange.of("a")));
        assertFalse(copy.deepEquals(df));
        assertEquals(4, copy.rvar("a").stream().complete().count());
    }

    @Test
    void testInt() {
        var copy = df.copy().fapply(FFillMissing.onInt(10, VRange.of("b")));
        assertFalse(copy.deepEquals(df));
        assertEquals(4, copy.rvar("b").stream().complete().count());
    }

    @Test
    void testString() {
        var copy = df.copy().fapply(FFillMissing.onLabel("x", VRange.of("c")).newInstance());
        assertFalse(copy.deepEquals(df));
        assertEquals(4, copy.rvar("c").stream().complete().count());
    }
}
