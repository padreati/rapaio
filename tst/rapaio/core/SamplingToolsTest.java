/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rapaio.core.tests.ChiSqGoodnessOfFit;
import rapaio.core.tools.DVector;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SamplingToolsTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    public void testSampleWR() {
        final int N = 1000;
        int[] sample = SamplingTools.sampleWR(10, N);
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
    public void testInvalidSizeSamplingWOR() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can't draw a sample without replacement bigger than population size.");
        SamplingTools.sampleWOR(10, 100);
    }

    @Test
    public void testSamplingWOR() {
        final int TRIALS = 100_000;
        VarDouble v = VarDouble.empty();
        for (int next : SamplingTools.sampleWOR(TRIALS * 2, TRIALS)) {
            v.addDouble(next);
        }
        double[] values = v.getDataAccessor().getData();
        DoubleArrays.quickSort(values, 0, v.rowCount());
        for (int i = 1; i < v.rowCount(); i++) {
            assertTrue(values[i] - values[i - 1] >= 1);
        }
    }

    @Test
    public void testSamplingWeightedWOR() {

        double[] w = new double[]{0.4, 0.3, 0.2, 0.06, 0.03, 0.01};
        DVector freq = DVector.empty(true, w.length);

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

        freq = DVector.empty(true, w.length);
        for (int i = 0; i < TRIALS; i++) {
            for (int next : SamplingTools.sampleWeightedWOR(1, w)) {
                freq.increment(next, 1);
            }
        }
        ChiSqGoodnessOfFit test = ChiSqGoodnessOfFit.from(freq, VarDouble.wrap(w));
        assertTrue(test.pValue() > 0.05);
    }

    @Test
    public void testInvalidNullProbWR() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Sampling probability array cannot be null.");
        SamplingTools.sampleWeightedWR(10, null);
    }

    @Test
    public void testInvalidNegativeProbabilities() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Frequencies must be positive.");
        SamplingTools.sampleWeightedWR(2, new double[]{-1, 1});
    }

    @Test
    public void testInvalidSizeWeightedWOR() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Required sample size is bigger than population size.");
        SamplingTools.sampleWeightedWOR(20, new double[]{0.1, 0.2, 0.3, 0.4});
    }

    @Test
    public void testInvalidSumZeroWeightedWR() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Sum of frequencies must be strict positive.");
        SamplingTools.sampleWeightedWR(2, new double[]{0, 0});
    }

    @Test
    public void testSampleWeightedWR() {
        double[] w = new double[]{0.002, 0.018, 0.18, 1.8};
        DVector freq = DVector.empty(true, w.length);
        final int TRIALS = 10_000;
        final int SAMPLES = 100;
        for (int i = 0; i < TRIALS; i++) {
            for (int next : SamplingTools.sampleWeightedWR(SAMPLES, w)) {
                freq.increment(next, 1);
            }
        }
        ChiSqGoodnessOfFit test = ChiSqGoodnessOfFit.from(freq, VarDouble.wrap(w));
        assertTrue(test.pValue() > 0.05);
    }

    @Test
    public void testRandomSampleSizes() {

        Frame df = SolidFrame.byVars(VarDouble.seq(100).withName("x"));

        double[] freq = new double[]{0.127, 0.5, 0.333};
        Frame[] frames = SamplingTools.randomSampleSlices(df, freq);

        for (int i = 0; i < frames.length - 1; i++) {
            assertEquals(((int) (100 * freq[i])), frames[i].rowCount());
        }

        int total = 0;
        for (Frame frame : frames) {
            total += frame.rowCount();
        }
        assertEquals(df.rowCount(), total);
    }

    @Test
    public void testRandomStratifiedSplit() {

        Frame df = SolidFrame.byVars(
                VarDouble.seq(100).withName("x"),
                VarNominal.from(100, row -> String.valueOf(row % 3)).withName("strata")
        );

        double[] p = new double[3];
        Arrays.fill(p, 1./3);
        Frame[] strata = SamplingTools.randomSampleStratifiedSplit(df, "strata", p);
        for (Frame st : strata) {
            ChiSqGoodnessOfFit test = ChiSqGoodnessOfFit.from(st.rvar("strata"), VarDouble.wrap(p));
            assertTrue(test.pValue() >= 0.9);
        }
    }
}
