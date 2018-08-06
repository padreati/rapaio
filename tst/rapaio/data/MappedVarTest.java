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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class MappedVarTest {

    @Test
    public void testBuilders() {
        Var a = VarDouble.wrap(1, 2, 3, 4, 5, 6).mapRows(0, 1, 2, 3).mapRows(2, 3);
        assertEquals(2, a.rowCount());
        assertEquals(3, a.getDouble(0), 1e-12);
        assertEquals(4, a.getDouble(1), 1e-12);

        Var b = a.bindRows(VarDouble.wrap(10, 11));
        assertEquals(4, b.rowCount());
        assertEquals(3, b.getDouble(0), 1e-12);
        assertEquals(10, b.getDouble(2), 1e-12);
    }

    @Test
    public void testDynamicMappedVar() {
        Var x = VarDouble.wrap(1, 2, 3, 4).mapRows(Mapping.range(0, 4));

        try {
            x.addDouble(10);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }

        try {
            x.addInt(10);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }

        try {
            VarNominal.copy("x", "y").mapRows(0, 1).addLabel("z");
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }

        try {
            x.addMissing();
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }

        try {
            x.remove(1);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }

        try {
            x.clear();
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }

    @Test
    public void testMappedNominal() {
        Var x = VarNominal.copy("a").mapRows(0);
        x.setLevels("x");
        assertEquals("x", x.getLabel(0));
    }

    @Test
    public void testMappedBinary() {
        Var x = VarBoolean.copy(true, false, true).mapRows(0, 2);
        assertEquals(2, x.rowCount());
        assertEquals(true, x.getBoolean(0));
        assertEquals(true, x.getBoolean(1));

        x.setBoolean(1, false);
        assertEquals(true, x.getBoolean(0));
        assertEquals(false, x.getBoolean(1));

        try {
            x.addBoolean(true);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }
    }

    @Test
    public void testMappedStamp() {
        Var x = VarLong.copy(100, 200).mapRows(0, 1);
        assertEquals(2, x.rowCount());
        assertEquals(100, x.getLong(0));
        assertEquals(200, x.getLong(1));

        x.setLong(1, 250);
        assertEquals(250, x.getLong(1));

        try {
            x.addLong(300);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }
}
