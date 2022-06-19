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

package rapaio.core.stat;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.VarDouble;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/18.
 */
public class MinimumTest {

    private static final double TOL = 1e-20;

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    void testDouble() {
        VarDouble x = VarDouble.from(100, row -> row % 7 == 0 ? Double.NaN : RandomSource.nextDouble());
        double min = 1.0;
        for (int i = 0; i < x.size(); i++) {
            if (x.isMissing(i)) {
                continue;
            }
            min = Math.min(min, x.getDouble(i));
        }
        Minimum minimum = Minimum.of(x);
        assertEquals(min, minimum.value(), TOL);

        assertEquals("> minimum[?]\n" +
                "total rows: 100 (complete: 85, missing: 15)\n" +
                "minimum: 0.0174893\n", minimum.toSummary());
    }
}
