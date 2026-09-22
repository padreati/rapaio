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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.tests.ChiSqGoodnessOfFit;
import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.util.collection.Doubles;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SamplingToolsTest {

    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(123);
    }

    @Test
    void testSampleWR() {
        final int N = 1000;
        int[] sample = SamplingTools.sampleWR(random, 10, N);
        assertEquals(N, sample.length);
        for (int aSample : sample) {
            assertTrue(aSample >= 0);
            assertTrue(aSample < 10);
        }
        ChiSqGoodnessOfFit test = ChiSqGoodnessOfFit
                .from(VarNominal.from(sample.length, row -> String.valueOf(sample[row])),
                        VarDouble.fill(10, 0.1));
        assertTrue(test.pValue() > 0.05);
    }

    @Test
    void testInvalidSizeSamplingWOR() {
        var ex = assertThrows(IllegalArgumentException.class, () -> SamplingTools.sampleWOR(random, 10, 100));
        assertEquals("Can't draw a sample without replacement bigger than population size.", ex.getMessage());
    }

    @Test
    void testSamplingWOR() {
        final int TRIALS = 100_000;
        VarDouble v = VarDouble.empty();
        for (int next : SamplingTools.sampleWOR(random, TRIALS * 2, TRIALS)) {
            v.addDouble(next);
        }
        double[] values = v.elements();
        Doubles.quickSort(values, 0, v.size(), Double::compare);
        for (int i = 1; i < v.size(); i++) {
            assertTrue(values[i] - values[i - 1] >= 1);
        }
    }

    @Test
    void testSamplingWeightedWOR() {

        double[] w = new double[] {0.4, 0.3, 0.2, 0.06, 0.03, 0.01};
        var freq = DensityVector.emptyByLabels(w.length);

        final int TRIALS = 10_000;
        for (int i = 0; i < TRIALS; i++) {
            for (int next : SamplingTools.sampleWeightedWOR(6, w)) {
                freq.increment(next, 1);
            }
        }
        freq.normalize();
        for (int i = 0; i < 6; i++) {
            assertEquals(1.0 / 6, freq.get(i), 1e-20);
        }

        freq = DensityVector.emptyByLabels(w.length);
        for (int i = 0; i < TRIALS; i++) {
            for (int next : SamplingTools.sampleWeightedWOR(random, 1, w)) {
                freq.increment(next, 1);
            }
        }
        // the samplers no longer normalise the caller's array, so normalise the expected probabilities here
        double total = Doubles.sum(w, 0, w.length);
        ChiSqGoodnessOfFit test = ChiSqGoodnessOfFit.from(freq, VarDouble.from(w.length, i -> w[i] / total));
        assertTrue(test.pValue() > 0.05);
    }

    @Test
    void testInvalidNullProbWR() {
        var ex = assertThrows(IllegalArgumentException.class, () -> SamplingTools.sampleWeightedWR(random, 10, null));
        assertEquals("Sampling probability array cannot be null.", ex.getMessage());
    }

    @Test
    void testInvalidNegativeProbabilities() {
        var ex = assertThrows(IllegalArgumentException.class, () -> SamplingTools.sampleWeightedWR(random, 2, new double[] {-1, 1}));
        assertEquals("Frequencies must be positive.", ex.getMessage());
    }

    @Test
    void testInvalidSizeWeightedWOR() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> SamplingTools.sampleWeightedWOR(random, 20, new double[] {0.1, 0.2, 0.3, 0.4}));
        assertEquals("Required sample size is bigger than population size.", ex.getMessage());
    }

    @Test
    void testInvalidSumZeroWeightedWR() {
        var ex = assertThrows(IllegalArgumentException.class, () -> SamplingTools.sampleWeightedWR(random, 2, new double[] {0, 0}));
        assertEquals("Sum of frequencies must be strict positive.", ex.getMessage());
    }

    @Test
    void testSampleWeightedWR() {
        double[] w = new double[] {0.002, 0.018, 0.18, 1.8};
        var freq = DensityVector.emptyByLabels(w.length);
        final int TRIALS = 10_000;
        final int SAMPLES = 100;
        for (int i = 0; i < TRIALS; i++) {
            for (int next : SamplingTools.sampleWeightedWR(random, SAMPLES, w)) {
                freq.increment(next, 1);
            }
        }
        // the samplers no longer normalise the caller's array, so normalise the expected probabilities here
        double total = Doubles.sum(w, 0, w.length);
        ChiSqGoodnessOfFit test = ChiSqGoodnessOfFit.from(freq, VarDouble.from(w.length, i -> w[i] / total));
        assertTrue(test.pValue() > 0.05);
    }

    @Test
    void testRandomSampleSizes() {

        Frame df = SolidFrame.byVars(VarDouble.seq(100).name("x"));

        double[] freq = new double[] {0.127, 0.5, 0.333};
        Frame[] frames = SamplingTools.randomSampleSlices(random, df, freq);

        // slices are sized from the normalised frequencies; the caller's array itself is left untouched
        double freqTotal = Doubles.sum(freq, 0, freq.length);
        for (int i = 0; i < frames.length - 1; i++) {
            assertEquals(((int) (df.rowCount() * freq[i] / freqTotal)), frames[i].rowCount());
        }

        int total = 0;
        for (Frame frame : frames) {
            total += frame.rowCount();
        }
        assertEquals(df.rowCount(), total);
    }

    @Test
    void testRandomStratifiedSplit() {

        Frame df = SolidFrame.byVars(
                VarDouble.seq(100).name("x"),
                VarNominal.from(100, row -> String.valueOf(row % 3)).name("strata")
        );

        double[] p = new double[3];
        Arrays.fill(p, 1. / 3);
        Frame[] strata = SamplingTools.randomSampleStratifiedSplit(random, df, "strata", p);

        for (Frame st : strata) {
            ChiSqGoodnessOfFit test = ChiSqGoodnessOfFit.from(st.rvar("strata"), VarDouble.wrap(p));
            assertTrue(test.pValue() >= 0.9);
        }
    }

    /**
     * Regression: the stratified split used to deal rows round-robin, ignoring the requested proportions.
     * With 4 strata of 25 rows and proportions 0.8/0.2 every stratum must contribute 20 and 5 rows.
     */
    @Test
    void stratifiedSplitHonoursProportions() {
        Frame df = SolidFrame.byVars(
                VarDouble.from(100, row -> (double) row).name("x"),
                VarNominal.from(100, row -> "s" + (row % 4)).name("strata")
        );
        double[] p = new double[] {0.8, 0.2};
        Frame[] parts = SamplingTools.randomSampleStratifiedSplit(random, df, "strata", p);
        assertEquals(80, parts[0].rowCount());
        assertEquals(20, parts[1].rowCount());
        for (String level : df.rvar("strata").levels()) {
            assertEquals(20, parts[0].rvar("strata").stream().filter(s -> s.getLabel().equals(level)).count(), level);
            assertEquals(5, parts[1].rvar("strata").stream().filter(s -> s.getLabel().equals(level)).count(), level);
        }
        // every row exactly once
        assertEquals(100, parts[0].rowCount() + parts[1].rowCount());
        double sum = parts[0].rvar("x").darray_().sum() + parts[1].rvar("x").darray_().sum();
        assertEquals(df.rvar("x").darray_().sum(), sum, 1e-9);
    }

    /**
     * Frequencies passed by the caller must not be normalised in place.
     */
    @Test
    void frequenciesAreNotMutated() {
        double[] freq = new double[] {3, 1};
        Frame df = SolidFrame.byVars(VarDouble.from(40, row -> (double) row).name("x"), VarNominal.from(40, row -> "s" + (row % 2)).name("strata"));
        SamplingTools.randomSampleSlices(random, df, freq);
        SamplingTools.randomSampleStratifiedSplit(random, df, "strata", freq);
        SamplingTools.sampleWeightedWR(random, 10, freq);
        SamplingTools.sampleWeightedWOR(random, 1, freq);
        assertEquals(3.0, freq[0]);
        assertEquals(1.0, freq[1]);
    }

    /**
     * Regression: without explicit weights the train/test split handed out the row indices as weights.
     */
    @Test
    void trainTestSplitDefaultWeightsAreOne() {
        Frame df = SolidFrame.byVars(VarDouble.from(10, row -> (double) row).name("x"));
        SamplingTools.TrainTestSplit tts = SamplingTools.trainTestSplit(random, df, 0.7);
        assertEquals(7, tts.trainDf().rowCount());
        assertEquals(3, tts.testDf().rowCount());
        assertEquals(7, tts.trainW().size());
        assertEquals(3, tts.testW().size());
        for (int i = 0; i < tts.trainW().size(); i++) {
            assertEquals(1.0, tts.trainW().getDouble(i));
        }
        for (int i = 0; i < tts.testW().size(); i++) {
            assertEquals(1.0, tts.testW().getDouble(i));
        }
    }
}
