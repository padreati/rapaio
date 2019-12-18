package rapaio.data.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.VarNominal;
import rapaio.ml.classifier.rule.OneRule;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/28/19.
 */
public class FFImputeWithClassifierTest {

    private static final String[] xd = new String[]{"a", "b", "c", "d", "?"};
    private static final String[] yd = new String[]{"x", "y", "?"};

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    void testBasic() {

        VarNominal x = VarNominal.from(100, row -> xd[RandomSource.nextInt(xd.length)]).withName("x");
        VarNominal y = VarNominal.from(100, row -> yd[RandomSource.nextInt(yd.length)]).withName("y");

        var model = new OneRule();

        FFImputeWithClassifier xfilter = FFImputeWithClassifier.of(model, VRange.of("x,y"), "x").newInstance();
        FFImputeWithClassifier yfilter = FFImputeWithClassifier.of(model, VRange.of("x,y"), "y");

        Frame df = SolidFrame.byVars(x, y);

        xfilter.fit(df);
        yfilter.fit(df);

        Frame copy = df.copy().fapply(xfilter, yfilter);

        for (int i = 0; i < 100; i++) {
            assertFalse(copy.isMissing(i, "x"));
            assertFalse(copy.isMissing(i, "y"));
        }
    }
}
