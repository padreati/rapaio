/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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

package rapaio.experiment.core.tools;

import static rapaio.math.MTools.*;

public class ConcreteTotalColEntropy extends AbstractDTableFunction {
	
	@Override
	protected double getInfo(int start, double total, double[] totals, double[][] values, int rowLength,
			int colLength) {
		double entropy = 0;
        for (int i = start; i < totals.length; i++) {
            if (totals[i] > 0) {
                entropy += -log2(totals[i] / total) * totals[i] / total;
            }
        }
        return entropy;
	}

	@Override
	protected int chooseLine(int row, int col) {
		return col;
	}

}
