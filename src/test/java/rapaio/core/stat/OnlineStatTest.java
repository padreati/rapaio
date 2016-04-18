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

package rapaio.core.stat;

import junit.framework.Assert;
import org.junit.Test;
import rapaio.core.CoreTools;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Index;
import rapaio.data.Numeric;
import rapaio.data.Var;

import static org.junit.Assert.assertEquals;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class OnlineStatTest {

    @Test
    public void testVariance() {

        RandomSource.setSeed(1223);

        int LEN = 1_000;
        Var v = new Normal(0, 1).sample(LEN);

        OnlineStat onlineStat = new OnlineStat();

        Var index = Index.seq(LEN);
        Var varLeft = Numeric.newFill(LEN);
        Var varRight = Numeric.newFill(LEN);
        Var varSum = Numeric.newFill(LEN);

        for (int i = 0; i < LEN; i++) {
            onlineStat.update(v.value(i));
            if (i > 0) {
                varLeft.setValue(i, onlineStat.variance());
            }
        }
        onlineStat.clean();
        for (int i = LEN - 1; i >= 0; i--) {
            onlineStat.update(v.value(i));
            if (i < LEN - 1) {
                varRight.setValue(i, onlineStat.variance());
            }
        }
        for (int i = 0; i < LEN; i++) {
            varSum.setValue(i, (varLeft.value(i) + varRight.value(i)) / 2);
        }
    }

    @Test
    public void testParallelStat() {
        Var a = Numeric.newWrap(1, 2, 3, 13, 17, 30);
        Var b = Numeric.newWrap(44, 5, 234, 12, 33, 1);
        Var ab = a.bindRows(b);
        OnlineStat soA = new OnlineStat();
        OnlineStat soB = new OnlineStat();
        a.stream().forEach(s -> soA.update(s.value()));
        b.stream().forEach(s -> soB.update(s.value()));

        OnlineStat soAll = new OnlineStat();
        soAll.update(soA);
        soAll.update(soB);

        soA.update(soB);

        Assert.assertEquals(soA.variance(), CoreTools.var(ab).value(), 1e-12);
        Assert.assertEquals(soA.mean(), CoreTools.mean(ab).value(), 1e-30);

        Assert.assertEquals(soA.variance(), soAll.variance(), 1e-12);
        Assert.assertEquals(soA.mean(), soAll.mean(), 1e-30);
    }

    @Test
    public void testWeightedStat() {

        Var a = Numeric.newWrap(1, 1, 2, 2, 2, 3, 3, 3, 3, 4);
        OnlineStat so1 = new OnlineStat();

        a.stream().forEach(s -> so1.update(s.value()));

        assertEquals(2.4, so1.mean(), 10e-12);
        assertEquals(0.9333333333333331, so1.variance(), 10e-12);

        WeightedOnlineStat so2 = new WeightedOnlineStat();
        so2.update(1, 1.5);
        so2.update(2, 3);
        so2.update(3, 4);
        so2.update(4, 0.88);
        so2.update(1, 0.5);
        so2.update(4, 0.12);

        assertEquals("mean", 2.4, so2.mean(), 10e-12);

    }
}
