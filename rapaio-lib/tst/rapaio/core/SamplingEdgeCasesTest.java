/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;

/**
 * Edge cases and invariants of {@link SamplingTools}: alias-table completeness, the boundaries of the weighted
 * sampler without replacement, zero frequencies, argument validation and slice sizing.
 */
public class SamplingEdgeCasesTest {

    /**
     * Every column of the alias table must either be always accepted ({@code prob >= 1}) or carry a genuine
     * alias. The clean-up loop used to skip the last remaining small column, leaving it with {@code prob}
     * just under one and no alias assigned at all.
     */
    @Test
    void aliasTableLeavesNoColumnWithoutAnAlias() {
        Random random = new Random(31);
        int checked = 0;
        for (int it = 0; it < 20000; it++) {
            int n = 2 + random.nextInt(40);
            double[] freq = new double[n];
            for (int i = 0; i < n; i++) {
                freq[i] = switch (it % 4) {
                    case 0 -> random.nextDouble();
                    case 1 -> 1;
                    case 2 -> random.nextInt(3) + 1;
                    default -> Math.exp(random.nextGaussian());
                };
            }
            double total = Arrays.stream(freq).sum();
            double[] p = new double[n];
            double[] prob = new double[n];
            for (int i = 0; i < n; i++) {
                p[i] = freq[i] / total;
                prob[i] = p[i] * n;
            }
            int[] alias = new int[n];
            // -1 marks "never assigned"; a correct table never consults such a slot
            Arrays.fill(alias, -1);

            SamplingTools.makeAliasWR(p, prob, alias);

            for (int i = 0; i < n; i++) {
                assertTrue(prob[i] >= 1.0 || alias[i] >= 0,
                        "column " + i + " of " + n + " has prob=" + prob[i] + " but no alias (iteration " + it + ")");
                checked++;
            }
        }
        assertTrue(checked > 100000, "expected a substantial number of columns to be checked, got " + checked);
    }

    @Test
    void weightedWorAcceptsEmptyAndFullSampleSizes() {
        Random random = new Random(42);
        double[] freq = {1, 2, 3};

        assertArrayEquals(new int[0], SamplingTools.sampleWeightedWOR(random, 0, freq));
        assertArrayEquals(new int[] {0, 1, 2}, SamplingTools.sampleWeightedWOR(random, 3, freq));

        var ex = assertThrows(IllegalArgumentException.class, () -> SamplingTools.sampleWeightedWOR(random, -1, freq));
        assertEquals("Sample size must not be negative, not -1.", ex.getMessage());
    }

    /**
     * A zero frequency means "never draw this index". It must hold whichever positions the zeros occupy.
     */
    @Test
    void weightedWorSkipsZeroFrequenciesWhilePositiveOnesRemain() {
        Random random = new Random(1);
        for (int it = 0; it < 2000; it++) {
            int[] tail = SamplingTools.sampleWeightedWOR(random, 3, new double[] {1, 1, 1, 0, 0, 0});
            Arrays.sort(tail);
            assertArrayEquals(new int[] {0, 1, 2}, tail);

            int[] head = SamplingTools.sampleWeightedWOR(random, 3, new double[] {0, 0, 0, 1, 1, 1});
            Arrays.sort(head);
            assertArrayEquals(new int[] {3, 4, 5}, head);

            int[] mixed = SamplingTools.sampleWeightedWOR(random, 2, new double[] {1, 0, 1, 0, 1, 0});
            assertEquals(2, mixed.length);
            for (int v : mixed) {
                assertTrue(v % 2 == 0, "drew a zero-frequency index " + v);
            }
            assertTrue(mixed[0] != mixed[1], "drew the same index twice");
        }
    }

