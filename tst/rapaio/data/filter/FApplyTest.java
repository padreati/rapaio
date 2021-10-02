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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;
import rapaio.data.VarRange;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/3/18.
 */
public class FApplyTest {

    private static final double TOL = 1e-20;

    @Test
    void spotTest() {
        var df = SolidFrame.byVars(
                VarNominal.copy("a", "b", "c").name("x1"),
                VarDouble.copy(1, 2, 3).name("x2")
        );
        df.fapply(FApply.onSpot(s -> {
            s.setMissing(0);
            s.setMissing(1);
        }, VarRange.all()));

        for (int i = 0; i < df.rowCount(); i++) {
            for (int j = 0; j < df.varCount(); j++) {
                assertTrue(df.isMissing(i, j));
            }
        }
    }

    @Test
    void testDouble() {
        Frame df = FFilterTestUtil.allDoubles(100, 2);
        Frame sign = df.copy().fapply(FApply.onDouble(Math::signum, VarRange.all()));

        for (int i = 0; i < df.varCount(); i++) {
            for (int j = 0; j < df.rowCount(); j++) {
                assertEquals(Math.signum(df.getDouble(j, i)), sign.getDouble(j, i), TOL);
            }
        }

        Frame sign2 = df.copy().fapply(FApply.onDouble(Math::signum, VarRange.all()).newInstance());
        assertTrue(sign.deepEquals(sign2));
    }

    @Test
    void testInt() {
        Frame df = SolidFrame.byVars(VarInt.seq(1, 100).name("x"));
        Frame copy = df.copy().fapply(FApply.onInt(i -> i + 1, VarRange.all()));
        for (int j = 0; j < df.rowCount(); j++) {
            assertEquals(j + 2, copy.getInt(j, 0));
        }
    }

    @Test
    void testString() {
        Frame df = SolidFrame.byVars(VarNominal.copy("a", "b", "a", "b").name("x"));
        Frame copy = df.copy().fapply(FApply.onLabel(l -> "a".equals(l) ? "b" : "a", VarRange.all()));
        for (int j = 0; j < df.rowCount(); j++) {
            assertEquals(j % 2 == 0 ? "b" : "a", copy.getLabel(j, 0));
        }
    }
}
