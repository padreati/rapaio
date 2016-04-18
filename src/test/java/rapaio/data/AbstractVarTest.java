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

import static org.junit.Assert.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class AbstractVarTest {

    @Test
    public void solidNumericCopyTest() {

        Var main = Numeric.newCopy(1, 3, 5, 8, 9);
        Var copy = main.mapRows(Mapping.range(0, main.rowCount())).solidCopy();

        assertEquals(main.rowCount(), copy.rowCount());
        assertEquals(main.value(0), copy.value(0), 10e-12);
        assertEquals(main.value(1), copy.value(1), 10e-12);
        assertEquals(main.value(2), copy.value(2), 10e-12);
        assertEquals(main.value(3), copy.value(3), 10e-12);
        assertEquals(main.value(4), copy.value(4), 10e-12);
    }

    @Test
    public void solidNominalCopyTest() {
        Var main = Nominal.newEmpty();
        main.addLabel("x");
        main.addLabel("y");
        main.addLabel("x");
        main.addMissing();
        Var copy = main.mapRows(Mapping.range(0, main.rowCount())).solidCopy();
        assertEquals(main.rowCount(), copy.rowCount());
        for (int i = 0; i < main.rowCount(); i++) {
            assertEquals(main.label(i), copy.label(i));
        }
    }

    @Test
    public void solidOrdinalCopyTest() {
        Var main = Ordinal.empty();
        main.addLabel("x");
        main.addLabel("y");
        main.addLabel("x");
        main.addMissing();
        Var copy = main.mapRows(Mapping.range(0, main.rowCount())).solidCopy();
        assertEquals(main.rowCount(), copy.rowCount());
        for (int i = 0; i < main.rowCount(); i++) {
            assertEquals(main.label(i), copy.label(i));
        }
    }

    @Test
    public void solidIndexCopyTest() {
        Var main = Index.wrap(1, 2, 3, 4, 5);
        main.addMissing();
        Var copy = main.mapRows(Mapping.range(0, main.rowCount())).solidCopy();
        assertEquals(main.rowCount(), copy.rowCount());
        for (int i = 0; i < main.rowCount(); i++) {
            assertEquals(main.index(i), copy.index(i));
        }
    }

    @Test
    public void solidStampCopyTest() {
        Var main = Stamp.wrap(1L, 2L, 3L, 4L, 6L);
        main.addMissing();
        Var copy = main.mapRows(Mapping.range(0, main.rowCount())).solidCopy();
        assertEquals(main.rowCount(), copy.rowCount());
        for (int i = 0; i < main.rowCount(); i++) {
            assertEquals(main.stamp(i), copy.stamp(i));
        }
    }

    @Test
    public void soliBinaryCopyTest() {
        Var main = Binary.copy(true, false, false, true);
        main.addMissing();
        Var copy = main.mapRows(Mapping.range(0, main.rowCount())).solidCopy();
        assertEquals(main.rowCount(), copy.rowCount());
        for (int i = 0; i < main.rowCount(); i++) {
            assertEquals(main.binary(i), copy.binary(i));
        }
    }

    @Test
    public void testBoundVar() {
        Numeric a = Numeric.newWrap(1, 2, 3);
        Numeric b = Numeric.newWrap(4, 5, 6);
        Numeric c = Numeric.newWrap(7, 8, 9, 10);

        Var d = a.bindRows(b);
        d = d.bindRows(c);

        assertEquals(10, d.rowCount());

        double mean = new Mean(d).value();
        assertEquals(5.5, mean, 10e-12);

        for (int i = 0; i < 10; i++) {
            assertEquals(i+1, d.index(i));
        }
    }
}
