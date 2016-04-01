/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.data;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


/**
 * Created by <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>.
 */
public class VRangeTest {

    @Test
    public void testSmoke() {
        Frame df = SolidFrame.newByVars(
                Numeric.newEmpty().withName("a"),
                Numeric.newEmpty().withName("b"),
                Numeric.newEmpty().withName("c"),
                Numeric.newEmpty().withName("d")
        );

        List<Integer> indexes = VRange.of(0, 2).parseVarIndexes(df);
        List<String> names = VRange.of(0, 2).parseVarNames(df);

        assertEquals(2, indexes.size());
        assertEquals(0, (int)indexes.get(0));
        assertEquals(2, (int)indexes.get(1));
    }
}
