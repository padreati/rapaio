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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/14/18.
 */
public class VarTypeTest {

    private final VarType[] types = new VarType[]{
            VarType.BINARY, VarType.INT, VarType.LONG, VarType.FLOAT, VarType.DOUBLE,
            VarType.NOMINAL, VarType.INSTANT, VarType.STRING};

    @Test
    void testNewInstance() {
        // every type must build an empty instance of itself, and one of the requested size
        assertEquals(VarType.values().length, types.length, "every VarType must be covered by this test");
        for (VarType type : types) {
            Var empty = type.newInstance();
            assertEquals(type, empty.type(), type.name());
            assertEquals(0, empty.size(), type.name());
            assertTrue(empty.deepEquals(type.newInstance()), type.name());

            Var sized = type.newInstance(3);
            assertEquals(type, sized.type(), type.name());
            assertEquals(3, sized.size(), type.name());
            for (int row = 0; row < 3; row++) {
                assertTrue(sized.isMissing(row), type.name() + " row " + row);
            }
        }
    }

    @Test
    void testIsCategory() {
        //                                        bin    int   long   flt    dbl    nom   instant  str
        boolean[] numeric = new boolean[]{true, true, true, true, true, false, false, false};
        boolean[] floating = new boolean[]{false, false, false, true, true, false, false, false};
        boolean[] nominal = new boolean[]{false, false, false, false, false, true, false, false};
        String[] code = new String[]{"bin", "int", "long", "flt", "dbl", "nom", "instant", "str"};

        assertEquals(VarType.values().length, types.length, "every VarType must be covered by this test");
        for (int i = 0; i < types.length; i++) {
            assertEquals(numeric[i], types[i].isNumeric(), types[i].name());
            assertEquals(floating[i], types[i].isFloatingPoint(), types[i].name());
            assertEquals(nominal[i], types[i].isNominal(), types[i].name());
            assertEquals(code[i], types[i].code(), types[i].name());
        }
    }
}
