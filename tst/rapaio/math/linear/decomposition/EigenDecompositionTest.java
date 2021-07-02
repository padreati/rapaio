/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.math.linear.decomposition;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.math.linear.DMatrix;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EigenDecompositionTest {

    private static final double TOL = 1e-12;
    private static final int TIMES = 100;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1234);
    }

    @Test
    void testSymmetric() {
        for (int i = 0; i < TIMES; i++) {
            DMatrix a = DMatrix.random(10, 10).scatter();
            EigenDecomposition evd = EigenDecomposition.from(a);

            DMatrix v = evd.getV();
            DMatrix d = evd.getD();
            DMatrix vt = evd.getV().t();

            assertTrue(a.deepEquals(v.dot(d).dot(vt), TOL));
        }
    }

    @Test
    void testNonSymmetric() {
        for (int i = 0; i < TIMES; i++) {
            DMatrix a = DMatrix.random(10, 10);

            EigenDecomposition evd = EigenDecomposition.from(a);

            DMatrix v = evd.getV();
            DMatrix d = evd.getD();

            assertTrue(a.dot(v).deepEquals(v.dot(d), TOL));
        }
    }


}
