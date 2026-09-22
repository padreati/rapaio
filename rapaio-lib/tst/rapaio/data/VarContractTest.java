/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Behaviour shared by every {@link Var} implementation: {@code removeRow} on any position, including the last one,
 * and the missing-value contract of the typed accessors documented on {@link Var}.
 */
public class VarContractTest {

    /**
     * One representative per storage type, holding three observations. The contract tests make a row missing and
     * read it back through every typed accessor the type supports.
     */
    private static List<Supplier<Var>> allTypes() {
        return List.of(
                () -> VarDouble.copy(1.0, 2.0, 3.0),
                () -> VarFloat.copy(1f, 2f, 3f),
                () -> VarInt.copy(1, 2, 3),
                () -> VarLong.copy(1L, 2L, 3L),
                () -> VarBinary.copy(1, 0, 1),
                () -> VarNominal.copy("a", "b", "c"),
                () -> VarInstant.from(1_000L, 2_000L, 3_000L),
                () -> VarString.copy("x", "y", "z")
        );
    }

    // removeRow

    @Test
    void removeLastRowShrinksEveryType() {
        for (Supplier<Var> supplier : allTypes()) {
            Var var = supplier.get();
            String first = var.getLabel(0);
            String second = var.getLabel(1);
            var.removeRow(2);
            assertEquals(2, var.size(), var.type() + ": size after removing the last row");
            assertEquals(first, var.getLabel(0), var.type().name());
            assertEquals(second, var.getLabel(1), var.type().name());

            var.removeRow(1);
            var.removeRow(0);
            assertEquals(0, var.size(), var.type() + ": size after removing every row");
        }
    }

    @Test
    void removeFirstAndMiddleRowKeepsOrder() {
        for (Supplier<Var> supplier : allTypes()) {
            Var var = supplier.get();
            String last = var.getLabel(2);
            var.removeRow(1);
            assertEquals(2, var.size(), var.type().name());
            assertEquals(last, var.getLabel(1), var.type().name());
            String kept = var.getLabel(1);
            var.removeRow(0);
            assertEquals(1, var.size(), var.type().name());
            assertEquals(kept, var.getLabel(0), var.type().name());
        }
    }

    @Test
    void removeRowOutOfBoundsThrows() {
        for (Supplier<Var> supplier : allTypes()) {
            Var var = supplier.get();
            assertThrows(IndexOutOfBoundsException.class, () -> var.removeRow(-1), var.type().name());
            assertThrows(IndexOutOfBoundsException.class, () -> var.removeRow(3), var.type().name());
            assertEquals(3, var.size(), var.type() + ": a rejected removal must not change the size");
        }
    }

    @Test
    void removedInstantIsNotRetained() {
        VarInstant var = VarInstant.from(1_000L, 2_000L, 3_000L);
        var.removeRow(2);
        var.addMissing();
        assertTrue(var.isMissing(2), "the slot freed by removeRow must not still hold the removed instant");
        assertEquals(3, var.size());
    }

    // missing-value contract: reads

    @Test
    void missingRowReadsAsMissingThroughEveryNumericAccessor() {
        for (Supplier<Var> supplier : allTypes()) {
            Var var = supplier.get();
            if (var.type() == VarType.STRING) {
                continue; // no numeric accessors
            }
            var.setMissing(1);
            String t = var.type().name();
            assertTrue(var.isMissing(1), t);
            assertTrue(Double.isNaN(var.getDouble(1)), t + ": getDouble on a missing row");
            assertTrue(Float.isNaN(var.getFloat(1)), t + ": getFloat on a missing row");
            int expectedInt = var.type() == VarType.NOMINAL ? -1 : VarInt.MISSING_VALUE;
            assertEquals(expectedInt, var.getInt(1), t + ": getInt on a missing row");
            if (var.type() != VarType.NOMINAL) {
                assertEquals(VarLong.MISSING_VALUE, var.getLong(1), t + ": getLong on a missing row");
            }
            // the neighbours are untouched
            assertFalse(var.isMissing(0), t);
            assertFalse(var.isMissing(2), t);
        }
    }

    // missing-value contract: writes

