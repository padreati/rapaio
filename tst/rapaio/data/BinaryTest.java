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
        BinaryVar b = BinaryVar.empty();
        b.addBinary(true);
        b.addBinary(true);
        b.addBinary(false);
        b.addMissing();
        b.addMissing();
        b.addBinary(true);

        assertEquals(1, b.stream().complete().filter(s -> !s.binary()).count());
        assertEquals(3, b.stream().complete().filter(VSpot::binary).count());
        assertEquals(2, b.stream().incomplete().count());

        assertEquals(10, BinaryVar.empty(10).stream().incomplete().count());
        assertEquals(0, BinaryVar.empty().stream().incomplete().count());
    }

    @Test
    public void testFill() {
        BinaryVar b = BinaryVar.fill(10, false);

        assertEquals(0, b.stream().incomplete().count());
        assertEquals(10, b.stream().complete().filter(s -> !s.binary()).count());
        assertEquals(0, b.stream().complete().filter(VSpot::binary).count());

        b = BinaryVar.fill(10, true);

        assertEquals(0, b.stream().incomplete().count());
        assertEquals(0, b.stream().complete().filter(s -> !s.binary()).count());
        assertEquals(10, b.stream().complete().filter(VSpot::binary).count());
    }

    @Test
    public void testNumericStats() {
        BinaryVar b = BinaryVar.copy(1, 1, 0, 0, 1, 0, 1, 1);
        b.printSummary();
        assertEquals(0.625, Mean.from(b).value(), 10e-10);
    }

    @Test
    public void testMissingValues() {
        BinaryVar bin = BinaryVar.copy(1, 0, 1, 0, -1, -1, 1, 0);
        assertEquals(8, bin.rowCount());
        assertEquals(true, bin.isMissing(4));
        assertEquals(false, bin.isMissing(7));

        bin = BinaryVar.empty();
        bin.addMissing();
        bin.addBinary(true);
        bin.setMissing(1);

        assertEquals(2, bin.rowCount());
        assertEquals(true, bin.isMissing(0));
        assertEquals(true, bin.isMissing(1));
    }

    @Test
    public void testBuilders() {
        BinaryVar bin = BinaryVar.copy(true, true, false, false);
        assertEquals(4, bin.rowCount());
        assertEquals(true, bin.binary(0));
        assertEquals(false, bin.binary(3));

        assertEquals(4, bin.rowCount());
    }

    @Test
    public void testOther() {
        BinaryVar bin = BinaryVar.empty();
        bin.addValue(1);
        bin.setValue(0, 0);
        bin.addIndex(1);
        bin.setIndex(1, 0);

        assertEquals(0, bin.value(0), 10e-10);
        assertEquals(0, bin.index(1));

        BinaryVar copy = bin.solidCopy();
        assertEquals(false, copy.binary(0));
        assertEquals(false, copy.binary(1));
        assertEquals(2, copy.rowCount());

        copy.remove(0);
        assertEquals(1, copy.rowCount());
        assertEquals(false, copy.binary(0));

        copy.clear();
        assertEquals(0, copy.rowCount());

        try {
            copy.remove(10);
            assertTrue("This should raise an exception", false);
        }catch(Throwable ignored){}
    }

    @Test
    public void testValueManipulation() {

        BinaryVar bin = BinaryVar.empty();
        bin.addValue(1);
        bin.addValue(0);
        bin.addValue(-1);

        assertEquals(3, bin.rowCount());
        assertEquals(true, bin.binary(0));
        assertEquals(false, bin.binary(1));
        assertEquals(true, bin.isMissing(2));

        try {
            bin.addValue(2);
            assertFalse("This should raise an exception", true);
        } catch (Throwable ignored) {
        }

        try {
            bin.setValue(0, 2);
            assertFalse("This should raise an exception", true);
        } catch (Throwable ignored) {
        }

        bin.setValue(0, -1);
        bin.setValue(1, 0);
        bin.setValue(2, 1);

        assertEquals(true, bin.isMissing(0));
        assertEquals(false, bin.binary(1));
        assertEquals(true, bin.binary(2));
    }

    @Test
    public void testIndexManipulation() {

        BinaryVar bin = BinaryVar.empty();
        bin.addIndex(1);
        bin.addIndex(0);
        bin.addIndex(-1);

        assertEquals(3, bin.rowCount());
        assertEquals(true, bin.binary(0));
        assertEquals(false, bin.binary(1));
        assertEquals(true, bin.isMissing(2));

        try {
            bin.addIndex(2);
            assertFalse("This should raise an exception", true);
        } catch (Throwable ignored) {
        }

        try {
            bin.setIndex(0, 2);
            assertFalse("This should raise an exception", true);
        } catch (Throwable ignored) {
        }

        bin.setIndex(0, -1);
        bin.setIndex(1, 0);
        bin.setIndex(2, 1);

        assertEquals(true, bin.isMissing(0));
        assertEquals(false, bin.binary(1));
        assertEquals(true, bin.binary(2));
    }

    @Test
    public void testStampManipulation() {

        BinaryVar bin = BinaryVar.empty();
        bin.addStamp(1);
        bin.addStamp(0);
        bin.addStamp(-1);

        assertEquals(3, bin.rowCount());
        assertEquals(true, bin.binary(0));
        assertEquals(false, bin.binary(1));
        assertEquals(true, bin.isMissing(2));

        try {
            bin.addStamp(2);
            assertFalse("This should raise an exception", true);
        } catch (Throwable ignored) {
        }

        try {
            bin.setStamp(0, 2);
            assertFalse("This should raise an exception", true);
        } catch (Throwable ignored) {
        }

        bin.setStamp(0, -1);
        bin.setStamp(1, 0);
        bin.setStamp(2, 1);

        assertEquals(true, bin.isMissing(0));
        assertEquals(false, bin.binary(1));
        assertEquals(true, bin.binary(2));

        assertEquals(1L, bin.stamp(0));
    }

    @Test
    public void testLabelManipulation() {
        BinaryVar bin = BinaryVar.copy(true, false, true);
        bin.addMissing();
        assertEquals("true", bin.label(0));
        assertEquals("false", bin.label(1));
        assertEquals("true", bin.label(2));
        assertEquals("?", bin.label(3));

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
