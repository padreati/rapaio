/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.core.stat;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.VarDouble;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/9/18.
 */
public class MaximumTest {

    private static final double TOL = 1e-20;

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(123);
    }

    @Test
    void testDouble() {
        VarDouble x = VarDouble.from(100, row -> row % 7 == 0 ? Double.NaN : random.nextDouble());
        double max = 0.0;
        for (int i = 0; i < x.size(); i++) {
            if (x.isMissing(i)) {
                continue;
            }
            max = Math.max(max, x.getDouble(i));
        }
        Maximum maximum = Maximum.of(x);
        assertEquals(max, maximum.value(), TOL);

        assertEquals("""
                > maximum[?]
                total rows: 100 (complete: 85, missing: 15)
                maximum: 0.9908989
                """, maximum.toSummary());
    }
}
