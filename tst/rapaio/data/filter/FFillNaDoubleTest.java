package rapaio.data.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.VRange;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/4/18.
 */
public class FFillNaDoubleTest {

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    void testDouble() {
        Frame src = FFilterTestUtil.allDoubleNominal(100, 2, 2);
        for (int i = 0; i < 100; i++) {
            int index = RandomSource.nextInt(2);
            double p = RandomSource.nextDouble();
            if (p < 0.2) {
                src.setMissing(i, index);
            }
        }

        assertTrue(src.rvar(0).stream().incomplete().count() > 0);
        assertTrue(src.rvar(1).stream().incomplete().count() > 0);

        Frame dst = src.fapply(FFillNaDouble.on(1, "v1"));
        assertEquals(0, src.rvar(0).stream().incomplete().count());
        assertTrue(src.rvar(1).stream().incomplete().count() > 0);

        dst = src.fapply(FFillNaDouble.on(2, VRange.all()).newInstance());
        assertEquals(0, src.rvar(0).stream().incomplete().count());
        assertEquals(0, src.rvar(1).stream().incomplete().count());

    }
}
