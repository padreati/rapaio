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

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/3/18.
 */
public class ShuffleRowsTest {

    @Test
    void testDouble() {
        Random random = new Random(42);

        Frame orig = TransformTestUtil.allDoubles(random, 100, 1);
        Frame[] shuffles = new Frame[10];
        for (int i = 0; i < shuffles.length; i++) {
            shuffles[i] = orig.fapply(ShuffleRows.filter(random).newInstance());
        }
        for (int i = 1; i < shuffles.length; i++) {
            assertFalse(shuffles[i - 1].deepEquals(shuffles[i]));
        }
    }
}
