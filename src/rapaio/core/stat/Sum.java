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

import static rapaio.core.MathBase.validNumber;
import static rapaio.workspace.Workspace.code;

/**
 * Computes the sum of elements for a {@link Vector} of values.
 * <p/>
 * Ignore invalid numeric values. See {@link rapaio.core.MathBase#validNumber(double)}.
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Sum implements Summarizable {

	private final Vector vector;
	private final double value;

	public Sum(Vector vector) {
		this.vector = vector;
		this.value = compute();
	}

	private double compute() {
		double sum = 0;
		for (int i = 0; i < vector.getRowCount(); i++) {
			if (validNumber(vector.getValue(i))) {
				sum += vector.getValue(i);
			}
		}
		return sum;
	}

	public double getValue() {
		return value;
	}

	@Override
	public void summary() {
		code(String.format("sum\n%.10f\n", value));
	}
}