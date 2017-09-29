package rapaio.ts;

import org.junit.Before;
import org.junit.Test;
import rapaio.data.NumericVar;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/29/17.
 */
public class AcfTest {

    private static final double TOL = 1e-15;

    private NumericVar ts1;
    private NumericVar ts2;
    private NumericVar ts3;

    // computed in R using forecast::Acf

    private NumericVar ref2 = NumericVar.wrap(
            0.99999999999999978, 0.84999999999999976, 0.70150375939849607, 0.55601503759398485, 0.41503759398496232,
            0.28007518796992475, 0.15263157894736840, 0.03421052631578946, -0.07368421052631578, -0.16954887218045109,
            -0.25187969924812026, -0.31917293233082700, -0.36992481203007516, -0.40263157894736834, -0.41578947368421043,
            -0.40789473684210520, -0.37744360902255636, -0.32293233082706763, -0.24285714285714277, -0.13571428571428570);

    private NumericVar ref3 = NumericVar.wrap(
            1.00000000000000000, -0.08483871645887758, -0.37360837037828093, 0.08956384227269978, -0.14231675101016045, -0.32257643351652959, 0.11176363749001397,
            0.26985473385123482, -0.03082066297576692, 0.03983518876574605, 0.15193314018403464, -0.09179660652770671, -0.29112332693108145, 0.10021816843439253,
            0.17114894484083301, -0.20094829853780788, 0.06786506328344186, 0.09861045268517940, -0.04513914344640205, -0.01762486202496256
    );

    @Before
    public void setUp() {

        ts1 = NumericVar.fill(20, 1).withName("ts1");
        ts2 = NumericVar.seq(1, 20).withName("ts2");
        ts3 = NumericVar.wrap(0.6979648509158923, -0.2406058329854418, -1.8975688306241107, 0.6038877149907240, -1.0588252484632350, -0.5431671075613055, -0.2601316660744313,
                1.5800360521101393, 0.6343785691225632, -1.2059157599825618, -0.9344843442712357, -0.9395890575710252, -1.3142520685298062, 0.6052511257189427,
                1.3506630966671445, -1.4497517922535001, -0.1403198814124193, 1.1962603720850546, -1.1999372448315526, -0.6278085018229833).withName("ts3");

    }

    @Test
    public void basicTest() {
        Acf acf1 = Acf.from(ts1, ts1.rowCount());
        acf1.printSummary();
        for (int i = 0; i < acf1.values().rowCount(); i++) {
            assertTrue(acf1.values().isMissing(i));
        }

        Acf acf2 = Acf.from(ts2, ts2.rowCount());
        acf2.printSummary();
        for (int i = 0; i < acf2.values().rowCount(); i++) {
            assertEquals(ref2.value(i), acf2.values().value(i), TOL);
        }

        Acf acf3 = Acf.from(ts3, ts3.rowCount());
        acf3.printSummary();
        for (int i = 0; i < acf3.values().rowCount(); i++) {
            assertEquals(ref3.value(i), acf3.values().value(i), TOL);
        }
    }
}
