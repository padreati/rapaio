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
import rapaio.data.IndexVar;
import rapaio.data.NumericVar;
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

        OnlineStat onlineStat = OnlineStat.empty();

        Var index = IndexVar.seq(LEN);
        Var varLeft = NumericVar.fill(LEN);
        Var varRight = NumericVar.fill(LEN);
        Var varSum = NumericVar.fill(LEN);

        for (int i = 0; i < LEN; i++) {
            onlineStat.update(v.getValue(i));
            if (i > 0) {
                varLeft.setValue(i, onlineStat.variance());
            }
        }
        onlineStat.clean();
        for (int i = LEN - 1; i >= 0; i--) {
            onlineStat.update(v.getValue(i));
            if (i < LEN - 1) {
                varRight.setValue(i, onlineStat.variance());
            }
        }
        for (int i = 0; i < LEN; i++) {
            varSum.setValue(i, (varLeft.getValue(i) + varRight.getValue(i)) / 2);
        }
    }

    @Test
    public void testParallelStat() {
        Var a = NumericVar.wrap(1, 2, 3, 13, 17, 30);
        Var b = NumericVar.wrap(44, 5, 234, 12, 33, 1);
        Var ab = a.bindRows(b);
        OnlineStat soA = OnlineStat.empty();
        OnlineStat soB = OnlineStat.empty();
        a.stream().forEach(s -> soA.update(s.getValue()));
        b.stream().forEach(s -> soB.update(s.getValue()));

        OnlineStat soAll = OnlineStat.empty();
        soAll.update(soA);
        soAll.update(soB);

        soA.update(soB);

        Assert.assertEquals(soA.variance(), CoreTools.variance(ab).getValue(), 1e-12);
        Assert.assertEquals(soA.mean(), CoreTools.mean(ab).getValue(), 1e-30);

        Assert.assertEquals(soA.variance(), soAll.variance(), 1e-12);
        Assert.assertEquals(soA.mean(), soAll.mean(), 1e-30);
    }
}
