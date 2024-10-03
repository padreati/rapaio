/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.core.distributions.Uniform;
import rapaio.data.Var;
import rapaio.data.VarDouble;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/10/17.
 */
public class WeightedOnlineStatTest {

    private static final double TOL = 1e-11;

    @Test
    void reverseTest() {

        Random random = new Random(124);
        Uniform unif = Uniform.of(0, 1);

        Var x = VarDouble.wrap(1, 2, 3, 4, 5, 6, 7, 10, 20);

        VarDouble w = VarDouble.from(x.size(), () -> unif.sampleNext(random));

        // normalize w
        w.tensor_().mul_(1.0 / w.tensor_().nanSum());

        WeightedOnlineStat left = WeightedOnlineStat.empty();
        for (int i = 0; i < x.size(); i++) {
            left.update(x.getDouble(i), w.getDouble(i));
        }

        WeightedOnlineStat right = WeightedOnlineStat.empty();
        for (int i = x.size() - 1; i >= 0; i--) {
            right.update(x.getDouble(i), w.getDouble(i));
        }

        assertEquals(left.variance(), right.variance(), TOL);
    }

    @Test
    void weightedTest() {

        Random random = new Random(123);

        Normal normal = Normal.of(0, 100);
        VarDouble x = VarDouble.from(100, () -> normal.sampleNext(random));
        VarDouble w = VarDouble.fill(100, 1);

        VarDouble wnorm = w.copy();
        wnorm.tensor_().mul_(1.0 / wnorm.tensor_().nanSum());

        WeightedOnlineStat wstat = WeightedOnlineStat.empty();
        WeightedOnlineStat wnstat = WeightedOnlineStat.empty();
        OnlineStat stat = OnlineStat.empty();

        for (int i = 0; i < x.size(); i++) {
            wstat.update(x.getDouble(i), w.getDouble(i));
            wnstat.update(x.getDouble(i), wnorm.getDouble(i));
            stat.update(x.getDouble(i));
        }

        assertEquals(wstat.mean(), stat.mean(), TOL);
        assertEquals(wstat.variance(), stat.variance(), TOL);

        assertEquals(wnstat.mean(), stat.mean(), TOL);
        assertEquals(wstat.variance(), stat.variance(), TOL);
    }

    @Test
    void multipleStats() {

        WeightedOnlineStat wos1 = WeightedOnlineStat.empty();
        WeightedOnlineStat wos2 = WeightedOnlineStat.empty();
        WeightedOnlineStat wos3 = WeightedOnlineStat.empty();

        WeightedOnlineStat wosTotal = WeightedOnlineStat.empty();

        Random random = new Random(1234L);
        Normal normal = Normal.of(0, 1);
        Uniform uniform = Uniform.of(0, 1);

        VarDouble x = VarDouble.from(100, () -> normal.sampleNext(random));
        VarDouble w = VarDouble.from(100, () -> uniform.sampleNext(random));

        double wsum = Sum.of(w).value();
        for (int i = 0; i < w.size(); i++) {
            w.setDouble(i, w.getDouble(i) / wsum);
        }

        for (int i = 0; i < 20; i++) {
            wos1.update(x.getDouble(i), w.getDouble(i));
        }
        for (int i = 20; i < 65; i++) {
            wos2.update(x.getDouble(i), w.getDouble(i));
        }
        for (int i = 65; i < 100; i++) {
            wos3.update(x.getDouble(i), w.getDouble(i));
        }

        for (int i = 0; i < 100; i++) {
            wosTotal.update(x.getDouble(i), w.getDouble(i));
        }

        WeightedOnlineStat t1 = WeightedOnlineStat.of(wos1, wos2, wos3);

        assertEquals(wosTotal.mean(), t1.mean(), TOL);
        assertEquals(wosTotal.variance(), t1.variance(), TOL);
        assertEquals(wosTotal.count(), t1.count(), TOL);
    }
}
