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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class BoundFrameTest {

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    // frame with first rows
    private Frame df1 = SolidFrame.byVars(
            VarDouble.wrap(1, 2, 3, 4).withName("x"),
            VarDouble.wrap(1 / 1., 1 / 2., 1 / 3., 1 / 4.).withName("1/x")
    );

    // frame with second set of rows
    private Frame df2 = SolidFrame.byVars(
            VarDouble.wrap(5, 6).withName("x"),
            VarDouble.wrap(1 / 5., 1 / 6.).withName("1/x")
    );

    // empty frame
    private Frame df3 = SolidFrame.byVars();

    // frame with different column names
    private Frame df4 = SolidFrame.byVars(
            VarDouble.wrap(7).withName("a"),
            VarDouble.wrap(1 / 7.).withName("b")
    );

    // frame with different column types
    private Frame df5 = SolidFrame.byVars(
            VarInt.wrap(7).withName("x"),
            VarDouble.wrap(1 / 7.).withName("1/x")
    );

    @Test
    public void testInvalidVarsWithSameName() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("bound frame does not allow variables with the same name");
        BoundFrame.byVars(VarDouble.wrap(1.).withName("x"), VarDouble.wrap(1.).withName("x"));
    }

    @Test
    public void testInvalidFramesWithVarsWithSameName() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("bound frame does not allow variables with the same name: x");
        BoundFrame.byVars(
                SolidFrame.byVars(VarDouble.wrap(1.).withName("x")),
                SolidFrame.byVars(VarDouble.wrap(2.).withName("x")));
    }

    @Test
    public void testBuildersByVars() {
        Frame df = BoundFrame.byVars(
                VarDouble.wrap(1, 2).withName("x"),
                VarDouble.wrap(1 / 1., 1 / 2.).withName("y"));

        assertEquals(2, df.varCount());
        assertEquals(2, df.rowCount());
        assertEquals(1, df.getDouble(0, 0), 1e-12);
        assertEquals(1 / 2., df.getDouble(1, 1), 1e-12);

        df = BoundFrame.byVars(new Var[]{});

        assertEquals(0, df.varCount());
        assertEquals(0, df.rowCount());

        df = BoundFrame.byVars(
                SolidFrame.byVars(VarDouble.wrap(1, 2).withName("x")),
                SolidFrame.byVars(VarDouble.wrap(1 / 1., 1 / 2.).withName("y"))
        );

        assertEquals(2, df.varCount());
        assertEquals(2, df.rowCount());
        assertEquals(1, df.getDouble(0, 0), 1e-12);
        assertEquals(1 / 2., df.getDouble(1, 1), 1e-12);

        df = BoundFrame.byVars(new Frame[]{});

        assertEquals(0, df.varCount());
        assertEquals(0, df.rowCount());

    }

    @Test
    public void testInvalidBindByRowsWithDifferentRowCount() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("can't bind by rows frames with different variable count");
        BoundFrame.byRows(df1, df2, df3);
    }

    @Test
    public void testInvalidBindRowsWithDifferentNames() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("can't bind by rows frames with different variable names or with different order of the variables");
        BoundFrame.byRows(df1, df2, df4);
    }

    @Test
    public void testBindRowsVarsWithDifferentTypes() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("can't bind by rows variable of different types");
        BoundFrame.byRows(df1, df2, df5);
    }

    @Test
    public void testBuildersByRows() {


        // test simple bind of rows
        BoundFrame df = BoundFrame.byRows(df1, df2);
        assertEquals(2, df.varCount());
        assertEquals(6, df.rowCount());
        for (int i = 0; i < 6; i++) {
            assertEquals(i + 1, df.getDouble(i, 0), 1e-12);
            assertEquals(1 / (i + 1.), df.getDouble(i, 1), 1e-12);
        }

        // test build from an empty frame
        df = BoundFrame.byRows(df3);
        assertEquals(0, df.rowCount());
        assertEquals(0, df.varCount());

        // test to build from nothing
        df = BoundFrame.byRows();
        assertEquals(0, df.rowCount());
        assertEquals(0, df.varCount());
    }

    @Test
    public void testVars() {
        Frame df = BoundFrame.byRows(df1, df2);

        // check var names after binding
        assertEquals(2, df.varNames().length);
        assertEquals("x", df.varNames()[0]);
        assertEquals("1/x", df.varNames()[1]);

        // check the properties of a bounded var after frame bind by rows
        assertEquals(6, df.rvar(0).rowCount());
        assertEquals(1., df.rvar(0).getDouble(0), 1e-12);
        assertEquals(6., df.rvar("x").getDouble(5), 1e-12);
        assertEquals(1 / 1., df.rvar(1).getDouble(0), 1e-12);
        assertEquals(1 / 6., df.rvar("1/x").getDouble(5), 1e-12);
        assertEquals(0, df.varIndex("x"));
        assertEquals(1, df.varIndex("1/x"));
    }

    @Test
    public void testBindMapVars() {
        Frame df = BoundFrame.byVars(df1);
        df = df.bindVars(VarDouble.wrap(-1, -2, -3, -4).withName("y"));

        assertEquals(3, df.varCount());
        assertEquals(4, df.rowCount());

        assertEquals(1.0, df.getDouble(0, "x"), 1e-12);
        assertEquals(-4.0, df.getDouble(3, "y"), 1e-12);

        df = BoundFrame.byVars(df1);
        df = df.bindVars(SolidFrame.byVars(
                VarDouble.wrap(-1, -2, -3, -4).withName("y")
        ));
        assertEquals(3, df.varCount());
        assertEquals(4, df.rowCount());

        assertEquals(1.0, df.getDouble(0, "x"), 1e-12);
        assertEquals(-4.0, df.getDouble(3, "y"), 1e-12);

        Frame dfMap = df.mapVars("x,y");
        assertEquals(2, dfMap.varCount());
        assertEquals(4, dfMap.rowCount());

        assertEquals(1.0, dfMap.getDouble(0, "x"), 1e-12);
        assertEquals(-4.0, dfMap.getDouble(3, "y"), 1e-12);
    }

    @Test
    public void testBindMapRows() {
        Frame df = BoundFrame.byVars(df1);
        df = df.bindRows(df2);

        assertEquals(2, df.varCount());
        assertEquals(6, df.rowCount());
        for (int i = 0; i < 6; i++) {
            assertEquals(i + 1, df.getDouble(i, 0), 1e-12);
            assertEquals(1 / (i + 1.), df.getDouble(i, 1), 1e-12);
        }

        df = df.bindRows(SolidFrame.byVars(VarDouble.empty().withName("x"), VarDouble.empty().withName("1/x")));
        assertEquals(2, df.varCount());
        assertEquals(6, df.rowCount());
        for (int i = 0; i < 6; i++) {
            assertEquals(i + 1, df.getDouble(i, 0), 1e-12);
            assertEquals(1 / (i + 1.), df.getDouble(i, 1), 1e-12);
        }

        df = df.mapRows(Mapping.wrap(0, 2, 4));
        assertEquals(2, df.varCount());
        assertEquals(3, df.rowCount());
        assertEquals(1.0, df.getDouble(0, "x"), 1e-12);
        assertEquals(1/3., df.getDouble(1, "1/x"), 1e-12);
    }
}
