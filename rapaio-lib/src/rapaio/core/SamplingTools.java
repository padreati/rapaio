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

import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.util.collection.Ints;

/**
 * Sampling utilities.
 * <p>
 * Every sampler comes in two flavours: one which takes a {@link Random} as its first argument, and one
 * without it which draws from a fresh {@code new Random()}. Only the former is reproducible. The remaining
 * arguments describe the population, either as a size ({@code populationSize}) for the uniform samplers or as
 * an array of non-negative frequencies ({@code freq}) for the weighted ones, which carries the population size
 * in its length; in both cases the returned array holds {@code sampleSize} indexes into that population.
 * <p>
 * Frequencies do not need to sum to one: they are normalized internally, and the caller's array is never
 * modified. A frequency array must be non-empty, free of negative and non-finite values, and have a strictly
 * positive sum.
 */
public final class SamplingTools {

    private SamplingTools() {
    }

    /**
     * Discrete sampling with repetition.
     * Nothing special, just using the uniform discrete sampler offered by the system.
     */
    public static int[] sampleWR(final int populationSize, int sampleSize) {
        return sampleWR(new Random(), populationSize, sampleSize);
    }


    /**
     * Discrete sampling with repetition. Nothing special, just using the uniform discrete sampler offered by the system.
     * For a deterministic sampling use random parameter with a given seed.
     *
     * @param populationSize population size, must be strictly positive
     * @param sampleSize     sample size, must not be negative
     */
    public static int[] sampleWR(final Random random, final int populationSize, int sampleSize) {
        if (populationSize <= 0) {
            throw new IllegalArgumentException("Population size must be strict positive, not " + populationSize + ".");
        }
        checkSampleSize(sampleSize);
        int[] sample = new int[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            sample[i] = random.nextInt(populationSize);
        }
        return sample;
    }

    private static void checkSampleSize(int sampleSize) {
        if (sampleSize < 0) {
            throw new IllegalArgumentException("Sample size must not be negative, not " + sampleSize + ".");
        }
    }

    /**
     * Draws uniform discrete sample without replacement.
     * <p>
     * Implements reservoir sampling.
     *
     * @param populationSize population size
     * @param sampleSize     sample size
     * @return sampling indexes
     */
    public static int[] sampleWOR(final int populationSize, final int sampleSize) {
        return sampleWOR(new Random(), populationSize, sampleSize);
    }

    /**
     * Draws uniform discrete sample without replacement.
     * <p>
     * Implements reservoir sampling.
     *
     * @param populationSize population size
     * @param sampleSize     sample size
     * @return sampling indexes
     */
    public static int[] sampleWOR(final Random random, final int populationSize, final int sampleSize) {
        checkSampleSize(sampleSize);
        if (sampleSize > populationSize) {
            throw new IllegalArgumentException("Can't draw a sample without replacement bigger than population size.");
        }
        int[] sample = new int[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            sample[i] = i;
        }
        for (int i = sampleSize; i > 1; i--) {
            int j = random.nextInt(i);
            int tmp = sample[i - 1];
            sample[i - 1] = sample[j];
            sample[j] = tmp;
        }

        for (int i = sampleSize; i < populationSize; i++) {
            int j = random.nextInt(i + 1);
            if (j < sampleSize) {
                sample[j] = i;
            }
        }
        return sample;
    }

    /**
     * Generate discrete weighted random samples with replacement (same values might occur)
     * with building aliases according to the new probabilities.
     * <p>
     * Implementation based on Vose alias-method algorithm
     *
     * @param sampleSize sample size
     * @param freq       sampling probabilities
     * @return sampling indexes
     */
    public static int[] sampleWeightedWR(final int sampleSize, final double[] freq) {
        return sampleWeightedWR(new Random(), sampleSize, freq);
    }

    /**
     * Generate discrete weighted random samples with replacement (same values might occur)
     * with building aliases according to the new probabilities.
     * <p>
     * Implementation based on Vose alias-method algorithm
     *
     * @param sampleSize sample size
     * @param freq       sampling probabilities
     * @return sampling indexes
     */
    public static int[] sampleWeightedWR(final Random random, final int sampleSize, final double[] freq) {

        checkSampleSize(sampleSize);
        double[] p = normalized(freq);

        double[] prob = Arrays.copyOf(p, p.length);
        for (int i = 0; i < prob.length; i++) {
            prob[i] *= prob.length;
        }
        int[] alias = new int[p.length];

        makeAliasWR(p, prob, alias);

        int[] sample = new int[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            int column = random.nextInt(prob.length);
            sample[i] = random.nextDouble() < prob[column] ? column : alias[column];
        }
        return sample;
    }

