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

package rapaio.data.transform;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarBinary;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarRange;
import rapaio.data.VarType;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/10/16.
 */
public class JitterTest {

    @Test
    void testSmoke() {

        Frame a = SolidFrame.byVars(
                VarDouble.fill(100, 0).name("num1"),
                VarDouble.fill(100, 0).name("num2"),
                VarBinary.fill(100, 1).name("bin"),
                VarNominal.from(100, r -> String.valueOf(r % 10)).name("nom")
        );

        Frame df1 = a.copy().fapply(Jitter.on(VarRange.onlyTypes(VarType.DOUBLE)));

        assertTrue(a.removeVars(VarRange.of("0~1")).deepEquals(df1.removeVars(VarRange.of("0~1"))));
        assertFalse(a.mapVars("0~1").deepEquals(df1.mapVars("0~1")));

        var filter = Jitter.on(VarRange.onlyTypes(VarType.DOUBLE)).newInstance();
        filter.fit(a.removeVars(VarRange.of("num1")));

        Frame df2 = filter.apply(a.copy());

        assertTrue(a.removeVars(VarRange.of("num2")).deepEquals(df2.removeVars(VarRange.of("num2"))));
        assertFalse(a.mapVars("num2").deepEquals(df2.mapVars("num2")));
    }

    @Test
    void testDouble() {
        Frame df = SolidFrame.byVars(VarDouble.from(100, () -> new Random().nextDouble()).name("x"));

        Frame df1 = df.copy().fapply(Jitter.on(new Random(111), VarRange.all()));
        Frame df2 = df.copy().fapply(Jitter.on(new Random(111), 0.1, VarRange.all()));
        Frame df3 = df.copy().fapply(Jitter.on(new Random(111), Normal.of(0.0, 0.1), VarRange.all()));

        assertTrue(df1.deepEquals(df2));
        assertTrue(df2.deepEquals(df3));

        var ff = Jitter.on(0.1, VarRange.all());
        ff.fit(df);
        assertArrayEquals(new String[] {"x"}, ff.varNames());
    }
}