    /**
     * When more places are requested than there are positive frequencies, the remainder has to be filled from
     * the zero-frequency indexes -- and uniformly at random. It used to be the same index every single time.
     */
    @Test
    void weightedWorFillsFromZeroFrequenciesUniformlyWhenForced() {
        Random random = new Random(7);
        int[] count = new int[6];
        int reps = 6000;
        for (int it = 0; it < reps; it++) {
            int[] sample = SamplingTools.sampleWeightedWOR(random, 4, new double[] {1, 1, 1, 0, 0, 0});
            assertEquals(4, sample.length);
            Set<Integer> distinct = new HashSet<>();
            for (int v : sample) {
                distinct.add(v);
                count[v]++;
            }
            assertEquals(4, distinct.size(), "sample without replacement has a repeated index");
            assertTrue(distinct.containsAll(Set.of(0, 1, 2)), "the positive frequencies must all be drawn");
        }
        for (int i = 0; i < 3; i++) {
            assertEquals(reps, count[i], "index " + i + " has positive frequency and must always be drawn");
        }
        double expected = reps / 3.0;
        for (int i = 3; i < 6; i++) {
            assertTrue(Math.abs(count[i] - expected) < 0.2 * expected,
                    "zero-frequency index " + i + " drawn " + count[i] + " times, expected about " + expected);
        }
    }

    /**
     * Guards the Efraimidis-Spirakis core against the refactoring above: the scheme is successive sampling
     * proportional to weight, so inclusion probabilities must match exact enumeration of draw orders.
     */
    @Test
    void weightedWorInclusionProbabilitiesMatchSuccessiveSampling() {
        double[] w = {1, 2, 3, 4, 5};
        int sampleSize = 2;
        int reps = 200000;
        int[] count = new int[w.length];
        Random random = new Random(99);
        for (int it = 0; it < reps; it++) {
            for (int v : SamplingTools.sampleWeightedWOR(random, sampleSize, w)) {
                count[v]++;
            }
        }
        double[] exact = exactInclusion(w, sampleSize);
        double sum = 0;
        for (int i = 0; i < w.length; i++) {
            double empirical = count[i] / (double) reps;
            sum += empirical;
            assertEquals(exact[i], empirical, 0.01, "inclusion probability of index " + i);
        }
        assertEquals(sampleSize, sum, 1e-9);
    }

    @Test
    void samplersRejectInvalidSizes() {
        Random random = new Random(1);

        assertEquals("Population size must be strict positive, not 0.",
                assertThrows(IllegalArgumentException.class, () -> SamplingTools.sampleWR(random, 0, 5)).getMessage());
        assertEquals("Population size must be strict positive, not -1.",
                assertThrows(IllegalArgumentException.class, () -> SamplingTools.sampleWR(random, -1, 2)).getMessage());
        assertEquals("Sample size must not be negative, not -1.",
                assertThrows(IllegalArgumentException.class, () -> SamplingTools.sampleWR(random, 5, -1)).getMessage());
        assertEquals("Sample size must not be negative, not -1.",
                assertThrows(IllegalArgumentException.class, () -> SamplingTools.sampleWOR(random, 5, -1)).getMessage());
        assertEquals("Sample size must not be negative, not -3.",
                assertThrows(IllegalArgumentException.class,
                        () -> SamplingTools.sampleWeightedWR(random, -3, new double[] {1, 1})).getMessage());

        // valid boundary: an empty sample of a non-empty population
        assertEquals(0, SamplingTools.sampleWR(random, 4, 0).length);
        assertEquals(0, SamplingTools.sampleWOR(random, 4, 0).length);
        assertEquals(0, SamplingTools.sampleWeightedWR(random, 0, new double[] {1, 1}).length);
    }

    @Test
    void frequenciesMustBeFinite() {
        Random random = new Random(1);
        for (double bad : new double[] {Double.NaN, Double.POSITIVE_INFINITY}) {
            assertEquals("Frequencies must be finite numbers, not " + bad + ".",
                    assertThrows(IllegalArgumentException.class,
                            () -> SamplingTools.sampleWeightedWR(random, 3, new double[] {bad, 1})).getMessage());
            assertEquals("Frequencies must be finite numbers, not " + bad + ".",
                    assertThrows(IllegalArgumentException.class,
                            () -> SamplingTools.sampleWeightedWOR(random, 1, new double[] {1, bad})).getMessage());
        }
    }

