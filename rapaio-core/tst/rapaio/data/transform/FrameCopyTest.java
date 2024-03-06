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

package rapaio.data.transform;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.data.Processing;
import rapaio.data.VarRange;
import rapaio.datasets.Datasets;

public class FrameCopyTest {

    @Test
    void testCopy() {
        var iris = Datasets.loadIrisDataset();
        var transform = Processing.newProcessing().add(FrameCopy.transform());

        var copy = transform.fitApply(iris);

        assertTrue(copy.deepEquals(iris));
        assertNotSame(copy, iris);

        var second = copy.copy();

        var alterTransform = transform.newInstance()
                .clear()
                .add(Jitter.on(VarRange.all()));
        alterTransform.fitApply(iris);

        assertFalse(iris.deepEquals(copy));
        assertTrue(copy.deepEquals(second));
    }
}
