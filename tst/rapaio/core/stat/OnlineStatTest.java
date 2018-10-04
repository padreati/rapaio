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

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;

import static org.junit.Assert.assertEquals;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class OnlineStatTest {

    @Test
    public void testVariance() {

        RandomSource.setSeed(1223);

        int LEN = 1_000;
        Var v = Normal.std().sample(LEN);

        OnlineStat onlineStat = OnlineStat.empty();

        Var index = VarInt.seq(LEN);
        Var varLeft = VarDouble.fill(LEN);
        Var varRight = VarDouble.fill(LEN);
        Var varSum = VarDouble.fill(LEN);

        for (int i = 0; i < LEN; i++) {
            onlineStat.update(v.getDouble(i));
            if (i > 0) {
                varLeft.setDouble(i, onlineStat.variance());
            }
        }
        onlineStat.clean();
        for (int i = LEN - 1; i >= 0; i--) {
            onlineStat.update(v.getDouble(i));
            if (i < LEN - 1) {
                varRight.setDouble(i, onlineStat.variance());
            }
        }
        for (int i = 0; i < LEN; i++) {
            varSum.setDouble(i, (varLeft.getDouble(i) + varRight.getDouble(i)) / 2);
        }
    }

    @Test
    public void testParallelStat() {
        Var a = VarDouble.wrap(1, 2, 3, 13, 17, 30);
        Var b = VarDouble.wrap(44, 5, 234, 12, 33, 1);
        Var ab = a.bindRows(b);
        OnlineStat soA = OnlineStat.empty();
        OnlineStat soB = OnlineStat.empty();
        a.stream().forEach(s -> soA.update(s.getDouble()));
        b.stream().forEach(s -> soB.update(s.getDouble()));

        OnlineStat soAll = OnlineStat.empty();
        soAll.update(soA);
        soAll.update(soB);

        soA.update(soB);

        assertEquals(soA.variance(), Variance.of(ab).biasedValue(), 1e-12);
        assertEquals(soA.mean(), Mean.of(ab).value(), 1e-14);

        assertEquals(soA.variance(), soAll.variance(), 1e-12);
        assertEquals(soA.mean(), soAll.mean(), 1e-30);
    }
}
