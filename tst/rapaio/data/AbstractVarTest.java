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

        Var main = NumericVar.copy(1, 3, 5, 8, 9);
        Var copy = main.mapRows(Mapping.range(0, main.getRowCount())).solidCopy();

        assertEquals(main.getRowCount(), copy.getRowCount());
        assertEquals(main.getValue(0), copy.getValue(0), 10e-12);
        assertEquals(main.getValue(1), copy.getValue(1), 10e-12);
        assertEquals(main.getValue(2), copy.getValue(2), 10e-12);
        assertEquals(main.getValue(3), copy.getValue(3), 10e-12);
        assertEquals(main.getValue(4), copy.getValue(4), 10e-12);
    }

    @Test
    public void solidNominalCopyTest() {
        Var main = NominalVar.empty();
        main.addLabel("x");
        main.addLabel("y");
        main.addLabel("x");
        main.addMissing();
        Var copy = main.mapRows(Mapping.range(0, main.getRowCount())).solidCopy();
        assertEquals(main.getRowCount(), copy.getRowCount());
        for (int i = 0; i < main.getRowCount(); i++) {
            assertEquals(main.getLabel(i), copy.getLabel(i));
        }
    }

    @Test
    public void solidOrdinalCopyTest() {
        Var main = OrdinalVar.empty();
        main.addLabel("x");
        main.addLabel("y");
        main.addLabel("x");
        main.addMissing();
        Var copy = main.mapRows(Mapping.range(0, main.getRowCount())).solidCopy();
        assertEquals(main.getRowCount(), copy.getRowCount());
        for (int i = 0; i < main.getRowCount(); i++) {
            assertEquals(main.getLabel(i), copy.getLabel(i));
        }
    }

    @Test
    public void solidIndexCopyTest() {
        Var main = IndexVar.wrap(1, 2, 3, 4, 5);
        main.addMissing();
        Var copy = main.mapRows(Mapping.range(0, main.getRowCount())).solidCopy();
        assertEquals(main.getRowCount(), copy.getRowCount());
        for (int i = 0; i < main.getRowCount(); i++) {
            assertEquals(main.getIndex(i), copy.getIndex(i));
        }
    }

    @Test
    public void solidStampCopyTest() {
        Var main = StampVar.wrap(1L, 2L, 3L, 4L, 6L);
        main.addMissing();
        Var copy = main.mapRows(Mapping.range(0, main.getRowCount())).solidCopy();
        assertEquals(main.getRowCount(), copy.getRowCount());
        for (int i = 0; i < main.getRowCount(); i++) {
            assertEquals(main.getStamp(i), copy.getStamp(i));
        }
    }

    @Test
    public void soliBinaryCopyTest() {
        Var main = BinaryVar.copy(true, false, false, true);
        main.addMissing();
        Var copy = main.mapRows(Mapping.range(0, main.getRowCount())).solidCopy();
        assertEquals(main.getRowCount(), copy.getRowCount());
        for (int i = 0; i < main.getRowCount(); i++) {
            assertEquals(main.getBinary(i), copy.getBinary(i));
        }
    }

    @Test
    public void testBoundVar() {
        NumericVar a = NumericVar.wrap(1, 2, 3);
        NumericVar b = NumericVar.wrap(4, 5, 6);
        NumericVar c = NumericVar.wrap(7, 8, 9, 10);

        Var d = a.bindRows(b);
        d = d.bindRows(c);

        assertEquals(10, d.getRowCount());

        double mean = Mean.from(d).getValue();
        assertEquals(5.5, mean, 10e-12);

        for (int i = 0; i < 10; i++) {
            assertEquals(i+1, d.getIndex(i));
        }
    }
}
