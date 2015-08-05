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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SolidFrameTest {

    @Test
    public void testEmptySolidFrame() {
        Frame df = SolidFrame.newWrapOf();
        assertEquals(0, df.rowCount());
        assertEquals(0, df.varCount());
    }

    @Test
    public void testColIndexes() {
        Frame df = SolidFrame.newWrapOf(
                Numeric.newEmpty().withName("x"),
                Numeric.newEmpty().withName("y"),
                Numeric.newEmpty().withName("z"));

        assertEquals(3, df.varCount());
        assertEquals("x", df.varNames()[0]);
        assertEquals("z", df.varNames()[2]);
        assertEquals(0, df.varIndex("x"));
        assertEquals(2, df.varIndex("z"));

        try {
            df.varIndex("q");
            assertTrue("should raise an exception", false);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            df.getVar(10);
            assertTrue("should raise an exception", false);
        } catch (IllegalArgumentException ignored) {
        }

        try {
            df.getVar(-1);
            assertTrue("should raise an exception", false);
        } catch (IllegalArgumentException ignored) {
        }

        assertEquals("x", df.varNames()[0]);
        assertEquals("y", df.varNames()[1]);
        assertEquals("z", df.varNames()[2]);
    }

    @Test
    public void testConvenientMethods() {
        List<Var> vars = new ArrayList<>();
        vars.add(Numeric.newCopyOf(1., 2., 3., 4.).withName("x"));
        vars.add(Numeric.newCopyOf(3., 5., 9., 12.).withName("y"));
        vars.add(Nominal.newEmpty(4, "ana", "are", "mere").withName("name"));
        vars.add(Index.newSeq(1, 4).withName("index"));
        Frame df = SolidFrame.newWrapOf(vars);

        assertEquals(1., df.value(0, 0), 1e-10);
        df.setValue(0, 0, 3.);
        assertEquals(3., df.value(0, 0), 1e-10);

        double t = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            t += df.value(i, 1) - 3.;
        }
        assertEquals(17., t, 1e-10);

        assertTrue(df.getVar("name").missing(0));
        assertTrue(df.getVar("name").missing(3));

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

    @Test
    public void testBuilders() {
        Var x = Numeric.newWrapOf(1, 2, 3, 4).withName("x");
        Var y = Nominal.newCopyOf("a", "c", "b", "a").withName("y");

        Frame df1 = SolidFrame.newWrapOf(x, y);

        assertEquals(2, df1.varCount());
        assertEquals(4, df1.rowCount());

        try {
            SolidFrame.newWrapOf(x, y.mapRows(Mapping.newRangeOf(0, 4)));
            assertTrue("should raise an exception", false);
        } catch (IllegalArgumentException ignored) {
        }

        Frame df2 = SolidFrame.newWrapOf(x).bindVars(y);
        assertEquals(2, df2.varCount());
        assertEquals(4, df2.rowCount());
        for (int i = 0; i < df1.rowCount(); i++) {
            assertEquals(df1.value(i, "x"), df2.value(i, "x"), 1e-12);
            assertEquals(df1.label(i, "y"), df2.label(i, "y"));
        }

        df2 = SolidFrame.newWrapOf(x).bindVars(SolidFrame.newWrapOf(y));
        assertEquals(2, df2.varCount());
        assertEquals(4, df2.rowCount());
        for (int i = 0; i < df1.rowCount(); i++) {
            assertEquals(df1.value(i, "x"), df2.value(i, "x"), 1e-12);
            assertEquals(df1.label(i, "y"), df2.label(i, "y"));
        }

        df2 = df1.mapVars("x").bindVars(df1.mapVars("y"));
        assertEquals(2, df2.varCount());
        assertEquals(4, df2.rowCount());
        for (int i = 0; i < df1.rowCount(); i++) {
            assertEquals(df1.value(i, "x"), df2.value(i, "x"), 1e-12);
            assertEquals(df1.label(i, "y"), df2.label(i, "y"));
        }

        df2 = SolidFrame.newWrapOf(y).bindVars(
                SolidFrame.newWrapOf(Numeric.newWrapOf(1, 2).withName("x"))
                        .bindRows(SolidFrame.newWrapOf(Numeric.newWrapOf(3, 4).withName("x")))
        );
        assertEquals(2, df2.varCount());
        assertEquals(4, df2.rowCount());
        for (int i = 0; i < df1.rowCount(); i++) {
            assertEquals(df1.value(i, "x"), df2.value(i, "x"), 1e-12);
            assertEquals(df1.label(i, "y"), df2.label(i, "y"));
        }

        try {
            SolidFrame.newWrapOf(
                    Numeric.newWrapOf(1, 2).withName("x"),
                    BoundVar.newFrom(Numeric.newWrapOf(3, 4).withName("y"))
            );
            assertTrue("should raise an exception", false);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testMatrixBuilders() {
        Frame df = SolidFrame.newMatrix(10, "a", "b", "c");
        assertEquals(10, df.rowCount());
        assertEquals(3, df.varCount());

        for (int i = 0; i < df.varCount(); i++) {
            for (int j = 0; j < df.rowCount(); j++) {
                assertEquals(0, df.value(j, i), 1e-12);
            }
        }
    }
}
