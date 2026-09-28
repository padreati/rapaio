/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import rapaio.data.stream.VSpot;
import rapaio.data.transform.VarStandardScaler;

/**
 * The type switches in {@code data} used to carry holes: {@code FLOAT}, {@code LONG} and {@code INSTANT}
 * were missing from several of them and fell into a {@code default} branch which compared values as strings,
 * threw, or silently did nothing. Every switch involved is now exhaustive, so a new {@link VarType} is a
 * compile error rather than a runtime surprise; these tests pin the behaviour of the previously missing types.
 */
public class TypeSwitchContractTest {

    /**
     * {@code AbstractVar.copy()} serves mapped and bound variables. Without a {@code FLOAT} case it threw
     * {@code IllegalArgumentException("Variable type does not hav an implementation.")}.
     */
    @Test
    void mappedAndBoundFloatVariablesCanBeCopied() {
        VarFloat source = VarFloat.copy(1.5f, 2.5f, VarFloat.MISSING_VALUE, 4.5f).name("x");

        Var mapped = source.mapRows(0, 2, 3);
        Var mappedCopy = mapped.copy();
        assertEquals(VarType.FLOAT, mappedCopy.type());
        assertTrue(mapped.deepEquals(mappedCopy));
        assertEquals(1.5f, mappedCopy.getFloat(0));
        assertTrue(mappedCopy.isMissing(1));
        assertEquals(4.5f, mappedCopy.getFloat(2));

        Var bound = source.bindRows(VarFloat.copy(9.5f).name("x"));
        Var boundCopy = bound.copy();
        assertEquals(VarType.FLOAT, boundCopy.type());
        assertTrue(bound.deepEquals(boundCopy));
        assertEquals(5, boundCopy.size());

        // a copy is independent of its source
        mappedCopy.setFloat(0, 100f);
        assertEquals(1.5f, source.getFloat(0));
    }

    /**
     * Without a {@code FLOAT} case, {@code deepEquals} compared floats by their label, so the tolerance was
     * ignored entirely and two values equal within it were reported different.
     */
    @Test
    void deepEqualsAppliesToleranceToFloats() {
        VarFloat a = VarFloat.copy(1.0f, 2.0f).name("x");
        VarFloat b = VarFloat.copy(1.0f, 2.0009f).name("x");

        assertTrue(a.deepEquals(b, 1e-2), "values within tolerance must compare equal");
        assertFalse(a.deepEquals(b, 1e-6), "values outside tolerance must compare different");
        assertFalse(a.deepEquals(b), "the default tolerance is tight enough to separate them");
        assertTrue(a.deepEquals(a.copy()));

        // infinities are equal to themselves, and different from everything else
        VarDouble inf1 = VarDouble.copy(Double.POSITIVE_INFINITY, 1.0).name("x");
        VarDouble inf2 = VarDouble.copy(Double.POSITIVE_INFINITY, 1.0).name("x");
        VarDouble negInf = VarDouble.copy(Double.NEGATIVE_INFINITY, 1.0).name("x");
        assertTrue(inf1.deepEquals(inf2));
        assertFalse(inf1.deepEquals(negInf));
    }

    /**
     * {@code VarString.deepEquals(Var)} used to be overridden in a way which ignored the variable name, so it
     * disagreed with the inherited {@code deepEquals(Var, double)} that the rest of the library uses.
     */
    @Test
    void stringDeepEqualsOverloadsAgree() {
        VarString a = VarString.copy("x", "y").name("a");
        VarString b = VarString.copy("x", "y").name("b");

        assertEquals(a.deepEquals(b), a.deepEquals(b, 1e-12));
        assertFalse(a.deepEquals(b), "variables with different names are not deep equal");
        assertTrue(a.deepEquals(VarString.copy("x", "y").name("a")));
    }

    /**
     * {@code refComparator} fell back to a lexicographic comparison of labels for {@code FLOAT} and
     * {@code INSTANT}: floats sorted as "10.0" &lt; "9.0", and an instant with a fractional second sorted
     * before one without, because '.' precedes 'Z'.
     */
    @Test
    void refComparatorOrdersFloatsAndInstantsByValue() {
        VarFloat floats = VarFloat.copy(9f, 10f, 1f).name("x");
        assertEquals(-1, signum(floats.refComparator().compare(0, 1)), "9 must sort before 10");
        assertEquals(1, signum(floats.refComparator().compare(0, 2)), "9 must sort after 1");
        assertEquals(1, signum(floats.refComparator(false).compare(0, 1)), "descending reverses the order");

        Instant whole = Instant.parse("2026-01-01T00:00:00Z");
        Instant fraction = Instant.parse("2026-01-01T00:00:00.500Z");
        Var instants = VarInstant.from(2, row -> row == 0 ? whole : fraction).name("t");
        // the lexicographic order of the labels is the opposite of the chronological one
        assertTrue(instants.getLabel(1).compareTo(instants.getLabel(0)) < 0);
        assertEquals(-1, signum(instants.refComparator().compare(0, 1)), "the earlier instant must sort first");
    }