    /**
     * Slices used to be truncated individually with the whole rounding remainder dumped into the last one:
     * five slices of {0.19, 0.19, 0.19, 0.19, 0.24} over ten rows gave sizes 1, 1, 1, 1, 6.
     */
    @Test
    void sliceSizesFollowTheRequestedProportions() {
        Frame df = SolidFrame.byVars(VarDouble.seq(9).name("x"));
        assertEquals(10, df.rowCount());

        double[] freq = {0.19, 0.19, 0.19, 0.19, 0.24};
        Frame[] parts = SamplingTools.randomSampleSlices(new Random(7), df, freq);

        double total = Arrays.stream(freq).sum();
        for (int i = 0; i < freq.length; i++) {
            double exact = freq[i] / total * df.rowCount();
            assertTrue(Math.abs(parts[i].rowCount() - exact) <= 1,
                    "slice " + i + " has " + parts[i].rowCount() + " rows, requested " + exact);
        }
        assertEquals(10, Arrays.stream(parts).mapToInt(Frame::rowCount).sum());
    }

    @Test
    void slicesPartitionTheRowsExactlyOnce() {
        Frame df = SolidFrame.byVars(VarDouble.seq(999).name("x"));
        Random random = new Random(11);
        for (double[] freq : new double[][] {{1, 1, 1}, {0.05, 0.9, 0.05}, {1, 1, 1, 1, 1, 1, 1}}) {
            Frame[] parts = SamplingTools.randomSampleSlices(random, df, freq);
            Set<Double> seen = new HashSet<>();
            for (Frame part : parts) {
                for (int row = 0; row < part.rowCount(); row++) {
                    assertTrue(seen.add(part.getDouble(row, "x")), "row appears in two slices");
                }
            }
            assertEquals(df.rowCount(), seen.size());

            double total = Arrays.stream(freq).sum();
            for (int i = 0; i < freq.length; i++) {
                double exact = freq[i] / total * df.rowCount();
                assertTrue(Math.abs(parts[i].rowCount() - exact) <= 1,
                        "slice " + i + " has " + parts[i].rowCount() + " rows, requested " + exact);
            }
        }
    }

    @Test
    void slicesWorkWithFewerRowsThanSlices() {
        Frame df = SolidFrame.byVars(VarDouble.seq(1).name("x"));
        assertEquals(2, df.rowCount());

        Frame[] parts = SamplingTools.randomSampleSlices(new Random(3), df, 1, 1, 1);
        assertEquals(3, parts.length);
        assertEquals(2, Arrays.stream(parts).mapToInt(Frame::rowCount).sum());
        for (Frame part : parts) {
            assertTrue(part.rowCount() >= 0 && part.rowCount() <= 1);
        }
    }

    /**
     * Inclusion probabilities of the Efraimidis-Spirakis scheme, computed by enumerating every draw order.
     */
    private static double[] exactInclusion(double[] w, int sampleSize) {
        double[] result = new double[w.length];
        enumerate(w, sampleSize, new boolean[w.length], 1.0, result, 0);
        return result;
    }

    private static void enumerate(double[] w, int sampleSize, boolean[] taken, double prob, double[] result, int depth) {
        if (depth == sampleSize) {
            for (int i = 0; i < w.length; i++) {
                if (taken[i]) {
                    result[i] += prob;
                }
            }
            return;
        }
        double remaining = 0;
        for (int i = 0; i < w.length; i++) {
            if (!taken[i]) {
                remaining += w[i];
            }
        }
        for (int i = 0; i < w.length; i++) {
            if (!taken[i]) {
                taken[i] = true;
                enumerate(w, sampleSize, taken, prob * w[i] / remaining, result, depth + 1);
                taken[i] = false;
            }
        }
    }
}
