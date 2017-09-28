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

import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static rapaio.core.RandomSource.nextDouble;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public final class SamplingTools {

    /**
     * Discrete sampling with repetition.
     * Nothing special, just using the uniform discrete sampler offered by the system.
     */
    public static int[] sampleWR(final int populationSize, int sampleSize) {
        int[] sample = new int[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            sample[i] = RandomSource.nextInt(populationSize);
        }
        return sample;
    }

    /**
     * Draws an uniform discrete sample without replacement.
     * <p>
     * Implements reservoir sampling.
     *
     * @param populationSize population size
     * @param sampleSizes     sample size
     * @return sampling indexes
     */
    public static int[][] multiSampleWOR(final int populationSize, final int... sampleSizes) {
        int total = Arrays.stream(sampleSizes).sum();
        int[] sample = sampleWOR(populationSize, total);
        int[][] result = new int[sampleSizes.length][];
        int start = 0;
        for (int i = 0; i < sampleSizes.length; i++) {
            result[i] = new int[sampleSizes[i]];
            System.arraycopy(sample, start, result[i], 0, result[i].length);
            start += result[i].length;
        }
        return result;
    }

    /**
     * Draws an uniform discrete sample without replacement.
     * <p>
     * Implements reservoir sampling.
     *
     * @param populationSize population size
     * @param sampleSize     sample size
     * @return sampling indexes
     */
    public static int[] sampleWOR(final int populationSize, final int sampleSize) {
        if (sampleSize > populationSize) {
            throw new IllegalArgumentException("Can't draw a sample without replacement bigger than population size.");
        }
        int[] sample = new int[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            sample[i] = i;
        }
        for (int i = sampleSize; i > 1; i--) {
            int j = RandomSource.nextInt(i);
            int tmp = sample[i - 1];
            sample[i - 1] = sample[j];
            sample[j] = tmp;
        }

        for (int i = sampleSize; i < populationSize; i++) {
            int j = RandomSource.nextInt(i + 1);
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

        normalize(freq);

        double[] prob = Arrays.copyOf(freq, freq.length);
        for (int i = 0; i < prob.length; i++) {
            prob[i] *= prob.length;
        }
        int[] alias = new int[freq.length];

        makeAliasWR(freq, prob, alias);

        int[] sample = new int[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            int column = RandomSource.nextInt(prob.length);
            sample[i] = RandomSource.nextDouble() < prob[column] ? column : alias[column];
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
        // validation
        if (sampleSize > freq.length) {
            throw new IllegalArgumentException("required sample size is bigger than population size");
        }

        normalize(freq);

        int[] result = new int[sampleSize];

        if (sampleSize == freq.length) {
            for (int i = 0; i < freq.length; i++) {
                result[i] = i;
            }
            return result;
        }

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
            k[i] = Math.pow(nextDouble(), 1. / freq[i]);
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
        while (pos < freq.length) {
            double r = nextDouble();
            double xw = Math.log(r) / Math.log(k[heap[1]]);

            double acc = 0;
            while (pos < freq.length) {
                if (acc + freq[pos] < xw) {
                    acc += freq[pos];
                    pos++;
                    continue;
                }
                break;
            }
            if (pos == freq.length) break;

            // min replaced with the new selected value
            double tw = Math.pow(k[heap[1]], freq[pos]);
            double r2 = nextDouble() * (1. - tw) + tw;
            double ki = Math.pow(r2, 1 / freq[pos]);

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

    private static void normalize(double[] freq) {

        if (freq == null) {
            throw new IllegalArgumentException("sampling probability array cannot be null");
        }

        double total = 0;
        for (double p : freq) {
            if (p < 0) {
                throw new IllegalArgumentException("frequencies must be positive.");
            }
            total += p;
        }
        if (total <= 0) {
            throw new IllegalArgumentException("sum of frequencies must be strict positive");
        }
        if (total != 1.0) {
            for (int i = 0; i < freq.length; i++) {
                freq[i] /= total;
            }
        }
    }

    /**
     * Builds discrete random sampler without replacement
     */
    private static void makeAliasWR(double[] p, double[] prob, int[] alias) {
        if (p.length == 0)
            throw new IllegalArgumentException("Probability var must be nonempty.");


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

        while (smallPos > 0) {
            prob[dq[smallPos - 1]] = 1.0;
            smallPos--;
        }
        while (largePos < dq.length) {
            prob[dq[largePos]] = 1.0;
            largePos++;
        }
    }

    public static List<Frame> randomSampleSlices(Frame frame, double... freq) {
        int total = 0;
        for (double f : freq) {
            total += (int) (f * frame.rowCount());
        }
        if (total > frame.rowCount()) {
            throw new IllegalArgumentException("total counts greater than available number of rows");
        }
        List<Frame> result = new ArrayList<>();

        List<Integer> rows = IntStream.range(0, frame.rowCount()).mapToObj(i -> i).collect(Collectors.toList());
        Collections.shuffle(rows, RandomSource.getRandom());

        int start = 0;
        for (double f : freq) {
            int len = (int) (f * frame.rowCount());
            result.add(frame.mapRows(Mapping.copy(rows.subList(start, start + len))));
            start += len;
        }
        if (start < frame.rowCount()) {
            result.add(frame.mapRows(Mapping.copy(rows.subList(start, frame.rowCount()))));
        }
        return result;
    }

    public static List<Frame> randomSampleStratifiedSplit(Frame df, String strataName, double p) {
        if (p <= 0 || p >= 1) {
            throw new IllegalArgumentException("Percentage must be in interval (0, 1)");
        }
        List<List<Integer>> maps = new ArrayList<>();
        for (int i = 0; i < df.var(strataName).levels().length; i++) {
            maps.add(new ArrayList<>());
        }
        df.var(strataName).stream().forEach(s -> maps.get(s.index()).add(s.row()));
        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();
        for (List<Integer> map : maps) {
            Collections.shuffle(map, RandomSource.getRandom());
            left.addAll(map.subList(0, (int) (p * map.size())));
            right.addAll(map.subList((int) (p * map.size()), map.size()));
        }
        Collections.shuffle(left, RandomSource.getRandom());
        Collections.shuffle(right, RandomSource.getRandom());

        List<Frame> list = new ArrayList<>();
        list.add(df.mapRows(Mapping.wrap(left)));
        list.add(df.mapRows(Mapping.wrap(right)));
        return list;
    }

    public static Frame randomBootstrap(Frame frame) {
        return randomBootstrap(frame, 1.0);
    }

    public static Frame randomBootstrap(Frame frame, double percent) {
        return MappedFrame.byRow(frame, Mapping.copy(SamplingTools.sampleWR(frame.rowCount(), (int) (percent * frame.rowCount()))));
    }
}
