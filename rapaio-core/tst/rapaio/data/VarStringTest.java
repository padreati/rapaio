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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/19/18.
 */
public class VarStringTest {

    private String[] largeValues;
    private final String[] shortValues = new String[] {"Ana", "are", "mere"};

    private Random random;

    @BeforeEach
    void beforeEach() {
        random = new Random(123);
        largeValues = new String[100];
        for (int i = 0; i < 100; i++) {
            StringBuilder sb = new StringBuilder();
            int len = random.nextInt(20) + 1;
            for (int j = 0; j < len; j++) {
                sb.append((char) ('a' + random.nextInt(26)));
            }
            largeValues[i] = sb.toString();
        }
    }

    @Test
    void testBuilders() {

        VarString empty1 = VarString.empty();
        assertEquals(0, empty1.size());

        VarString empty2 = VarString.empty(10);
        assertEquals(10, empty2.size());
        for (int i = 0; i < 10; i++) {
            assertNull(empty2.getLabel(i));
        }

        VarString empty3 = empty2.newInstance(empty2.size());
        assertTrue(empty2.deepEquals(empty3));

        VarString copy1 = VarString.copy(largeValues);
        assertTrue(copy1.deepEquals(VarString.copy(Arrays.asList(largeValues))));
        assertTrue(copy1.deepEquals(VarString.wrap(Arrays.asList(largeValues))));

        Iterator<String> it = Arrays.asList(largeValues).iterator();
        assertTrue(copy1.deepEquals(VarString.from(largeValues.length, it::next)));

        VarString copy2 = VarString.copy(largeValues).name("copy");
        assertEquals("copy", copy2.name());

        VarString copy3 = copy2.copy();
        assertTrue(copy2.deepEquals(copy3));

        assertEquals(
                "VarText [name:\"copy\", rowCount:100, values: omt, hyhvnlwuznrcbaqk, iyedusfwdkelqbxete, ovascfqio, maajxky, rnlrytgkbgic, ahcbrqdsxv, hpfqgtmdypsbzxvf, oeygjbumaa, k, ..., ldif, tciudeieeo]",
                copy2.toString());
    }

    @Test
    void testAddRemoveClear() {
        VarString text = VarString.copy(shortValues);
        text.addRows(3);

        assertEquals(6, text.size());
        for (int i = 0; i < 3; i++) {
            assertNull(text.getLabel(3 + i));
        }

        text.removeRow(2);
        text.removeRow(2);
        assertEquals("Ana", text.getLabel(0));
        assertEquals("are", text.getLabel(1));
        assertNull(text.getLabel(2));
        assertNull(text.getLabel(3));

        text.clearRows();
        assertEquals(0, text.size());
    }

    @Test
    void testGetDouble() {
        assertThrows(OperationNotAvailableException.class, () -> VarString.empty(1).getDouble(0));
    }

    @Test
    void testSetDouble() {
        assertThrows(OperationNotAvailableException.class, () -> VarString.empty(1).setDouble(0, 0));
    }

    @Test
    void testAddDouble() {
        assertThrows(OperationNotAvailableException.class, () -> VarString.empty(1).addDouble(10.f));
    }

    @Test
    void testGetInt() {
        assertThrows(OperationNotAvailableException.class, () -> VarString.empty(1).getInt(0));
    }

    @Test
    void testSetInt() {
        assertThrows(OperationNotAvailableException.class, () -> VarString.empty(1).setInt(0, 0));
    }

    @Test
    void testAddInt() {
        assertThrows(OperationNotAvailableException.class, () -> VarString.empty(1).addInt(10));
    }

    @Test
    void testGetLong() {
        assertThrows(OperationNotAvailableException.class, () -> VarString.empty(1).getLong(0));
    }

    @Test
    void testSetLong() {
        assertThrows(OperationNotAvailableException.class, () -> VarString.empty(1).setLong(0, 0));
    }

    @Test
    void testAddLong() {
        assertThrows(OperationNotAvailableException.class, () -> VarString.empty(1).addLong(10));
    }

    @Test
    void testSetLevels() {
        assertThrows(OperationNotAvailableException.class, () -> VarString.empty(1).setLevels(new String[] {}));
    }

    @Test
    void testLevels() {
        assertThrows(OperationNotAvailableException.class, () -> VarString.empty(1).levels());
    }

    @Test
    public void testAddSetLevel() {
        VarString x = VarString.copy("Ana");
        x.setLabel(0, "Maria");
        x.addLabel("John");

        assertEquals(2, x.size());
        assertEquals("Maria", x.getLabel(0));
        assertEquals("John", x.getLabel(1));
    }

    @Test
    void testMissingOperations() {
        VarString x = VarString.empty(1);
        assertEquals(1, x.size());
        assertNull(x.getLabel(0));

        x.setLabel(0, "l1");
        x.addLabel("l2");

        assertEquals("l1", x.getLabel(0));
        assertEquals("l2", x.getLabel(1));

        x.addLabel("l3");
        x.setMissing(1);
        x.addMissing();

        assertEquals("l1", x.getLabel(0));
        assertTrue(x.isMissing(1));
        assertEquals("l3", x.getLabel(2));
        assertTrue(x.isMissing(3));
    }
}
