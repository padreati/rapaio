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

package rapaio.data.filter.var;

import org.junit.Assert;
import org.junit.Test;
import rapaio.data.*;

/**
 * Tests for variable type transformations
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/7/16.
 */
public class VFToTest {

    @Test
    public void testToNumeric() {
        Var num1 = Numeric.wrap(1.0, 2.0, 1.2, Double.NaN, 3.0, Double.NaN, 3.2);
        Var nom1 = Nominal.copy("1", "2", "1.2", "?", "3", "?", "3.2");
        Var nom2 = Nominal.copy("1", "2", "1.2", "mimi", "3", "lulu", "3.2");
        Var idx1 = Index.copy(1, 2, 3, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 4);
        Var bin1 = Binary.copy(1, 0, 1, -1, 1, -1, 0);

        // by default transformer

        Assert.assertTrue(Numeric.wrap(1, 2, 1.2, Double.NaN, 3, Double.NaN, 3.2)
                .deepEquals(num1.fitApply(VFToNumeric.byDefault())));

        Assert.assertTrue(Numeric.wrap(1, 2, 1.2, Double.NaN, 3, Double.NaN, 3.2)
                .deepEquals(nom1.fitApply(VFToNumeric.byDefault())));

        Assert.assertTrue(Numeric.wrap(1, 2, 1.2, Double.NaN, 3, Double.NaN, 3.2)
                .deepEquals(nom2.fitApply(VFToNumeric.byDefault())));

        Assert.assertTrue(Numeric.wrap(1, 2, 3, Double.NaN, 3, Double.NaN, 4)
                .deepEquals(idx1.fitApply(VFToNumeric.byDefault())));

        Assert.assertTrue(Numeric.wrap(1, 0, 1, Double.NaN, 1, Double.NaN, 0)
                .deepEquals(bin1.fitApply(VFToNumeric.byDefault())));

        // by spot transformer

        Assert.assertTrue(Numeric.wrap(1, 1, 1, 0, 1, 0, 1)
                .deepEquals(num1.fitApply(VFToNumeric.bySpot(s -> s.missing() ? 0.0 : 1.0))));

        // by value transformer

        Assert.assertTrue(Numeric.wrap(1, 2, 1.2, Double.NaN, 3, Double.NaN, 3.2)
                .deepEquals(num1.fitApply(VFToNumeric.byValue(x -> x))));

        // by index transformer

        Assert.assertTrue(Numeric.wrap(1, 2, 3, Double.NaN, 3, Double.NaN, 4)
                .deepEquals(idx1.fitApply(VFToNumeric.byIndex(x -> x == Integer.MIN_VALUE ? Double.NaN : Double.valueOf(x)))));

        // by label transformer

        Assert.assertTrue(num1
                .deepEquals(nom1.fitApply(VFToNumeric.byLabel(txt -> txt.equals("?") ? Double.NaN : Double.parseDouble(txt)))));
    }

    @Test
    public void testToIndex() {
        Var num1 = Numeric.wrap(1.0, 2.0, 1.2, Double.NaN, 3.0, Double.NaN, 3.2);
        Var nom1 = Nominal.copy("1", "2", "1.2", "?", "3", "?", "4");
        Var nom2 = Nominal.copy("1", "2", "1.2", "mimi", "3", "lulu", "3.2");
        Var idx1 = Index.copy(1, 2, 3, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 4);
        Var bin1 = Binary.copy(1, 0, 1, -1, 1, -1, 0);

        // by default transformer

        Assert.assertTrue(Index.wrap(1, 2, 1, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 3)
                .deepEquals(num1.fitApply(VFToIndex.byDefault())));

        Assert.assertTrue(Index.wrap(1, 2, Integer.MIN_VALUE, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 4)
                .deepEquals(nom1.fitApply(VFToIndex.byDefault())));

        Assert.assertTrue(Index.wrap(1, 2, Integer.MIN_VALUE, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, Integer.MIN_VALUE)
                .deepEquals(nom2.fitApply(VFToIndex.byDefault())));

        Assert.assertTrue(Index.wrap(1, 2, 3, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 4)
                .deepEquals(idx1.fitApply(VFToIndex.byDefault())));

        Assert.assertTrue(Index.wrap(1, 0, 1, Integer.MIN_VALUE, 1, Integer.MIN_VALUE, 0)
                .deepEquals(bin1.fitApply(VFToIndex.byDefault())));

        // by spot transformer

        Assert.assertTrue(Index.wrap(1, 1, 1, 0, 1, 0, 1)
                .deepEquals(num1.fitApply(VFToIndex.bySpot(s -> s.missing() ? 0 : 1))));

        // by value transformer

        Assert.assertTrue(Index.wrap(1, 2, 1, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 3)
                .deepEquals(num1.fitApply(VFToIndex.byValue(x -> Double.isNaN(x) ? Integer.MIN_VALUE : Double.valueOf(x).intValue()))));

        // by index transformer

        Assert.assertTrue(Index.wrap(1, 2, 3, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 4)
                .deepEquals(idx1.fitApply(VFToIndex.byIndex(x -> x))));

        // by label transformer

        Assert.assertTrue(Index.wrap(1, 2, Integer.MIN_VALUE, Integer.MIN_VALUE, 3, Integer.MIN_VALUE, 4)
                .deepEquals(nom1.fitApply(VFToIndex.byLabel(txt -> {
                    if (txt.equals("?"))
                        return Integer.MIN_VALUE;
                    try {
                        return Integer.parseInt(txt);
                    } catch (NumberFormatException e) {
                        return Integer.MIN_VALUE;
                    }
                }))));
    }

}
