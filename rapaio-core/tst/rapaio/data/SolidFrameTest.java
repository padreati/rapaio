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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.narray.NArrays;
import rapaio.narray.Shape;
import rapaio.narray.NArray;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SolidFrameTest {

    private static final double TOL = 1e-12;

    private Var x;
    private Var y;
    private Frame df1;

    @BeforeEach
    void setUp() {
        x = VarDouble.wrap(1, 2, 3, 4).name("x");
        y = VarNominal.copy("a", "c", "b", "a").name("y");
        df1 = SolidFrame.byVars(
                VarDouble.seq(1, 10).name("x"),
                VarDouble.seq(21, 30).name("y"),
                VarDouble.seq(101, 111).name("z"));
    }

    @Test
    void testEmptySolidFrame() {
        Frame df = SolidFrame.byVars();
        assertEquals(0, df.rowCount());
        assertEquals(0, df.varCount());
    }

    @Test
    void testInvalidVarName() {
        assertEquals(-1, df1.varIndex("q"));
    }

    @Test
    void testInvalidVarIndexNegative() {
        var ex = assertThrows(ArrayIndexOutOfBoundsException.class, () -> df1.rvar(-10));
        assertEquals("Index -10 out of bounds for length 3", ex.getMessage());
    }

    @Test
    void testInvalidBuildFromMappedVars() {
        var ex = assertThrows(IllegalArgumentException.class, () -> SolidFrame.byVars(x, y.mapRows(Mapping.range(0, 4))));
        assertEquals("Not allowed mapped vectors in solid frame", ex.getMessage());
    }

    @Test
    void testInvalidBuildFromBoundVars() {
        var ex = assertThrows(IllegalArgumentException.class, () -> SolidFrame.byVars(x, y.mapRows(0, 1).bindRows(y.mapRows(2, 3))));
        assertEquals("Not allowed bounded vectors in solid frame", ex.getMessage());
    }

    @Test
    void testColIndexes() {
        assertEquals(3, df1.varCount());
        assertEquals("x", df1.varNames()[0]);
        assertEquals("z", df1.varNames()[2]);
        assertEquals(0, df1.varIndex("x"));
        assertEquals(2, df1.varIndex("z"));

        assertEquals("x", df1.varNames()[0]);
        assertEquals("y", df1.varNames()[1]);
        assertEquals("z", df1.varNames()[2]);

        assertEquals("x", df1.varName(0));
        assertEquals("y", df1.varName(1));
        assertEquals("z", df1.varName(2));
    }

    @Test
    void testConvenientMethods() {
        List<Var> vars = new ArrayList<>();
        vars.add(VarDouble.copy(1., 2., 3., 4.).name("x"));
        vars.add(VarDouble.copy(3., 5., 9., 12.).name("y"));
        vars.add(VarNominal.empty(4, "ana", "are", "mere").name("name"));
        vars.add(VarInt.seq(1, 4).name("index"));
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

        df.setInt(1, 2, 2);
        assertEquals("mere", df.getLabel(1, 2));

        assertEquals(1, df.getInt(0, 3));
        assertEquals(2, df.getInt(1, 3));
        assertEquals(3, df.getInt(2, 3));
        assertEquals(4, df.getInt(3, 3));

        df.setInt(0, 3, 5);
        assertEquals(5, df.getInt(0, 3));
    }


    @Test
    void testBuilders() {

        Frame df1 = SolidFrame.byVars(x, y);

        assertEquals(2, df1.varCount());
        assertEquals(4, df1.rowCount());

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
                SolidFrame.byVars(VarDouble.wrap(1, 2).name("x"))
                        .bindRows(SolidFrame.byVars(VarDouble.wrap(3, 4).name("x")))
        );
        assertEquals(2, df2.varCount());
        assertEquals(4, df2.rowCount());
        for (int i = 0; i < df1.rowCount(); i++) {
            assertEquals(df1.getDouble(i, "x"), df2.getDouble(i, "x"), TOL);
            assertEquals(df1.getLabel(i, "y"), df2.getLabel(i, "y"));
        }

        assertEquals(2, SolidFrame.byVars(2, x, y).rowCount());
        Frame empty1 = SolidFrame.emptyFrom(df1, 0);
        assertEquals(0, empty1.rowCount());
        assertEquals("x", empty1.rvar(0).name());
    }

    @Test
    void testMatrixBuilders() {
        NArray<Double> rm = NArrays.stride(Shape.of(3, 3),
                1, 2, 3,
                2, 3, 4,
                3, 4, 5
        );
        Frame fm = SolidFrame.matrix(rm, "a", "b", "c");

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(rm.get(i, j), fm.getDouble(i, j), TOL);
            }
        }
        assertTrue(fm.deepEquals(SolidFrame.matrix(rm, Arrays.asList("a", "b", "c"))));

        Frame df = SolidFrame.matrix(10, "a", "b", "c");
        assertEquals(10, df.rowCount());
        assertEquals(3, df.varCount());

        for (int i = 0; i < df.varCount(); i++) {
            for (int j = 0; j < df.rowCount(); j++) {
                assertEquals(0, df.getDouble(j, i), 1e-12);
            }
        }
    }

    @Test
    void testType() {
        assertEquals(VarType.DOUBLE, SolidFrame.byVars(x, y).type("x"));
        assertEquals(VarType.NOMINAL, SolidFrame.byVars(x, y).type("y"));
    }

    @Test
    void testAddClearRows() {
        NArray<Double> rm = NArrays.stride(Shape.of(3, 3),
                1, 2, 3,
                2, 3, 4,
                3, 4, 5
        );
        Frame fm = SolidFrame.matrix(rm, "a", "b", "c");
        fm.addRows(3);

        assertEquals(6, fm.rowCount());
        for (int i = 0; i < 3; i++) {
            assertEquals(i + 1, fm.getDouble(i, "a"), TOL);
            assertTrue(fm.isMissing(i + 3, "a"));
        }

        fm.clearRows();
        assertEquals(0, fm.rowCount());
        assertEquals(3, fm.varCount());
    }
}
