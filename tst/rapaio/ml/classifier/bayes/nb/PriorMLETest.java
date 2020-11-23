package rapaio.ml.classifier.bayes.nb;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/14/19.
 */
public class PriorMLETest {

    private static final double TOL = 1e-12;

    private static final int N = 10_000;
    private static final String TARGET = "target";

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1234);
    }

    @Test
    void testNaming() {
        assertEquals("MLE", new PriorMLE().name());
        assertEquals("MLE{}", new PriorMLE().fittedName());

        VarNominal target = VarNominal.copy("a", "a", "b").name(TARGET);
        Prior prior = new PriorMLE();
        prior.fitPriors(SolidFrame.byVars(target), VarDouble.fill(3, 1), TARGET);
        assertEquals("MLE{a:0.6666667,b:0.3333333}", prior.fittedName());
    }

    @Test
    void testPrediction() {
        VarNominal target = VarNominal.copy("a", "b", "a", "c", "a", "b").name(TARGET);
        PriorMLE prior = new PriorMLE();
        prior.fitPriors(SolidFrame.byVars(target), VarDouble.fill(6, 1), TARGET);

        assertEquals(0.5, prior.computePrior("a"), TOL);
        assertEquals(1./3, prior.computePrior("b"), TOL);
        assertEquals(1./6, prior.computePrior("c"), TOL);
        assertEquals(Double.NaN, prior.computePrior("d"), TOL);
    }

    @Test
    void testNewInstance() {
        PriorMLE prior = new PriorMLE();
        VarNominal target = VarNominal.copy("a", "b", "c").name("target");
        prior.fitPriors(SolidFrame.byVars(target), VarDouble.fill(target.rowCount(), 1), "target");

        assertEquals("MLE{a:0.3333333,b:0.3333333,c:0.3333333}", prior.fittedName());

        Prior copy = prior.newInstance();
        assertNotNull(copy);
        assertEquals("MLE{}", copy.fittedName());
    }
}
