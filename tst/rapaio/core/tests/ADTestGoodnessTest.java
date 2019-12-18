package rapaio.core.tests;

import org.junit.jupiter.api.Test;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/9/17.
 */
public class ADTestGoodnessTest {

    private static final double TOL = 1e-5;

    @Test
    void basicTest() {
        VarDouble x = VarDouble.wrap(6.0747159, -8.9637424, -1.1363964, 1.5831864, -3.4660379, 2.6695147, 3.0571496, 0.8348192, -11.3294910, 13.8572907);

        ADTestGoodness test = ADTestGoodness.from(x, 1, 5);
        // verified with R
        test.printSummary();

//        assertEquals(0.7283345, test.a2, TOL);
        assertEquals(0.5318083, test.pValue(), TOL);

        ADTestGoodness test2 = ADTestGoodness.from(x);
        test2.printSummary();

        ADTestGoodness test3 = ADTestGoodness.from(x, 1, Double.NaN);
        test3.printSummary();
    }
}
