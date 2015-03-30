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
 */

package rapaio.data;

import org.junit.Test;
import rapaio.data.filter.frame.FFRefSort;
import rapaio.datasets.Datasets;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class MappedFrameTest {

    @Test
    public void colsSortedTest() throws IOException, URISyntaxException {
        Frame orig = Datasets.loadIrisDataset();
        Frame sort = new FFRefSort(RowComparators.numeric(orig.var(1), true)).fitApply(orig);
        sort = new FFRefSort(RowComparators.numeric(orig.var(2), true)).fitApply(sort);
        for (int i = 0; i < sort.rowCount(); i++) {
            assertEquals(sort.value(i, 0), sort.var(0).value(i), 1e-10);
        }
    }

    @Test
    public void testBuilders() {
        Frame df = SolidFrame.newWrapOf(
                Numeric.newWrapOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).withName("x"),
                Index.newWrapOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).withName("y")
        );

        Frame mapped = MappedFrame.newByRow(df, 0, 2, 4, 6, 8);
        assertEquals(5, mapped.rowCount());
        assertEquals(2, mapped.varCount());
        assertEquals(1, mapped.value(0, "x"), 1e-12);
        assertEquals(9, mapped.value(4, "x"), 1e-12);

        mapped = MappedFrame.newByRow(df, Mapping.newRangeOf(0, 10), "x,y");
        assertEquals(2, mapped.varCount());
        assertEquals(10, mapped.rowCount());

        mapped = MappedFrame.newByRow(df, Mapping.newRangeOf(0, 10), "x");
        assertEquals(1, mapped.varCount());
        assertEquals(10, mapped.rowCount());
    }

    @Test
    public void testMapAndBound() {
        Var x = Numeric.newWrapOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).withName("x");
        Var y = Index.newWrapOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).withName("y");
        Var z = Numeric.newWrapOf(1 / 1., 1 / 2., 1 / 3., 1 / 4., 1 / 5., 1 / 6., 1 / 7., 1 / 8., 1 / 9., 1 / 10.).withName("z");
        Frame df1 = SolidFrame.newWrapOf(x, y, z);

        Frame a = df1
                .mapRows(Mapping.newRangeOf(0, 10))
                .mapRows(Mapping.newRangeOf(0, 4))
                .mapVars("x,y");
        Frame b = df1.mapRows(Mapping.newRangeOf(0, 4)).mapVars("z");
        Frame c = df1.mapRows(Mapping.newRangeOf(4, 10)).mapVars("x,y");
        Frame d = df1.mapRows(Mapping.newRangeOf(4, 10)).mapVars("z");

        Frame df2 = a.bindVars(b).bindRows(c.bindVars(d));

        assertEquals(df1.rowCount(), df2.rowCount());
        assertEquals(df1.varCount(), df2.varCount());
        for (int i = 0; i < df1.rowCount(); i++) {
            for (int j = 0; j < df1.varCount(); j++) {
                assertEquals(df1.value(i, j), df2.value(i, j), 1e-12);
            }
        }

        df2 = df2.solidCopy();
        assertEquals(df1.rowCount(), df2.rowCount());
        assertEquals(df1.varCount(), df2.varCount());
        for (int i = 0; i < df1.varNames().length; i++) {
            assertEquals(df1.varNames()[i], df2.varNames()[i]);
        }
        for (int i = 0; i < df1.rowCount(); i++) {
            for (int j = 0; j < df1.varCount(); j++) {
                assertEquals(df1.value(i, j), df2.value(i, j), 1e-12);
            }
        }


        df2 = a.bindRows(c).bindVars(b.bindRows(d));

        assertEquals(df1.rowCount(), df2.rowCount());
        assertEquals(df1.varCount(), df2.varCount());
        for (int i = 0; i < df1.rowCount(); i++) {
            for (int j = 0; j < df1.varCount(); j++) {
                assertEquals(df1.value(i, j), df2.value(i, j), 1e-12);
            }
        }

        df2 = MappedFrame.newByRow(df1, Mapping.newRangeOf(0, 10)).mapVars("x");
        df2 = df2.bindVars(y, z);

        assertEquals(df1.rowCount(), df2.rowCount());
        assertEquals(df1.varCount(), df2.varCount());
        for (int i = 0; i < df1.rowCount(); i++) {
            for (int j = 0; j < df1.varCount(); j++) {
                assertEquals(df1.value(i, j), df2.value(i, j), 1e-12);
            }
        }
    }
}
