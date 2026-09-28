/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.core.tests.HTest.Alternative;
import rapaio.data.Var;
import rapaio.data.VarDouble;

/**
 * The confidence interval reported by the t and z tests follows the alternative
 * hypothesis, as R's {@code t.test} does: {@code [ciLow, +Inf)} for "greater",
 * {@code (-Inf, ciHigh]} for "less", the central interval for two tails. Before
 * 2026-09-28 only {@link TTestOneSample} did this; the other five always reported
 * the two-sided interval. Reference values from scipy 1.18
 * ({@code ttest_1samp/ttest_ind/ttest_rel(...).confidence_interval}).
 */
public class OneSidedConfidenceIntervalTest {

    private static final double TOL = 1e-12;

    private static final Var X = VarDouble.copy(7.8, 6.6, 6.5, 7.4, 7.3, 7.0, 6.4, 7.1, 6.7, 7.6, 6.8);
    private static final Var Y = VarDouble.copy(4.5, 5.4, 6.1, 6.1, 5.4, 5., 4.1, 5.5);

    @Test
    void oneSidedIntervalsOpenTowardsTheAlternative() {
        List<Function<Alternative, HTest>> tests = List.of(
                alt -> TTestOneSample.test(X, 7, 0.1, alt),
                alt -> TTestTwoSamples.welchTest(X, Y, 2, 0.1, alt),
                alt -> TTestTwoSamples.test(X, Y, 2, 0.1, alt),
                alt -> TTestTwoPaired.test(X, Y, 2, 0.05, alt),
                alt -> ZTestOneSample.test(X, 7, 0.5, 0.05, alt),
                alt -> ZTestTwoSamples.test(X, Y, 2, 0.5, 0.5, 0.05, alt),
                alt -> ZTestTwoPaired.test(X, Y, 2, 0.5, 0.05, alt)
        );
        for (Function<Alternative, HTest> f : tests) {
            HTest two = f.apply(Alternative.TWO_TAILS);
            HTest greater = f.apply(Alternative.GREATER_THAN);
            HTest less = f.apply(Alternative.LESS_THAN);
            String name = two.getClass().getSimpleName();

            assertTrue(Double.isFinite(two.ciLow()) && Double.isFinite(two.ciHigh()), name + " two-sided finite");
            assertEquals(Double.POSITIVE_INFINITY, greater.ciHigh(), name + " greater: ciHigh");
            assertEquals(Double.NEGATIVE_INFINITY, less.ciLow(), name + " less: ciLow");
            // the one-sided bound at level sl lies inside the two-sided interval at the same sl
            assertTrue(greater.ciLow() > two.ciLow(), name + " greater: ciLow inside two-sided");
            assertTrue(less.ciHigh() < two.ciHigh(), name + " less: ciHigh inside two-sided");
            // and the two bounds are symmetric around the estimate
            double estimate = (two.ciLow() + two.ciHigh()) / 2;
            assertEquals(estimate - greater.ciLow(), less.ciHigh() - estimate, 1e-9, name + " symmetric");
        }
    }

    @Test
    void tTestsMatchScipyOneSidedIntervals() {
        // ttest_1samp(x, 7, alternative='greater').confidence_interval(0.9)
        assertEquals(6.826059908594174, TTestOneSample.test(X, 7, 0.1, Alternative.GREATER_THAN).ciLow(), TOL);
        // ttest_ind(x, y, equal_var=False, alternative=...).confidence_interval(0.9)
        assertEquals(1.3657289375904371, TTestTwoSamples.welchTest(X, Y, 2, 0.1, Alternative.GREATER_THAN).ciLow(), TOL);
        assertEquals(2.145634698773196, TTestTwoSamples.welchTest(X, Y, 2, 0.1, Alternative.LESS_THAN).ciHigh(), TOL);
        // ttest_rel(x[:8], y, alternative=...).confidence_interval(0.95)
        assertEquals(1.1760154869709178, TTestTwoPaired.test(X, Y, 2, 0.05, Alternative.GREATER_THAN).ciLow(), TOL);
        assertEquals(2.323984513029082, TTestTwoPaired.test(X, Y, 2, 0.05, Alternative.LESS_THAN).ciHigh(), TOL);
    }

    @Test
    void helperMatchesClosedForm() {
        Normal n = Normal.of(10, 2);
        double[] two = HTest.confidenceInterval(n, 0.05, Alternative.TWO_TAILS);
        double[] gt = HTest.confidenceInterval(n, 0.05, Alternative.GREATER_THAN);
        double[] lt = HTest.confidenceInterval(n, 0.05, Alternative.LESS_THAN);
        assertEquals(10 - 1.959963984540054 * 2, two[0], 1e-12);
        assertEquals(10 + 1.959963984540054 * 2, two[1], 1e-12);
        assertEquals(10 - 1.6448536269514722 * 2, gt[0], 1e-12);
        assertEquals(Double.POSITIVE_INFINITY, gt[1]);
        assertEquals(Double.NEGATIVE_INFINITY, lt[0]);
        assertEquals(10 + 1.6448536269514722 * 2, lt[1], 1e-12);
    }
}
