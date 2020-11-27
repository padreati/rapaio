/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.data.Var;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.*;

public class KDETest {

    private static final double TOL = 1e-5;

    private Var sample;
    private Var x = VarDouble.seq(-20, 20, 0.01);
    private Var y;

    @BeforeEach
    public void beforeEach() {
        RandomSource.setSeed(1234);
        sample = Normal.of(0, 1).sample(1_000);
        y = VarDouble.from(x, Normal.of(0, 1)::pdf);
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

    @Test
    void testNames() {
        assertEquals("KFuncGaussian", new KFuncGaussian().toSummary());
        assertEquals("KFuncBiWeight", new KFuncBiWeight().toSummary());
        assertEquals("KFuncCosine", new KFuncCosine().toSummary());
        assertEquals("KFuncEpanechnikov", new KFuncEpanechnikov().toSummary());
        assertEquals("KFuncTriangular", new KFuncTriangular().toSummary());
        assertEquals("KFuncTricube", new KFuncTricube().toSummary());
        assertEquals("KFuncTriweight", new KFuncTriweight().toSummary());
        assertEquals("KFuncUniform", new KFuncUniform().toSummary());
    }

    private void test(KFunc fun) {
        KDE kde = KDE.of(sample, fun);
        Var z = VarDouble.from(x, kde::pdf);
        Var delta = VarDouble.from(x.size(), row -> y.getDouble(row) - z.getDouble(row));
        Mean mean = Mean.of(delta);
        assertTrue(Math.abs(mean.value()) < TOL);
    }

    @Test
    void testOtherThings() {
        assertEquals(0, new KFuncCosine().pdf(10, 1, 2), TOL);
        assertEquals(0, new KFuncTricube().pdf(10, 1, 2), TOL);
        assertEquals(0, new KFuncTriweight().pdf(10, 1, 2), TOL);
        assertEquals(0, new KFuncUniform().pdf(10, 1, 2), TOL);

        assertEquals("KFuncGaussian", KDE.of(VarDouble.wrap(1, 2, 3, 4)).kernel().toSummary());
        assertEquals(1.037094286807564, KDE.of(VarDouble.wrap(1, 2, 3, 4)).bandwidth(), TOL);
    }

    @Test
    void testBuilders() {
        VarDouble sample = VarDouble.from(100, Normal.std()::sampleNext);

        assertEquals("KFuncGaussian", KDE.of(sample).kernel().toSummary());
        assertEquals("KFuncGaussian", KDE.of(sample, 10).kernel().toSummary());
        assertEquals("KFuncGaussian", KDE.of(sample, new KFuncGaussian()).kernel().toSummary());
        assertEquals("KFuncGaussian", KDE.of(sample, new KFuncGaussian(), 10).kernel().toSummary());
    }
}
