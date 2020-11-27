/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

package rapaio.data.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Var;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/2/18.
 */
public class VTransformBoxCoxTest {

    private static final double TOL = 1e-20;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1233);
    }

    @Test
    void testDouble() {
        Normal normal = Normal.of(1, 10);
        double[] values = new double[100];
        for (int i = 0; i < values.length; i++) {
            values[i] = normal.sampleNext();
        }
        Var x = VarDouble.wrap(values);

        Var bc1 = x.copy().fapply(VTransformBoxCox.with(0));
        Var bc2 = x.copy().fapply(VTransformBoxCox.with(1.1, 12));

        assertEquals(1.1, VTransformBoxCox.with(1.1, 2.0).lambda(), TOL);
        assertEquals(2.0, VTransformBoxCox.with(1.1, 2.0).shift(), TOL);

        for (int i = 0; i < x.size(); i++) {
            assertEquals(Math.log(x.getDouble(i)), bc1.getDouble(i), TOL);
            assertEquals((Math.pow(x.getDouble(i) + 12, 1.1) - 1) / 1.1, bc2.getDouble(i), TOL);
        }
    }
}
