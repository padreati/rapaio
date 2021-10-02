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

package rapaio.data.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/23/20.
 */
public class IndexLabelTest {

    @Test
    void testBuilders() {
        var index = IndexLabel.fromLabelValues("a", "b", "c");
        assertNotNull(index);
        assertEquals(3, index.size());
        assertEquals(Arrays.asList("a", "b", "c"), index.getValueStrings());

        index = IndexLabel.fromLabelValues(Arrays.asList("b", "c", "d"));
        assertNotNull(index);
        assertEquals(3, index.size());
        assertEquals(Arrays.asList("b", "c", "d"), index.getValueStrings());

        Var v = VarNominal.empty(0, "a", "b");
        index = IndexLabel.fromVarLevels(true, v);
        assertEquals(3, index.size());
        assertEquals(Arrays.asList("?", "a", "b"), index.getValueStrings());

        index = IndexLabel.fromVarLevels(false, v);
        assertNotNull(index);
        assertEquals(2, index.size());
        assertEquals(Arrays.asList("a", "b"), index.getValueStrings());

        var bin = VarBinary.empty(0);
        index = IndexLabel.fromVarLevels(true, bin);
        assertNotNull(index);
        assertEquals(3, index.size());
        assertEquals(Arrays.asList("?", "0", "1"), index.getValueStrings());

        index = IndexLabel.fromVarLevels(false, bin);
        assertNotNull(index);
        assertEquals(2, index.size());
        assertEquals(Arrays.asList("0", "1"), index.getValueStrings());

        var ex = assertThrows(IllegalArgumentException.class,
                () -> IndexLabel.fromVarLevels(true, VarDouble.empty()));
        assertEquals("Builder from levels not available for this type of variable.", ex.getMessage());
    }

    @Test
    void testWorkingWithStrings() {

        String[] values = new String[]{"a", "b", "c", "a", "b"};

        var index = IndexLabel.fromLabelValues(values);

        assertEquals(3, index.size());
        assertEquals("a", index.getValue(0));
        assertEquals("b", index.getValue(1));
        assertEquals("c", index.getValue(2));

        assertEquals("a", index.getValueString(0));
        assertEquals("b", index.getValueString(1));
        assertEquals("c", index.getValueString(2));

        assertTrue(index.containsValue("a"));
        assertTrue(index.containsValue("b"));
        assertTrue(index.containsValue("c"));
        assertFalse(index.containsValue("d"));
        assertFalse(index.containsValue("e"));

        assertEquals(0, index.getIndex("a"));
        assertEquals(1, index.getIndex("b"));
        assertEquals(2, index.getIndex("c"));
        assertEquals(-1, index.getIndex("d"));
        assertEquals(-1, index.getIndex("e"));

        assertEquals(Arrays.asList("a", "b", "c"), index.getValues());
    }

