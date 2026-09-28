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

package rapaio.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import rapaio.core.param.ListParam;
import rapaio.core.param.MultiListParam;
import rapaio.core.param.MultiParam;
import rapaio.core.param.ParamSet;
import rapaio.core.param.ValueParam;
import rapaio.core.stat.OnlineStat;
import rapaio.core.stat.Quantiles;
import rapaio.core.tools.HistogramTable;
import rapaio.data.VarDouble;

/**
 * Degenerate inputs and validation in {@code core.stat.Quantiles},
 * {@code core.tools.HistogramTable}, {@code core.stat.OnlineStat} and {@code core.param},
 * all listed in the 2026-09-22 review and fixed 2026-09-28.
 */
public class CoreEdgeCasesTest {

    private static final double TOL = 1e-12;

    // --- Quantiles -------------------------------------------------------------------------

    @Test
    void quantilesHandleDegenerateSamples() {
        // no complete value: one NaN per requested probability (was an array of var.size())
        Quantiles empty = Quantiles.of(VarDouble.copy(Double.NaN, Double.NaN, Double.NaN), 0.25, 0.5, 0.75);
        assertEquals(3, empty.values().length);
        for (double v : empty.values()) {
            assertTrue(Double.isNaN(v));
        }
        // toString used to index percentiles out of range
        assertTrue(empty.toString().contains("0.25"));
        assertTrue(empty.toSummary().contains("complete: 0"));

        // exactly one complete value: every quantile is that value (was 0)
        Quantiles single = Quantiles.of(VarDouble.copy(Double.NaN, 7.5), 0, 0.5, 1);
        assertEquals(3, single.values().length);
        for (double v : single.values()) {
            assertEquals(7.5, v, 0);
        }
        // and no probabilities at all is legal
        assertEquals(0, Quantiles.of(VarDouble.copy(1, 2, 3)).values().length);
    }

    @Test
    void quantilesRejectProbabilitiesOutsideUnitInterval() {
        VarDouble x = VarDouble.seq(1, 10);
        for (double p : new double[] {-0.1, 1.1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> Quantiles.of(x, p), "p=" + p);
            assertThrows(IllegalArgumentException.class, () -> Quantiles.of(x, Quantiles.Type.R8, p), "p=" + p);
        }
        // boundaries are valid and give the extremes
        assertEquals(1, Quantiles.of(x, 0).values()[0], TOL);
        assertEquals(10, Quantiles.of(x, 1).values()[0], TOL);
    }

    @Test
    void quantilesDoNotShareTheCallerArray() {
        double[] probs = {0.25, 0.75};
        Quantiles q = Quantiles.of(VarDouble.seq(1, 10), probs);
        probs[0] = 0.9;
        assertTrue(q.toString().contains("0.25"));
        assertEquals(0.25, q.probabilities()[0], 0);
        // the returned array is a copy too
        q.probabilities()[0] = 0.9;
        assertEquals(0.25, q.probabilities()[0], 0);
    }

    // --- HistogramTable --------------------------------------------------------------------

    @Test
    void histogramTableHandlesConstantAndTinyData() {
        // constant data: iqr == 0 and range == 0 gave 0 bins and an AIOOBE
        HistogramTable constant = new HistogramTable(VarDouble.fill(20, 3.0), Double.NaN, Double.NaN, 0);
        assertTrue(constant.bins() >= 1);
        assertEquals(20, constant.freq().sum(), TOL);
        assertEquals(3.0, constant.min(), TOL);
        assertEquals(3.0, constant.max(), TOL);

        // more than half the values equal: iqr == 0 but the range is positive (Sturges fallback)
        VarDouble tied = VarDouble.from(21, r -> r < 15 ? 1.0 : r);
        HistogramTable table = new HistogramTable(tied, Double.NaN, Double.NaN, 0);
        assertTrue(table.bins() >= 1 && table.bins() <= 1024);
        assertEquals(21, table.freq().sum(), TOL);

        // single value, and no complete value at all
        assertEquals(1, new HistogramTable(VarDouble.copy(2.0), Double.NaN, Double.NaN, 0).bins());
        HistogramTable allMissing = new HistogramTable(VarDouble.copy(Double.NaN, Double.NaN), Double.NaN, Double.NaN, 0);
        assertTrue(allMissing.bins() >= 1);
        assertEquals(0, allMissing.freq().sum(), TOL);

        // an explicit bin count is still honoured, and every value is counted
        HistogramTable explicit = new HistogramTable(VarDouble.seq(0, 10, 0.5), 0, 10, 4);
        assertEquals(4, explicit.bins());
        assertEquals(21, explicit.freq().sum(), TOL);
    }

    // --- OnlineStat ------------------------------------------------------------------------

    @Test
    void onlineStatEmptyStateIsUniformlyNaN() {
        OnlineStat os = OnlineStat.empty();
        assertEquals(0, os.n(), 0);
        for (double v : new double[] {os.mean(), os.sum(), os.min(), os.max(), os.variance(), os.sd(),
                os.sampleVariance(), os.skewness(), os.kurtosis()}) {
            assertTrue(Double.isNaN(v), "empty accessor should be NaN");
        }
    }

