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

package rapaio.data.filter;

import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.data.VarType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/3/18.
 */
public class FRetainTypesTest {

    @Test
    void testAll() {

        Frame src = FFilterTestUtil.allDoubleNominal(100, 5, 3);

        Frame allInt = src.fapply(FRetainTypes.on(VarType.INT));
        Frame allNom = src.fapply(FRetainTypes.on(VarType.NOMINAL));
        Frame allDouble = src.fapply(FRetainTypes.on(VarType.DOUBLE).newInstance());
        Frame all = src.fapply(FRetainTypes.on(VarType.DOUBLE, VarType.NOMINAL));

        assertEquals(100, allInt.rowCount());
        assertEquals(0, allInt.varCount());

        assertEquals(3, allNom.varCount());
        assertEquals(100, allNom.rowCount());

        assertEquals(5, allDouble.varCount());
        assertEquals(100, allDouble.rowCount());

        assertTrue(all.deepEquals(src));
    }
}