    @Test
    void testWorkingWithVars() {

        var v = VarNominal.copy("a", "b", "c", "a", "b", "?");

        var index = IndexLabel.fromVarLevels(true, v);

        assertEquals(4, index.size());
        assertEquals("a", index.getValue(v, 0));
        assertEquals("b", index.getValue(v, 1));
        assertEquals("c", index.getValue(v, 2));
        assertEquals("a", index.getValue(v, 3));
        assertEquals("b", index.getValue(v, 4));
        assertEquals("?", index.getValue(v, 5));

        assertTrue(index.containsValue(v, 0));
        assertTrue(index.containsValue(v, 1));
        assertTrue(index.containsValue(v, 2));

        assertEquals(Arrays.asList("a","b", "c", "a", "b", "?"), index.getValueList(v));

        assertEquals(1, index.getIndex(v, 0));
        assertEquals(2, index.getIndex(v, 1));
        assertEquals(3, index.getIndex(v, 2));
        assertEquals(1, index.getIndex(v, 3));
        assertEquals(2, index.getIndex(v, 4));
        assertEquals(0, index.getIndex(v, 5));

        assertEquals(Arrays.asList(1, 2, 3, 1, 2, 0), index.getIndexList(v));

        index = IndexLabel.fromVarLevels(false, v);

        assertEquals(3, index.size());
        assertEquals("a", index.getValue(v, 0));
        assertEquals("b", index.getValue(v, 1));
        assertEquals("c", index.getValue(v, 2));
        assertEquals("a", index.getValue(v, 3));
        assertEquals("b", index.getValue(v, 4));
        assertEquals("?", index.getValue(v, 5));

        assertTrue(index.containsValue("a"));
        assertTrue(index.containsValue("b"));
        assertTrue(index.containsValue("c"));
        assertFalse(index.containsValue("d"));
        assertFalse(index.containsValue("e"));
        assertFalse(index.containsValue("?"));

        assertEquals(Arrays.asList("a","b", "c", "a", "b"), index.getValueList(v));

        assertEquals(0, index.getIndex(v, 0));
        assertEquals(1, index.getIndex(v, 1));
        assertEquals(2, index.getIndex(v, 2));
        assertEquals(0, index.getIndex(v, 3));
        assertEquals(1, index.getIndex(v, 4));
        assertEquals(-1, index.getIndex(v, 5));

        assertEquals(Arrays.asList(0, 1, 2, 0, 1), index.getIndexList(v));
    }

    @Test
    void testWorkingWithFrames() {

        var df = SolidFrame.byVars(VarNominal.copy("a", "b", "c", "a", "b", "?").name("x"));

        var index = IndexLabel.fromVarLevels(true, df, "x");

        assertEquals(4, index.size());
        assertEquals("a", index.getValue(df, "x", 0));
        assertEquals("b", index.getValue(df, "x", 1));
        assertEquals("c", index.getValue(df, "x", 2));
        assertEquals("a", index.getValue(df, "x", 3));
        assertEquals("b", index.getValue(df, "x", 4));
        assertEquals("?", index.getValue(df, "x", 5));

        assertTrue(index.containsValue(df, "x", 0));
        assertTrue(index.containsValue(df, "x", 1));
        assertTrue(index.containsValue(df, "x", 2));

        assertEquals(Arrays.asList("a","b", "c", "a", "b", "?"), index.getValueList(df, "x"));

        assertEquals(1, index.getIndex(df, "x", 0));
        assertEquals(2, index.getIndex(df, "x", 1));
        assertEquals(3, index.getIndex(df, "x", 2));
        assertEquals(1, index.getIndex(df, "x", 3));
        assertEquals(2, index.getIndex(df, "x", 4));
        assertEquals(0, index.getIndex(df, "x", 5));

        assertEquals(Arrays.asList(1, 2, 3, 1, 2, 0), index.getIndexList(df, "x"));

        index = IndexLabel.fromVarLevels(false, df, "x");

        assertEquals(3, index.size());
        assertEquals("a", index.getValue(df, "x", 0));
        assertEquals("b", index.getValue(df, "x", 1));
        assertEquals("c", index.getValue(df, "x", 2));
        assertEquals("a", index.getValue(df, "x", 3));
        assertEquals("b", index.getValue(df, "x", 4));
        assertEquals("?", index.getValue(df, "x", 5));

        assertTrue(index.containsValue("a"));
        assertTrue(index.containsValue("b"));
        assertTrue(index.containsValue("c"));
        assertFalse(index.containsValue("d"));
        assertFalse(index.containsValue("e"));
        assertFalse(index.containsValue("?"));

        assertEquals(Arrays.asList("a","b", "c", "a", "b"), index.getValueList(df, "x"));

        assertEquals(0, index.getIndex(df, "x", 0));
        assertEquals(1, index.getIndex(df, "x", 1));
        assertEquals(2, index.getIndex(df, "x", 2));
        assertEquals(0, index.getIndex(df, "x", 3));
        assertEquals(1, index.getIndex(df, "x", 4));
        assertEquals(-1, index.getIndex(df, "x", 5));

        assertEquals(Arrays.asList(0, 1, 2, 0, 1), index.getIndexList(df, "x"));

    }
}
