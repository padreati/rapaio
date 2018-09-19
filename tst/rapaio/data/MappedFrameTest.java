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
import rapaio.data.filter.frame.FFRefSort;
import rapaio.datasets.Datasets;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class MappedFrameTest {

    private static final double TOL = 1e-20;

    @Test
    public void colsSortedTest() throws IOException, URISyntaxException {
        Frame orig = Datasets.loadIrisDataset();
        Frame sort = new FFRefSort(RowComparators.doubleComparator(orig.rvar(1), true)).fitApply(orig);
        sort = new FFRefSort(RowComparators.doubleComparator(orig.rvar(2), true)).fitApply(sort);
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

        df1.printLines();

        Frame b = df1.mapRows(Mapping.range(0, 4)).mapVars("z");
        Frame c = df1.mapRows(Mapping.range(4, 10)).mapVars("x,y");
        Frame d = df1.mapRows(Mapping.range(4, 10)).mapVars("z");

        Frame df2 = a.bindVars(b).bindRows(c.bindVars(d));
        df2.printLines();

        assertEquals(df1.rowCount(), df2.rowCount());
        assertEquals(df1.varCount(), df2.varCount());

        assertTrue(df1.deepEquals(df2));

        df2 = df2.solidCopy();
        df2.printLines();

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
        df2.printLines();

        assertTrue(df1.deepEquals(df2));

        df2 = MappedFrame.byRow(df1, Mapping.range(0, 10)).mapVars("x");
        df2 = df2.bindVars(y, z);
        df2.printLines();

        assertTrue(df1.deepEquals(df2));

        Frame df3 = df1
                .mapRows(1, 3, 5, 7, 9)
                .mapVars("x,z")
                .mapRows(1, 3)
                .mapVars("z");
        df3.printLines();

        assertTrue(df3.varCount() == 1);
        assertTrue(df3.rvar(0).type() == VType.DOUBLE);
        assertEquals(1.0 / 3, df3.getDouble(0, 0), TOL);
        assertEquals(1.0 / 7, df3.getDouble(1, 0), TOL);

        assertTrue(VarDouble.wrap(1.0 / 3, 1.0 / 7).withName("z").deepEquals(df3.rvar(0)));
    }
}
