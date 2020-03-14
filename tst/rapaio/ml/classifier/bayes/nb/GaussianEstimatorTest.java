package rapaio.ml.classifier.bayes.nb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VType;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/5/20.
 */
public class GaussianEstimatorTest {

    private static final double TOL = 1e-12;
    private Frame df;

    @BeforeEach
    void beforeEach() {
        VarNominal target = VarNominal.copy("a", "a", "a", "a", "a", "b", "b", "b", "b", "b").withName("t");
        VarDouble x = VarDouble.copy(10, 11, 12, 11, 13, 2, 3, 4, 3, 2).withName("x");
        VarDouble y = VarDouble.copy(9, 10, 11, 12, 11, 13, 2, 3, 4, 3).withName("y");
        df = SolidFrame.byVars(x, y, target);
    }

    @Test
    void testBuilders() {
        VarDouble d1 = VarDouble.empty().withName("d1");
        VarDouble d2 = VarDouble.empty().withName("d2");

        VarBinary b1 = VarBinary.empty().withName("b1");
        VarBinary b2 = VarBinary.empty().withName("b2");

        VarNominal n1 = VarNominal.empty(0).withName("n1");
        VarNominal n2 = VarNominal.empty(0).withName("n2");


        assertEquals(Arrays.asList("d1", "d2"), GaussianEstimator.forType(SolidFrame.byVars(d1, d2, b1, b2, n1, n2), VType.DOUBLE)
                .stream().flatMap(v -> v.getTestNames().stream()).collect(Collectors.toList()));
        assertEquals(Arrays.asList("d1", "d2"), GaussianEstimator.forType(SolidFrame.byVars(d1, d2, b1, b2, n1, n2), VType.DOUBLE)
                .stream().flatMap(v -> v.getTestNames().stream()).collect(Collectors.toList()));

        assertEquals(Arrays.asList("b1", "n1"), GaussianEstimator.forNames("b1","n1").stream()
                .flatMap(v -> v.getTestNames()
                .stream()).collect(Collectors.toList()));
    }

    @Test
    void testNaming() {
        Estimator estim = GaussianEstimator.forName("x");
        assertEquals("Gaussian{test=x}", estim.name());
        assertEquals("Gaussian{test=x, values=[]}", estim.fittedName());
    }

    @Test
    void testNewInstance() {
        Estimator e = GaussianEstimator.forName("x");
        e.fit(df, VarDouble.fill(df.rowCount(), 1), "t");

        assertEquals("Gaussian{test=x, values=[a:Normal(mu=11.4, sd=1.0198039), b:Normal(mu=2.8, sd=0.7483315)]}", e.fittedName());

        Estimator copy = e.newInstance();
        assertNotNull(copy);
        assertEquals("Gaussian{test=x, values=[]}", copy.fittedName());
    }

    @Test
    void testFit() {
        GaussianEstimator estimator1 = GaussianEstimator.forName("x");
        estimator1.fit(df, VarDouble.fill(df.rowCount(), 1), "t");

        assertEquals(Collections.singletonList("x"), estimator1.getTestNames());
        assertEquals("Gaussian{test=x, values=[a:Normal(mu=11.4, sd=1.0198039), b:Normal(mu=2.8, sd=0.7483315)]}", estimator1.fittedName());

        assertEquals(Arrays.asList("a", "b"), estimator1.getTargetLevels());
        assertEquals(11.4, estimator1.getFittedNormal("a").mean(), TOL);
        assertEquals(1.019803902718557, estimator1.getFittedNormal("a").sd(), TOL);

        VarDouble pred1 = VarDouble.from(df.rowCount(), row -> estimator1.predict(df, row, "a"));
        assertTrue(pred1.getDouble(0) < pred1.getDouble(1));
        assertEquals(pred1.getDouble(1), pred1.getDouble(3), TOL);


        GaussianEstimator estimator2 = GaussianEstimator.forName("y");
        estimator2.fit(df, VarDouble.fill(df.rowCount(), 1), "t");

        assertEquals(Collections.singletonList("y"), estimator2.getTestNames());
        assertEquals("Gaussian{test=y, values=[a:Normal(mu=10.6, sd=1.0198039), b:Normal(mu=5, sd=4.0496913)]}", estimator2.fittedName());

        assertEquals(Arrays.asList("a", "b"), estimator2.getTargetLevels());
        assertEquals(5, estimator2.getFittedNormal("b").mean(), TOL);
        assertEquals(4.049691346263317, estimator2.getFittedNormal("b").sd(), TOL);
    }

}
