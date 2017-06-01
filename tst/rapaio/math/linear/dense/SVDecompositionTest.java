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
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.math.linear.RM;

import static org.junit.Assert.*;

public class SVDecompositionTest {

    private static final double TOL = 1e-14;
    RandomSource randomSource = RandomSource.createRandom();

    @Test
    public void testBuilder() {

        randomSource.setSeed(1234);

        final int ROUNDS = 100;
        for (int round = 0; round < ROUNDS; round++) {


            int n = randomSource.nextInt(20) + 1;
            int m = randomSource.nextInt(20) + n;

            RM a = SolidRM.random(m, n);

            SVDecomposition svd = SVDecomposition.from(a);

            RM u = svd.getU();
            RM s = svd.getS();
            RM v = svd.getV();

            assertTrue(a.isEqual(u.dot(s).dot(v.t()), TOL));

            double[] sv = svd.getSingularValues();
            for (int i = 0; i < n - 1; i++) {
                assertEquals(s.get(i, i), sv[i], TOL);
                assertTrue(sv[i] >= sv[i + 1]);
            }

            assertEquals(sv[0], svd.norm2(), TOL);
            assertEquals(n, svd.rank());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDimension() {
        SVDecomposition.from(SolidRM.random(10, 50));
    }

    @Test
    public void conditionNumberTest() {

        randomSource.setSeed(1234);

        // for random matrices we expect a low condition number

        for (int i = 0; i < 100; i++) {
            double c = SVDecomposition.from(SolidRM.random(10, 10)).cond();
//            System.out.println(Math.log10(c));
            assertTrue(Math.log10(c) < 4);
        }

        // for ill conditioned the condition number explodes

        Normal norm = new Normal(0, 0.000001);

        for (int i = 0; i < 100; i++) {
            RM a = SolidRM.random(10, 10);

            // we create the first column as a slightly modified
            // version of the second column, thus we have linearity
            for (int j = 0; j < 10; j++) {
                a.set(j, 0, a.get(j, 1) + norm.sampleNext());
            }

            double c = SVDecomposition.from(a).cond();
//            System.out.println(Math.log10(c));
            assertTrue(Math.log10(c) > 5);
        }

    }
}
