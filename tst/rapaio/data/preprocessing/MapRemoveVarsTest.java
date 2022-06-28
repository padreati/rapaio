/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.preprocessing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarRange;
import rapaio.data.VarType;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/10/16.
 */
public class MapRemoveVarsTest {

    private Frame df;

    @BeforeEach
    void setUp() {
        df = SolidFrame.byVars(
                VarDouble.fill(10, 1).name("a"),
                VarDouble.fill(10, 2).name("b"),
                VarDouble.fill(10, 3).name("c"),
                VarNominal.from(10, r -> String.valueOf(r % 3)).name("d")
        );
    }

    @Test
    void testMapVars() {
        assertMapEquals(VarRange.all());
        assertMapEquals(VarRange.onlyTypes(VarType.DOUBLE));
        assertMapEquals(VarRange.onlyTypes(VarType.NOMINAL));
    }

    private boolean assertMapEquals(VarRange varRange) {
        return df.mapVars(varRange).deepEquals(SelectVars.map(varRange).fapply(df));
    }

    @Test
    void testRemoveVars() {
        assertRemoveVars(VarRange.all());
        assertRemoveVars(VarRange.onlyTypes(VarType.DOUBLE));
        assertRemoveVars(VarRange.onlyTypes(VarType.NOMINAL));
    }

    private boolean assertRemoveVars(VarRange varRange) {
        return df.removeVars(varRange).deepEquals(RemoveVars.remove(varRange).fapply(df));
    }

    @Test
    void testBoth() {

        Frame df1 = df.mapVars(VarRange.onlyTypes(VarType.DOUBLE)).removeVars(VarRange.of(1));
        Frame df2 = RemoveVars.remove(VarRange.of(1)).fapply(SelectVars.map(VarRange.onlyTypes(VarType.DOUBLE)).fapply(df));

        assertTrue(df1.deepEquals(df2));
    }

    @Test
    void testInstance() {
        Transform map = SelectVars.map(VarRange.onlyTypes(VarType.DOUBLE)).newInstance();
        map.fit(df.mapVars("0,1"));

        assertEquals(2, df.apply(map).varCount());

        Transform remove = RemoveVars.remove(VarRange.onlyTypes(VarType.DOUBLE)).newInstance();
        remove.fit(df.mapVars("0,1"));

        assertEquals(2, remove.apply(df).varCount());
    }
}
