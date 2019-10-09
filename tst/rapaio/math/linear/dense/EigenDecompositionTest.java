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

package rapaio.math.linear.dense;

import org.junit.Test;
import rapaio.core.*;
import rapaio.math.linear.*;

import static org.junit.Assert.assertTrue;

public class EigenDecompositionTest {

    private static final double TOL = 1e-12;

    @Test
    public void testSymmetric() {

        RandomSource.setSeed(1234);
        for (int i = 0; i < 100; i++) {

            RM a = SolidRM.random(10, 10).scatter();

            EigenDecomposition evd = EigenDecomposition.from(a);

            RM v = evd.getV();
            RM d = evd.getD();
            RM vt = evd.getV().t();

            assertTrue(a.isEqual(v.dot(d).dot(vt), TOL));
        }
    }

    @Test
    public void testNonSymmetric() {
        for(int i=0; i<100; i++) {
            RM a = SolidRM.random(10, 10);

            EigenDecomposition evd = EigenDecomposition.from(a);

            RM v = evd.getV();
            RM d = evd.getD();

            assertTrue(a.dot(v).isEqual(v.dot(d), TOL));
        }
    }


}
