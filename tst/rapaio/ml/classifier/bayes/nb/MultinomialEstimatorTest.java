package rapaio.ml.classifier.bayes.nb;

import org.junit.jupiter.api.Test;
import rapaio.core.distributions.Binomial;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/14/20.
 */
public class MultinomialEstimatorTest {

    private static final double TOL = 1e-12;

    @Test
    void testBuilders() {

        var estim1 = MultinomialEstimator.fromNames("x", "y", "z");
        var estim2 = MultinomialEstimator.fromNames(Arrays.asList("x", "y", "z"));
        var estim3 = MultinomialEstimator.fromRange(SolidFrame.byVars(
                VarDouble.empty().withName("x"), VarDouble.empty().withName("y"), VarDouble.empty().withName("z"),
                VarNominal.empty().withName("a")
        ), VRange.onlyTypes(VType.DOUBLE));

        var estim4 = estim3.newInstance();

        var estim5 = MultinomialEstimator.fromNames(1, "x", "y", "z");
        var estim6 = MultinomialEstimator.fromNames(1, Arrays.asList("x", "y", "z"));
        var estim7 = MultinomialEstimator.fromRange(1, SolidFrame.byVars(
                VarDouble.empty().withName("x"), VarDouble.empty().withName("y"), VarDouble.empty().withName("z"),
                VarNominal.empty().withName("a")
        ), VRange.onlyTypes(VType.DOUBLE));

        var estim8 = estim7.newInstance();

        assertEquals("Multinomial{laplaceSmoother=0.00001, tests=[x,y,z]}", estim1.name());
        assertEquals(estim1.name(), estim2.name());
        assertEquals(estim1.name(), estim3.name());
        assertEquals(estim1.name(), estim4.name());

        assertEquals("Multinomial{laplaceSmoother=1, tests=[x,y,z]}", estim5.name());
        assertEquals(estim5.name(), estim6.name());
        assertEquals(estim5.name(), estim7.name());
        assertEquals(estim5.name(), estim8.name());
    }

    @Test
    void testInvalidBuilders() {

        Var x1 = VarDouble.wrap(0, 1.1, 2).withName("x1");
        Var x2 = VarDouble.wrap(-1, 2, 3).withName("x2");
        Var x3 = VarDouble.wrap(1, 0, 100).withName("x3");
        Var t = VarNominal.copy("a", "b", "c").withName("t");

        var ex = assertThrows(IllegalArgumentException.class, () -> MultinomialEstimator.fromNames("x3"));
        assertEquals("Multinomial estimator must apply to at least 2 variables.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> MultinomialEstimator.fromNames("x1", "x3")
                .fit(SolidFrame.byVars(x1, x3, t), null, "t"));
        assertEquals("Test variable name: x1 must contain only semi-positive integer values.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> MultinomialEstimator.fromNames("x2", "x3")
                .fit(SolidFrame.byVars(x2, x3, t), null, "t"));
        assertEquals("Test variable name: x2 must contain only semi-positive integer values.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> MultinomialEstimator.fromNames("x1", "x3")
                .fit(SolidFrame.byVars(x2, x3, t), null, "t"));
        assertEquals("Frame does not contain some test variables, for example: x1.", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> MultinomialEstimator.fromNames("x3", "t")
                .fit(SolidFrame.byVars(x3, t), null, "t"));
        assertEquals("Target variable cannot be a test variable.", ex.getMessage());
    }

    @Test
    void testFittedName() {

        Var x1 = VarDouble.copy(1, 2, 3, 1, 2, 3).withName("x1");
        Var x2 = VarDouble.copy(3, 2, 1, 3, 2, 1).withName("x2");
        Var t = VarNominal.copy("a", "a", "a", "b", "b", "b").withName("t");

        var estimator = MultinomialEstimator.fromNames("x1", "x2");
        estimator.fit(SolidFrame.byVars(x1, x2, t), null, "t");

        assertEquals("Multinomial{laplaceSmoother=0.00001, tests=[x1,x2],distributions=[a:[0.5,0.5],b:[0.5,0.5]]}", estimator.fittedName());
    }

    @Test
    void testFitAndPredict() {
        Var x1 = VarDouble.copy(1, 2, 3, 1, 2, 3, 0).withName("x1");
        Var x2 = VarDouble.copy(3, 2, 1, 3, 2, 1, 0).withName("x2");
        Var t = VarNominal.copy("a", "a", "a", "b", "b", "b").withName("t");

        var estimator = MultinomialEstimator.fromNames("x1", "x2");
        var df = SolidFrame.byVars(x1, x2, t);
        estimator.fit(df, null, "t");

        assertEquals(Binomial.of(0.5, 4).pdf(1), estimator.predict(df, 0, "a"), TOL);
        assertEquals(Binomial.of(0.5, 4).pdf(3), estimator.predict(df, 0, "b"), TOL);
        assertEquals(Binomial.of(0.5, 4).pdf(2), estimator.predict(df, 1, "a"), TOL);
        assertEquals(Binomial.of(0.5, 4).pdf(2), estimator.predict(df, 1, "b"), TOL);
        assertEquals(Binomial.of(0.5, 4).pdf(3), estimator.predict(df, 2, "a"), TOL);
        assertEquals(Binomial.of(0.5, 4).pdf(1), estimator.predict(df, 2, "b"), TOL);
        df.setDouble(6, "x1", -1);
        assertEquals(MultinomialEstimator.eps, estimator.predict(df, 6, "a"), TOL);
        assertEquals(Double.NaN, estimator.predict(df, 0, "nonexistent"));
    }
}
