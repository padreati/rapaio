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

package rapaio.ml.nnet;

import rapaio.core.RandomSource;

/**
 * User: Aurelian Tutuianu <padreati@yahoo.com>
 */
public class NetNode {

	double value = RandomSource.nextDouble() / 10.;
	NetNode[] inputs;
	double[] weights;
	double gamma;

	public void setInputs(NetNode[] inputs) {
		this.inputs = inputs;
		if (inputs == null) {
			this.weights = null;
			return;
		}
		this.weights = new double[inputs.length];
		for (int i = 0; i < weights.length; i++) {
			weights[i] = RandomSource.nextDouble() / 10.;
		}
	}
}
