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
import rapaio.data.filter.frame.FRefSort;
import rapaio.datasets.Datasets;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class MappedFrameTest {

    private static final double TOL = 1e-20;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void colsSortedTest() {
        Frame orig = Datasets.loadIrisDataset();
        Frame sort = FRefSort.by(RowComparators.doubleComparator(orig.rvar(1), true)).fapply(orig);
        sort = FRefSort.by(RowComparators.doubleComparator(orig.rvar(2), true)).fapply(sort);
        for (int i = 0; i < sort.rowCount(); i++) {
            assertEquals(sort.getDouble(i, 0), sort.rvar(0).getDouble(i), 1e-10);
        }
    }

    @Test
    public void testBuilders() {
        Frame df = SolidFrame.byVars(
                VarDouble.wrap(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).withName("x"),
                VarInt.wrap(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).withName("y")
        );

        Frame mapped = MappedFrame.byRow(df, 0, 2, 4, 6, 8);
        assertEquals(5, mapped.rowCount());
        assertEquals(2, mapped.varCount());
        assertEquals(1, mapped.getDouble(0, "x"), 1e-12);
        assertEquals(9, mapped.getDouble(4, "x"), 1e-12);

        mapped = MappedFrame.byRow(df, Mapping.range(0, 10), "x,y");
        assertEquals(2, mapped.varCount());
        assertEquals(10, mapped.rowCount());

        mapped = MappedFrame.byRow(df, Mapping.range(0, 10), "x");
        assertEquals(1, mapped.varCount());
        assertEquals(10, mapped.rowCount());

        Mapping mapping = null;
        Frame empty = df.mapRows(mapping);
        assertEquals(0, empty.rowCount());
    }

    @Test
    public void testMapAndBound() {
        final int N = 10;

        Var x = VarDouble.from(N, row -> row * 1.0).withName("x");
        Var y = VarInt.from(N, row -> row * 2).withName("y");
        Var z = VarDouble.from(N, row -> 1.0 / row).withName("z");
        Frame df1 = SolidFrame.byVars(x, y, z);

        Frame a = df1
                .mapRows(Mapping.range(0, 10))
                .mapRows(Mapping.range(0, 4))
                .mapVars("x,y");

        Frame b = df1.mapRows(Mapping.range(0, 4)).mapVars("z");
        Frame c = df1.mapRows(Mapping.range(4, 10)).mapVars("x,y");
        Frame d = df1.mapRows(Mapping.range(4, 10)).mapVars("z");

        Frame df2 = a.bindVars(b).bindRows(c.bindVars(d));

        assertEquals(df1.rowCount(), df2.rowCount());
        assertEquals(df1.varCount(), df2.varCount());

        assertTrue(df1.deepEquals(df2));

        df2 = df2.copy();

        assertEquals(df1.rowCount(), df2.rowCount());
        assertEquals(df1.varCount(), df2.varCount());
        for (int i = 0; i < df1.varNames().length; i++) {
            assertEquals(df1.varNames()[i], df2.varNames()[i]);
        }
        for (int i = 0; i < df1.rowCount(); i++) {
            for (int j = 0; j < df1.varCount(); j++) {
                assertEquals(df1.getDouble(i, j), df2.getDouble(i, j), 1e-12);
            }
        }

        df2 = a.bindRows(c).bindVars(b.bindRows(d));

        assertTrue(df1.deepEquals(df2));

        df2 = MappedFrame.byRow(df1, Mapping.range(0, 10)).mapVars("x");
        df2 = df2.bindVars(y, z);

        assertTrue(df1.deepEquals(df2));

        Frame df3 = df1
                .mapRows(1, 3, 5, 7, 9)
                .mapVars("x,z")
                .mapRows(1, 3)
                .mapVars("z");

        assertEquals(1, df3.varCount());
        assertSame(df3.rvar(0).type(), VType.DOUBLE);
        assertEquals(1.0 / 3, df3.getDouble(0, 0), TOL);
        assertEquals(1.0 / 7, df3.getDouble(1, 0), TOL);

        assertTrue(VarDouble.wrap(1.0 / 3, 1.0 / 7).withName("z").deepEquals(df3.rvar(0)));
    }

    @Test
    public void testVarNamesAndTypes() {
        final int N = 10;
        Var x = VarDouble.from(N, row -> row * 1.0).withName("x");
        Var y = VarInt.from(N, row -> row * 2).withName("y");
        Var z = VarDouble.from(N, row -> 1.0 / row).withName("z");
        Frame df1 = SolidFrame.byVars(x, y, z).mapRows(0, 1, 2);

        String[] varNames = df1.varNames();
        for (int i = 0; i < 3; i++) {
            assertEquals(varNames[i], df1.varName(i));
        }

        assertSame(VType.DOUBLE, df1.type("x"));
        assertSame(VType.INT, df1.type("y"));
        assertSame(VType.DOUBLE, df1.type("z"));
    }

    @Test
    public void testInvalidVarIndex() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("var name: y does not exist");
        SolidFrame.byVars(VarDouble.seq(10).withName("x")).mapRows(0, 1).rvar("y");
    }

    @Test
    public void testAddClearRows() {
        final int N = 10;
        Var x = VarDouble.from(N, row -> row * 1.0).withName("x");
        Var y = VarInt.from(N, row -> row * 2).withName("y");
        Var z = VarDouble.from(N, row -> 1.0 / row).withName("z");
        Frame df1 = SolidFrame.byVars(x, y, z).mapRows(0, 1, 2);

        Frame df2 = df1.addRows(100);
        assertEquals(103, df2.rowCount());
        for (int i = 3; i < 103; i++) {
            assertTrue(df2.isMissing(i, "x"));
        }

        Frame df3 = df1.clearRows();
        assertEquals(0, df3.rowCount());
        assertEquals("x", df3.varName(0));
    }

    @Test
    public void testGettersSetters() {
        List<Var> varList = Arrays.asList(
                VarDouble.wrap(0, VarDouble.MISSING_VALUE, 2, VarDouble.MISSING_VALUE).withName("a"),
                VarInt.wrap(0, VarInt.MISSING_VALUE, 2, VarInt.MISSING_VALUE).withName("b"),
                VarNominal.copy("a", "?", "b", "?").withName("c"),
                VarLong.copy(0, VarLong.MISSING_VALUE, 2, VarLong.MISSING_VALUE).withName("d"),
                VarBinary.copy(1, 0, 1, 1).withName("e")
        );
        varList.get(4).setMissing(1);

        MappedFrame map = (MappedFrame) SolidFrame.byVars(varList).mapRows(0, 1, 2).mapVars("a,b,c,d,e");

        assertEquals(5, map.varCount());

        assertEquals(0, map.getDouble(0, "a"), TOL);
        assertTrue(map.isMissing(1, "a"));
        assertEquals(2, map.getDouble(2, "a"), TOL);

        assertEquals(0, map.getInt(0, "b"));
        assertTrue(map.isMissing(1, "b"));
        assertEquals(2, map.getInt(2, "b"));

        assertEquals("a", map.getLabel(0, "c"));
        assertTrue(map.isMissing(1, "c"));
        assertEquals("b", map.getLabel(2, "c"));

        assertEquals(0, map.getLong(0, "d"));
        assertTrue(map.isMissing(1, "d"));
        assertEquals(2, map.getLong(2, "d"));

        assertEquals(1, map.getInt(0, "e"));
        assertTrue(map.isMissing(1, "e"));
        assertEquals(1, map.getInt(2, "e"));

        map.setDouble(0, 0, 10);
        map.setDouble(1, "a", 20);
        map.setInt(0, 1, 10);
        map.setInt(1, "b", 20);
        map.setLabel(0, 2, "10");
        map.setLabel(1, "c", "20");
        map.setLong(0, 3, 10);
        map.setLong(1, "d", 20);
        map.setInt(0, 4, 1);
        map.setInt(1, "e", 0);

        assertEquals(10, map.getDouble(0, 0), TOL);
        assertEquals(20, map.getDouble(1, "a"), TOL);
        assertEquals(10, map.getInt(0, 1));
        assertEquals(20, map.getInt(1, "b"));
        assertEquals("10", map.getLabel(0, 2));
        assertEquals("20", map.getLabel(1, "c"));
        assertEquals(10, map.getLong(0, 3));
        assertEquals(20, map.getLong(1, "d"));
        assertEquals(1, map.getInt(0, 4));
        assertEquals(0, map.getInt(1, "e"));

        List<String> levels = map.levels("c");
        assertEquals(5, levels.size());
        assertEquals("?", levels.get(0));
        assertEquals("a", levels.get(1));
        assertEquals("b", levels.get(2));
        assertEquals("10", levels.get(3));
        assertEquals("20", levels.get(4));

        map.setMissing(0, 0);
        map.setMissing(1, "a");

        assertTrue(map.isMissing(0));
        assertFalse(map.isMissing(2));
        assertTrue(map.isMissing(0, 0));
        assertTrue(map.isMissing(0, "a"));
    }
}
