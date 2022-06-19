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
public class MeanTest {

    private static final double TOL = 1e-20;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void testDouble() {
        VarDouble x = VarDouble.from(100, row -> row % 2 == 0 ? Double.NaN : row);
        Mean mean = Mean.of(x);
        assertEquals(50, mean.value(), TOL);

        assertEquals("> mean[?]\n" +
                "total rows: 100 (complete: 50, missing: 50)\n" +
                "mean: 50\n", mean.toSummary());

        assertEquals(Double.NaN, Mean.of(VarDouble.wrap(Double.NaN)).value(), TOL);

        double[] values = x.stream().mapToDouble().toArray();
        assertEquals(50, Mean.of(values, 0, values.length).value(), TOL);

        assertEquals(Double.NaN, Mean.of(VarDouble.empty(10).stream().mapToDouble().toArray(), 0, 10).value(), TOL);
    }
}
