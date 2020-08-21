package rapaio.data.filter;

import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/3/18.
 */
public class FApplyTest {

    private static final double TOL = 1e-20;

    @Test
    void spotTest() {
        var df = SolidFrame.byVars(
                VarNominal.copy("a", "b", "c").withName("x1"),
                VarDouble.copy(1, 2, 3).withName("x2")
        );
        df.fapply(FApply.onSpot(s -> {
            s.setMissing(0);
            s.setMissing(1);
        }, VRange.all()));

        for (int i = 0; i < df.rowCount(); i++) {
            for (int j = 0; j < df.varCount(); j++) {
                assertTrue(df.isMissing(i, j));
            }
        }
    }

    @Test
    void testDouble() {
        Frame df = FFilterTestUtil.allDoubles(100, 2);
        Frame sign = df.copy().fapply(FApply.onDouble(Math::signum, VRange.all()));

        for (int i = 0; i < df.varCount(); i++) {
            for (int j = 0; j < df.rowCount(); j++) {
                assertEquals(Math.signum(df.getDouble(j, i)), sign.getDouble(j, i), TOL);
            }
        }

        Frame sign2 = df.copy().fapply(FApply.onDouble(Math::signum, VRange.all()).newInstance());
        assertTrue(sign.deepEquals(sign2));
    }

    @Test
    void testInt() {
        Frame df = SolidFrame.byVars(VarInt.seq(1, 100).withName("x"));
        Frame copy = df.copy().fapply(FApply.onInt(i -> i + 1, VRange.all()));
        for (int j = 0; j < df.rowCount(); j++) {
            assertEquals(j + 2, copy.getInt(j, 0));
        }
    }

    @Test
    void testString() {
        Frame df = SolidFrame.byVars(VarNominal.copy("a", "b", "a", "b").withName("x"));
        Frame copy = df.copy().fapply(FApply.onLabel(l -> "a".equals(l) ? "b" : "a", VRange.all()));
        for (int j = 0; j < df.rowCount(); j++) {
            assertEquals(j % 2 == 0 ? "b" : "a", copy.getLabel(j, 0));
        }
    }
}
