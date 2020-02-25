package rapaio.ml.classifier.bayes.nb;

import org.junit.jupiter.api.Test;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/4/20.
 */
public class PriorUniformTest {

    private static final String TARGET = "target";
    private static final double TOLERANCE = 1e-20;

    @Test
    void testNaming() {
        PriorUniform prior = new PriorUniform();
        assertEquals("Uniform", prior.name());
        assertEquals("Uniform{value=?,targetLevels=[]}", prior.fittedName());

        VarNominal target = VarNominal.copy("a", "b", "a", "c", "a", "b").withName(TARGET);
        prior.fitPriors(SolidFrame.byVars(target), VarDouble.fill(6, 1), TARGET);

        assertEquals("Uniform", prior.name());
        assertEquals("Uniform{value=0.3333333,targetLevels=[a,b,c]}", prior.fittedName());
    }

    @Test
    void testPrediction() {
        Prior prior = new PriorUniform();
        VarNominal target = VarNominal.copy("a", "b", "a", "c", "a", "b").withName(TARGET);
        prior.fitPriors(SolidFrame.byVars(target), VarDouble.fill(6, 1), TARGET);

        assertEquals(1./3, prior.computePrior("a"), TOLERANCE);
        assertEquals(1./3, prior.computePrior("b"), TOLERANCE);
        assertEquals(1./3, prior.computePrior("c"), TOLERANCE);
        assertEquals(Double.NaN, prior.computePrior("d"), TOLERANCE);
    }

    @Test
    void testNewInstance() {
        PriorUniform prior = new PriorUniform();
        VarNominal target = VarNominal.copy("a", "b", "c").withName("target");
        prior.fitPriors(SolidFrame.byVars(target), VarDouble.fill(target.rowCount(), 1), "target");

        assertEquals("Uniform{value=0.3333333,targetLevels=[a,b,c]}", prior.fittedName());

        Prior copy = prior.newInstance();
        assertNotNull(copy);
        assertEquals("Uniform{value=?,targetLevels=[]}", copy.fittedName());
    }
}
