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

package rapaio.ml.eval.metric;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import rapaio.data.VarDouble;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/17/15.
 */
public class GiniTest {

    private static final double TOL = 1e-6;

    /**
     * This test is documented from here:
     */
    @Test
    void kaggleTests() {
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
    void weightedGiniTest() {
        Gini gini = Gini.from(
                VarDouble.wrap(0, 0, 1, 0, 1),
                VarDouble.wrap(0.1, 0.4, 0.3, 1.2, 0.0),
                VarDouble.wrap(1, 2, 5, 4, 3)
        );
        assertEquals(-0.821428571428572, gini.normalizedGini(), TOL);
    }
}
