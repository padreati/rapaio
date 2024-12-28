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

package rapaio.ml.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.datasets.Datasets;

public class LDATest {

    @Test
    void scratchTest() {

        // we need a better test, this for now it should be enough

        Frame iris = Datasets.loadIrisDataset();

        LDA lda = LDA.newModel().scaling.set(true);
        lda.fit(iris, "class");

        Frame df = lda.transform(iris, 2);
        assertNotNull(df);
        assertEquals(3, df.varCount());

        // we know we have only two relevant eigen values
        assertTrue(lda.eigenValues().getDouble(0) > 0.1);
        assertTrue(lda.eigenValues().getDouble(1) > 0.1);
        assertTrue(lda.eigenValues().getDouble(2) < 0.1);
        assertTrue(lda.eigenValues().getDouble(3) < 0.1);
    }
}
