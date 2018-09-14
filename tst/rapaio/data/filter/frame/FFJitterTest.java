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
import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VRange;
import rapaio.data.VarBoolean;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.data.VarType;
import rapaio.data.filter.FFilter;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/10/16.
 */
public class FFJitterTest {

    @Test
    public void testSmoke() {

        Frame a = SolidFrame.byVars(
                VarDouble.fill(100, 0).withName("num1"),
                VarDouble.fill(100, 0).withName("num2"),
                VarBoolean.fill(100, true).withName("bin"),
                VarNominal.from(100, r -> String.valueOf(r % 10)).withName("nom")
        );

        Frame df1 = new FFJitter(VRange.onlyTypes(VarType.DOUBLE)).fitApply(a.solidCopy());

        Assert.assertTrue(a.removeVars("0~1").deepEquals(df1.removeVars("0~1")));
        Assert.assertFalse(a.mapVars("0~1").deepEquals(df1.mapVars("0~1")));

        FFilter filter = new FFJitter(VRange.onlyTypes(VarType.DOUBLE)).newInstance();
        filter.train(a.removeVars("num1"));

        Frame df2 = filter.apply(a.solidCopy());

        Assert.assertTrue(a.removeVars("num2").deepEquals(df2.removeVars("num2")));
        Assert.assertFalse(a.mapVars("num2").deepEquals(df2.mapVars("num2")));
    }
}
