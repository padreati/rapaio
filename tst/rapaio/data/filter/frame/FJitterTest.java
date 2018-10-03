/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.data.filter.frame;

import org.junit.Assert;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.VType;
import rapaio.data.VarBoolean;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.filter.FFilter;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/10/16.
 */
public class FJitterTest {

    @Test
    public void testSmoke() {

        Frame a = SolidFrame.byVars(
                VarDouble.fill(100, 0).withName("num1"),
                VarDouble.fill(100, 0).withName("num2"),
                VarBoolean.fill(100, true).withName("bin"),
                VarNominal.from(100, r -> String.valueOf(r % 10)).withName("nom")
        );

        Frame df1 = a.solidCopy().fapply(FJitter.on(VRange.onlyTypes(VType.DOUBLE)));

        assertTrue(a.removeVars(VRange.of("0~1")).deepEquals(df1.removeVars(VRange.of("0~1"))));
        Assert.assertFalse(a.mapVars("0~1").deepEquals(df1.mapVars("0~1")));

        FFilter filter = FJitter.on(VRange.onlyTypes(VType.DOUBLE)).newInstance();
        filter.fit(a.removeVars(VRange.of("num1")));

        Frame df2 = filter.apply(a.solidCopy());

        assertTrue(a.removeVars(VRange.of("num2")).deepEquals(df2.removeVars(VRange.of("num2"))));
        Assert.assertFalse(a.mapVars("num2").deepEquals(df2.mapVars("num2")));
    }

    @Test
    public void testDouble() {
        Frame df = SolidFrame.byVars(VarDouble.from(100, RandomSource::nextDouble).withName("x"));

        RandomSource.setSeed(111);
        Frame df1 = df.solidCopy().fapply(FJitter.on(VRange.all()));
        RandomSource.setSeed(111);
        Frame df2 = df.solidCopy().fapply(FJitter.on(0.1, VRange.all()));
        RandomSource.setSeed(111);
        Frame df3 = df.solidCopy().fapply(FJitter.on(new Normal(0.0, 0.1), VRange.all()));

        assertTrue(df1.deepEquals(df2));
        assertTrue(df2.deepEquals(df3));

        FFilter ff = FJitter.on(0.1, VRange.all());
        ff.fit(df);
        assertArrayEquals(new String[]{"x"}, ff.varNames());
    }
}
