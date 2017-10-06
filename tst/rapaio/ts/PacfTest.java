package rapaio.ts;

import org.junit.Before;
import org.junit.Test;
import rapaio.data.NumericVar;
import rapaio.graphics.opt.GOption;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static rapaio.graphics.Plotter.densityLine;
import static rapaio.graphics.Plotter.dvLines;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/3/17.
 */
public class PacfTest {

    private static final double TOL = 1e-13;

    private NumericVar ts1;
    private NumericVar ts2;
    private NumericVar ts3;

    // computed in R using forecast::Acf

    private NumericVar ref2;
    private NumericVar ref3;


    @Before
    public void setUp() {

        ts1 = NumericVar.fill(20, 1).withName("ts1");
        ts2 = NumericVar.seq(1, 20).withName("ts2");
        ts3 = NumericVar.wrap(
                0.6979648509158923, -0.2406058329854418, -1.8975688306241107, 0.6038877149907240,
                -1.0588252484632350, -0.5431671075613055, -0.2601316660744313, 1.5800360521101393,
                0.6343785691225632, -1.2059157599825618, -0.9344843442712357, -0.9395890575710252,
                -1.3142520685298062, 0.6052511257189427, 1.3506630966671445, -1.4497517922535001,
                -0.1403198814124193, 1.1962603720850546, -1.1999372448315526, -0.6278085018229833
        ).withName("ts3");

        ref2 = NumericVar.wrap(
                0.849999999999999756, -0.075662128293706235, -0.076350697060726891, -0.076986286385123531, -0.077470340055607045,
                -0.077678098116149980, -0.077449846796252123, -0.076580332319109073, -0.074806500059323516, -0.071794351849237673,
                -0.067126804527072773, -0.060296031466335422, -0.050705555565239362, -0.037688112631549968, -0.020542168393234521,
                0.001421888810447209, 0.028861216320893255, 0.062486252156896252, 0.103404452912941217);

        ref3 = NumericVar.wrap(
                -0.08483871645887758, -0.38356674116065448, 0.01555188698220464, -0.32324824719487172,
                -0.43207097239004993, -0.31460709753153510, -0.18877219827433805, -0.28427895048627366,
                -0.24893705184301876, -0.17752364799019998, -0.11827176558536588, -0.36049570502238704,
                -0.19733966821910784, -0.03111685847675113, -0.16525578038575647, -0.01996207694747570,
                -0.20301424343853458, 0.01427113035194936, 0.04975825217532487
        );
    }

    @Test
    public void basicTest() {

        Pacf pacf2 = Pacf.from(ts2, ts2.rowCount() - 1);
        pacf2.printSummary();
        for (int i = 0; i < pacf2.values().rowCount(); i++) {
            assertEquals(ref2.value(i), pacf2.values().value(i), TOL);
        }

        Pacf pacf3 = Pacf.from(ts3, ts3.rowCount() - 1);
        pacf3.printSummary();
        for (int i = 0; i < pacf3.values().rowCount(); i++) {
            assertEquals("err at i=" + i , ref3.value(i), pacf3.values().value(i), TOL);
        }
    }
}