    @Test
    void settingTheMissingSentinelMakesTheRowMissing() {
        for (Supplier<Var> supplier : allTypes()) {
            String t = supplier.get().type().name();
            if (supplier.get().type() == VarType.STRING) {
                continue;
            }
            Var v1 = supplier.get();
            v1.setDouble(1, Double.NaN);
            assertTrue(v1.isMissing(1), t + ": setDouble(NaN)");

            Var v2 = supplier.get();
            v2.setFloat(1, Float.NaN);
            assertTrue(v2.isMissing(1), t + ": setFloat(NaN)");

            Var v3 = supplier.get();
            v3.setInt(1, VarInt.MISSING_VALUE);
            assertTrue(v3.isMissing(1), t + ": setInt(VarInt.MISSING_VALUE)");

            if (v1.type() != VarType.NOMINAL) {
                Var v4 = supplier.get();
                v4.setLong(1, VarLong.MISSING_VALUE);
                assertTrue(v4.isMissing(1), t + ": setLong(VarLong.MISSING_VALUE)");
            }
        }
    }

    @Test
    void addingTheMissingSentinelAppendsAMissingRow() {
        for (Supplier<Var> supplier : allTypes()) {
            String t = supplier.get().type().name();
            if (supplier.get().type() == VarType.STRING) {
                continue;
            }
            Var v1 = supplier.get();
            v1.addDouble(Double.NaN);
            assertEquals(4, v1.size(), t);
            assertTrue(v1.isMissing(3), t + ": addDouble(NaN)");

            Var v2 = supplier.get();
            v2.addFloat(Float.NaN);
            assertTrue(v2.isMissing(3), t + ": addFloat(NaN)");

            Var v3 = supplier.get();
            v3.addInt(VarInt.MISSING_VALUE);
            assertTrue(v3.isMissing(3), t + ": addInt(VarInt.MISSING_VALUE)");

            if (v1.type() != VarType.NOMINAL) {
                Var v4 = supplier.get();
                v4.addLong(VarLong.MISSING_VALUE);
                assertTrue(v4.isMissing(3), t + ": addLong(VarLong.MISSING_VALUE)");
            }
        }
    }

    @Test
    void nominalNaNDoesNotSelectLevelZero() {
        VarNominal var = VarNominal.copy("a", "b", "c");
        var.setDouble(0, Double.NaN);
        assertTrue(var.isMissing(0));
        assertEquals("?", var.getLabel(0));
        assertEquals("b", var.getLabel(1));
        var.setDouble(1, 2);
        assertEquals("c", var.getLabel(1), "non missing doubles are still level indexes");
    }

    @Test
    void longMissingSurvivesTheIntRoundTrip() {
        VarInt ints = VarInt.copy(1, 2, 3);
        ints.setMissing(1);
        VarLong longs = VarLong.copy(ints);
        assertTrue(longs.isMissing(1), "VarLong.copy(VarInt) must keep missing rows missing");
        assertEquals(1L, longs.getLong(0));
        assertEquals(3L, longs.getLong(2));
    }

    @Test
    void doubleMissingSurvivesTheIntAndLongRoundTrip() {
        VarDouble d = VarDouble.copy(1.5, Double.NaN, 3.5);
        VarInt i = VarInt.from(3, row -> d.getInt(row));
        assertTrue(i.isMissing(1));
        VarLong l = VarLong.from(3, row -> d.getLong(row));
        assertTrue(l.isMissing(1));
    }

    @Test
    void emptyStringVarIsAllMissing() {
        VarString var = VarString.empty(3);
        for (int i = 0; i < 3; i++) {
            assertTrue(var.isMissing(i), "row " + i);
            assertEquals(VarString.MISSING_VALUE, var.getLabel(i));
        }
        var.addRows(2);
        assertEquals(5, var.size());
        assertTrue(var.isMissing(4));
        // a null label is also missing
        var.setLabel(0, null);
        assertTrue(var.isMissing(0));
    }

    @Test
    void instantDeepEqualsHandlesMissingRows() {
        VarInstant a = VarInstant.from(3, row -> row == 1 ? null : Instant.ofEpochMilli(row));
        VarInstant b = VarInstant.from(3, row -> row == 1 ? null : Instant.ofEpochMilli(row));
        assertTrue(a.deepEquals(b));
        b.setMissing(2);
        assertFalse(a.deepEquals(b));
    }
}
