/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class FrameTest {

    @Test
    public void testRemove() {
        SolidFrame df = SolidFrame.byVars(
                NominalVar.copy("a", "b").withName("x"),
                NominalVar.copy("x", "y").withName("y"),
                NumericVar.wrap(1, 2).withName("z")
        );
        Frame df1 = df.removeVars("x,z");

        assertEquals(1, df1.getVarCount());
        assertEquals("y", df1.getVarNames()[0]);

        df1 = df.removeVars(VRange.of("y"));
        assertEquals(2, df1.getVarCount());
        assertEquals("x", df1.getVarNames()[0]);
        assertEquals("z", df1.getVarNames()[1]);

        df1 = df.removeRows(0);
        assertEquals(1, df1.getRowCount());
        assertEquals(3, df1.getVarCount());
        assertEquals("b", df1.getLabel(0, "x"));
    }

    @Test
    public void testSetters() {
        SolidFrame df = SolidFrame.byVars(
                NominalVar.copy("a", "b").withName("x"),
                NominalVar.copy("x", "y").withName("y"),
                NumericVar.wrap(1, 2).withName("z")
        );

        df.setValue(1, "z", 100);
        assertEquals(100, df.getValue(1, "z"), 1e-12);

        df.setIndex(1, "z", 10);
        assertEquals(10, df.getIndex(1, "z"));

        df.setLabel(1, "x", "xxx");
        assertEquals("xxx", df.getLabel(1, "x"));
    }

    @Test
    public void testMissing() {
        SolidFrame df = SolidFrame.byVars(
                NominalVar.copy("a", "b").withName("x"),
                NominalVar.copy("x", "y").withName("y"),
                NumericVar.wrap(1, 2).withName("z")
        );

        assertEquals(false, df.isMissing(0));
        assertEquals(false, df.isMissing(1));

        df.setMissing(0, "x");
        assertEquals(true, df.isMissing(0, "x"));
        assertEquals(true, df.isMissing(0, 0));
        assertEquals(true, df.isMissing(0));
        assertEquals(false, df.isMissing(1));

        df.setMissing(0, 1);
        assertEquals(true, df.isMissing(0, "y"));
        assertEquals(true, df.isMissing(0, 0));
        assertEquals(true, df.isMissing(0));
        assertEquals(false, df.isMissing(1));
    }
}
