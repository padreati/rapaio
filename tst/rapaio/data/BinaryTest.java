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
import rapaio.core.stat.Mean;
import rapaio.data.stream.VSpot;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
public class BinaryTest {

    @Test
    public void testEmpty() {
        VarBoolean b = VarBoolean.empty();
        b.addBoolean(true);
        b.addBoolean(true);
        b.addBoolean(false);
        b.addMissing();
        b.addMissing();
        b.addBoolean(true);

        assertEquals(1, b.stream().complete().filter(s -> !s.getBoolean()).count());
        assertEquals(3, b.stream().complete().filter(VSpot::getBoolean).count());
        assertEquals(2, b.stream().incomplete().count());

        assertEquals(10, VarBoolean.empty(10).stream().incomplete().count());
        assertEquals(0, VarBoolean.empty().stream().incomplete().count());
    }

    @Test
    public void testFill() {
        VarBoolean b = VarBoolean.fill(10, false);

        assertEquals(0, b.stream().incomplete().count());
        assertEquals(10, b.stream().complete().filter(s -> !s.getBoolean()).count());
        assertEquals(0, b.stream().complete().filter(VSpot::getBoolean).count());

        b = VarBoolean.fill(10, true);

        assertEquals(0, b.stream().incomplete().count());
        assertEquals(0, b.stream().complete().filter(s -> !s.getBoolean()).count());
        assertEquals(10, b.stream().complete().filter(VSpot::getBoolean).count());
    }

    @Test
    public void testNumericStats() {
        VarBoolean b = VarBoolean.copy(1, 1, 0, 0, 1, 0, 1, 1);
        b.printSummary();
        assertEquals(0.625, Mean.from(b).value(), 10e-10);
    }

    @Test
    public void testMissingValues() {
        VarBoolean bin = VarBoolean.copy(1, 0, 1, 0, -1, -1, 1, 0);
        assertEquals(8, bin.rowCount());
        assertEquals(true, bin.isMissing(4));
        assertEquals(false, bin.isMissing(7));

        bin = VarBoolean.empty();
        bin.addMissing();
        bin.addBoolean(true);
        bin.setMissing(1);

        assertEquals(2, bin.rowCount());
        assertEquals(true, bin.isMissing(0));
        assertEquals(true, bin.isMissing(1));
    }

    @Test
    public void testBuilders() {
        VarBoolean bin = VarBoolean.copy(true, true, false, false);
        assertEquals(4, bin.rowCount());
        assertEquals(true, bin.getBoolean(0));
        assertEquals(false, bin.getBoolean(3));

        assertEquals(4, bin.rowCount());
    }

    @Test
    public void testOther() {
        VarBoolean bin = VarBoolean.empty();
        bin.addDouble(1);
        bin.setDouble(0, 0);
        bin.addInt(1);
        bin.setInt(1, 0);

        assertEquals(0, bin.getDouble(0), 10e-10);
        assertEquals(0, bin.getInt(1));

        VarBoolean copy = bin.solidCopy();
        assertEquals(false, copy.getBoolean(0));
        assertEquals(false, copy.getBoolean(1));
        assertEquals(2, copy.rowCount());

        copy.remove(0);
        assertEquals(1, copy.rowCount());
        assertEquals(false, copy.getBoolean(0));

        copy.clear();
        assertEquals(0, copy.rowCount());

        try {
            copy.remove(10);
            assertTrue("This should raise an exception", false);
        }catch(Throwable ignored){}
    }

    @Test
    public void testValueManipulation() {

        VarBoolean bin = VarBoolean.empty();
        bin.addDouble(1);
        bin.addDouble(0);
        bin.addDouble(-1);

        assertEquals(3, bin.rowCount());
        assertEquals(true, bin.getBoolean(0));
        assertEquals(false, bin.getBoolean(1));
        assertEquals(true, bin.isMissing(2));

        try {
            bin.addDouble(2);
            assertFalse("This should raise an exception", true);
        } catch (Throwable ignored) {
        }

        try {
            bin.setDouble(0, 2);
            assertFalse("This should raise an exception", true);
        } catch (Throwable ignored) {
        }

        bin.setDouble(0, -1);
        bin.setDouble(1, 0);
        bin.setDouble(2, 1);

        assertEquals(true, bin.isMissing(0));
        assertEquals(false, bin.getBoolean(1));
        assertEquals(true, bin.getBoolean(2));
    }

    @Test
    public void testIndexManipulation() {

        VarBoolean bin = VarBoolean.empty();
        bin.addInt(1);
        bin.addInt(0);
        bin.addInt(-1);

        assertEquals(3, bin.rowCount());
        assertEquals(true, bin.getBoolean(0));
        assertEquals(false, bin.getBoolean(1));
        assertEquals(true, bin.isMissing(2));

        try {
            bin.addInt(2);
            assertFalse("This should raise an exception", true);
        } catch (Throwable ignored) {
        }

        try {
            bin.setInt(0, 2);
            assertFalse("This should raise an exception", true);
        } catch (Throwable ignored) {
        }

        bin.setInt(0, -1);
        bin.setInt(1, 0);
        bin.setInt(2, 1);

        assertEquals(true, bin.isMissing(0));
        assertEquals(false, bin.getBoolean(1));
        assertEquals(true, bin.getBoolean(2));
    }

    @Test
    public void testStampManipulation() {

        VarBoolean bin = VarBoolean.empty();
        bin.addLong(1);
        bin.addLong(0);
        bin.addLong(-1);

        assertEquals(3, bin.rowCount());
        assertEquals(true, bin.getBoolean(0));
        assertEquals(false, bin.getBoolean(1));
        assertEquals(true, bin.isMissing(2));

        try {
            bin.addLong(2);
            assertFalse("This should raise an exception", true);
        } catch (Throwable ignored) {
        }

        try {
            bin.setLong(0, 2);
            assertFalse("This should raise an exception", true);
        } catch (Throwable ignored) {
        }

        bin.setLong(0, -1);
        bin.setLong(1, 0);
        bin.setLong(2, 1);

        assertEquals(true, bin.isMissing(0));
        assertEquals(false, bin.getBoolean(1));
        assertEquals(true, bin.getBoolean(2));

        assertEquals(1L, bin.getLong(0));
    }

    @Test
    public void testLabelManipulation() {
        VarBoolean bin = VarBoolean.copy(true, false, true);
        bin.addMissing();
        assertEquals("true", bin.getLabel(0));
        assertEquals("false", bin.getLabel(1));
        assertEquals("true", bin.getLabel(2));
        assertEquals("?", bin.getLabel(3));

        try {
            bin.addLabel("x");
            assertTrue("This should raise an exception", false);
        } catch(Throwable ignored) {}
        try {
            bin.setLabel(0, "x");
            assertTrue("This should raise an exception", false);
        } catch(Throwable ignored) {}

        try {
            bin.setLevels("a");
            assertTrue("This should raise an exception", false);
        } catch(Throwable ignored) {}

        try {
            bin.levels();
            assertTrue("This should raise an exception", false);
        } catch(Throwable ignored) {}
    }
}
