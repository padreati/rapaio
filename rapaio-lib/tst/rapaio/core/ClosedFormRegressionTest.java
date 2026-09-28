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

package rapaio.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.core.stat.Kurtosis;
import rapaio.core.stat.OnlineStat;
import rapaio.core.tests.ADTestGoodness;
import rapaio.core.tests.ChiSqConditionalIndependence;
import rapaio.core.tests.ChiSqIndependence;
import rapaio.core.tests.KSTestTwoSamples;
import rapaio.darray.DArrays;
import rapaio.darray.Shape;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.math.MathTools;

/**
 * Pins closed forms of sample statistics and hypothesis tests to reference values
 * (scipy 1.18 / statsmodels 0.15 / R semantics), see the docs review 2026-09-22.
 */
public class ClosedFormRegressionTest {

    @Test
    void digammaMatchesScipy() {
        double[][] ref = {
                {0.1, -10.423754940411076}, {0.5, -1.9635100260214235}, {1, -0.57721566490153287},
                {1.5, 0.03648997397857652}, {2, 0.42278433509846713}, {3.7, 1.1671535393615113},
                {10, 2.2517525890667209}, {25.5, 3.2189424728839198}, {100, 4.6001618527380881},
                {1e-3, -1000.5755719318103}, {1e6, 13.81551005796419}, {-0.5, 0.03648997397857652}
        };
        for (double[] r : ref) {
            assertEquals(r[1], MathTools.digamma(r[0]), Math.abs(r[1]) * 1e-13, "digamma(" + r[0] + ")");
        }
        assertTrue(Double.isNaN(MathTools.digamma(0)));
        assertTrue(Double.isNaN(MathTools.digamma(-3)));
    }

    @Test
    void sampleExcessKurtosisG2MatchesScipyUnbiased() {
        // scipy.stats.kurtosis([1, 2, 45, 109, 200], bias=False) == R e1071::kurtosis(type = 2)
        Kurtosis kt = Kurtosis.of(VarDouble.wrap(1, 2, 45, 109, 200));
        assertEquals(0.015935171025782235, kt.bigG2(), 1e-12);
        assertEquals(-0.9960162072435548, kt.g2(), 1e-12);
    }

    @Test
    void onlineStatMaxOfNegativeDataAndSumAfterMerge() {
        OnlineStat os = OnlineStat.empty();
        for (double v : new double[] {-3.0, -1.5, -0.5, -7.25}) {
            os.update(v);
        }
        assertEquals(-0.5, os.max(), 0);
        assertEquals(-7.25, os.min(), 0);
        assertEquals(-12.25, os.sum(), 1e-12);

        OnlineStat other = OnlineStat.empty();
        other.update(4);
        other.update(6);
        os.update(other);
        assertEquals(6, os.max(), 0);
        assertEquals(-12.25 + 10, os.sum(), 1e-12);
        assertEquals(6, os.n(), 0);
        assertEquals((-12.25 + 10) / 6, os.mean(), 1e-12);
    }

    @Test
    void ksTwoSampleStatisticIsTheEcdfDistance() {
        // scipy.stats.ks_2samp
        assertEquals(1.0, KSTestTwoSamples.from(VarDouble.wrap(1), VarDouble.wrap(2)).d(), 1e-12);
        assertEquals(1.0 / 3, KSTestTwoSamples.from(VarDouble.wrap(1, 2, 3), VarDouble.wrap(2, 3, 4)).d(), 1e-12);
        assertEquals(0.6, KSTestTwoSamples.from(VarDouble.wrap(1, 2, 3, 4, 5), VarDouble.wrap(3.5, 4.5, 10)).d(), 1e-12);
        // missing values are ignored rather than poisoning the statistic
        assertEquals(0.6, KSTestTwoSamples.from(VarDouble.wrap(1, Double.NaN, 2, 3, 4, 5),
                VarDouble.wrap(3.5, 4.5, Double.NaN, 10)).d(), 1e-12);
    }

    @Test
    void andersonDarlingBothParametersEstimatedMatchesStatsmodels() {
        // statsmodels.stats.diagnostic.normal_ad on this sample: A2 = 0.23421608387707238, p = 0.7800523873348982
        VarDouble x = VarDouble.wrap(-0.90207037730767281, 0.39564098627536382, 0.92003178751900894, 1.0822347983420022,
                1.2814150742335235, 1.2939121448528399, 1.3196870460749439, 1.3510375686175209, 1.3724545435042446,
                1.6381409111921172, 1.9755145418569253, 2.1433443556737855, 2.1871699672307687, 2.2957328990235411,
                2.3675148153128358, 2.6302752729094787, 2.6909410358623957, 2.7721050846902497, 2.9001481780274943,
                2.9663976849914224, 3.1320613951224319, 3.2556808063345706, 3.6094341595088628, 3.7308881287281568,
                3.7375015681649977, 3.8254652231919768, 3.8616420060157655, 3.9350186845040911, 4.0646183711066977,
                4.2319588451509915, 4.3011855756494022, 4.5009023916129145, 4.5555838708578964, 4.7569006026145448,
                4.7587959497256573, 4.8811294327824282, 5.2544824139360653, 5.2579445854417832, 5.4450826773480605,
                7.2832952017409225);
        ADTestGoodness test = ADTestGoodness.from(x);
        assertEquals(0.23421608387707238, test.a2(), 1e-10);
        assertEquals(0.7800523873348982, test.getPValueStar(), 1e-6);

        // mean known, sd estimated by maximum likelihood around the known mean: A2 = 0.285046314979958
        ADTestGoodness known = ADTestGoodness.from(x, 3.0, Double.NaN);
        assertEquals(0.285046314979958, known.a2(), 1e-10);
        assertTrue(Double.isNaN(known.getPValueStar()));

        // both parameters given: the two p-values coincide
        ADTestGoodness full = ADTestGoodness.from(x, 3.0, 2.0);
        assertEquals(full.pValue(), full.getPValueStar(), 0);
    }

    @Test
    void yatesCorrectionIsClampedAtTheDeviation() {
        // R: chisq.test(matrix(c(10, 10, 10, 11), 2)) -> X-squared = 0, p-value = 1
        ChiSqIndependence test = ChiSqIndependence.from(DArrays.stride(Shape.of(2, 2), 10, 10, 10, 11), true);
        assertEquals(0, test.getChiValue(), 1e-12);
        assertEquals(1, test.pValue(), 1e-12);
    }

    @Test
    void conditionalIndependenceInstancesAreIndependent() {
        VarNominal x = VarNominal.copy("a", "a", "b", "b", "a", "b", "a", "b", "a", "a", "b", "b").name("x");
        VarNominal y = VarNominal.copy("u", "v", "u", "v", "u", "v", "v", "u", "u", "v", "u", "v").name("y");
        VarNominal z = VarNominal.copy("p", "p", "p", "p", "p", "p", "q", "q", "q", "q", "q", "q").name("z");

        ChiSqConditionalIndependence first = ChiSqConditionalIndependence.from(x, y, z);
        ChiSqConditionalIndependence second = ChiSqConditionalIndependence.from(x, y, z);
        assertEquals(first.getStatistic(), second.getStatistic(), 0);
        assertEquals(first.pValue(), second.pValue(), 0);
        assertTrue(first.getStatistic() > 0);
    }
}
