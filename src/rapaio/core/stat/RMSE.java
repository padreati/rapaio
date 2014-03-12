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
import rapaio.data.Frame;
import rapaio.data.Vector;
import rapaio.workspace.Workspace;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class RMSE implements Summarizable {

	private final List<Vector> source;
	private final List<Vector> target;
	private double value;

	public RMSE(Frame dfSource, Frame dfTarget) {
		source = new ArrayList<>();
		for (int i = 0; i < dfSource.getColCount(); i++) {
			if (dfSource.getCol(i).getType().isNumeric()) {
				source.add(dfSource.getCol(i));
			}
		}
		target = new ArrayList<>();
		for (int i = 0; i < dfTarget.getColCount(); i++) {
			if (dfTarget.getCol(i).getType().isNumeric()) {
				target.add(dfTarget.getCol(i));
			}
		}
		compute();
	}

	public RMSE(Vector source, Vector target) {
		this.source = new ArrayList<>();
		this.source.add(source);
		this.target = new ArrayList<>();
		this.target.add(target);
		compute();
	}

	private void compute() {
		double total = 0;
		double count = 0;

		for (int i = 0; i < source.size(); i++) {
			for (int j = 0; j < source.get(i).getRowCount(); j++) {
				count++;
				total += MathBase.pow(source.get(i).getValue(j) - target.get(i).getValue(j), 2);
			}
		}
		value = MathBase.sqrt(total / count);
	}

	public double getValue() {
		return value;
	}

	@Override
	public void summary() {
		// TODO summary for RMSE not implemented
		Workspace.code("not implemented");
	}
}
