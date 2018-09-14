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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SolidFrameTest {

    @Test
    public void testEmptySolidFrame() {
        Frame df = SolidFrame.byVars();
        assertEquals(0, df.rowCount());
        assertEquals(0, df.varCount());
    }

    @Test
    public void testColIndexes() {
        Frame df = SolidFrame.byVars(
                VarDouble.empty().withName("x"),
                VarDouble.empty().withName("y"),
                VarDouble.empty().withName("z"));

        assertEquals(3, df.varCount());
        assertEquals("x", df.varNames()[0]);
        assertEquals("z", df.varNames()[2]);
        assertEquals(0, df.varIndex("x"));
        assertEquals(2, df.varIndex("z"));

        try {
            df.varIndex("q");
            assertTrue("should raise an exception", false);
        } catch (NullPointerException ignored) {
        }

        try {
            df.rvar(10);
            assertTrue("should raise an exception", false);
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        try {
            df.rvar(-1);
            assertTrue("should raise an exception", false);
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        assertEquals("x", df.varNames()[0]);
        assertEquals("y", df.varNames()[1]);
        assertEquals("z", df.varNames()[2]);
    }

    @Test
    public void testConvenientMethods() {
        List<Var> vars = new ArrayList<>();
        vars.add(VarDouble.copy(1., 2., 3., 4.).withName("x"));
        vars.add(VarDouble.copy(3., 5., 9., 12.).withName("y"));
        vars.add(VarNominal.empty(4, "ana", "are", "mere").withName("name"));
        vars.add(VarInt.seq(1, 4).withName("index"));
        Frame df = SolidFrame.byVars(vars);

        assertEquals(1., df.getDouble(0, 0), 1e-10);
        df.setDouble(0, 0, 3.);
        assertEquals(3., df.getDouble(0, 0), 1e-10);

        double t = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            t += df.getDouble(i, 1) - 3.;
        }
        assertEquals(17., t, 1e-10);

        assertTrue(df.rvar("name").isMissing(0));
        assertTrue(df.rvar("name").isMissing(3));

        df.setLabel(0, 2, "ana");
        df.setLabel(1, 2, "are");
        df.setLabel(2, 2, "mere");

        assertEquals("ana", df.getLabel(0, 2));
        assertEquals("are", df.getLabel(1, 2));
        assertEquals("mere", df.getLabel(2, 2));

        df.setInt(1, 2, 3);
        assertEquals("mere", df.getLabel(1, 2));

        assertEquals(1, df.getInt(0, 3));
        assertEquals(2, df.getInt(1, 3));
        assertEquals(3, df.getInt(2, 3));
        assertEquals(4, df.getInt(3, 3));

        df.setInt(0, 3, 5);
        assertEquals(5, df.getInt(0, 3));
    }

    @Test
    public void testBuilders() {
        Var x = VarDouble.wrap(1, 2, 3, 4).withName("x");
        Var y = VarNominal.copy("a", "c", "b", "a").withName("y");

        Frame df1 = SolidFrame.byVars(x, y);

        assertEquals(2, df1.varCount());
        assertEquals(4, df1.rowCount());

        try {
            SolidFrame.byVars(x, y.mapRows(Mapping.range(0, 4)));
            assertTrue("should raise an exception", false);
        } catch (IllegalArgumentException ignored) {
        }

        Frame df2 = SolidFrame.byVars(x).bindVars(y);
        assertEquals(2, df2.varCount());
        assertEquals(4, df2.rowCount());
        for (int i = 0; i < df1.rowCount(); i++) {
            assertEquals(df1.getDouble(i, "x"), df2.getDouble(i, "x"), 1e-12);
            assertEquals(df1.getLabel(i, "y"), df2.getLabel(i, "y"));
        }

        df2 = SolidFrame.byVars(x).bindVars(SolidFrame.byVars(y));
        assertEquals(2, df2.varCount());
        assertEquals(4, df2.rowCount());
        for (int i = 0; i < df1.rowCount(); i++) {
            assertEquals(df1.getDouble(i, "x"), df2.getDouble(i, "x"), 1e-12);
            assertEquals(df1.getLabel(i, "y"), df2.getLabel(i, "y"));
        }

        df2 = df1.mapVars("x").bindVars(df1.mapVars("y"));
        assertEquals(2, df2.varCount());
        assertEquals(4, df2.rowCount());
        for (int i = 0; i < df1.rowCount(); i++) {
            assertEquals(df1.getDouble(i, "x"), df2.getDouble(i, "x"), 1e-12);
            assertEquals(df1.getLabel(i, "y"), df2.getLabel(i, "y"));
        }

        df2 = SolidFrame.byVars(y).bindVars(
                SolidFrame.byVars(VarDouble.wrap(1, 2).withName("x"))
                        .bindRows(SolidFrame.byVars(VarDouble.wrap(3, 4).withName("x")))
        );
        assertEquals(2, df2.varCount());
        assertEquals(4, df2.rowCount());
        for (int i = 0; i < df1.rowCount(); i++) {
            assertEquals(df1.getDouble(i, "x"), df2.getDouble(i, "x"), 1e-12);
            assertEquals(df1.getLabel(i, "y"), df2.getLabel(i, "y"));
        }

        try {
            SolidFrame.byVars(
                    VarDouble.wrap(1, 2).withName("x"),
                    BoundVar.from(VarDouble.wrap(3, 4).withName("y"))
            );
            assertTrue("should raise an exception", false);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testMatrixBuilders() {
        Frame df = SolidFrame.matrix(10, "a", "b", "c");
        assertEquals(10, df.rowCount());
        assertEquals(3, df.varCount());

        for (int i = 0; i < df.varCount(); i++) {
            for (int j = 0; j < df.rowCount(); j++) {
                assertEquals(0, df.getDouble(j, i), 1e-12);
            }
        }
    }
}
