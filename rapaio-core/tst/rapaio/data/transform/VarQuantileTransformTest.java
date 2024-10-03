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

package rapaio.data.transform;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.data.Var;
import rapaio.data.VarDouble;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/1/18.
 */
public class VarQuantileTransformTest {

    @Test
    void testDouble() {
        Var x = VarDouble.seq(10, 11, 0.01);

        VarTransform f1 = VarQuantileTransform.split(2);
        Var q1 = x.fapply(f1);

        assertEquals(101, q1.size());
        assertEquals(3, q1.levels().size());
        assertEquals("?", q1.levels().get(0));
        assertEquals("-Inf~10.5", q1.levels().get(1));
        assertEquals("10.5~Inf", q1.levels().get(2));

        for (int i = 0; i < x.size() / 2 + 1; i++) {
            assertEquals("-Inf~10.5", q1.getLabel(i));
        }
        for (int i = x.size() / 2 + 1; i < x.size(); i++) {
            assertEquals("10.5~Inf", q1.getLabel(i));
        }

        VarTransform f2 = VarQuantileTransform.with(0.25, 0.50, 0.75);
        Var q2 = x.fapply(f2);

        assertEquals(101, q2.size());
        assertEquals(5, q2.levels().size());
        for (int i = 1; i < x.size(); i++) {
            assertTrue(x.getInt(i - 1) <= x.getInt(i));
        }

        Random random = new Random();
        Var y = VarDouble.from(100, row -> row % 7 == 0 ? Double.NaN : random.nextDouble());
        Var qy = y.fapply(VarQuantileTransform.split(10));
        for (int i = 0; i < y.size(); i++) {
            if (y.isMissing(i)) {
                assertTrue(qy.isMissing(i));
            } else {
                assertFalse(qy.isMissing(i));
            }
        }
    }

    @Test
    void testInvalidNumperOfPercentiles() {
        var ex = assertThrows(IllegalArgumentException.class, VarQuantileTransform::with);
        assertEquals("Number of quantiles must be positive.", ex.getMessage());
    }

    @Test
    void testInvalidK() {
        var ex = assertThrows(IllegalArgumentException.class, () -> VarQuantileTransform.split(1));
        assertEquals("Number of parts k: 1 of the split must be greater than 1.", ex.getMessage());
    }
}
