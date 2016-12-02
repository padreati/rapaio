/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.math;

import org.junit.Test;
import rapaio.sys.WS;

import static org.junit.Assert.assertEquals;
import static rapaio.math.MathTools.*;

public class MathToolsTest {

    @Test
    public void baseWithRefTest() {

        // test values computed in other libraries

        double tol = 1e-12;

        assertEquals(12, sqrt(144), tol);
        assertEquals(13, rint(12.625761527), tol);

        assertEquals(1.5240638224307843, lnGamma(0.2), tol);
        assertEquals(-0.08537409000331583, lnGamma(1.2), tol);
        assertEquals(0.6931471805599453, lnGamma(3), tol);
        assertEquals(104971.46785449513, lnGamma(12453), tol);


        assertEquals(0.21599999999999986, betaIncReg(0.3, 2, 2), tol);

        assertEquals(0.3632574910905676, invBetaIncReg(0.3, 2, 2), tol);

        assertEquals(0.9999999567157739, incompleteGamma(2, 20), tol);
        assertEquals(0.4081672865401445, incompleteGamma(2, 1.4), tol);

        assertEquals(-1.537159819202354, logBinomial(3, 10, 0.4), tol);
        assertEquals(0.5, beta(1, 2), tol);
    }



    @Test
    public void combinationsTest() {

        double x = MathTools.combinations(10, 5);
        WS.printf("%f", x);


    }

}
