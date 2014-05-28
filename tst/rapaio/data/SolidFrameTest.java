/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class SolidFrameTest {

    @Test
    public void testEmptySolidFrame() {
        Frame df = new SolidFrame(0, new Vector[0], new String[]{});
        assertEquals(0, df.rowCount());
        assertEquals(0, df.colCount());
    }

    @Test
    public void testRowId() {
        Frame df = new SolidFrame(10, new Vector[]{new Numeric(10)}, new String[]{"x"});
        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(i, df.rowId(i));
        }
    }

    @Test
    public void testColIndexes() {
        Vector[] vectors = new Vector[]{
                new Numeric(0),
                new Numeric(0),
                new Numeric(0)
        };
        Frame df = new SolidFrame(0, vectors, new String[]{"x", "y", "z"});

        assertEquals(3, df.colCount());
        assertEquals("x", df.colNames()[0]);
        assertEquals("z", df.colNames()[2]);
        assertEquals(0, df.colIndex("x"));
        assertEquals(2, df.colIndex("z"));

        boolean exceptional = false;
        try {
            df.colIndex("q");
        } catch (IllegalArgumentException ex) {
            exceptional = true;
        }
        assertEquals(true, exceptional);

        exceptional = false;
        try {
            df.col(10);
        } catch (IllegalArgumentException ex) {
            exceptional = true;
        }
        assertEquals(true, exceptional);

        exceptional = false;
        try {
            df.col(-1);
        } catch (IllegalArgumentException ex) {
            exceptional = true;
        }
        assertEquals(true, exceptional);

        assertEquals("x", df.colNames()[0]);
        assertEquals("y", df.colNames()[1]);
        assertEquals("z", df.colNames()[2]);
    }

    @Test
    public void testConvenientMethods() {
        Vector[] vectors = new Vector[]{
                new Numeric(new double[]{1., 2., 3., 4.}),
                new Numeric(new double[]{3., 5., 9., 12.}),
                new Nominal(4, new String[]{"ana", "are", "mere"}),
                Vectors.newSeq(1, 4, 1)
        };
        Frame df = new SolidFrame(4, vectors, new String[]{"x", "y", "name", "index"});

        assertEquals(1., df.value(0, 0), 1e-10);
        df.setValue(0, 0, 3.);
        assertEquals(3., df.value(0, 0), 1e-10);

        double t = 0;
        for (int i = 0; i < df.rowCount(); i++) {
            t += df.value(i, 1) - 3.;
        }
        assertEquals(17., t, 1e-10);

        assertTrue(df.col("name").missing(0));
        assertTrue(df.col("name").missing(3));

        df.setLabel(0, 2, "ana");
        df.setLabel(1, 2, "are");
        df.setLabel(2, 2, "mere");

        assertEquals("ana", df.label(0, 2));
        assertEquals("are", df.label(1, 2));
        assertEquals("mere", df.label(2, 2));

        df.setIndex(1, 2, 3);
        assertEquals("mere", df.label(1, 2));

        assertEquals(1, df.index(0, 3));
        assertEquals(2, df.index(1, 3));
        assertEquals(3, df.index(2, 3));
        assertEquals(4, df.index(3, 3));

        df.setIndex(0, 3, 5);
        assertEquals(5, df.index(0, 3));
    }
}
