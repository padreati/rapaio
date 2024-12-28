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

package rapaio.data.transform;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.data.Var;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarLong;
import rapaio.data.VarNominal;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/28/18.
 */
public class VarToIntTransformTest {

    @Test
    void testToInt() {
        Var num1 = VarDouble.wrap(1.0, 2.0, 1.2, VarDouble.MISSING_VALUE, 3.0, VarDouble.MISSING_VALUE, 3.2);
        Var nom1 = VarNominal.copy("1", "2", "1.2", "?", "3", "?", "4");
        Var nom2 = VarNominal.copy("1", "2", "1.2", "mimi", "3", "lulu", "3.2");
        Var idx1 = VarInt.copy(1, 2, 3, VarInt.MISSING_VALUE, 3, VarInt.MISSING_VALUE, 4);
        Var bin1 = VarBinary.copy(1, 0, 1, -1, 1, -1, 0);

        // by default transformer

        assertTrue(VarInt.wrap(1, 2, 1, VarInt.MISSING_VALUE, 3, VarInt.MISSING_VALUE, 3)
                .deepEquals(num1.fapply(VarToIntTransform.byDefault())));

        assertTrue(VarInt.wrap(1, 2, VarInt.MISSING_VALUE, VarInt.MISSING_VALUE, 3, VarInt.MISSING_VALUE, 4)
                .deepEquals(nom1.fapply(VarToIntTransform.byDefault())));

        assertTrue(VarInt.wrap(1, 2, VarInt.MISSING_VALUE, VarInt.MISSING_VALUE, 3, VarInt.MISSING_VALUE, VarInt.MISSING_VALUE)
                .deepEquals(nom2.fapply(VarToIntTransform.byDefault())));

        assertTrue(VarInt.wrap(1, 2, 3, VarInt.MISSING_VALUE, 3, VarInt.MISSING_VALUE, 4)
                .deepEquals(idx1.fapply(VarToIntTransform.byDefault())));

        assertTrue(VarInt.wrap(1, 0, 1, VarInt.MISSING_VALUE, 1, VarInt.MISSING_VALUE, 0)
                .deepEquals(bin1.fapply(VarToIntTransform.byDefault())));

        // by spot transformer

        assertTrue(VarInt.wrap(1, 1, 1, VarInt.MISSING_VALUE, 1, VarInt.MISSING_VALUE, 1)
                .deepEquals(num1.fapply(VarToIntTransform.fromSpot(s -> s.isMissing() ? 0 : 1))));

        // by value transformer

        assertTrue(VarInt.wrap(1, 2, 1, VarInt.MISSING_VALUE, 3, VarInt.MISSING_VALUE, 3)
                .deepEquals(num1.fapply(VarToIntTransform.fromDouble(x -> Double.valueOf(x).intValue()))));

        // by index transformer

        assertTrue(VarInt.wrap(1, 2, 3, VarInt.MISSING_VALUE, 3, VarInt.MISSING_VALUE, 4)
                .deepEquals(idx1.fapply(VarToIntTransform.fromInt(x -> x))));

        // by label transformer

        assertTrue(VarInt.wrap(1, 2, Integer.MIN_VALUE, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 4)
                .deepEquals(nom1.fapply(VarToIntTransform.fromLabel(txt -> {
                    if (txt.equals("?")) {
                        return Integer.MIN_VALUE;
                    }
                    try {
                        return Integer.parseInt(txt);
                    } catch (NumberFormatException e) {
                        return Integer.MIN_VALUE;
                    }
                }))));
    }

    @Test
    void testUnsupportedLongToDouble() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> VarLong.wrap(1, 2, 3).fapply(VarToIntTransform.byDefault()));
        assertEquals("Variable type: long is not supported.", ex.getMessage());
    }
}
