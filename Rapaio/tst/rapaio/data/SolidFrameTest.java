/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SolidFrameTest {

    @Test
    public void testEmptySolidFrame() {
        Frame df = new SolidFrame("df", 0, new Vector[0]);
        assertEquals(0, df.getRowCount());
        assertEquals(0, df.getColCount());
        assertEquals("df", df.getName());
    }

    @Test
    public void testRowId() {
        Frame df = new SolidFrame("df", 10, new Vector[]{new NumericVector("x", 10)});
        for (int i = 0; i < df.getRowCount(); i++) {
            assertEquals(i, df.getRowId(i, 0));
        }
    }

    @Test
    public void testColIndexes() {
        Vector[] vectors = new Vector[]{
                new NumericVector("x", 0),
                new NumericVector("y", 0),
                new NumericVector("z", 0)
        };
        Frame df = new SolidFrame("df", 0, vectors);

        assertEquals(3, df.getColCount());
        assertEquals("x", df.getColNames()[0]);
        assertEquals("z", df.getColNames()[2]);
        assertEquals(0, df.getColIndex("x"));
        assertEquals(2, df.getColIndex("z"));

        boolean exceptional = false;
        try {
            df.getColIndex("q");
        } catch (IllegalArgumentException ex) {
            exceptional = true;
        }
        assertEquals(true, exceptional);

        exceptional = false;
        try {
            df.getCol(10);
        } catch (IllegalArgumentException ex) {
            exceptional = true;
        }
        assertEquals(true, exceptional);

        exceptional = false;
        try {
            df.getCol(-1);
        } catch (IllegalArgumentException ex) {
            exceptional = true;
        }
        assertEquals(true, exceptional);

        assertEquals("x", df.getCol(0).getName());
        assertEquals("y", df.getCol(1).getName());
        assertEquals("z", df.getCol(2).getName());

        assertEquals("x", df.getCol("x").getName());
        assertEquals("z", df.getCol("z").getName());
    }

    @Test
    public void testConvenientMethods() {
        Vector[] vectors = new Vector[]{
                new NumericVector("x", new double[]{1., 2., 3., 4.}),
                new NumericVector("y", new double[]{3., 5., 9., 12.}),
                new NominalVector("name", 4, new String[]{"ana", "are", "mere"}),
                new IndexVector("index", 1, 4, 1)
        };
        Frame df = new SolidFrame("df", 4, vectors);

        assertEquals(1., df.getValue(0, 0), 1e-10);
        df.setValue(0, 0, 3.);
        assertEquals(3., df.getValue(0, 0), 1e-10);

        double t = 0;
        for (int i = 0; i < df.getRowCount(); i++) {
            t += df.getValue(i, 1) - 3.;
        }
        assertEquals(17., t, 1e-10);

        assertTrue(df.getCol("name").isMissing(0));
        assertTrue(df.getCol("name").isMissing(3));

        df.setLabel(0, 2, "ana");
        df.setLabel(1, 2, "are");
        df.setLabel(2, 2, "mere");

        assertEquals("ana", df.getLabel(0, 2));
        assertEquals("are", df.getLabel(1, 2));
        assertEquals("mere", df.getLabel(2, 2));

        df.setIndex(1, 2, 3);
        assertEquals("mere", df.getLabel(1, 2));

        assertEquals(1, df.getIndex(0, 3));
        assertEquals(2, df.getIndex(1, 3));
        assertEquals(3, df.getIndex(2, 3));
        assertEquals(4, df.getIndex(3, 3));

        df.setIndex(0, 3, 5);
        assertEquals(5, df.getIndex(0, 3));
    }
}
