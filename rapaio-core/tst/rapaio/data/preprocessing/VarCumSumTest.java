/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.preprocessing;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import rapaio.data.Var;
import rapaio.data.VarDouble;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/1/18.
 */
public class VarCumSumTest {

    private static final double TOL = 1e-20;

    @Test
    void testDouble() {

        Var fill = VarDouble.fill(100, 1);
        Var cs = fill.copy().fapply(VarCumSum.transform());

        for (int i = 0; i < fill.size(); i++) {
            assertEquals(1, fill.getDouble(i), TOL);
            assertEquals(i + 1, cs.getDouble(i), TOL);
        }
    }
}
