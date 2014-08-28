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
import rapaio.core.VarRange;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class FrameTest {

    @Test
    public void testRemove() {
        SolidFrame df = SolidFrame.newWrapOf(
                Nominal.newCopyOf("a", "b").withName("x"),
                Nominal.newCopyOf("x", "y").withName("y"),
                Numeric.newWrapOf(1, 2).withName("z")
        );
        Frame df1 = df.removeVars("x,z");

        assertEquals(1, df1.varCount());
        assertEquals("y", df1.varNames()[0]);

        df1 = df.removeVars(new VarRange("y"));
        assertEquals(2, df1.varCount());
        assertEquals("x", df1.varNames()[0]);
        assertEquals("z", df1.varNames()[1]);

        df1 = df.removeRows(0);
        assertEquals(1, df1.rowCount());
        assertEquals(3, df1.varCount());
        assertEquals("b", df1.label(0, "x"));
    }

    @Test
    public void testSetters() {
        SolidFrame df = SolidFrame.newWrapOf(
                Nominal.newCopyOf("a", "b").withName("x"),
                Nominal.newCopyOf("x", "y").withName("y"),
                Numeric.newWrapOf(1, 2).withName("z")
        );

        df.setValue(1, "z", 100);
        assertEquals(100, df.value(1, "z"), 1e-12);

        df.setIndex(1, "z", 10);
        assertEquals(10, df.index(1, "z"));

        df.setLabel(1, "x", "xxx");
        assertEquals("xxx", df.label(1, "x"));
    }

    @Test
    public void testMissing() {
        SolidFrame df = SolidFrame.newWrapOf(
                Nominal.newCopyOf("a", "b").withName("x"),
                Nominal.newCopyOf("x", "y").withName("y"),
                Numeric.newWrapOf(1, 2).withName("z")
        );

        assertEquals(false, df.missing(0));
        assertEquals(false, df.missing(1));

        df.setMissing(0, "x");
        assertEquals(true, df.missing(0, "x"));
        assertEquals(true, df.missing(0, 0));
        assertEquals(true, df.missing(0));
        assertEquals(false, df.missing(1));

        df.setMissing(0, 1);
        assertEquals(true, df.missing(0, "y"));
        assertEquals(true, df.missing(0, 0));
        assertEquals(true, df.missing(0));
        assertEquals(false, df.missing(1));
    }
}
