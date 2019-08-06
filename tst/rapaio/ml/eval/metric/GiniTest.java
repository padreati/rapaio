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

package rapaio.ml.eval.metric;

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;
import rapaio.ml.eval.metric.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/17/15.
 */
public class GiniTest {

    private static final double TOL = 1e-6;

    @Test
    public void testSmoke() {

        VarDouble x = VarDouble.copy(1, 2, 3, 4, 5, 6, 7, 8, 9);
        VarDouble y = VarDouble.copy(1, 4, 7, 2, 9, 3, 8, 5, 6);

        double eval = Gini.from(x, y).normalizedGini();
        System.out.println(eval);

        System.out.println(Gini.from(x, x).normalizedGini());
        System.out.println(Gini.from(y, x).normalizedGini());
    }

    /**
     * This test is documented from here:
     */
    @Test
    public void kaggleTests() {
        test(VarDouble.wrap(5.1, 3.2, 1.7, 6.2, 8.1), VarDouble.wrap(3.1, 5.2, 2.7, 5.1, 1.1), -0.043621399177, -0.335443037975);
        test(VarDouble.wrap(1, 2, 3), VarDouble.wrap(10, 20, 30), 0.111111, 1);
        test(VarDouble.wrap(1, 2, 3), VarDouble.wrap(30, 20, 10), -0.111111, -1);
        test(VarDouble.wrap(1, 2, 3), VarDouble.wrap(0, 0, 0), -0.111111, -1);
        test(VarDouble.wrap(3, 2, 1), VarDouble.wrap(0, 0, 0), 0.111111, 1);
        test(VarDouble.wrap(1, 2, 4, 3), VarDouble.wrap(0, 0, 0, 0), -0.1, -0.8);
        test(VarDouble.wrap(2, 1, 4, 3), VarDouble.wrap(0, 0, 2, 1), 0.125, 1);
        test(VarDouble.wrap(0, 20, 40, 0, 10), VarDouble.wrap(40, 40, 10, 5, 5), 0, 0);
        test(VarDouble.wrap(40, 0, 20, 0, 10), VarDouble.wrap(1000000, 40, 40, 5, 5), 0.171428, 0.6);
        test(VarDouble.wrap(40, 20, 10, 0, 0), VarDouble.wrap(40, 20, 10, 0, 0), 0.285714, 1);
        test(VarDouble.wrap(1, 1, 0, 1), VarDouble.wrap(0.86, 0.26, 0.52, 0.32), -0.041666, -0.333333);
    }

    private void test(VarDouble x, VarDouble y, double g1, double g2) {
        Gini ng = Gini.from(x, y);
        assertEquals(g1, ng.gini(), TOL);
        assertEquals(g2, ng.normalizedGini(), TOL);
    }

    /**
     * This test is documented from here: https://www.kaggle.com/c/liberty-mutual-fire-peril/discussion/9880
     */
    @Test
    public void weightedGiniTest() {
        Gini gini = Gini.from(
                VarDouble.wrap(0, 0, 1, 0, 1),
                VarDouble.wrap(0.1, 0.4, 0.3, 1.2, 0.0),
                VarDouble.wrap(1, 2, 5, 4, 3)
        );
        assertEquals(-0.821428571428572, gini.normalizedGini(), TOL);

        RandomSource.setSeed(1234);
        VarDouble x = Normal.std().sample(100);
        VarDouble y = Normal.std().sample(100);

        /*
        This does not work, and I do not understand why.
         */
        /*
        Gini gini1 = Gini.from(x, y, NumVar.fill(x.rowCount(), 1));
        Gini gini2 = Gini.from(x, y);

        assertEquals(gini1.gini(), gini2.gini(), TOL);
        assertEquals(gini1.normalizedGini(), gini2.normalizedGini(), TOL);
        */
    }
}
