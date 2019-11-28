package rapaio.data.filter;

import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.VarDouble;
import rapaio.ml.regression.RegressionModel;
import rapaio.ml.regression.simple.L2RegressionModel;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/28/19.
 */
public class FFImputeWithRegressionTest {

    private static final double TOL = 1e-20;

    private Normal normal;

    @Before
    public void setUp() {
        RandomSource.setSeed(123);
        normal = Normal.std();
    }

    @Test
    public void testBasic() {

        VarDouble x = VarDouble.from(100, row -> row%7==0 ? Double.NaN : normal.sampleNext()).withName("x");
        VarDouble y = VarDouble.from(100, row -> row%9==0 ? Double.NaN : normal.sampleNext()).withName("y");

        double xm = Mean.of(x).value();
        double ym = Mean.of(y).value();

        RegressionModel model = L2RegressionModel.newL2();

        FFImputeWithRegression xfilter = FFImputeWithRegression.of(model, VRange.of("x"), "x").newInstance();
        FFImputeWithRegression yfilter = FFImputeWithRegression.of(model, VRange.of("y"), "y");

        Frame df = SolidFrame.byVars(x, y);

        xfilter.fit(df);
        yfilter.fit(df);

        Frame copy = df.copy().fapply(xfilter, yfilter);

        for (int i = 0; i < 100; i++) {
            if(x.isMissing(i)) {
                assertEquals(xm, copy.getDouble(i, "x"), TOL);
            } else {
                assertEquals(x.getDouble(i), copy.getDouble(i, "x"), TOL);
            }
            if(y.isMissing(i)) {
                assertEquals(ym, copy.getDouble(i, "y"), TOL);
            } else {
                assertEquals(y.getDouble(i), copy.getDouble(i, "y"), TOL);
            }
        }
    }
}
