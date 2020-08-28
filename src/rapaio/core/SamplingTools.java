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

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import rapaio.data.Frame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarInt;
import rapaio.util.collection.IArrays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public final class SamplingTools {

    private SamplingTools() {
    }

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
            throw new IllegalArgumentException("Required sample size is bigger than population size.");
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
            k[i] = Math.pow(RandomSource.nextDouble(), 1. / freq[i]);
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
            double r = RandomSource.nextDouble();
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
            double r2 = RandomSource.nextDouble() * (1. - tw) + tw;
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
            throw new IllegalArgumentException("Sampling probability array cannot be null.");
        }
        double total = 0;
        for (double p : freq) {
            if (p < 0) {
                throw new IllegalArgumentException("Frequencies must be positive.");
            }
            total += p;
        }
        if (total <= 0) {
            throw new IllegalArgumentException("Sum of frequencies must be strict positive.");
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

    public static Frame[] randomSampleSlices(Frame frame, double... freq) {
        normalize(freq);
        int[] rows = new int[frame.rowCount()];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = i;
        }
        it.unimi.dsi.fastutil.ints.IntArrays.shuffle(rows, RandomSource.getRandom());

        Frame[] result = new Frame[freq.length];
        int start = 0;
        for (int i = 0; i < freq.length; i++) {
            int len = (int) (freq[i] * frame.rowCount());
            if (i == freq.length - 1) {
                len = frame.rowCount() - start;
            }
            result[i] = frame.mapRows(Arrays.copyOfRange(rows, start, start + len));
            start += len;
        }
        return result;
    }

    public static Frame[] randomSampleStratifiedSplit(Frame df, String strataName, double... freq) {
        Mapping[] maps = getMappingsForStratifiedSplit(df, strataName, freq);

        Frame[] list = new Frame[freq.length];
        for (int i = 0; i < freq.length; i++) {
            list[i] = df.mapRows(maps[i]);
        }
        return list;
    }

    private static Mapping[] getMappingsForStratifiedSplit(Frame df, String strataName, double[] freq) {
        normalize(freq);
        List<Mapping> groups = new ArrayList<>();
        for (int i = 0; i < df.levels(strataName).size(); i++) {
            groups.add(Mapping.empty());
        }
        df.rvar(strataName).stream().forEach(s -> groups.get(s.getInt()).add(s.row()));

        Mapping[] maps = new Mapping[freq.length];
        for (int i = 0; i < freq.length; i++) {
            maps[i] = Mapping.empty();
        }

        int mapPos = 0;
        for (Mapping group : groups) {
            group.shuffle();
            IntIterator it = group.iterator();
            while (it.hasNext()) {
                maps[mapPos++].add(it.nextInt());
                if (mapPos == freq.length) {
                    mapPos = 0;
                }
            }
        }
        for (int i = 0; i < freq.length; i++) {
            maps[i].shuffle();
        }
        return maps;
    }

    public static class TrainTestSplit {

        public final Frame trainDf;
        public final Var trainW;

        public final Frame testDf;
        public final Var testW;

        public TrainTestSplit(Frame trainDf, Var trainW, Frame testDf, Var testW) {
            this.trainDf = trainDf;
            this.trainW = trainW;
            this.testDf = testDf;
            this.testW = testW;
        }
    }

    public static TrainTestSplit trainTestSplit(Frame df, double p) {
        return trainTestSplit(df, null, p, true, null);
    }

    public static TrainTestSplit trainTestSplit(Frame df, Var w, double p) {
        return trainTestSplit(df, w, p, true, null);
    }

    public static TrainTestSplit trainTestSplit(Frame df, Var w, double p, boolean shuffle) {
        return trainTestSplit(df, w, p, shuffle, null);
    }

    public static TrainTestSplit trainTestSplit(Frame df, Var w, double p, boolean shuffle, String strata) {

        int trainSize = (int) (df.rowCount() * p);
        int testSize = df.rowCount() - trainSize;

        if (w == null) {
            w = VarInt.seq(df.rowCount());
        }

        if (strata == null) {
            int[] rows = IArrays.newSeq(0, df.rowCount());
            if (shuffle) {
                IntArrays.shuffle(rows, RandomSource.getRandom());
            }
            var trainMapping = Mapping.wrap(IArrays.newCopy(rows, 0, trainSize));
            var testMapping = Mapping.wrap(IArrays.newCopy(rows, trainSize, testSize));
            return new TrainTestSplit(df.mapRows(trainMapping), w.mapRows(trainMapping), df.mapRows(testMapping), w.mapRows(testMapping));
        }

        var mappings = getMappingsForStratifiedSplit(df, strata, new double[]{p, 1 - p});
        return new TrainTestSplit(df.mapRows(mappings[0]), w.mapRows(mappings[0]), df.mapRows(mappings[1]), w.mapRows(mappings[1]));
    }
}
