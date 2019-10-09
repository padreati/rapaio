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
import org.junit.Before;
import org.junit.Test;
import rapaio.data.*;
import rapaio.data.filter.*;

import static org.junit.Assert.assertTrue;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/10/16.
 */
public class FMapRemoveVarsTest {

    private Frame df;

    @Before
    public void setUp() {
        df = SolidFrame.byVars(
                VarDouble.fill(10, 1).withName("a"),
                VarDouble.fill(10, 2).withName("b"),
                VarDouble.fill(10, 3).withName("c"),
                VarNominal.from(10, r -> String.valueOf(r%3)).withName("d")
        );
    }

    @Test
    public void testMapVars() {
        assertMapEquals(VRange.all());
        assertMapEquals(VRange.onlyTypes(VType.DOUBLE));
        assertMapEquals(VRange.onlyTypes(VType.NOMINAL));
    }

    private boolean assertMapEquals(VRange vRange) {
        return df.mapVars(vRange).deepEquals(FMapVars.map(vRange).fapply(df));
    }

    @Test
    public void testRemoveVars() {
        assertRemoveVars(VRange.all());
        assertRemoveVars(VRange.onlyTypes(VType.DOUBLE));
        assertRemoveVars(VRange.onlyTypes(VType.NOMINAL));
    }

    private boolean assertRemoveVars(VRange vRange) {
        return df.removeVars(vRange).deepEquals(FRemoveVars.remove(vRange).fapply(df));
    }

    @Test
    public void testBoth() {

        Frame df1 = df.mapVars(VRange.onlyTypes(VType.DOUBLE)).removeVars(VRange.of(1));
        Frame df2 = FRemoveVars.remove(VRange.of(1)).fapply(FMapVars.map(VRange.onlyTypes(VType.DOUBLE)).fapply(df));

        assertTrue(df1.deepEquals(df2));
    }

    @Test
    public void testInstance() {
        FFilter map = FMapVars.map(VRange.onlyTypes(VType.DOUBLE)).newInstance();
        map.fit(df.mapVars("0,1"));

        Assert.assertEquals(2, df.apply(map).varCount());

        FFilter remove = FRemoveVars.remove(VRange.onlyTypes(VType.DOUBLE)).newInstance();
        remove.fit(df.mapVars("0,1"));

        Assert.assertEquals(2, remove.apply(df).varCount());
    }
}
