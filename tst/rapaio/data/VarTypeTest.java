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


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/14/18.
 */
public class VarTypeTest {

    private VarType[] types = new VarType[]{
            VarType.BINARY, VarType.INT, VarType.LONG, VarType.DOUBLE, VarType.NOMINAL, VarType.STRING};

    @Test
    void testNewInstance() {

        VarDouble varDouble = VarDouble.empty();
        VarLong varLong = VarLong.empty();
        VarInt varInt = VarInt.empty();
        VarBinary varBinary = VarBinary.empty();
        VarNominal varNominal = VarNominal.empty();
        VarString varString = VarString.empty();

        assertTrue(varDouble.deepEquals(varDouble.type().newInstance()));
        assertTrue(varLong.deepEquals(varLong.type().newInstance()));
        assertTrue(varInt.deepEquals(varInt.type().newInstance()));
        assertTrue(varBinary.deepEquals(varBinary.type().newInstance()));
        assertTrue(varNominal.deepEquals(varNominal.type().newInstance()));
        assertTrue(varString.deepEquals(varString.type().newInstance()));
    }

    @Test
    void testIsCategory() {
        boolean[] numeric = new boolean[]{true, true, false, true, false, false};
        boolean[] nominal = new boolean[]{false, false, false, false, true, false};
        String[] code = new String[]{"bin", "int", "long", "dbl", "nom", "str"};

        for (int i = 0; i < types.length; i++) {
            assertEquals(numeric[i], types[i].isNumeric());
            assertEquals(nominal[i], types[i].isNominal());
            assertEquals(code[i], types[i].code());
        }
    }
}
