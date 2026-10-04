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

package rapaio.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.core.distributions.empirical.KDE;
import rapaio.core.distributions.empirical.KFuncGaussian;
import rapaio.core.distributions.empirical.KFuncUniform;
import rapaio.core.param.ListParam;
import rapaio.core.param.MultiListParam;
import rapaio.core.param.MultiParam;
import rapaio.core.param.ParamSet;
import rapaio.core.param.ValueParam;
import rapaio.core.stat.Covariance;
import rapaio.core.stat.GeometricMean;
import rapaio.core.stat.Quantiles;
import rapaio.core.tools.DistanceMatrix;
import rapaio.core.tools.HistogramTable;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;

/**
 * Regression tests for the lower severity findings of the review of {@code rapaio.core}.
 */
public class CoreLowSeverityRegressionTest {

    // --- KDE -------------------------------------------------------------------------------

    @Test
    void kdeCountsTheSampleValuesOnBothEdgesOfTheKernelSupport() {
        // the binary search used the position of a value equal to the upper edge as an exclusive bound
        KDE kde = KDE.of(VarDouble.wrap(0), new KFuncUniform(), 1);
        assertEquals(0.5, kde.pdf(-1), 1e-15);
        assertEquals(0.5, kde.pdf(1), 1e-15);
        assertEquals(kde.pdf(-1), kde.pdf(1), 0.0);
    }

    @Test
    void kdeCountsEveryDuplicateOnTheEdge() {
        // binarySearch landed in the middle of the run of zeros, so some of them were skipped
        KDE kde = KDE.of(VarDouble.wrap(0, 0, 0, 2), new KFuncUniform(), 1);
        // all four points are at |u| = 1, each contributes 0.5: 2.0 / (4 * 1)
        assertEquals(0.5, kde.pdf(1), 1e-15);
    }

    @Test
    void kdeMatchesTheDirectSumOfTheEstimator() {
        Random random = new Random(3);
        for (int trial = 0; trial < 50; trial++) {
            double[] sample = new double[5 + random.nextInt(20)];
            for (int i = 0; i < sample.length; i++) {
                sample[i] = random.nextInt(6);
            }
            double h = 1 + random.nextInt(2);
            KDE kde = KDE.of(VarDouble.wrap(sample.clone()), new KFuncUniform(), h);
            for (double x = -2; x <= 8; x += 0.5) {
                double sum = 0;
                for (double s : sample) {
                    sum += Math.abs((x - s) / h) <= 1 ? 0.5 : 0;
                }
                assertEquals(sum / (sample.length * h), kde.pdf(x), 1e-14, "trial " + trial + " x=" + x);
            }
        }
    }

    @Test
    void kdeRejectsANonPositiveBandwidth() {
        assertThrows(IllegalArgumentException.class, () -> KDE.of(VarDouble.wrap(1, 2, 3), 0));
        assertThrows(IllegalArgumentException.class, () -> KDE.of(VarDouble.wrap(1, 2, 3), -1));
    }

    @Test
    void silvermanBandwidthOfASingleValueIsUsable() {
        // the variance of one value is NaN, which the sd == 0 fallback did not catch, and every density was NaN
        assertEquals(1.06, KDE.silvermanBandwidth(VarDouble.wrap(5)), 1e-15);
        KDE kde = KDE.of(VarDouble.wrap(5));
        assertEquals(1 / Math.sqrt(2 * Math.PI) / 1.06, kde.pdf(5), 1e-12);
        assertTrue(Double.isNaN(KDE.silvermanBandwidth(VarDouble.empty())));
        // the existing value for four points is unchanged
        assertEquals(1.037094286807564, KDE.silvermanBandwidth(VarDouble.wrap(1, 2, 3, 4)), 1e-12);
    }

    @Test
    void gaussianKernelDensityIsTruncatedAtFourBandwidthsAsDocumented() {
        KDE kde = KDE.of(VarDouble.wrap(0), new KFuncGaussian(), 1);
        assertEquals(0.0, kde.pdf(4.5), 0.0);
        assertTrue(kde.pdf(3.9) > 0);
    }

    // --- stat ------------------------------------------------------------------------------

    @Test
    void geometricMeanOfAnAllMissingSampleIsNotDefined() {
        assertFalse(GeometricMean.of(VarDouble.wrap(Double.NaN)).isDefined());
        assertFalse(GeometricMean.of(VarDouble.empty()).isDefined());
        assertFalse(GeometricMean.of(VarDouble.wrap(-1, 2)).isDefined());
        assertTrue(GeometricMean.of(VarDouble.wrap(1, 4)).isDefined());
        assertEquals(2.0, GeometricMean.of(VarDouble.wrap(1, 4)).value(), 1e-15);
    }

    @Test
    void covarianceRejectsVariablesOfDifferentLength() {
        // the extra rows were silently dropped; R: cov(c(1,2,3), c(1,2,3,100)) is an error
        assertThrows(IllegalArgumentException.class,
                () -> Covariance.of(VarDouble.wrap(1, 2, 3), VarDouble.wrap(1, 2, 3, 100)));
        assertEquals(1.0, Covariance.of(VarDouble.wrap(1, 2, 3), VarDouble.wrap(1, 2, 3)).value(), 1e-15);
    }

