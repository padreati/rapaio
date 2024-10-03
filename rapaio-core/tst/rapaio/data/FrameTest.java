/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class FrameTest {

    private static final double TOL = 1e-20;

    @Test
    void testRemove() {
        SolidFrame df = SolidFrame.byVars(
                VarNominal.copy("a", "b").name("x"),
                VarNominal.copy("x", "y").name("y"),
                VarDouble.wrap(1, 2).name("z")
        );
        Frame df1 = df.removeVars(VarRange.of("x,z"));

        assertEquals(1, df1.varCount());
        assertEquals("y", df1.varNames()[0]);

        df1 = df.removeVars(VarRange.of("y"));
        assertEquals(2, df1.varCount());
        assertEquals("x", df1.varNames()[0]);
        assertEquals("z", df1.varNames()[1]);

        df1 = df.removeRows(0);
        assertEquals(1, df1.rowCount());
        assertEquals(3, df1.varCount());
        assertEquals("b", df1.getLabel(0, "x"));

        Frame copy = df.removeVars(VarRange.of(new ArrayList<>()));
        assertTrue(copy.deepEquals(df));
    }

    @Test
    void testSetters() {
        SolidFrame df = SolidFrame.byVars(
                VarNominal.copy("a", "b").name("x"),
                VarNominal.copy("x", "y").name("y"),
                VarDouble.wrap(1, 2).name("z")
        );

        df.setDouble(1, "z", 100);
        assertEquals(100, df.getDouble(1, "z"), 1e-12);

        df.setInt(1, "z", 10);
        assertEquals(10, df.getInt(1, "z"));

        df.setLabel(1, "x", "xxx");
        assertEquals("xxx", df.getLabel(1, "x"));
    }

    @Test
    void testMissing() {
        SolidFrame df = SolidFrame.byVars(
                VarNominal.copy("a", "b").name("x"),
                VarNominal.copy("x", "y").name("y"),
                VarDouble.wrap(1, 2).name("z")
        );

        assertFalse(df.isMissing(0));
        assertFalse(df.isMissing(1));

        df.setMissing(0, "x");
        assertTrue(df.isMissing(0, "x"));
        assertTrue(df.isMissing(0, 0));
        assertTrue(df.isMissing(0));
        assertFalse(df.isMissing(1));

        df.setMissing(0, 1);
        assertTrue(df.isMissing(0, "y"));
        assertTrue(df.isMissing(0, 0));
        assertTrue(df.isMissing(0));
        assertFalse(df.isMissing(1));
    }

    @Test
    void testMappers() {

        SolidFrame df = SolidFrame.byVars(
                VarDouble.seq(100).name("x"),
                VarDouble.seq(100).name("y"),
                VarDouble.seq(100).name("z")
        );

        Frame map1 = df.mapVars("x", "y");
        Frame map2 = df.mapVars(Collections.singletonList("x,y"));

        assertTrue(map1.deepEquals(map2));
        assertEquals(2, map1.varCount());
        assertEquals("x", map1.varName(0));
        assertEquals("y", map2.varName(1));

        Frame map3 = df.removeVars("z");
        Frame map4 = df.removeVars(2);
        Frame map5 = df.removeVars(VarRange.of(2));

        assertTrue(map1.deepEquals(map3));
        assertTrue(map1.deepEquals(map4));
        assertTrue(map1.deepEquals(map5));
    }

    @Test
    void testVarStream() {

        List<Var> varList = Arrays.asList(
                VarDouble.seq(100).name("x"),
                VarDouble.seq(100).name("y"),
                VarDouble.seq(100).name("z")
        );
        SolidFrame df = SolidFrame.byVars(varList);

        Var[] array = df.varStream().toArray(Var[]::new);
        List<Var> list = df.varList();

        for (int i = 0; i < 3; i++) {
            assertTrue(varList.get(i).deepEquals(array[i]));
            assertTrue(varList.get(i).deepEquals(list.get(i)));
        }
    }

    @Test
    void testSpotStream() {
        List<Var> varList = Arrays.asList(
                VarDouble.seq(100).name("x"),
                VarDouble.seq(100).name("y"),
                VarDouble.seq(100).name("z")
        );
        SolidFrame df = SolidFrame.byVars(varList);

        df.stream().forEach(s -> assertEquals(s.row(), s.getDouble("x"), TOL));
        df.spotList().forEach(s -> assertEquals(s.row(), s.getDouble("y"), TOL));
    }

    @Test
    void testDeepEquals() {

        assertFalse(SolidFrame.byVars(VarDouble.seq(100)).deepEquals(SolidFrame.byVars(VarDouble.seq(10))));
        assertFalse(SolidFrame.byVars(VarDouble.seq(10).name("x")).deepEquals(SolidFrame.byVars(
                VarDouble.seq(10).name("x"),
                VarDouble.seq(10).name("y")
        )));
        assertFalse(SolidFrame.byVars(VarDouble.seq(10)).deepEquals(SolidFrame.byVars(
                VarDouble.seq(10).name("x")
        )));

    }

}
