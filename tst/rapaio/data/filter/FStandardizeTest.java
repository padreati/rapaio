/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.filter;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.VarRange;
import rapaio.data.VarType;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/3/18.
 */
public class FStandardizeTest {

    @Test
    void testDouble() {
        Frame src = FFilterTestUtil.allDoubles(100, 2);

        FStandardize filter = FStandardize.on(VarRange.all());
        filter.fit(src);

        Frame std1 = src.copy().fapply(filter);
        Frame std2 = src.copy().fapply(FStandardize.on(VarRange.onlyTypes(VarType.DOUBLE)).newInstance());

        assertTrue(std1.deepEquals(std2));
    }
}
