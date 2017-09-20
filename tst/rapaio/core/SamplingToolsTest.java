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

import org.junit.Test;
import rapaio.core.tests.ChiSqGoodnessOfFit;
import rapaio.core.tests.KSTestOneSample;
import rapaio.core.tools.DVector;
import rapaio.data.NumericVar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static rapaio.core.CoreTools.distDUnif;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SamplingToolsTest {

    @Test
    public void worTest() {

        RandomSource.setSeed(123);

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
        ChiSqGoodnessOfFit test = ChiSqGoodnessOfFit.from(freq, NumericVar.wrap(w));
        assertTrue(test.pValue() > 0.05);
        test.printSummary();
    }

    @Test
    public void wrTest() {

        RandomSource.setSeed(123);

        double[] w = new double[]{0.001, 0.009, 0.09, 0.9};
        DVector freq = DVector.empty(true, w.length);
        final int TRIALS = 10_000;
        final int SAMPLES = 100;
        for (int i = 0; i < TRIALS; i++) {
            for (int next : SamplingTools.sampleWeightedWR(SAMPLES, w)) {
                freq.increment(next, 1);
            }
        }
        ChiSqGoodnessOfFit test = ChiSqGoodnessOfFit.from(freq, NumericVar.wrap(w));
        assertTrue(test.pValue() > 0.05);
        test.printSummary();
    }

    @Test
    public void worUnifTest() {

        double[] freq = new double[10];
        final int TRIALS = 100_000;
        final int SAMPLES = 3;
        NumericVar v = NumericVar.empty();
        for (int i = 0; i < TRIALS; i++) {
            for (int next : SamplingTools.sampleWOR(10, SAMPLES)) {
                freq[next]++;
                v.addValue(next);
            }
        }
        for (double f : freq) {
            System.out.print(String.format("%.6f, ", f / (1. * TRIALS * SAMPLES)));
        }
        KSTestOneSample.from(v, distDUnif(0, 9)).printSummary();
        System.out.println();
    }
}
