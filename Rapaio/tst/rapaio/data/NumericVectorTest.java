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
import static rapaio.core.BaseMath.*;
import rapaio.data.NumericVector;
import rapaio.data.Vector;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class NumericVectorTest {

    @Test
    public void smokeTest() {
        Vector v = new NumericVector("x", 0);
        assertEquals("x", v.getName());

        boolean flag = v.isNumeric();
        assertEquals(true, flag);
        assertEquals(false, v.isNominal());

        assertEquals(0, v.getRowCount());
    }

    @Test
    public void testGetterSetter() {
        Vector v = new NumericVector("x", 10);
        for (int i = 0; i < 10; i++) {
            v.setValue(i, log(10 + i));
        }

        for (int i = 0; i < 10; i++) {
            assertEquals(log(10 + i), v.getValue(i), 1e-10);
            assertEquals((int) Math.rint(log(10 + i)), v.getIndex(i));
        }

        for (int i = 0; i < 10; i++) {
            v.setIndex(i, i * i);
        }

        for (int i = 0; i < 10; i++) {
            assertEquals(i * i, v.getIndex(i));
            assertEquals(i * i, v.getValue(i), 1e-10);
        }

        for (int i = 0; i < v.getRowCount(); i++) {
            assertEquals("", v.getLabel(i));
        }
        boolean exceptional = false;
        try {
            v.setLabel(0, "test");
        } catch (Throwable ex) {
            exceptional = true;
        }
        assertTrue(exceptional);

        assertEquals(0, v.getDictionary().length);
    }

    @Test
    public void testOneNumeric() {
        Vector one = new OneNumericVector(PI);

        assertEquals(1, one.getRowCount());
        assertEquals(PI, one.getValue(0), 1e-10);

        one = new OneNumericVector("test", E);
        assertEquals("test", one.getName());
        assertEquals(1, one.getRowCount());
        assertEquals(E, one.getValue(0), 1e-10);
    }
}
