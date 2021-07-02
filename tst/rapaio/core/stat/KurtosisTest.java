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

package rapaio.core.stat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/10/18.
 */
public class KurtosisTest {

    private static final double TOL = 1e-12;

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(123);
    }

    @Test
    void testDouble() {
        Kurtosis kt = Kurtosis.of(VarDouble.wrap(1, 2, 45, 109, 200));

        // these values were computed in R from
        // library(fBasics)
        // kurtosis(c(1, 2, 45, 109, 200))

        assertEquals(-1.7174503726358747, kt.value(), TOL);
        assertEquals(-0.9960162072435548, kt.g2(), TOL);
        assertEquals(-1.7174503726358747, kt.b2(), TOL);
        assertEquals(0.007967585512890452, kt.bigG2(), TOL);

        assertEquals("> kurtosis[?]\n" +
                "total rows: 5 (complete: 5, missing: 0)\n" +
                "kurtosis (g2): -0.9960162\n" +
                "kurtosis (b2): -1.7174504\n" +
                "kurtosis (G2): 0.0079676\n", kt.toSummary());
    }
}
