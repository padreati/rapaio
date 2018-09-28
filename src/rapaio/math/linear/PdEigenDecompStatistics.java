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

public class PdEigenDecompStatistics extends EigenDecompStrategy{

	@Override
	public EigenPair getEigenDecomp(RM s, int maxRuns, double tol) {
		// runs QR decomposition algorithm for maximum of iterations
        // to provide a solution which has other than diagonals under
        // tolerance

        // this works only for positive definite
        // here we check only symmetry
		//QR is not implemented
		/*
        if (s.rowCount() != s.colCount())
            throw new IllegalArgumentException("This eigen pair method works only for positive definite matrices");
        QR qr = s.qr();
        s = qr.getR().dot(qr.getQ());
        RM ev = qr.getQ();
        for (int i = 0; i < maxRuns - 1; i++) {
            qr = s.qr();
            s = qr.getR().dot(qr.getQ());
            ev = ev.dot(qr.getQ());
            if (inTolerance(s, tol))
                break;
        }
        return EigenPair.from(s.diag(), ev.solidCopy());
        */
		return null;
	}

}
