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

        Var main = VarDouble.copy(1, 3, 5, 8, 9);
        Var copy = main.mapRows(Mapping.range(0, main.rowCount())).copy();

        assertEquals(main.rowCount(), copy.rowCount());
        assertEquals(main.getDouble(0), copy.getDouble(0), 10e-12);
        assertEquals(main.getDouble(1), copy.getDouble(1), 10e-12);
        assertEquals(main.getDouble(2), copy.getDouble(2), 10e-12);
        assertEquals(main.getDouble(3), copy.getDouble(3), 10e-12);
        assertEquals(main.getDouble(4), copy.getDouble(4), 10e-12);
    }

    @Test
    public void solidNominalCopyTest() {
        Var main = VarNominal.empty();
        main.addLabel("x");
        main.addLabel("y");
        main.addLabel("x");
        main.addMissing();
        Var copy = main.mapRows(Mapping.range(0, main.rowCount())).copy();
        assertEquals(main.rowCount(), copy.rowCount());
        for (int i = 0; i < main.rowCount(); i++) {
            assertEquals(main.getLabel(i), copy.getLabel(i));
        }
    }

    @Test
    public void solidIndexCopyTest() {
        Var main = VarInt.wrap(1, 2, 3, 4, 5);
        main.addMissing();
        Var copy = main.mapRows(Mapping.range(0, main.rowCount())).copy();
        assertEquals(main.rowCount(), copy.rowCount());
        for (int i = 0; i < main.rowCount(); i++) {
            assertEquals(main.getInt(i), copy.getInt(i));
        }
    }

    @Test
    public void solidStampCopyTest() {
        Var main = VarLong.wrap(1L, 2L, 3L, 4L, 6L);
        main.addMissing();
        Var copy = main.mapRows(Mapping.range(0, main.rowCount())).copy();
        assertEquals(main.rowCount(), copy.rowCount());
        for (int i = 0; i < main.rowCount(); i++) {
            assertEquals(main.getLong(i), copy.getLong(i));
        }
    }

    @Test
    public void soliBinaryCopyTest() {
        Var main = VarBinary.copy(1, 0, 0, 1);
        main.addMissing();
        Var copy = main.mapRows(Mapping.range(0, main.rowCount())).copy();
        assertEquals(main.rowCount(), copy.rowCount());
        for (int i = 0; i < main.rowCount(); i++) {
            assertEquals(main.getInt(i), copy.getInt(i));
        }
    }

    @Test
    public void testBoundVar() {
        VarDouble a = VarDouble.wrap(1, 2, 3);
        VarDouble b = VarDouble.wrap(4, 5, 6);
        VarDouble c = VarDouble.wrap(7, 8, 9, 10);

        Var d = a.bindRows(b);
        d = d.bindRows(c);

        assertEquals(10, d.rowCount());

        double mean = Mean.of(d).value();
        assertEquals(5.5, mean, 10e-12);

        for (int i = 0; i < 10; i++) {
            assertEquals(i+1, d.getInt(i));
        }
    }
}
