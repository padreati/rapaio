package rapaio.core.stat;

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Uniform;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.solid.SolidVarDouble;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/10/17.
 */
public class WeightedOnlineStatTest {

    private static final double TOL = 1e-11;

    @Test
    public void reverseTest() {

        RandomSource.setSeed(124);
        Uniform unif = new Uniform(0, 1);

        Var x = SolidVarDouble.wrap(1, 2, 3, 4, 5, 6, 7, 10, 20);

        VarDouble w = SolidVarDouble.from(x.rowCount(), row -> unif.sampleNext());

        // normalize w
        double wsum = Sum.from(w).value();
        for (int i = 0; i < w.rowCount(); i++) {
            w.setDouble(i, w.getDouble(i) / wsum);
        }

        SolidFrame.byVars(x, w).printLines();

        WeightedOnlineStat left = WeightedOnlineStat.empty();
        for (int i = 0; i < x.rowCount(); i++) {
            left.update(x.getDouble(i), w.getDouble(i));
        }

        WeightedOnlineStat right = WeightedOnlineStat.empty();
        for (int i = x.rowCount() - 1; i >= 0; i--) {
            right.update(x.getDouble(i), w.getDouble(i));
        }

        assertEquals(left.variance(), right.variance(), TOL);
    }

    @Test
    public void weightedTest() {

        RandomSource.setSeed(123);

        Normal normal = new Normal(0, 100);
        VarDouble x = SolidVarDouble.from(100, normal::sampleNext);
        VarDouble w = SolidVarDouble.fill(100, 1);

        VarDouble wnorm = w.solidCopy();
        double wsum = Sum.from(w).value();
        for (int i = 0; i < wnorm.rowCount(); i++) {
            wnorm.setDouble(i, wnorm.getDouble(i) / wsum);
        }

        WeightedOnlineStat wstat = WeightedOnlineStat.empty();
        WeightedOnlineStat wnstat = WeightedOnlineStat.empty();
        OnlineStat stat = OnlineStat.empty();

        for (int i = 0; i < x.rowCount(); i++) {
            wstat.update(x.getDouble(i), w.getDouble(i));
            wnstat.update(x.getDouble(i), wnorm.getDouble(i));
            stat.update(x.getDouble(i));
        }

        assertEquals(wstat.mean(), stat.mean(), TOL);
        assertEquals(wstat.variance(), stat.variance(), TOL);

        assertEquals(wnstat.mean(), stat.mean(), TOL);
        assertEquals(wstat.variance(), stat.variance(), TOL);
    }

    @Test
    public void multipleStats() {

        WeightedOnlineStat wos1 = WeightedOnlineStat.empty();
        WeightedOnlineStat wos2 = WeightedOnlineStat.empty();
        WeightedOnlineStat wos3 = WeightedOnlineStat.empty();

        WeightedOnlineStat wosTotal = WeightedOnlineStat.empty();

        RandomSource.setSeed(1234L);
        Normal normal = Normal.from(0, 1);
        Uniform uniform = new Uniform(0, 1);

        VarDouble x = SolidVarDouble.from(100, normal::sampleNext);
        VarDouble w = SolidVarDouble.from(100, uniform::sampleNext);

        double wsum = Sum.from(w).value();
        for (int i = 0; i < w.rowCount(); i++) {
            w.setDouble(i, w.getDouble(i)/wsum);
        }

        for (int i = 0; i < 20; i++) {
            wos1.update(x.getDouble(i), w.getDouble(i));
        }
        for (int i = 20; i < 65; i++) {
            wos2.update(x.getDouble(i), w.getDouble(i));
        }
        for (int i = 65; i < 100; i++) {
            wos3.update(x.getDouble(i), w.getDouble(i));
        }

        for (int i = 0; i < 100; i++) {
            wosTotal.update(x.getDouble(i), w.getDouble(i));
        }

        WeightedOnlineStat t1 = WeightedOnlineStat.from(wos1, wos2, wos3);

        assertEquals(wosTotal.mean(), t1.mean(), TOL);
        assertEquals(wosTotal.variance(), t1.variance(), TOL);
        assertEquals(wosTotal.count(), t1.count(), TOL);
    }
}