    /**
     * Draw m <= n weighted random samples, weight by probabilities
     * without replacement.
     * <p>
     * Weighted random sampling without replacement.
     * Implements Efraimidis-Spirakis method.
     *
     * @param sampleSize number of samples
     * @param freq       var of probabilities
     * @return sampling indexes
     * @see "http://link.springer.com/content/pdf/10.1007/978-0-387-30162-4_478.pdf"
     */
    public static int[] sampleWeightedWOR(final int sampleSize, final double[] freq) {
        return sampleWeightedWOR(new Random(), sampleSize, freq);
    }

    /**
     * Draw m <= n weighted random samples, weight by probabilities
     * without replacement.
     * <p>
     * Weighted random sampling without replacement.
     * Implements Efraimidis-Spirakis method.
     * <p>
     * An index with zero frequency is never drawn while an index with a positive frequency is still available.
     * If the requested sample size exceeds the number of positive frequencies, all of those are included and the
     * remaining places are filled uniformly at random from among the zero-frequency indexes.
     *
     * @param sampleSize number of samples, in {@code [0, freq.length]}
     * @param freq       var of probabilities
     * @return sampling indexes
     * @see "http://link.springer.com/content/pdf/10.1007/978-0-387-30162-4_478.pdf"
     */
    public static int[] sampleWeightedWOR(final Random random, final int sampleSize, final double[] freq) {
        // validation
        checkSampleSize(sampleSize);
        double[] p = normalized(freq);
        if (sampleSize > p.length) {
            throw new IllegalArgumentException("Required sample size is bigger than population size.");
        }

        if (sampleSize == 0) {
            return new int[0];
        }
        if (sampleSize == p.length) {
            return Ints.seq(0, sampleSize);
        }

        // separate the indexes which can be drawn from those with zero frequency, so that the search below
        // only ever sees strictly positive weights (a zero weight would make its key pow(u, +Inf))
        int[] positive = new int[p.length];
        int[] zero = new int[p.length];
        int positiveCount = 0;
        int zeroCount = 0;
        for (int i = 0; i < p.length; i++) {
            if (p[i] > 0) {
                positive[positiveCount++] = i;
            } else {
                zero[zeroCount++] = i;
            }
        }

        if (zeroCount == 0) {
            return efraimidisSpirakis(random, sampleSize, p);
        }
        if (sampleSize == positiveCount) {
            return Arrays.copyOf(positive, positiveCount);
        }
        if (sampleSize < positiveCount) {
            double[] positiveP = new double[positiveCount];
            for (int i = 0; i < positiveCount; i++) {
                positiveP[i] = p[positive[i]];
            }
            int[] local = efraimidisSpirakis(random, sampleSize, normalized(positiveP));
            int[] result = new int[sampleSize];
            for (int i = 0; i < sampleSize; i++) {
                result[i] = positive[local[i]];
            }
            return result;
        }

        // more places than indexes with positive frequency: keep all of them, fill the rest uniformly
        int[] result = new int[sampleSize];
        System.arraycopy(positive, 0, result, 0, positiveCount);
        int[] fill = sampleWOR(random, zeroCount, sampleSize - positiveCount);
        for (int i = 0; i < fill.length; i++) {
            result[positiveCount + i] = zero[fill[i]];
        }
        return result;
    }

    /**
     * Efraimidis-Spirakis weighted sampling without replacement, with the exponential jump optimization.
     * Requires {@code 0 < sampleSize < p.length}, all {@code p[i] > 0} and {@code sum(p) == 1}.
     */
    private static int[] efraimidisSpirakis(final Random random, final int sampleSize, final double[] p) {

        int[] result = new int[sampleSize];

        int len = 1;
        while (len <= sampleSize) {
            len *= 2;
        }
        len = len * 2;

        int[] heap = new int[len];
        double[] k = new double[sampleSize];

        // fill with invalid ids
        for (int i = 0; i < len; i++) {
            heap[i] = -1;
        }
        // fill heap base
        for (int i = 0; i < sampleSize; i++) {
            heap[i + len / 2] = i;
            k[i] = pow(random.nextDouble(), 1. / p[i]);
            result[i] = i;
        }

        // learn heap
        for (int i = len / 2 - 1; i > 0; i--) {
            if (heap[i * 2] == -1) {
                heap[i] = -1;
                continue;
            }
            if (heap[i * 2 + 1] == -1) {
                heap[i] = heap[i * 2];
                continue;
            }
            if (k[heap[i * 2]] < k[heap[i * 2 + 1]]) {
                heap[i] = heap[i * 2];
            } else {
                heap[i] = heap[i * 2 + 1];
            }
        }

        // exhaust the source
        int pos = sampleSize;
        while (pos < p.length) {
            double r = random.nextDouble();
            double xw = log(r) / log(k[heap[1]]);

            double acc = 0;
            while (pos < p.length) {
                if (acc + p[pos] < xw) {
                    acc += p[pos];
                    pos++;
                    continue;
                }
                break;
            }
            if (pos == p.length) {
                break;
            }

            // min replaced with the new selected value
            double tw = pow(k[heap[1]], p[pos]);
            double r2 = random.nextDouble() * (1. - tw) + tw;
            double ki = pow(r2, 1 / p[pos]);

            k[heap[1]] = ki;
            result[heap[1]] = pos++;
            int start = heap[1] + len / 2;
            while (start > 1) {
                start /= 2;
                if (heap[start * 2 + 1] == -1) {
                    heap[start] = heap[start * 2];
                    continue;
                }
                if (k[heap[start * 2]] < k[heap[start * 2 + 1]]) {
                    heap[start] = heap[start * 2];
                } else {
                    heap[start] = heap[start * 2 + 1];
                }
            }
        }
        return result;
    }

