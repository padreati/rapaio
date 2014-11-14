/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.core.sample;

import org.junit.Test;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SamplingTest {

	//    @Test
	public void worTest() {

		double[] w = null;
//        w = new double[]{0.5, 0.1, 0.1, 0.1, 0.1, 0.1};
//        w = new double[]{0.2, 0.2, 0.2, 0.2, 0.2, 0.2};
//        w = new double[]{0.6, 0.05, 0.1, 0.05, 0.2, 0.8, 0.3, 0.7};
		w = new double[]{0.4, 0.3, 0.2, 0.1};
		double[] freq = new double[w.length];
		final int TRIALS = 1_000_000;
		final int SAMPLES = 2;
		for (int i = 0; i < TRIALS; i++) {
			for (int next : Sampling.sampleWeightedWOR(SAMPLES, w)) {
				freq[next]++;
//                System.out.print(next + " ");
			}
//            System.out.println();
		}

		for (int i = 0; i < freq.length; i++) {
			System.out.print(String.format("%.4f, ", freq[i] / (1. * TRIALS)));
		}
		System.out.println();
	}

	//    @Test
	public void wrTest() {
		double[] w = null;
//        w = new double[]{0.5, 0.5};
//        w = new double[]{0.2, 0.1, 0.4, 0.3};
		w = new double[]{0.001, 0.009, 0.09, 0.9};
		double[] freq = new double[w.length];
		final int TRIALS = 100_000;
		final int SAMPLES = 100;
		for (int i = 0; i < TRIALS; i++) {
			for (int next : Sampling.sampleWeightedWR(SAMPLES, w)) {
				freq[next]++;
			}
		}
		for (int i = 0; i < freq.length; i++) {
			System.out.print(String.format("%.6f, ", freq[i] / (1. * TRIALS * SAMPLES)));
		}
		System.out.println();
	}

	@Test
	public void worUnifTest() {

		double[] freq = new double[10];
		final int TRIALS = 100_000;
		final int SAMPLES = 3;
		for (int i = 0; i < TRIALS; i++) {
			for (int next : Sampling.sampleWOR(SAMPLES, 10)) {
				freq[next]++;
			}
		}
		for (double f : freq) {
			System.out.print(String.format("%.6f, ", f / (1. * TRIALS * SAMPLES)));
		}
		System.out.println();
	}
}
