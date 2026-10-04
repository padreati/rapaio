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

package rapaio.core.stat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.data.VarDouble;

/**
 * Infinite values are legitimate observations: they are not missing, and the statistics have to give the
 * answer the infinity implies, as R and numpy do, and not a NaN produced by the correction terms.
 */
public class InfinityHandlingTest {

    private static final double INF = Double.POSITIVE_INFINITY;
    private static final double NINF = Double.NEGATIVE_INFINITY;

    private static void assertSame(double expected, double actual) {
        assertEquals(expected, actual, 0.0);
    }

    @Test
    void sumWithAnInfiniteValueIsInfinite() {
        // the answer used to depend on the number of other values: a lone infinity was kept, with others it was NaN
        assertSame(INF, Sum.of(VarDouble.wrap(INF)).value());
        assertSame(INF, Sum.of(VarDouble.wrap(1, INF)).value());
        assertSame(INF, Sum.of(VarDouble.wrap(INF, 1)).value());
        assertSame(INF, Sum.of(VarDouble.wrap(1, 2, INF, 3, 4)).value());
        assertSame(NINF, Sum.of(VarDouble.wrap(1, 2, NINF, 3)).value());
    }

    @Test
    void sumWhichOverflowsIsInfinite() {
        assertSame(INF, Sum.of(VarDouble.wrap(1e308, 1e308)).value());
        assertSame(NINF, Sum.of(VarDouble.wrap(-1e308, -1e308, -1e308)).value());
    }

    @Test
    void sumOfOppositeInfinitiesIsNotANumber() {
        assertTrue(Double.isNaN(Sum.of(VarDouble.wrap(INF, 1, NINF)).value()));
    }

    @Test
    void sumOfFiniteValuesIsStillCompensated() {
        // Neumaier compensation must keep working: the plain sum of these loses the small terms
        VarDouble x = VarDouble.wrap(1e16, 1, -1e16, 1);
        assertEquals(2.0, Sum.of(x).value(), 0.0);
        assertEquals(6.0, Sum.of(VarDouble.wrap(1, 2, 3)).value(), 0.0);
    }

    @Test
    void sumSkipsMissingValuesAroundAnInfinity() {
        assertSame(INF, Sum.of(VarDouble.wrap(Double.NaN, 1, INF, Double.NaN)).value());
    }

    @Test
    void meanWithAnInfiniteValueIsInfinite() {
        // the residual correction subtracted the infinite mean from the infinite value, giving NaN
        assertSame(INF, Mean.of(VarDouble.wrap(INF)).value());
        assertSame(INF, Mean.of(VarDouble.wrap(1, INF)).value());
        assertSame(INF, Mean.of(VarDouble.wrap(1, Double.NaN, INF)).value());
        assertSame(NINF, Mean.of(VarDouble.wrap(NINF, 2, 3)).value());
        assertSame(INF, Mean.of(VarDouble.wrap(INF, INF)).value());
    }

    @Test
    void meanOfOppositeInfinitiesIsNotANumber() {
        assertTrue(Double.isNaN(Mean.of(VarDouble.wrap(INF, NINF)).value()));
    }

    @Test
    void meanOfLargeFiniteValuesWhoseSumOverflows() {
        // the sum is infinite but the mean is representable: R gives 1e308 using an extended accumulator
        assertEquals(1e308, Mean.of(VarDouble.wrap(1e308, 1e308)).value(), 1e308 * 1e-15);
        assertEquals(1e308, Mean.of(VarDouble.wrap(1e308, 1e308, 1e308)).value(), 1e308 * 1e-15);
        assertEquals(-1e308, Mean.of(VarDouble.wrap(-1e308, -1e308)).value(), 1e308 * 1e-15);
        assertEquals(0.0, Mean.of(VarDouble.wrap(1e308, 1e308, -1e308, -1e308)).value(), 1e308 * 1e-15);
    }

    @Test
    void meanOfFiniteValuesIsUnchanged() {
        assertEquals(2.0, Mean.of(VarDouble.wrap(1, 2, 3)).value(), 0.0);
        assertEquals(1e300, Mean.of(VarDouble.wrap(1e300, 1e300)).value(), 1e300 * 1e-15);
    }

    @Test
    void varianceOfASampleWithAnInfinityIsNotANumber() {
        // the variance of an infinite sample is undefined, as in R where var(c(1,2,Inf)) is NaN
        assertTrue(Double.isNaN(Variance.of(VarDouble.wrap(1, 2, INF)).value()));
    }

    @Test
    void quantileOfOrderStatisticsNextToAnInfinityIsExact() {
        // the interpolation 4 + 0 * (Inf - 4) multiplied a zero weight by an infinite difference
        VarDouble x = VarDouble.wrap(1, 2, 3, 4, INF);
        assertSame(4.0, Quantiles.of(x, 0.75).values()[0]);
        assertSame(1.0, Quantiles.of(x, 0.0).values()[0]);
        assertSame(INF, Quantiles.of(x, 1.0).values()[0]);
        assertSame(INF, Quantiles.of(VarDouble.wrap(1, INF, INF), 1.0).values()[0]);
        assertSame(NINF, Quantiles.of(VarDouble.wrap(NINF, NINF, 1), 0.0).values()[0]);
        assertSame(INF, Quantiles.of(VarDouble.wrap(INF, INF, INF), 0.4).values()[0]);
    }

    @Test
    void quantileBetweenAFiniteAndAnInfiniteValueIsInfinite() {
        assertSame(INF, Quantiles.of(VarDouble.wrap(1, INF), 0.5).values()[0]);
        assertSame(NINF, Quantiles.of(VarDouble.wrap(NINF, 1), 0.5).values()[0]);
        // between opposite infinities there is no defined value
        assertTrue(Double.isNaN(Quantiles.of(VarDouble.wrap(NINF, INF), 0.5).values()[0]));
    }

    @Test
    void type8QuantileNextToAnInfinityIsNotNaN() {
        VarDouble x = VarDouble.wrap(1, 2, 3, 4, INF);
        assertSame(INF, Quantiles.of(x, Quantiles.Type.R8, 0.75).values()[0]);
        assertSame(1.0, Quantiles.of(x, Quantiles.Type.R8, 0.0).values()[0]);
    }

    @Test
    void quantileOfFiniteDataIsUnchanged() {
        VarDouble x = VarDouble.wrap(1, 2, 3, 4);
        // R: quantile(c(1,2,3,4), .75, type=7) == 3.25
        assertEquals(3.25, Quantiles.of(x, 0.75).values()[0], 1e-15);
        assertEquals(1.9, Quantiles.of(x, 0.3).values()[0], 1e-15);
        // the extreme finite values whose difference overflows
        assertEquals(0.0, Quantiles.of(VarDouble.wrap(-1e308, 1e308), 0.5).values()[0], 1e293);
    }
}