    /**
     * Returns a normalized copy of the given frequencies (they sum to one). The caller's array is not modified.
     * <p>
     * Frequencies must be non-negative and finite, and their sum strictly positive; individual zero frequencies
     * are allowed and mean the corresponding index is never drawn.
     */
    private static double[] normalized(double[] freq) {
        if (freq == null) {
            throw new IllegalArgumentException("Sampling probability array cannot be null.");
        }
        double total = 0;
        for (double p : freq) {
            if (p < 0) {
                throw new IllegalArgumentException("Frequencies must be positive.");
            }
            if (!Double.isFinite(p)) {
                throw new IllegalArgumentException("Frequencies must be finite numbers, not " + p + ".");
            }
            total += p;
        }
        if (total <= 0) {
            throw new IllegalArgumentException("Sum of frequencies must be strict positive.");
        }
        double[] result = Arrays.copyOf(freq, freq.length);
        for (int i = 0; i < result.length; i++) {
            result[i] /= total;
        }
        return result;
    }

    /**
     * Builds the Vose alias table for sampling with replacement. On return every column {@code c} either has
     * {@code prob[c] >= 1} (it is always accepted, and {@code alias[c]} is never consulted) or has been paired
     * with a column whose index is stored in {@code alias[c]}.
     * <p>
     * Package-private so that invariant can be asserted directly by the tests.
     */
    static void makeAliasWR(double[] p, double[] prob, int[] alias) {
        if (p.length == 0) {
            throw new IllegalArgumentException("Probability var must be nonempty.");
        }

        int[] dq = new int[p.length];
        int smallPos = -1;
        int largePos = prob.length;

        for (int i = 0; i < prob.length; ++i) {
            if (prob[i] >= 1.) {
                dq[largePos - 1] = i;
                largePos--;
            } else {
                dq[smallPos + 1] = i;
                smallPos++;
            }
        }

        while (smallPos >= 0 && largePos <= p.length - 1) {
            int small = dq[smallPos--];
            int large = dq[largePos++];

            alias[small] = large;
            prob[large] = prob[large] + prob[small] - 1.;

            if (prob[large] >= 1.0) {
                dq[largePos - 1] = large;
                largePos--;
            } else {
                dq[smallPos + 1] = large;
                smallPos++;
            }
        }

        while (smallPos >= 0) {
            prob[dq[smallPos--]] = 1.0;
        }
        while (largePos < dq.length) {
            prob[dq[largePos]] = 1.0;
            largePos++;
        }
    }

    /**
     * Splits the rows of a frame into {@code freq.length} random disjoint slices whose sizes follow the
     * normalized frequencies. Cut points are placed at the rounded cumulative proportions, so every slice is
     * within one row of its requested share and the rounding remainder is not accumulated into the last one.
     */
    public static Frame[] randomSampleSlices(Frame frame, double... freq) {
        return randomSampleSlices(new Random(), frame, freq);
    }

    /**
     * Splits the rows of a frame into {@code freq.length} random disjoint slices whose sizes follow the
     * normalized frequencies. Cut points are placed at the rounded cumulative proportions, so every slice is
     * within one row of its requested share and the rounding remainder is not accumulated into the last one.
     */
    public static Frame[] randomSampleSlices(final Random random, Frame frame, double... freq) {
        double[] p = normalized(freq);
        int rowCount = frame.rowCount();
        int[] rows = Ints.seq(0, rowCount);
        Ints.shuffle(rows, random);

        Frame[] result = new Frame[p.length];
        double cumulative = 0;
        int start = 0;
        for (int i = 0; i < p.length; i++) {
            cumulative += p[i];
            // the last slice takes whatever is left, so that the slices always cover every row exactly once
            int end = i == p.length - 1 ? rowCount : Math.clamp(Math.round(cumulative * rowCount), start, rowCount);
            result[i] = frame.mapRows(Arrays.copyOfRange(rows, start, end));
            start = end;
        }
        return result;
    }

