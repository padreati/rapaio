/*
 *
 *  * Apache License
 *  * Version 2.0, January 2004
 *  * http://www.apache.org/licenses/
 *  *
 *  * Copyright 2013 - 2022 Aurelian Tutuianu
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package rapaio.math;


import static java.lang.Math.rint;
import static java.lang.Math.sqrt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static rapaio.math.MathTools.*;

import org.junit.jupiter.api.Test;

public class MathToolsTest {

    @Test
    void baseWithRefTest() {

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

        assertEquals(0.9999999567157739, incGamma(2, 20), tol);
        assertEquals(0.4081672865401445, incGamma(2, 1.4), tol);

        assertEquals(-1.537159819202354, logBinomial(3, 10, 0.4), tol);
        assertEquals(0.5, beta(1, 2), tol);
    }
}
