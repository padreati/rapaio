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

package rapaio.data.preprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.math.linear.DMatrix;

public class PCATransformTest {

    private static final int n = 10;
    private static final Frame df =
            SolidFrame.matrix(DMatrix.eye(n), IntStream.range(1, n + 1).boxed().map(i -> "v" + i).collect(Collectors.toList()));

    @Test
    void percentageTest() {
        assertEquals(1, df.fapply(PCATransform.coverVariance(0)).varCount());
        assertEquals(2, df.fapply(PCATransform.coverVariance(0.12)).varCount());
        assertEquals(9, df.fapply(PCATransform.coverVariance(0.9)).varCount());
        assertEquals(10, df.fapply(PCATransform.coverVariance(1 - 1e-20)).varCount());
        assertEquals(10, df.fapply(PCATransform.coverVariance(1)).varCount());
    }

    @Test
    void countTest() {
        assertEquals(1, df.fapply(PCATransform.featureCount(1)).varCount());
        assertEquals(3, df.fapply(PCATransform.featureCount(3)).varCount());
    }

    @Test
    void otherTests() {
        assertTrue(df.fapply(PCATransform.coverVariance(0.12)).deepEquals(df.fapply(PCATransform.featureCount(2))));

        var pca = PCATransform.featureCount(3);
        assertTrue(pca.fapply(df).deepEquals(pca.newInstance().fapply(df)));
    }
}
