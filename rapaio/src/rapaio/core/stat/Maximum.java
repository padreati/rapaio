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

import rapaio.core.MathBase;
import rapaio.core.Summarizable;
import rapaio.data.Vector;
import rapaio.data.VectorIterator;

import static rapaio.workspace.Workspace.code;

/**
 * Finds the maximum getValue from a {@link Vector} of values.
 * <p/>
 * Ignores missing elements.
 * <p/>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 * Date: 9/7/13
 * Time: 12:39 PM
 */
public class Maximum implements Summarizable {

	private final Vector vector;
	private final double value;

	public Maximum(Vector vector) {
		this.vector = vector;
		this.value = compute();
	}

	private double compute() {
		double max = Double.MIN_VALUE;
		boolean valid = false;
		VectorIterator it = vector.getIterator(true);
		while (it.next()) {
			max = MathBase.max(max, it.getValue());
			valid = true;
		}
		return valid ? max : Double.NaN;
	}

	public double getValue() {
		return value;
	}

	@Override
	public void summary() {
		code(String.format("maximum\n%.10f", value));
	}
}
