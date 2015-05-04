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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
@Deprecated
public class BoundFrameTest {

    // frame with first rows
    private Frame df1 = SolidFrame.newWrapOf(
            Numeric.newWrapOf(1, 2, 3, 4).withName("x"),
            Numeric.newWrapOf(1 / 1., 1 / 2., 1 / 3., 1 / 4.).withName("1/x")
    );

    // frame with second set of rows
    private Frame df2 = SolidFrame.newWrapOf(
            Numeric.newWrapOf(5, 6).withName("x"),
            Numeric.newWrapOf(1 / 5., 1 / 6.).withName("1/x")
    );

    // empty frame
    private Frame df3 = SolidFrame.newWrapOf();

    // frame with different column names
    private Frame df4 = SolidFrame.newWrapOf(
            Numeric.newWrapOf(7).withName("a"),
            Numeric.newWrapOf(1 / 7.).withName("b")
    );

    // frame with different column types
    private Frame df5 = SolidFrame.newWrapOf(
            Index.newWrapOf(7).withName("x"),
            Numeric.newWrapOf(1 / 7.).withName("1/x")
    );

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

    @Test
    public void testBuildersByRows() {


        // test simple bind of rows
        BoundFrame df = BoundFrame.newByRows(df1, df2);
        assertEquals(2, df.varCount());
        assertEquals(6, df.rowCount());
        for (int i = 0; i < 6; i++) {
            assertEquals(i + 1, df.value(i, 0), 1e-12);
            assertEquals(1 / (i + 1.), df.value(i, 1), 1e-12);
        }

        // test bind of rows plus an empty frame (different var count)
        try {
            df = BoundFrame.newByRows(df1, df2, df3);
            assertTrue("should raise an exception", false);
        } catch (IllegalArgumentException ignored) {
        }

        // test build from an empty frame
        df = BoundFrame.newByRows(df3);
        assertEquals(0, df.rowCount());
        assertEquals(0, df.varCount());

        // test to build from nothing
        df = BoundFrame.newByRows();
        assertEquals(0, df.rowCount());
        assertEquals(0, df.varCount());

        // test to build from frames with different names
        try {
            df = BoundFrame.newByRows(df1, df2, df4);
            assertTrue("should raise an exception", false);
        } catch (IllegalArgumentException ignored) {
        }

        // test to build from frames with different types
        try {
            df = BoundFrame.newByRows(df1, df2, df5);
            assertTrue("should raise an exception", false);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testVars() {
        Frame df = BoundFrame.newByRows(df1, df2);

        // check var names after binding
        assertEquals(2, df.varNames().length);
        assertEquals("x", df.varNames()[0]);
        assertEquals("1/x", df.varNames()[1]);

        // check the properties of a bounded var after frame bind by rows
        assertEquals(6, df.var(0).rowCount());
        assertEquals(1., df.var(0).value(0), 1e-12);
        assertEquals(6., df.var("x").value(5), 1e-12);
        assertEquals(1 / 1., df.var(1).value(0), 1e-12);
        assertEquals(1 / 6., df.var("1/x").value(5), 1e-12);
        assertEquals(0, df.varIndex("x"));
        assertEquals(1, df.varIndex("1/x"));
    }

    @Test
    public void testBindMapVars() {
        Frame df = BoundFrame.newByVars(df1);
        df = df.bindVars(Numeric.newWrapOf(-1, -2, -3, -4).withName("y"));

        assertEquals(3, df.varCount());
        assertEquals(4, df.rowCount());

        assertEquals(1.0, df.value(0, "x"), 1e-12);
        assertEquals(-4.0, df.value(3, "y"), 1e-12);

        df = BoundFrame.newByVars(df1);
        df = df.bindVars(SolidFrame.newWrapOf(
                Numeric.newWrapOf(-1, -2, -3, -4).withName("y")
        ));
        assertEquals(3, df.varCount());
        assertEquals(4, df.rowCount());

        assertEquals(1.0, df.value(0, "x"), 1e-12);
        assertEquals(-4.0, df.value(3, "y"), 1e-12);

        Frame dfMap = df.mapVars("x,y");
        assertEquals(2, dfMap.varCount());
        assertEquals(4, dfMap.rowCount());

        assertEquals(1.0, dfMap.value(0, "x"), 1e-12);
        assertEquals(-4.0, dfMap.value(3, "y"), 1e-12);
    }

    @Test
    public void testBindMapRows() {
        Frame df = BoundFrame.newByVars(df1);
        df = df.bindRows(df2);

        assertEquals(2, df.varCount());
        assertEquals(6, df.rowCount());
        for (int i = 0; i < 6; i++) {
            assertEquals(i + 1, df.value(i, 0), 1e-12);
            assertEquals(1 / (i + 1.), df.value(i, 1), 1e-12);
        }

        df = df.bindRows(SolidFrame.newWrapOf(Numeric.newEmpty().withName("x"), Numeric.newEmpty().withName("1/x")));
        assertEquals(2, df.varCount());
        assertEquals(6, df.rowCount());
        for (int i = 0; i < 6; i++) {
            assertEquals(i + 1, df.value(i, 0), 1e-12);
            assertEquals(1 / (i + 1.), df.value(i, 1), 1e-12);
        }

        df = df.mapRows(Mapping.newCopyOf(0,2,4));
        assertEquals(2, df.varCount());
        assertEquals(3, df.rowCount());
        assertEquals(1.0, df.value(0, "x"), 1e-12);
        assertEquals(1/3., df.value(1, "1/x"), 1e-12);
    }
}
