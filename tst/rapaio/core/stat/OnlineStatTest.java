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
import rapaio.core.CoreTools;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.data.IdxVar;
import rapaio.data.NumVar;
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

        Var index = IdxVar.seq(LEN);
        Var varLeft = NumVar.fill(LEN);
        Var varRight = NumVar.fill(LEN);
        Var varSum = NumVar.fill(LEN);

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
        Var a = NumVar.wrap(1, 2, 3, 13, 17, 30);
        Var b = NumVar.wrap(44, 5, 234, 12, 33, 1);
        Var ab = a.bindRows(b);
        OnlineStat soA = OnlineStat.empty();
        OnlineStat soB = OnlineStat.empty();
        a.stream().forEach(s -> soA.update(s.value()));
        b.stream().forEach(s -> soB.update(s.value()));

        OnlineStat soAll = OnlineStat.empty();
        soAll.update(soA);
        soAll.update(soB);

        soA.update(soB);

        assertEquals(soA.variance(), CoreTools.variance(ab).biasedValue(), 1e-12);
        assertEquals(soA.mean(), CoreTools.mean(ab).value(), 1e-30);

        assertEquals(soA.variance(), soAll.variance(), 1e-12);
        assertEquals(soA.mean(), soAll.mean(), 1e-30);
    }
}
