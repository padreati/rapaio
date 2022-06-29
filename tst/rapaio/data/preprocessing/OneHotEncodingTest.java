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

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.data.VarRange;
import rapaio.data.VarType;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/4/18.
 */
public class OneHotEncodingTest {

    private static final double TOL = 1e-20;

    @BeforeEach
    void setUp() {
        RandomSource.setSeed(123);
    }

    @Test
    void testDouble() {
        Frame df = TransformTestUtil.allDoubleNominal(100, 2, 2);

        Frame f1 = df.fapply(OneHotEncoding.on(VarRange.onlyTypes(VarType.DOUBLE)));
        assertTrue(f1.deepEquals(df));

        Frame f2 = df.fapply(OneHotEncoding.on("v1,v2"));
        assertTrue(f2.deepEquals(df));
    }

    @Test
    void testNominal() {
        Frame df = TransformTestUtil.allDoubleNominal(100, 2, 2).mapVars(VarRange.of(2));

        List<String> levels = df.rvar(0).levels();

        Frame f1 = df.fapply(OneHotEncoding.on(false, true, "all"));
        assertEquals(levels.size(), f1.varCount());

        for (int i = 0; i < levels.size(); i++) {
            assertTrue(f1.varName(i).contains(levels.get(i)));
        }

        for (int i = 0; i < f1.rowCount(); i++) {
            double sum = 0;
            for (int j = 0; j < f1.varCount(); j++) {
                sum += f1.getDouble(i, j);
            }
            assertEquals(1.0, sum, TOL);
        }

        Frame f2 = df.fapply(OneHotEncoding.on(true, false, VarRange.all()).newInstance());
        assertEquals(levels.size() - 2, f2.varCount());

        for (int i = 2; i < levels.size(); i++) {
            assertTrue(f2.varName(i - 2).contains(levels.get(i)));
        }
    }
}
