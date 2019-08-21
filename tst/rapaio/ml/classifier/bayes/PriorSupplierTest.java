package rapaio.ml.classifier.bayes;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rapaio.core.*;
import rapaio.core.distributions.*;
import rapaio.data.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/14/19.
 */
public class PriorSupplierTest {

    private static final double TOL = 1e-12;
    private static final int N = 10_000;
    private static final String TARGET_NAME = "target";
    private Normal normal;
    private Var weights;
    private Var target;


    @Before
    public void setUp() {
        RandomSource.setSeed(1234);
        normal = Normal.of(0, 10);
        weights = normal.sample(N);
        target = VarNominal.copy(Arrays
                .stream(SamplingTools.sampleWR(10, N))
                .mapToObj(String::valueOf)
                .collect(Collectors.toList()))
                .withName(TARGET_NAME);
    }

    @Test
    public void testMLE() {
        List<String> levels = new ArrayList<>(target.levels());
        levels.remove(0);

        assertEquals("PRIOR_MLE", PriorSupplier.PRIOR_MLE.name());

        Map<String, Double> priors = PriorSupplier.PRIOR_MLE.learnPriors(SolidFrame.byVars(target), weights, TARGET_NAME);
        double totalWeight = weights.op().sum();

        assertEquals(levels.size(), priors.size());
        for (String level : levels) {
            assertTrue(priors.containsKey(level));

            double weightSum = 0.0;
            for (int i = 0; i < target.rowCount(); i++) {
                if (target.getLabel(i).equals(level)) {
                    weightSum += weights.getDouble(i);
                }
            }
            assertEquals(weightSum / totalWeight, priors.get(level), TOL);
        }
    }

    @Test
    public void testUniform() {
        List<String> levels = new ArrayList<>(target.levels());
        levels.remove(0);

        assertEquals("PRIOR_UNIFORM", PriorSupplier.PRIOR_UNIFORM.name());

        Map<String, Double> priors = PriorSupplier.PRIOR_UNIFORM.learnPriors(SolidFrame.byVars(target), weights, TARGET_NAME);
        assertEquals(levels.size(), priors.size());

        for (String level : levels) {
            assertTrue(priors.containsKey(level));
            assertEquals(1.0 / levels.size(), priors.get(level), TOL);
        }
    }
}
