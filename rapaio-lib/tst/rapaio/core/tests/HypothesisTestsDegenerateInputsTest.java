/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package rapaio.core.tests;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.darray.DArrays;
import rapaio.darray.Shape;
import rapaio.data.VarDouble;

/**
 * Degenerate inputs of the hypothesis tests. The reference values were computed outside of the code under test,
 * from the definitions and from the closed forms of the chi-square survival function.
 */
public class HypothesisTestsDegenerateInputsTest {

    @Test
    void goodnessOfFitRejectsProbabilitiesWhichDoNotSumToOne() {
        // R: chisq.test(c(10,20,30), p=c(.5,.5,.5)) is an error, "probabilities must sum to 1"
        var ex = assertThrows(IllegalArgumentException.class,
                () -> ChiSqGoodnessOfFit.from(VarDouble.copy(10, 20, 30), VarDouble.wrap(0.5, 0.5, 0.5)));
        assertTrue(ex.getMessage().contains("sum to 1"), ex.getMessage());
        assertThrows(IllegalArgumentException.class,
                () -> ChiSqGoodnessOfFit.from(VarDouble.copy(10, 20, 30), VarDouble.wrap(0.2, 0.2, 0.2)));
    }

    @Test
    void goodnessOfFitRejectsNegativeAndNotFiniteProbabilities() {
        assertThrows(IllegalArgumentException.class,
                () -> ChiSqGoodnessOfFit.from(VarDouble.copy(10, 20, 30), VarDouble.wrap(1.5, -0.25, -0.25)));
        assertThrows(IllegalArgumentException.class,
                () -> ChiSqGoodnessOfFit.from(VarDouble.copy(10, 20, 30), VarDouble.wrap(Double.NaN, 0.5, 0.5)));
    }

    @Test
    void goodnessOfFitAcceptsProbabilitiesWithRoundingError() {
        double third = 1.0 / 3;
        assertDoesNotThrow(() -> ChiSqGoodnessOfFit.from(VarDouble.copy(10, 20, 30), VarDouble.wrap(third, third, third)));
        assertDoesNotThrow(() -> ChiSqGoodnessOfFit.from(VarDouble.copy(10, 20, 30), VarDouble.wrap(0.1, 0.2, 0.7)));
    }

    @Test
    void goodnessOfFitLeavesOutACategoryWhichCanNotOccurAndDidNotOccur() {
        // observed (0, 30, 30) against (0, .5, .5): the first category is left out, the remaining two
        // are exactly on their expected counts. It used to add an infinity and report a p value of 0
        var test = ChiSqGoodnessOfFit.from(VarDouble.copy(0, 30, 30), VarDouble.wrap(0, 0.5, 0.5));
        assertEquals(0.0, test.getChiValue(), 1e-12);
        assertEquals(1, test.df());
        assertEquals(1.0, test.pValue(), 1e-12);

        // observed (0, 25, 35): chi = 25/30 + 25/30 = 5/3, one degree of freedom, p = erfc(sqrt(chi/2))
        var other = ChiSqGoodnessOfFit.from(VarDouble.copy(0, 25, 35), VarDouble.wrap(0, 0.5, 0.5));
        assertEquals(5.0 / 3, other.getChiValue(), 1e-12);
        assertEquals(1, other.df());
        assertEquals(0.19670560245894686, other.pValue(), 1e-12);
    }

    @Test
    void goodnessOfFitWithAnObservationInACategoryWhichCanNotOccurIsInfinite() {
        var test = ChiSqGoodnessOfFit.from(VarDouble.copy(3, 30, 30), VarDouble.wrap(0, 0.5, 0.5));
        assertEquals(Double.POSITIVE_INFINITY, test.getChiValue(), 0.0);
        assertEquals(0.0, test.pValue(), 0.0);
    }

    @Test
    void yatesCorrectionIsAppliedToA2x2TableOnly() {
        // R: chisq.test(matrix(c(10,30,20,20,30,10), 2), correct=TRUE)$statistic == 20
        var m = DArrays.stride(Shape.of(2, 3), 10.0, 20, 30, 30, 20, 10);
        var corrected = ChiSqIndependence.from(m, true);
        var plain = ChiSqIndependence.from(m, false);
        assertEquals(20.0, corrected.getChiValue(), 1e-12);
        assertEquals(plain.getChiValue(), corrected.getChiValue(), 0.0);
        assertEquals(2, corrected.getDegrees());
        assertEquals(4.5399929762484854e-05, corrected.pValue(), 1e-12);
        assertTrue(!corrected.toSummary().contains("Yates"), "the summary must not claim a correction which was not applied");
    }

    @Test
    void yatesCorrectionOfA2x2TableIsUnchanged() {
        // R: 33.112, as pinned by the existing test
        var corrected = ChiSqIndependence.from(DArrays.stride(Shape.of(2, 2), 38.0, 11, 14, 51), true);
        assertEquals(33.11197280210042, corrected.getChiValue(), 1e-10);
        assertTrue(corrected.toSummary().contains("Yates"));
    }