    public static Frame[] randomSampleStratifiedSplit(Random random, Frame df, String strataName, double... freq) {
        Mapping[] maps = getMappingsForStratifiedSplit(random, df, strataName, freq);

        Frame[] list = new Frame[freq.length];
        for (int i = 0; i < freq.length; i++) {
            list[i] = df.mapRows(maps[i]);
        }
        return list;
    }

    /**
     * Splits the rows of a frame into {@code freq.length} mappings so that every stratum (level of the
     * strata variable) is divided among the mappings in the requested proportions.
     * <p>
     * Within each stratum the rows are shuffled and then cut at the cumulative proportions, so mapping
     * {@code i} receives approximately {@code freq[i]} of every stratum (rounding is handled by placing the
     * cut points at the nearest row); a stratum with fewer rows than mappings contributes to the first mappings
     * in proportion order. Rows with missing strata are ignored.
     */
    private static Mapping[] getMappingsForStratifiedSplit(Random random, Frame df, String strataName, double[] freq) {
        double[] p = normalized(freq);
        double[] cumulative = new double[p.length];
        double acc = 0;
        for (int i = 0; i < p.length; i++) {
            acc += p[i];
            cumulative[i] = acc;
        }
        cumulative[p.length - 1] = 1.0;

        Var strata = df.rvar(strataName);
        List<Mapping> groups = new ArrayList<>();
        for (int i = 0; i < strata.levels().size(); i++) {
            groups.add(Mapping.empty());
        }
        for (int row = 0; row < df.rowCount(); row++) {
            if (!strata.isMissing(row)) {
                groups.get(strata.getInt(row)).add(row);
            }
        }

        Mapping[] maps = new Mapping[p.length];
        for (int i = 0; i < p.length; i++) {
            maps[i] = Mapping.empty();
        }
        for (Mapping group : groups) {
            group.shuffle(random);
            int size = group.size();
            int start = 0;
            for (int i = 0; i < p.length; i++) {
                int end = (int) Math.round(cumulative[i] * size);
                for (int j = start; j < end; j++) {
                    maps[i].add(group.get(j));
                }
                start = end;
            }
        }
        for (int i = 0; i < p.length; i++) {
            maps[i].shuffle(random);
        }
        return maps;
    }

    public record TrainTestSplit(Frame trainDf, Var trainW, Frame testDf, Var testW) {
    }

    public static TrainTestSplit trainTestSplit(Frame df, double p) {
        return trainTestSplit(df, null, p, true, null);
    }

    public static TrainTestSplit trainTestSplit(Random random, Frame df, double p) {
        return trainTestSplit(random, df, null, p, true, null);
    }

    public static TrainTestSplit trainTestSplit(Frame df, Var w, double p) {
        return trainTestSplit(df, w, p, true, null);
    }

    public static TrainTestSplit trainTestSplit(Frame df, Var w, double p, boolean shuffle) {
        return trainTestSplit(df, w, p, shuffle, null);
    }

    public static TrainTestSplit trainTestSplit(Frame df, Var w, double p, boolean shuffle, String strata) {
        return trainTestSplit(new Random(), df, w, p, shuffle, strata);
    }

    public static TrainTestSplit trainTestSplit(final Random random, Frame df, Var w, double p, boolean shuffle, String strata) {

        int trainSize = (int) (df.rowCount() * p);
        int testSize = df.rowCount() - trainSize;

        if (w == null) {
            w = VarDouble.fill(df.rowCount(), 1);
        }

        if (strata == null) {
            int[] rows = Ints.seq(0, df.rowCount());
            if (shuffle) {
                Ints.shuffle(rows, random);
            }
            var trainMapping = Mapping.wrap(Ints.copyOf(rows, 0, trainSize));
            var testMapping = Mapping.wrap(Ints.copyOf(rows, trainSize, testSize));
            return new TrainTestSplit(df.mapRows(trainMapping), w.mapRows(trainMapping), df.mapRows(testMapping), w.mapRows(testMapping));
        }

        var mappings = getMappingsForStratifiedSplit(random, df, strata, new double[] {p, 1 - p});
        return new TrainTestSplit(df.mapRows(mappings[0]), w.mapRows(mappings[0]), df.mapRows(mappings[1]), w.mapRows(mappings[1]));
    }
}
