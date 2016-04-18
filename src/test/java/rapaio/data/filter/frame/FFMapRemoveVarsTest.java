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

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import rapaio.data.*;
import rapaio.data.filter.FFilter;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/10/16.
 */
public class FFMapRemoveVarsTest {

    private Frame df;

    @Before
    public void setUp() throws Exception {
        df = SolidFrame.newByVars(
                Numeric.newFill(10, 1).withName("a"),
                Numeric.newFill(10, 2).withName("b"),
                Numeric.newFill(10, 3).withName("c"),
                Nominal.newFrom(10, r -> String.valueOf(r%3)).withName("d")
        );
    }

    @Test
    public void testMapVars() {
        assertMapEquals(VRange.all());
        assertMapEquals(VRange.onlyTypes(VarType.NUMERIC));
        assertMapEquals(VRange.onlyTypes(VarType.NOMINAL));
    }

    private boolean assertMapEquals(VRange vRange) {
        return df.mapVars(vRange).deepEquals(new FFMapVars(vRange).fitApply(df));
    }

    @Test
    public void testRemoveVars() {
        assertRemoveVars(VRange.all());
        assertRemoveVars(VRange.onlyTypes(VarType.NUMERIC));
        assertRemoveVars(VRange.onlyTypes(VarType.NOMINAL));
    }

    private boolean assertRemoveVars(VRange vRange) {
        return df.removeVars(vRange).deepEquals(new FFRemoveVars(vRange).fitApply(df));
    }

    @Test
    public void testBoth() {

        Frame df1 = df.mapVars(VRange.onlyTypes(VarType.NUMERIC)).removeVars(VRange.of(1));
        Frame df2 = new FFRemoveVars(VRange.of(1)).fitApply(new FFMapVars(VRange.onlyTypes(VarType.NUMERIC)).fitApply(df));

        Assert.assertTrue(df1.deepEquals(df2));
    }

    @Test
    public void testInstance() {
        FFilter map = new FFMapVars(VRange.onlyTypes(VarType.NUMERIC)).newInstance();
        map.train(df.mapVars("0,1"));

        Assert.assertEquals(2, map.apply(df).varCount());

        FFilter remove = new FFRemoveVars(VRange.onlyTypes(VarType.NUMERIC)).newInstance();
        remove.train(df.mapVars("0,1"));

        Assert.assertEquals(2, remove.apply(df).varCount());
    }
}