    @Test
    void independenceTestOfATableWithASingleRowOrColumnHasAClearMessage() {
        // it failed with a message about the degrees of freedom of the chi-square distribution
        var oneRow = assertThrows(IllegalArgumentException.class,
                () -> ChiSqIndependence.from(DArrays.stride(Shape.of(1, 3), 10.0, 20, 30), false));
        assertTrue(oneRow.getMessage().contains("at least 2 rows and 2 columns"), oneRow.getMessage());
        var oneCol = assertThrows(IllegalArgumentException.class,
                () -> ChiSqIndependence.from(DArrays.stride(Shape.of(3, 1), 10.0, 20, 30), false));
        assertTrue(oneCol.getMessage().contains("at least 2 rows and 2 columns"), oneCol.getMessage());
    }

    @Test
    void twoSampleKolmogorovSmirnovHasNoConfidenceInterval() {
        var test = KSTestTwoSamples.from(VarDouble.copy(1, 2, 3), VarDouble.copy(4, 5, 6));
        assertTrue(Double.isNaN(test.ciLow()));
        assertTrue(Double.isNaN(test.ciHigh()));
        var one = KSTestOneSample.from(VarDouble.copy(1, 2, 3), Normal.std());
        assertTrue(Double.isNaN(one.ciLow()));
        assertTrue(Double.isNaN(one.ciHigh()));
    }

    @Test
    void kolmogorovSmirnovOfAnEmptySampleIsNotANumber() {
        // the p value was 1.0, a confident "no difference" from no data
        var oneSample = KSTestOneSample.from(VarDouble.empty(), Normal.std());
        assertTrue(Double.isNaN(oneSample.pValue()));
        assertTrue(Double.isNaN(oneSample.d()));

        var allMissing = KSTestOneSample.from(VarDouble.copy(Double.NaN, Double.NaN), Normal.std());
        assertTrue(Double.isNaN(allMissing.pValue()));

        var twoSamples = KSTestTwoSamples.from(VarDouble.empty(), VarDouble.copy(1, 2, 3));
        assertTrue(Double.isNaN(twoSamples.pValue()));
        assertTrue(Double.isNaN(twoSamples.d()));
        assertTrue(Double.isNaN(KSTestTwoSamples.from(VarDouble.copy(1, 2, 3), VarDouble.empty()).pValue()));
    }

    @Test
    void kolmogorovSmirnovOfOrdinarySamplesIsUnchanged() {
        var test = KSTestTwoSamples.from(VarDouble.copy(1, 2, 3), VarDouble.copy(4, 5, 6));
        assertEquals(1.0, test.d(), 0.0);
        assertTrue(test.pValue() > 0 && test.pValue() < 1);
    }

    @Test
    void pooledTTestOfTwoConstantSamplesIsRefused() {
        // the standard error is 0, the statistic infinite, and the p value was a confident 0 with the confidence
        // interval [-1, -1]. R: t.test(c(1,1,1), c(2,2,2), var.equal=TRUE) is an error, "data are essentially constant"
        var ex = assertThrows(IllegalArgumentException.class,
                () -> TTestTwoSamples.test(VarDouble.copy(1, 1, 1), VarDouble.copy(2, 2, 2), 0));
        assertTrue(ex.getMessage().contains("essentially constant"), ex.getMessage());
    }

    @Test
    void welchTTestOfTwoConstantSamplesIsRefusedWithTheSameMessage() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> TTestTwoSamples.welchTest(1.0, 3, 2.0, 3, 0.0, 0.0, 0.0));
        assertTrue(ex.getMessage().contains("essentially constant"), ex.getMessage());
    }

    @Test
    void tTestOfSamplesWithOneConstantSampleIsStillComputed() {
        // only the pooled error of both samples matters: one constant sample is an ordinary case
        var test = TTestTwoSamples.test(VarDouble.copy(1, 1, 1), VarDouble.copy(2, 3, 4), 0);
        assertTrue(Double.isFinite(test.pValue()));
        assertTrue(test.pValue() > 0 && test.pValue() < 1);
    }

    @Test
    void andersonDarlingAgreesWithTheReferenceImplementation() {
        // the correction term of the p value is z = 0.01265 + 0.1757 / n in Marsaglia and Marsaglia (2004) and in
        // ADGofTest::ad.test.pvalue; the sign of 0.01265 was reversed, which changes the value for every n
        VarDouble x = VarDouble.wrap(6.0747159, -8.9637424, -1.1363964, 1.5831864, -3.4660379, 2.6695147, 3.0571496,
                0.8348192, -11.3294910, 13.8572907);
        // 1 - ad.test.pvalue(A^2 = 0.7283345415692928, n = 10), independently computed
        assertEquals(0.531736923277633, ADTestGoodness.from(x, 1, 5).pValue(), 1e-9);
    }
}
