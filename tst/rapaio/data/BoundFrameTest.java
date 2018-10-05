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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import rapaio.core.RandomSource;
import rapaio.datasets.Datasets;

import java.util.ArrayList;
import java.util.function.BiConsumer;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class BoundFrameTest {

    private static final double TOL = 1e-20;

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

    @Before
    public void setUp() {
        RandomSource.setSeed(1234);
    }

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

        Var[] vars = new Var[]{
                VarDouble.wrap(1, 2, 3, Double.NaN).withName("a"),
                VarInt.wrap(1, 2, 3, Integer.MIN_VALUE).withName("b"),
                VarLong.wrap(1, 2, 3, Long.MIN_VALUE).withName("c"),
                VarBinary.copy(1, 0, 1, -1).withName("d"),
                VarNominal.copy("x1", "x2", "x3", "?").withName("e")
        };

        SolidFrame df = SolidFrame.byVars(vars);

        SolidFrame df1 = SolidFrame.byVars(vars[0], vars[1], vars[2]);
        SolidFrame df2 = SolidFrame.byVars(vars[3], vars[4]);

        Frame bound1 = BoundFrame.byVars(vars);
        Frame bound2 = BoundFrame.byVars(df1, df2);

        assertTrue(df.deepEquals(bound1));
        assertTrue(df.deepEquals(bound2));

        Frame empty = BoundFrame.byVars(new Frame[]{});

        assertEquals(0, empty.varCount());
        assertEquals(0, empty.rowCount());
    }

    @Test
    public void testInvalidBindByRowsWithDifferentRowCount() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can't bind by rows frames with different variable counts.");
        BoundFrame.byRows(df1, df2, df3);
    }

    @Test
    public void testInvalidBindRowsWithDifferentNames() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can't bind by rows frames with different variable names " +
                "or with different order of the variables.");
        BoundFrame.byRows(df1, df2, df4);
    }

    @Test
    public void testBindRowsVarsWithDifferentTypes() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Can't bind by rows variable of different types.");
        BoundFrame.byRows(df1, df2, df5);
    }

    @Test
    public void testBuildersByRows() {

        Frame source = Datasets.loadRandom();

        Frame df1 = source.mapRows(Mapping.range(0, 20)).solidCopy();
        Frame df2 = source.mapRows(Mapping.range(20, 100)).solidCopy();

        Frame bound = BoundFrame.byRows(df1, df2);
        assertTrue(bound.deepEquals(source));

        VType[] types = new VType[]{VType.BINARY, VType.DOUBLE, VType.INT, VType.LONG, VType.NOMINAL};
        String[] names = new String[]{"boolean", "double", "int", "long", "nominal"};
        BiConsumer[] verifyIndex = new BiConsumer[]{
                (i, j) -> assertEquals(source.getInt((int) i, (int) j), bound.getInt((int) i, (int) j)),
                (i, j) -> assertEquals(source.getDouble((int) i, (int) j), bound.getDouble((int) i, (int) j), TOL),
                (i, j) -> assertEquals(source.getInt((int) i, (int) j), bound.getInt((int) i, (int) j)),
                (i, j) -> assertEquals(source.getLong((int) i, (int) j), bound.getLong((int) i, (int) j)),
                (i, j) -> assertEquals(source.getLabel((int) i, (int) j), bound.getLabel((int) i, (int) j)),
        };
        BiConsumer[] verifyName = new BiConsumer[]{
                (i, j) -> assertEquals(source.getInt((int) i, (int) j), bound.getInt((int) i, names[(int) j])),
                (i, j) -> assertEquals(source.getDouble((int) i, (int) j), bound.getDouble((int) i, names[(int) j]), TOL),
                (i, j) -> assertEquals(source.getInt((int) i, (int) j), bound.getInt((int) i, names[(int) j])),
                (i, j) -> assertEquals(source.getLong((int) i, (int) j), bound.getLong((int) i, names[(int) j])),
                (i, j) -> assertEquals(source.getLabel((int) i, (int) j), bound.getLabel((int) i, names[(int) j])),
        };

        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 5; j++) {
                verifyIndex[j].accept(i, j);
                verifyName[j].accept(i, j);
                assertEquals(source.isMissing(i, j), bound.isMissing(i, j));
            }
        }

        for (int i = 0; i < 10; i++) {
            bound.setInt(i, 0, 1);
            bound.setInt(i+10, "boolean", 0);
            bound.setMissing(i+20, 0);
            bound.setMissing(i+30, "boolean");

            bound.setDouble(i, 1, 1);
            bound.setDouble(i+10, "double", 2);

            bound.setInt(i, 2, 1);
            bound.setInt(i+10, "int", 2);

            bound.setLong(i, 3, 1L);
            bound.setLong(i+10, "long", 2L);

            bound.setLabel(i, 4, "xx");
            bound.setLabel(i+10, "nominal", "yy");
        }

        assertTrue(bound.deepEquals(BoundFrame.byRows(df1, df2)));
        for (int i = 0; i < 5; i++) {
            assertEquals(i, bound.varIndex(names[i]));
            assertEquals(names[i], bound.varName(i));
            assertSame(types[i], bound.type(names[i]));
        }

        String[] levels = new String[]{"?", "c", "d", "b", "a", "e", "xx", "yy"};
        assertArrayEquals(levels, bound.levels("nominal").toArray(new String[0]));

        // test build from an empty frame
        Frame df = BoundFrame.byRows(df3);
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

        Frame empty = BoundFrame.byVars(new ArrayList<>());
        assertEquals(0, empty.varCount());
        assertEquals(0, empty.rowCount());
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
        assertEquals(1 / 3., df.getDouble(1, "1/x"), 1e-12);
    }

    @Test
    public void testInvalidVarName() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Variable with name: uu does not exists.");
        Frame df = Datasets.loadRandom();
        BoundFrame.byVars(df).rvar("uu");
    }

    @Test
    public void testInvalidClearRows() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("This operation is not available for bound frames.");
        Frame df = Datasets.loadRandom();
        BoundFrame.byVars(df).addRows(10);
    }

    @Test
    public void testInvalidAddRows() {
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("This operation is not available for bound frames.");
        Frame df = Datasets.loadRandom();
        BoundFrame.byVars(df).clearRows();
    }
}
