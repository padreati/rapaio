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
package rapaio.ml.distance;

import rapaio.data.Frame;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class EuclideanDistance implements Distance {

	@Override
	public double measure(Frame df, int row1, int row2) {
		return measure(df, row1, df, row2);
	}

	@Override
	public double measure(Frame df1, int row1, Frame df2, int row2) {
		double total = 0;
		for (int i = 0; i < df1.getColCount(); i++) {
			if (df1.getCol(i).getType().isNominal()) {
				if (df1.getIndex(row1, i) == df2.getIndex(row2, i)) {
					total++;
				}
			} else {
				total += df1.getValue(row1, i) * df2.getValue(row2, i);
			}
		}
		return total;
	}
}
