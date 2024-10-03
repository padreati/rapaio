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

import rapaio.data.Frame;
import rapaio.data.VarRange;
import rapaio.data.VarType;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/4/18.
 */
public class QuantileTransformTest {

    @Test
    void testDouble() {
        Frame src = TransformTestUtil.allDoubleNominal(new Random(), 100, 2, 2);

        Frame q1 = src.fapply(QuantileTransform.on(VarRange.all(), 0.5));
        Frame q2 = src.fapply(QuantileTransform.split(VarRange.onlyTypes(VarType.DOUBLE), 2).newInstance());

        assertTrue(q1.deepEquals(q2));
    }

    @Test
    void testInvalidSplit() {
        var ex = assertThrows(IllegalArgumentException.class, () -> TransformTestUtil.allDoubleNominal(new Random(), 100, 2, 2)
                .fapply(QuantileTransform.split(VarRange.all(), 1)));
        assertEquals("Frame quantile discrete filter allows only splits greater than 1.", ex.getMessage());
    }

    @Test
    void testInvalidProbabilities() {
        var ex = assertThrows(IllegalArgumentException.class, () -> TransformTestUtil.allDoubleNominal(new Random(), 100, 2, 2)
                .fapply(QuantileTransform.on(VarRange.all())));
        assertEquals("Frame quantile discrete filter requires at least one probability.", ex.getMessage());
    }
}
