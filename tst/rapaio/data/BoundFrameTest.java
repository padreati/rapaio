/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.data;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class BoundFrameTest {

    @Test
    public void testBuildersByVar() {
        Frame df = BoundFrame.newByVars(
                Numeric.newWrapOf(1, 2).withName("x"),
                Numeric.newWrapOf(1 / 1., 1 / 2.).withName("y"));

        assertEquals(2, df.varCount());
        assertEquals(2, df.rowCount());
        assertEquals(1, df.value(0, 0), 1e-12);
        assertEquals(1 / 2., df.value(1, 1), 1e-12);

        try {
            BoundFrame.newByVars(
                    Numeric.newWrapOf(1.).withName("x"),
                    Numeric.newWrapOf(2.).withName("x"));
            assertTrue("should raise an exception", false);
        } catch (IllegalArgumentException ignored) {
        }

        df = BoundFrame.newByVars(new Var[]{});

        assertEquals(0, df.varCount());
        assertEquals(0, df.rowCount());

        df = BoundFrame.newByVars(
                SolidFrame.newWrapOf(Numeric.newWrapOf(1, 2).withName("x")),
                SolidFrame.newWrapOf(Numeric.newWrapOf(1 / 1., 1 / 2.).withName("y"))
        );

        assertEquals(2, df.varCount());
        assertEquals(2, df.rowCount());
        assertEquals(1, df.value(0, 0), 1e-12);
        assertEquals(1 / 2., df.value(1, 1), 1e-12);

        try {
            BoundFrame.newByVars(
                    SolidFrame.newWrapOf(Numeric.newWrapOf(1.).withName("x")),
                    SolidFrame.newWrapOf(Numeric.newWrapOf(2.).withName("x")));
            assertTrue("should raise an exception", false);
        } catch (IllegalArgumentException ignored) {
        }

        df = BoundFrame.newByVars(new Frame[]{});

        assertEquals(0, df.varCount());
        assertEquals(0, df.rowCount());

    }
}
