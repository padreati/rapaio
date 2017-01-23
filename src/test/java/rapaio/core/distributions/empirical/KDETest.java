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

package rapaio.core.distributions.empirical;

import org.junit.Before;
import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.core.tests.KSTest;
import rapaio.data.Numeric;
import rapaio.data.Var;

import static org.junit.Assert.assertTrue;

public class KDETest {


    private Normal normal = new Normal(0, 1);
    private Var sample;

    private Var x = Numeric.seq(-20, 20, 0.01);
    private Var y;

    @Before
    public void setUp() throws Exception {
        RandomSource.setSeed(1234);
        sample = normal.sample(1_000);
        y = Numeric.from(x, normal::pdf);
    }

    @Test
    public void testAll() {
        test(new KFuncGaussian());
        test(new KFuncBiWeight());
        test(new KFuncCosine());
        test(new KFuncEpanechnikov());
        test(new KFuncTriangular());
        test(new KFuncTricube());
        test(new KFuncTriweight());
        test(new KFuncUniform());
    }

    public void test(KFunc fun) {
        KDE kde = new KDE(sample, fun);
        Var z = Numeric.from(x, kde::pdf);
        Var delta = Numeric.from(x.rowCount(), row -> y.value(row)-z.value(row));
        Mean mean = Mean.from(delta);
        mean.printSummary();
        assertTrue(Math.abs(mean.value())<1e-5);
    }

}
