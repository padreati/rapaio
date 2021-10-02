/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>.
 */
public class MappedVarTest {

    private static final double TOL = 1e-12;

    @Test
    void testBuilders() {
        Var a = VarDouble.wrap(1, 2, 3, 4, 5, 6).mapRows(0, 1, 2, 3).mapRows(2, 3);
        assertEquals(2, a.size());
        assertEquals(3, a.getDouble(0), TOL);
        assertEquals(4, a.getDouble(1), TOL);

        Var b = a.bindRows(VarDouble.wrap(10, 11));
        assertEquals(4, b.size());
        assertEquals(3, b.getDouble(0), TOL);
        assertEquals(10, b.getDouble(2), TOL);

        Var seq = VarInt.seq(100);
        Var mapSeq = MappedVar.byRows(seq, 1, 2, 3);
        assertEquals(3, mapSeq.size());
        for (int i = 0; i < mapSeq.size(); i++) {
            assertEquals(i + 1, mapSeq.getInt(i));
        }
    }

    @Test
    void testSource() {
        VarDouble x = VarDouble.seq(0, 100);
        MappedVar map = x.mapRows(Mapping.range(10));

        assertNotEquals(x.size(), map.size());
        assertTrue(x.deepEquals(map.getSource()));
    }

    @Test
    void testInvalidAddRows() {
        assertThrows(OperationNotAvailableException.class, () -> VarDouble.scalar(1).mapRows(0).addRows(10));
    }

    @Test
    void testInvalidAddDouble() {
        assertThrows(OperationNotAvailableException.class, () -> VarDouble.scalar(1).mapRows(0).addDouble(0));
    }

    @Test
    void testInvalidAddInt() {
        assertThrows(OperationNotAvailableException.class, () -> VarDouble.scalar(1).mapRows(0).addInt(0));
    }

    @Test
    void testInvalidAddNominal() {
        assertThrows(OperationNotAvailableException.class, () -> VarNominal.copy("1").mapRows(0).addLabel("0"));
    }

    @Test
    void testInvalidAddLong() {
        assertThrows(OperationNotAvailableException.class, () -> VarLong.scalar(1).mapRows(0).addLong(0));
    }

    @Test
    void testInvalidAddBoolean() {
        assertThrows(OperationNotAvailableException.class, () -> VarBinary.fill(1, 1).mapRows(0).addInt(1));
    }

    @Test
    void testInvalidAddMissing() {
        assertThrows(OperationNotAvailableException.class, () -> VarBinary.fill(1, 1).mapRows(0).addMissing());
    }

    @Test
    void testMappedNominal() {
        Var x = VarNominal.copy("a").mapRows(0);
        x.setLevels("x");
        assertEquals("x", x.getLabel(0));

        x.setLabel(0, "y");
        assertEquals("y", x.getLabel(0));

        List<String> levels = x.levels();
        assertEquals("?", levels.get(0));
        assertEquals("x", levels.get(1));
        assertEquals("y", levels.get(2));
    }

    @Test
    void testMappedBinary() {
        Var x = VarBinary.copy(1, 0, 1).mapRows(0, 2);
        assertEquals(2, x.size());
        assertEquals(1, x.getInt(0));
        assertEquals(1, x.getInt(1));

        x.setInt(1, 0);
        assertEquals(1, x.getInt(0));
        assertEquals(0, x.getInt(1));
    }

    @Test
    void testMappedLong() {
        Var x = VarLong.copy(100, 200).mapRows(0, 1);
        assertEquals(2, x.size());
        assertEquals(100, x.getLong(0));
        assertEquals(200, x.getLong(1));

        x.setLong(1, 250);
        assertEquals(250, x.getLong(1));
    }

    @Test
    void testMappedDouble() {
        VarDouble x = VarDouble.seq(10);
        MappedVar y = x.mapRows(1, 2, 3);
        y.setDouble(0, 10);
        y.setMissing(1);

        assertEquals(10, x.getDouble(1), TOL);
        assertTrue(x.isMissing(2));
        assertEquals(3, x.getDouble(3), TOL);
    }

    @Test
    void testMappedInt() {
        VarInt x = VarInt.seq(10);
        MappedVar y = x.mapRows(1, 2, 3);
        y.setInt(0, 10);
        y.setMissing(1);

        assertEquals(10, x.getInt(1), TOL);
        assertTrue(x.isMissing(2));
        assertEquals(3, x.getInt(3), TOL);
    }

    @Test
    void testMissing() {
        VarInt x = VarInt.empty(10);
        Var map = x.mapRows(0, 1, 2);
        assertTrue(map.isMissing(0));
    }

    @Test
    void testRemoveClearRows() {
        Var x = VarInt.seq(100).mapRows(1, 2, 3, 4);
        assertTrue(x.deepEquals(VarInt.wrap(1, 2, 3, 4)));

        x.removeRow(1);
        assertTrue(x.deepEquals(VarInt.wrap(1, 3, 4)));

        x.clearRows();
        assertTrue(x.deepEquals(VarInt.empty()));
    }

    @Test
    void testNewInstance() {
        Var x = VarInt.seq(10).mapRows(1, 2, 3);
        assertTrue(x.newInstance(0).deepEquals(VarInt.empty()));
    }

    @Test
    void testToString() {
        assertEquals("MappedVar[type=int, name:?, rowCount:3]", VarInt.seq(10).mapRows(1, 2, 3).toString());
    }

    @Test
    void testPrinting() {
        int[] mapping = new int[]{1, 3, 6, 9};

        VarDouble vDouble = VarDouble.from(10, row -> (row + 1) % 3 == 0 ? 1. : row);
        Var mapped = vDouble.mapRows(mapping);
        assertEquals("""
                MappedVar(type=dbl) [name:"?", rowCount:4]
                row value\s
                [0]   1  \s
                [1]   3  \s
                [2]   6  \s
                [3]   9  \s
                """, mapped.toContent());

        assertEquals("""
                MappedVar(type=dbl) [name:"?", rowCount:4]
                row value\s
                [0]   1  \s
                [1]   3  \s
                [2]   6  \s
                [3]   9  \s
                """, mapped.toFullContent());

        assertEquals("""
                > summary(name: ?, type: DOUBLE)
                rows: 4, complete: 4, missing: 0
                        ? [dbl]    \s
                   Min. : 1.0000000\s
                1st Qu. : 2.5000000\s
                 Median : 4.5000000\s
                   Mean : 4.7500000\s
                2nd Qu. : 6.7500000\s
                   Max. : 9.0000000\s
                                   \s

                """, mapped.toSummary());

        assertEquals("MappedVar[type=dbl, name:?, rowCount:4]", mapped.toString());
    }
}
