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

package rapaio.math.optimization.linesearch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import rapaio.math.optimization.linesearch.LearningRateLineSearch;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/31/21.
 */
public class LearningRateLineSearchTest {

    @Test
    void smokeTest() {
        assertEquals(1.0, LearningRateLineSearch.from(1).search(null, null, null, null));
        assertEquals(0.5, LearningRateLineSearch.from(0.5).search(null, null, null, null));
    }

    @Test
    void validityTest() {
        var ex = assertThrows(IllegalArgumentException.class, () -> LearningRateLineSearch.from(0));
        assertEquals("Learning rate must have a finite positive value", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> LearningRateLineSearch.from(-1));
        assertEquals("Learning rate must have a finite positive value", ex.getMessage());

        ex = assertThrows(IllegalArgumentException.class, () -> LearningRateLineSearch.from(Double.POSITIVE_INFINITY));
        assertEquals("Learning rate must have a finite positive value", ex.getMessage());
    }
}
