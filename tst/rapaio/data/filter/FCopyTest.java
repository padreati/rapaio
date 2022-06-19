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

package rapaio.data.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.data.FrameTransform;
import rapaio.data.VarRange;
import rapaio.datasets.Datasets;

public class FCopyTest {

    @Test
    void testCopy() {
        var iris = Datasets.loadIrisDataset();
        var transform = FrameTransform.newTransform(FCopy.filter());

        var copy = transform.fapply(iris);

        assertTrue(copy.deepEquals(iris));
        assertNotSame(copy, iris);

        var second = copy.copy();

        var alterTransform = transform.newInstance()
                .clear()
                .add(FJitter.on(VarRange.all()));
        alterTransform.fapply(iris);

        assertFalse(iris.deepEquals(copy));
        assertTrue(copy.deepEquals(second));
    }
}
