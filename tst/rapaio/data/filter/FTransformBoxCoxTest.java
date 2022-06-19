/*
 *
 *  * Apache License
 *  * Version 2.0, January 2004
 *  * http://www.apache.org/licenses/
 *  *
 *  * Copyright 2013 - 2022 Aurelian Tutuianu
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package rapaio.data.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.VarRange;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/4/18.
 */
public class FTransformBoxCoxTest {

    @Test
    void testDouble() {
        Frame src = FFilterTestUtil.allDoubleNominal(100, 2, 2);

        Frame f1 = src.copy().fapply(FTransformBoxCox.on(1.1, 1.1, VarRange.of(1)));
        Frame f2 = src.copy().fapply(FTransformBoxCox.on(1.1, 1.1, "v2").newInstance());

        assertTrue(f1.deepEquals(f2));
        assertFalse(src.deepEquals(f1));
    }
}