    /**
     * {@code RowComparators.longComparator} was {@code a < b ? -1 : 1}, so it never reported equality, which
     * breaks the comparator contract and the stability of any sort using it.
     */
    @Test
    void longComparatorReportsEquality() {
        VarLong values = VarLong.copy(7, 7, 9).name("x");
        assertEquals(0, values.refComparator().compare(0, 1), "equal longs must compare equal");
        assertEquals(-1, signum(values.refComparator().compare(0, 2)));
        assertEquals(1, signum(values.refComparator().compare(2, 0)));

        assertEquals(0, RowComparators.longComparator(values, true).compare(0, 1));
        assertEquals(0, RowComparators.longComparator(values, false).compare(0, 1));
    }

    /**
     * {@code VSpot.compareTo} compared {@code FLOAT} and {@code INSTANT} spots as strings, and threw
     * {@code NullPointerException} on the {@code null} label a missing {@code VarString} row returns.
     */
    @Test
    void spotComparisonFollowsTheVariableType() {
        VarFloat floats = VarFloat.copy(9f, 10f).name("x");
        assertEquals(-1, signum(new VSpot(0, floats).compareTo(new VSpot(1, floats))));

        Var instants = VarInstant.from(2,
                row -> Instant.parse(row == 0 ? "2026-01-01T00:00:00Z" : "2026-01-01T00:00:00.500Z")).name("t");
        assertEquals(-1, signum(new VSpot(0, instants).compareTo(new VSpot(1, instants))));

        VarLong longs = VarLong.copy(7, 7).name("x");
        assertEquals(0, new VSpot(0, longs).compareTo(new VSpot(1, longs)));

        // a missing VarString row has a null label; comparing it must not throw
        VarString strings = VarString.copy("a", "b").name("s");
        strings.setLabel(1, null);
        assertEquals(1, signum(new VSpot(0, strings).compareTo(new VSpot(1, strings))));
        assertEquals(-1, signum(new VSpot(1, strings).compareTo(new VSpot(0, strings))));
        assertEquals(0, new VSpot(1, strings).compareTo(new VSpot(1, strings)));
    }

    /**
     * {@code isNumeric} excluded {@code FLOAT} and {@code LONG}. It now answers "does this value have a
     * numeric meaning"; the separate {@code isFloatingPoint} answers "can an arbitrary real be stored here".
     */
    @Test
    void numericAndFloatingPointAreDistinctQuestions() {
        assertTrue(VarType.FLOAT.isNumeric());
        assertTrue(VarType.LONG.isNumeric());
        assertTrue(VarType.FLOAT.isFloatingPoint());
        assertFalse(VarType.LONG.isFloatingPoint());
        assertFalse(VarType.INT.isFloatingPoint());
        assertFalse(VarType.INSTANT.isNumeric());
        assertFalse(VarType.NOMINAL.isNumeric());

        // every floating point type is numeric
        for (VarType type : VarType.values()) {
            if (type.isFloatingPoint()) {
                assertTrue(type.isNumeric(), type.name());
            }
        }
    }

    /**
     * Standardization writes back in place, so it can only apply to a floating point variable: writing a
     * standardized value into an {@code INT} or {@code LONG} variable truncates it to a handful of integers.
     * The filter used to do exactly that to {@code INT} and {@code BINARY} variables.
     */
    @Test
    void standardScalerDoesNotTruncateIntegralVariables() {
        VarInt ints = VarInt.from(100, row -> row).name("x");
        Var scaledInts = ints.copy().fapply(VarStandardScaler.filter());
        assertTrue(ints.deepEquals(scaledInts.name("x")), "an int variable must be left untouched, not truncated");

        VarLong longs = VarLong.from(100, row -> (long) row * 1_000_000_000L).name("x");
        Var scaledLongs = longs.copy().fapply(VarStandardScaler.filter());
        assertTrue(longs.deepEquals(scaledLongs.name("x")), "a long variable must be left untouched");

        // a float variable is standardized, which it was not before
        VarFloat floats = VarFloat.from(100, row -> (float) row).name("x");
        Var scaledFloats = floats.copy().fapply(VarStandardScaler.filter());
        assertNotEquals(floats.getFloat(0), scaledFloats.getFloat(0), "a float variable must be standardized");
        double mean = 0;
        for (int row = 0; row < scaledFloats.size(); row++) {
            mean += scaledFloats.getDouble(row);
        }
        assertEquals(0.0, mean / scaledFloats.size(), 1e-5);
    }

    private static int signum(int value) {
        return Integer.signum(value);
    }
}