    @Test
    void quantileValuesAreACopy() {
        Quantiles q = Quantiles.of(VarDouble.wrap(1, 2, 3, 4), 0.5);
        q.values()[0] = -999;
        assertEquals(2.5, q.values()[0], 0.0);
        assertTrue(q.toString().contains("2.5"));
    }

    // --- tools -----------------------------------------------------------------------------

    @Test
    void distanceMatrixRejectsIndexesOutsideTheMatrix() {
        // (2, -1) used to land on the packed slot of (1, 1)
        DistanceMatrix m = DistanceMatrix.empty(3);
        assertThrows(IndexOutOfBoundsException.class, () -> m.set(2, -1, 7.0));
        assertEquals(0.0, m.get(1, 1), 0.0);
        assertThrows(IndexOutOfBoundsException.class, () -> m.get(3, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> m.get(0, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> m.get(-1, -1));
        m.set(0, 2, 5.0);
        assertEquals(5.0, m.get(2, 0), 0.0);
        assertEquals(5.0, m.get(0, 2), 0.0);
    }

    @Test
    void histogramOfConstantDataHasOneBin() {
        // Sturges gave 6 bins for 20 equal values, five of which could never receive a value
        HistogramTable h = new HistogramTable(VarDouble.fill(20, 3.0), Double.NaN, Double.NaN, 0);
        assertEquals(1, h.bins());
        assertEquals(20, h.freq().getDouble(0), 0.0);
    }

    @Test
    void trainTestSplitRejectsAProportionOutsideTheUnitInterval() {
        var df = SolidFrame.byVars(VarDouble.seq(1, 10, 1).name("x"));
        assertThrows(IllegalArgumentException.class, () -> SamplingTools.trainTestSplit(new Random(1), df, 1.5));
        assertThrows(IllegalArgumentException.class, () -> SamplingTools.trainTestSplit(new Random(1), df, -0.1));
        assertThrows(IllegalArgumentException.class, () -> SamplingTools.trainTestSplit(new Random(1), df, Double.NaN));
        assertEquals(0, SamplingTools.trainTestSplit(new Random(1), df, 0).trainDf().rowCount());
        assertEquals(10, SamplingTools.trainTestSplit(new Random(1), df, 1).trainDf().rowCount());
    }

    // --- param -----------------------------------------------------------------------------

    private static final class P extends ParamSet<P> {
        final ListParam<String, P> list = new ListParam<>(this, List.of("a"), "list", (e, n) -> true);
        final MultiParam<String, Integer, P> map = new MultiParam<>(this, Map.of(), "map", m -> true);
        final MultiListParam<String, Integer, P> groups = new MultiListParam<>(this, Map.of(), "groups", m -> true);
        final ValueParam<String, P> unset = new ValueParam<>(this, null, "unset");
        final ValueParam<Integer, P> value = new ValueParam<>(this, 1, "value");

        P copy(P other) {
            return copyParameterValues(other);
        }
    }

    @Test
    void assigningAParameterItsOwnValueKeepsTheValue() {
        // the live view was cleared before it was copied back
        P p = new P();
        p.list.set("x", "y");
        p.list.set(p.list.get());
        assertEquals(List.of("x", "y"), p.list.get());

        p.map.set(Map.of("k", 1));
        p.map.set(p.map.get());
        assertEquals(Map.of("k", 1), p.map.get());

        p.groups.set(Map.of("g", List.of(1, 2)));
        p.groups.set(p.groups.get());
        assertEquals(List.of(1, 2), p.groups.get("g"));
    }

    @Test
    void copyingAParameterSetWithAnUnsetParameterSucceeds() {
        // the copy went through set(), whose default validator rejects the null of an unset parameter
        P source = new P();
        source.value.set(5);
        P copy = new P().copy(source);
        assertNull(copy.unset.get());
        assertEquals(5, copy.value.get());
    }

    @Test
    void listsOfAMultiListParameterAreNotSharedWithTheCaller() {
        P a = new P();
        ArrayList<Integer> list = new ArrayList<>(List.of(1));
        a.groups.set(Map.of("k", list));
        list.add(99);
        assertEquals(List.of(1), a.groups.get("k"));

        P b = new P().copy(a);
        assertThrows(UnsupportedOperationException.class, () -> a.groups.get("k").add(2));
        assertEquals(List.of(1), b.groups.get("k"));
    }

    @Test
    void clearingAListParameterEmptiesItAsDocumented() {
        // clear() empties the list, unlike the other parameters; the callers rely on it to replace a default
        P p = new P();
        p.list.add("b");
        p.list.clear();
        assertTrue(p.list.get().isEmpty());
        p.list.set(p.list.defaultValue());
        assertTrue(p.list.hasDefaultValue());
    }
}
