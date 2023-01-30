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

package rapaio.core.stat;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import rapaio.data.VarDouble;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/10/18.
 */
public class SkewnessTest {

    private static final double TOL = 1e-12;

    @Test
    void testDouble() {
        Skewness sk = Skewness.of(VarDouble.wrap(1, 2, 45, 109, 200));

        // these values were computed in R from
        // library(fBasics)
        // skewness(c(1, 2, 45, 109, 200))
        assertEquals(0.493673230307975, sk.value(), TOL);
        assertEquals(0.493673230307975, sk.b1(), TOL);
        assertEquals(0.6899293135253384, sk.g1(), TOL);
        assertEquals(1.0284858964749477, sk.bigG1(), TOL);

        assertEquals("""
                > skewness[?]
                total rows: 5 (complete: 5, missing: 0)
                skewness (g1): 0.6899293
                skewness (b1): 0.4936732
                skewness (G1): 1.0284859
                """, sk.toSummary());
    }
}