    @Test
    void onlineStatIgnoresNonFiniteValues() {
        OnlineStat os = OnlineStat.empty();
        os.update(1);
        os.update(Double.NaN);
        os.update(3);
        os.update(Double.POSITIVE_INFINITY);
        assertEquals(2, os.n(), 0);
        assertEquals(2, os.mean(), TOL);
        assertEquals(4, os.sum(), TOL);
        assertEquals(1, os.min(), TOL);
        assertEquals(3, os.max(), TOL);
        assertEquals(2, os.sampleVariance(), TOL);
    }

    // --- core.param ------------------------------------------------------------------------

    private static final class NullParams extends ParamSet<NullParams> {

        @SuppressWarnings("unused")
        public final ValueParam<String, NullParams> value = new ValueParam<>(this, null, "value", __ -> true);
    }

    private static final class DuplicateParams extends ParamSet<DuplicateParams> {

        @SuppressWarnings("unused")
        public final ValueParam<String, DuplicateParams> a = new ValueParam<>(this, "x", "value", __ -> true);
        @SuppressWarnings("unused")
        public final ValueParam<String, DuplicateParams> b = new ValueParam<>(this, "y", "value", __ -> true);
    }

    private static final class Params extends ParamSet<Params> {

        Params copyOf(Params other) {
            return copyParameterValues(other);
        }


        public final ListParam<String, Params> names =
                new ListParam<>(this, List.of(), "names", (existing, added) -> {
                    for (String a : added) {
                        if (existing.contains(a) || a.isEmpty()) {
                            return false;
                        }
                    }
                    return true;
                });

        public final MultiParam<String, Integer, Params> counts =
                new MultiParam<>(this, Map.of(), "counts", m -> m.values().stream().allMatch(v -> v > 0));

        public final MultiListParam<String, Integer, Params> groups =
                new MultiListParam<>(this, Map.of(), "groups", m -> m.keySet().stream().noneMatch(String::isEmpty));
    }

    @Test
    void listParamKeepsItsValueWhenValidationFails() {
        Params p = new Params();
        p.names.set("a", "b");
        assertEquals(List.of("a", "b"), p.names.get());

        // add of an already present value is rejected and changes nothing
        assertThrows(IllegalArgumentException.class, () -> p.names.add("a"));
        assertEquals(List.of("a", "b"), p.names.get());
        // rejected set leaves the previous values in place (used to leave the param empty)
        assertThrows(IllegalArgumentException.class, () -> p.names.set("c", ""));
        assertEquals(List.of("a", "b"), p.names.get());
        // set replaces, so the same values are accepted again
        p.names.set("a", "b");
        assertEquals(List.of("a", "b"), p.names.get());

        assertThrows(UnsupportedOperationException.class, () -> p.names.get().add("z"));
    }

    @Test
    void listParamDefaultDetection() {
        Params p = new Params();
        assertTrue(p.names.hasDefaultValue());
        p.names.add("a");
        assertFalse(p.names.hasDefaultValue());
        p.names.clear();
        assertTrue(p.names.hasDefaultValue());
    }

    @Test
    void multiParamAppliesItsValidator() {
        Params p = new Params();
        p.counts.add("a", 1);
        assertEquals(1, p.counts.get("a"));

        // the validator was dead before: these used to be stored
        assertThrows(IllegalArgumentException.class, () -> p.counts.add("b", 0));
        assertThrows(IllegalArgumentException.class, () -> p.counts.set(Map.of("b", -1)));
        assertThrows(IllegalArgumentException.class, () -> p.counts.add(Map.of("b", -2)));
        assertEquals(Map.of("a", 1), p.counts.get());
        assertThrows(UnsupportedOperationException.class, () -> p.counts.get().put("z", 1));

        p.groups.add("g", 1, 2);
        assertEquals(List.of(1, 2), p.groups.get("g"));
        assertThrows(IllegalArgumentException.class, () -> p.groups.add("", 1));
        assertEquals(1, p.groups.get().size());
    }

    @Test
    void multiListParamComparesListLengths() {
        Params p = new Params();
        assertTrue(p.groups.hasDefaultValue());
        p.groups.add("g", 1, 2);
        assertFalse(p.groups.hasDefaultValue());

        // a shorter list under the same key used to throw IndexOutOfBounds
        Params other = new Params();
        other.groups.add("g", 1, 2);
        other.groups.set(Map.of("g", List.of(1)));
        assertFalse(other.groups.hasDefaultValue());
    }

    @Test
    void paramSetFormatsNullAndReportsMissingParameters() {
        Params p = new Params();
        p.counts.add("a", 2);
        String values = p.getStringParameterValues(true);
        assertTrue(values.contains("counts={a=2}"), values);
        // a null valued parameter prints instead of throwing NPE
        assertEquals("value=null", new NullParams().getStringParameterValues(false));

        // parameter values copy across sets of the same type
        Params copy = new Params().copyOf(p);
        assertEquals(Map.of("a", 2), copy.counts.get());
        assertEquals(p.getStringParameterValues(true), copy.getStringParameterValues(true));

        // registering two parameters under one name names the offender
        var ex = assertThrows(IllegalArgumentException.class, () -> new DuplicateParams());
        assertEquals("Parameters already contain a parameter named value.", ex.getMessage());
    }
}
