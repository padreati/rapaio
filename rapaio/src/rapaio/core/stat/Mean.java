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
package rapaio.core.stat;

import rapaio.core.Summarizable;
import rapaio.data.Vector;
import rapaio.data.VectorIterator;

import static rapaio.workspace.Workspace.code;

/**
 * Compensated version of arithmetic mean of values from a {@code Vector}.
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 9/7/13
 * Time: 12:21 PM
 */
public final class Mean implements Summarizable {

	private final Vector vector;
	private final double value;

	public Mean(Vector vector) {
		this.vector = vector;
		this.value = compute();
	}

	private double compute() {
		double sum = 0.;
		double count = 0;
		VectorIterator it = vector.getIterator(true);
		while (it.next()) {
			sum += it.getValue();
			count++;
		}
		if (count == 0) {
			return Double.NaN;
		}
		sum /= count;
		double t = 0;
		it.reset();
		while (it.next()) {
			t += it.getValue() - sum;
		}
		sum += t / count;
		return sum;
	}

	public double getValue() {
		return value;
	}

	@Override
	public void summary() {
		code(String.format("> mean\n%.10f", value));
	}
}
