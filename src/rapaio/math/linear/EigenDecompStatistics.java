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

package rapaio.math.linear;

import rapaio.math.linear.dense.EigenDecomposition;
import rapaio.math.linear.dense.SolidDMatrix;
import rapaio.math.linear.dense.SolidDVector;

public class EigenDecompStatistics extends EigenDecompStrategy{

	@Override
	public EigenPair getEigenDecomp(DMatrix s, int maxRuns, double tol) {
		int n = s.colCount();
        EigenDecomposition evd = EigenDecomposition.from(s);

        double[] _values = evd.getRealEigenvalues();
        DMatrix _vectors = evd.getV();

        DVector values = SolidDVector.zeros(n);
        DMatrix vectors = SolidDMatrix.empty(n, n);

        for (int i = 0; i < values.size(); i++) {
            values.set(values.size() - i - 1, _values[i]);
        }
        for (int i = 0; i < vectors.rowCount(); i++) {
            for (int j = 0; j < vectors.colCount(); j++) {
                vectors.set(i, vectors.colCount() - j - 1, _vectors.get(i, j));
            }
        }
        return EigenPair.from(values, vectors);
	}

}
