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
public class MappedVarTest {

    @Test
    public void testBuilders() {
        Var a = Numeric.newWrap(1, 2, 3, 4, 5, 6).mapRows(0, 1, 2, 3).mapRows(2, 3);
        assertEquals(2, a.rowCount());
        assertEquals(3, a.value(0), 1e-12);
        assertEquals(4, a.value(1), 1e-12);

        Var b = a.bindRows(Numeric.newWrap(10, 11));
        assertEquals(4, b.rowCount());
        assertEquals(3, b.value(0), 1e-12);
        assertEquals(10, b.value(2), 1e-12);
    }

    @Test
    public void testDynamicMappedVar() {
        Var x = Numeric.newWrap(1, 2, 3, 4).mapRows(Mapping.range(0, 4));

        try {
            x.addValue(10);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }

        try {
            x.addIndex(10);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }

        try {
            Nominal.newCopy("x", "y").mapRows(0, 1).addLabel("z");
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
        Var x = Nominal.newCopy("a").mapRows(0);
        x.setLevels("x");
        assertEquals("x", x.label(0));
    }

    @Test
    public void testMappedBinary() {
        Var x = Binary.copy(true, false, true).mapRows(0, 2);
        assertEquals(2, x.rowCount());
        assertEquals(true, x.binary(0));
        assertEquals(true, x.binary(1));

        x.setBinary(1, false);
        assertEquals(true, x.binary(0));
        assertEquals(false, x.binary(1));

        try {
            x.addBinary(true);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignored) {
        }
    }

    @Test
    public void testMappedStamp() {
        Var x = Stamp.copy(100, 200).mapRows(0, 1);
        assertEquals(2, x.rowCount());
        assertEquals(100, x.stamp(0));
        assertEquals(200, x.stamp(1));

        x.setStamp(1, 250);
        assertEquals(250, x.stamp(1));

        try {
            x.addStamp(300);
            assertTrue("should raise an exception", false);
        } catch (Throwable ignore) {
        }
    }
}
