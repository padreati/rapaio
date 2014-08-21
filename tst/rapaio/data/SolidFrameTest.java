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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SolidFrameTest {

    @Test
    public void testEmptySolidFrame() {
        Frame df = new SolidFrame(0, new Var[0], new String[]{});
        assertEquals(0, df.rowCount());
        assertEquals(0, df.varCount());
    }

    @Test
    public void testColIndexes() {
        Var[] vars = new Var[]{Numeric.newEmpty(), Numeric.newEmpty(), Numeric.newEmpty()};
        Frame df = new SolidFrame(0, vars, new String[]{"x", "y", "z"});

        assertEquals(3, df.varCount());
        assertEquals("x", df.varNames()[0]);
        assertEquals("z", df.varNames()[2]);
        assertEquals(0, df.varIndex("x"));
        assertEquals(2, df.varIndex("z"));

        boolean exceptional = false;
        try {
            df.varIndex("q");
        } catch (IllegalArgumentException ex) {
            exceptional = true;
        }
        assertEquals(true, exceptional);

        exceptional = false;
        try {
            df.var(10);
        } catch (IllegalArgumentException ex) {
            exceptional = true;
        }
        assertEquals(true, exceptional);

        exceptional = false;
        try {
            df.var(-1);
        } catch (IllegalArgumentException ex) {
            exceptional = true;
        }
        assertEquals(true, exceptional);

        assertEquals("x", df.varNames()[0]);
        assertEquals("y", df.varNames()[1]);
        assertEquals("z", df.varNames()[2]);
    }

    @Test
    public void testConvenientMethods() {
        Var[] vars = new Var[]{
                Numeric.newCopyOf(1., 2., 3., 4.),
                Numeric.newCopyOf(3., 5., 9., 12.),
                Nominal.newEmpty(4, "ana", "are", "mere"),
                Index.newSeq(1, 4)
        };
        Frame df = new SolidFrame(4, vars, new String[]{"x", "y", "name", "index"});

        assertEquals(1., df.value(0, 0), 1e-10);
        df.setValue(0, 0, 3.);
        assertEquals(3., df.value(0, 0), 1e-10);

        double t = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            t += df.value(i, 1) - 3.;
        }
        assertEquals(17., t, 1e-10);

        assertTrue(df.var("name").missing(0));
        assertTrue(df.var("name").missing(3));

        df.setLabel(0, 2, "ana");
        df.setLabel(1, 2, "are");
        df.setLabel(2, 2, "mere");

        assertEquals("ana", df.label(0, 2));
        assertEquals("are", df.label(1, 2));
        assertEquals("mere", df.label(2, 2));

        df.setIndex(1, 2, 3);
        assertEquals("mere", df.label(1, 2));

        assertEquals(1, df.index(0, 3));
        assertEquals(2, df.index(1, 3));
        assertEquals(3, df.index(2, 3));
        assertEquals(4, df.index(3, 3));

        df.setIndex(0, 3, 5);
        assertEquals(5, df.index(0, 3));
    }
}
