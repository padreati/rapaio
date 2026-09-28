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

package rapaio.core.distributions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * Pins the closed-form moments and entropies of every {@link Distribution} to values
 * computed with scipy 1.18 ({@code scipy.stats.<dist>.stats(moments='mvsk')} and
 * {@code .entropy()}), so that a formula cannot silently drift from the reference.
 * <p>
 * Conventions used to map rapaio parameters onto scipy:
 * <ul>
 * <li>{@code Gamma(alpha, beta)}: {@code beta} is a <b>scale</b> ({@code gamma(alpha, scale=beta)}),
 * consistent with {@code pdf}/{@code cdf} and with {@code gamma.csv} (R {@code dgamma(shape, scale)});</li>
 * <li>{@code LogNormal(mu, sigma)}: {@code lognorm(s=sigma, scale=exp(mu))};</li>
 * <li>{@code StudentT(df, mu, sigma)}: {@code t(df, loc=mu, scale=sigma)};</li>
 * <li>{@code Exponential(lambda)}: rate, {@code expon(scale=1/lambda)};</li>
 * <li>{@code Hypergeometric(m, n, k)}: {@code hypergeom(M=m+n, n=m, N=k)};</li>
 * <li>{@code Binomial(p, n)}: {@code binom(n, p)}; {@code DUniform(a, b)} inclusive: {@code randint(a, b+1)}.</li>
 * </ul>
 * Entropies are in nats. Kurtosis is excess kurtosis.
 */
public class DistributionMomentsReferenceTest {

    private static final double REL = 1e-9;

    private record Case(String name, Distribution d, double mean, double var, double skew, double kurt, double entropy) {
    }

    private static final List<Case> CASES = List.of(
            new Case("Gamma(3,2)", Gamma.of(3, 2), 6, 12, 1.154700538379252, 2, 2.540725690922956),
            new Case("Gamma(0.5,0.5)", Gamma.of(0.5, 0.5), 0.25, 0.125, 2.82842712474619, 12, -0.6025372506459571),
            new Case("Gamma(5,5)", Gamma.of(5, 5), 25, 125, 0.8944271909999159, 1.2, 3.763021069054845),
            new Case("Gamma(1,1)", Gamma.of(1, 1), 1, 1, 2, 6, 1),
            new Case("Gamma(0.5,5)", Gamma.of(0.5, 5), 2.5, 12.5, 2.82842712474619, 12, 1.700047842348088),

            new Case("Normal(0,1)", Normal.std(), 0, 1, 0, 0, 1.418938533204673),
            new Case("Normal(10,2)", Normal.of(10, 2), 10, 4, 0, 0, 2.112085713764618),

            new Case("LogNormal(0,1)", LogNormal.of(0, 1), 1.648721270700128, 4.670774270471604, 6.184877138632554,
                    110.9363921763115, 1.418938533204673),
            new Case("LogNormal(0,0.5)", LogNormal.of(0, 0.5), 1.133148453066826, 0.3646958540123865, 1.750189655069718,
                    5.898445673784778, 0.7257913526447274),
            new Case("LogNormal(1,0.25)", LogNormal.of(1, 0.25), 2.804569356237226, 0.507288214182372, 0.7782516357974832,
                    1.095931274730179, 1.032644172084782),
            new Case("LogNormal(0.3,2)", LogNormal.of(0.3, 2), 9.974182454814722, 5332.175275721047, 414.359343300147,
                    9220556.977307005, 2.412085713764618),

            new Case("StudentT(5,2,3)", StudentT.of(5, 2, 3), 2, 15, 0, 6, 2.726114961082506),
            new Case("StudentT(1.5)", StudentT.of(1.5), 0, Double.POSITIVE_INFINITY, Double.NaN, Double.NaN, 2.149659467844649),
            new Case("StudentT(2)", StudentT.of(2), 0, Double.POSITIVE_INFINITY, Double.NaN, Double.NaN, 1.960279229160082),
            new Case("StudentT(3)", StudentT.of(3), 0, 3, Double.NaN, Double.POSITIVE_INFINITY, 1.773477571863291),
            new Case("StudentT(4)", StudentT.of(4), 0, 2, 0, Double.POSITIVE_INFINITY, 1.681760016878666),
            new Case("StudentT(10)", StudentT.of(10), 0, 1.25, 0, 1, 1.521262492975681),

            new Case("ChiSquare(1)", ChiSquare.of(1), 1, 2, 2.82842712474619, 12, 0.7837571104739336),
            new Case("ChiSquare(2)", ChiSquare.of(2), 2, 4, 2, 6, 1.693147180559945),
            new Case("ChiSquare(5)", ChiSquare.of(5), 5, 10, 1.264911064067352, 2.4, 2.423095090065),
            new Case("ChiSquare(10)", ChiSquare.of(10), 10, 20, 0.8944271909999159, 1.2, 2.84673033718069),

            new Case("Fisher(5,10)", Fisher.of(5, 10), 1.25, 1.354166666666667, 3.867020319812938, 50.86153846153847,
                    1.130759804909061),
            new Case("Fisher(3,4)", Fisher.of(3, 4), 2, Double.POSITIVE_INFINITY, Double.NaN, Double.NaN, 1.540376482443123),
            new Case("Fisher(10,20)", Fisher.of(10, 20), 1.111111111111111, 0.4320987654320987, 1.835192095981922,
                    6.893877551020408, 0.8032580797014504),
            new Case("Fisher(2,9)", Fisher.of(2, 9), 1.285714285714286, 2.975510204081633, 5.465943944999486,
                    146.4444444444445, 1.222222222222221),

            new Case("Exponential(1)", Exponential.of(1), 1, 1, 2, 6, 1),
            new Case("Exponential(2.5)", Exponential.of(2.5), 0.4, 0.16, 2, 6, 0.083709268125845),
            new Case("Exponential(0.2)", Exponential.of(0.2), 5, 25, 2, 6, 2.609437912434101),

            new Case("Uniform(0,1)", Uniform.of(0, 1), 0.5, 0.08333333333333333, 0, -1.2, 0),
            new Case("Uniform(-2,5)", Uniform.of(-2, 5), 1.5, 4.083333333333333, 0, -1.2, 1.945910149055313),

            new Case("Poisson(1)", Poisson.of(1), 1, 1, 1, 1, 1.304842242256252),
            new Case("Poisson(5)", Poisson.of(5), 5, 5, 0.4472135954999579, 0.2, 2.204395243428367),
            new Case("Poisson(10)", Poisson.of(10), 10, 10, 0.3162277660168379, 0.1, 2.561409935274912),
            new Case("Poisson(100)", Poisson.of(100), 100, 100, 0.1, 0.01, 3.720686072260421),
            new Case("Poisson(0.3)", Poisson.of(0.3), 0.3, 0.3, 1.825741858350554, 3.333333333333333, 0.6911439590927788),

            new Case("Bernoulli(0.3)", Bernoulli.of(0.3), 0.3, 0.21, 0.8728715609439694, -1.238095238095238,
                    0.6108643020548935),
            new Case("Bernoulli(0.5)", Bernoulli.of(0.5), 0.5, 0.25, 0, -2, 0.6931471805599453),

            new Case("DUniform(1,6)", DUniform.of(1, 6), 3.5, 2.916666666666667, 0, -1.268571428571428, 1.791759469228055),
            new Case("DUniform(-3,3)", DUniform.of(-3, 3), 0, 4, 0, -1.25, 1.945910149055313),

            new Case("Hypergeometric(20,20,30)", Hypergeometric.of(20, 20, 30), 15, 1.923076923076923, 0,
                    -0.1189189189189189, 1.745551479912629),
            new Case("Hypergeometric(70,70,100)", Hypergeometric.of(70, 70, 100), 50, 7.194244604316546, 0,
                    -0.02722627737226277, 2.405563313913633),
            new Case("Hypergeometric(10,2,5)", Hypergeometric.of(10, 2, 5), 4.166666666666667, 0.4419191919191919,
                    -0.2005706145689628, -0.7748571428571429, 0.9866546772648829),
            new Case("Hypergeometric(30,10,12)", Hypergeometric.of(30, 10, 12), 9, 1.615384615384615,
                    -0.1656412194672512, -0.1301903407166565, 1.654722233384343),
            new Case("Hypergeometric(2000,2000,2000)", Hypergeometric.of(2000, 2000, 2000), 1000, 250.0625156289072, 0,
                    -0.0004998749061796348, 4.17979400255507)
    );

    @TestFactory
    Stream<DynamicTest> momentsMatchScipy() {
        List<DynamicTest> tests = new ArrayList<>();
        for (Case c : CASES) {
            tests.add(DynamicTest.dynamicTest(c.name + ".mean", () -> close(c.mean, c.d.mean())));
            tests.add(DynamicTest.dynamicTest(c.name + ".var", () -> close(c.var, c.d.var())));
            tests.add(DynamicTest.dynamicTest(c.name + ".skewness", () -> close(c.skew, c.d.skewness())));
            tests.add(DynamicTest.dynamicTest(c.name + ".kurtosis", () -> close(c.kurt, c.d.kurtosis())));
            tests.add(DynamicTest.dynamicTest(c.name + ".entropy", () -> close(c.entropy, c.d.entropy())));
        }
        return tests.stream();
    }

    /**
     * The Binomial entropy is the standard {@code 0.5 log(2 pi e n p (1-p))} large-{@code n} approximation,
     * so it is compared with a loose tolerance only.
     */
    @Test
    void binomialEntropyApproximation() {
        assertEquals(1.277907356882021, Binomial.of(0.1, 10).entropy(), 0.1);
        assertEquals(2.511233116182558, Binomial.of(0.9, 100).entropy(), 0.02);
        assertEquals(2.223423915810263, Binomial.of(0.5, 20).entropy(), 0.02);
    }

    @Test
    void binomialMoments() {
        Binomial b = Binomial.of(0.1, 10);
        close(1, b.mean());
        close(0.9, b.var());
        close(0.8432740427115679, b.skewness());
        close(0.5111111111111112, b.kurtosis());
        b = Binomial.of(0.9, 100);
        close(90, b.mean());
        close(9, b.var());
        close(-0.2666666666666667, b.skewness());
        close(0.05111111111111116, b.kurtosis());
    }

    @Test
    void gammaMode() {
        close(4, Gamma.of(3, 2).mode());
        close(20, Gamma.of(5, 5).mode());
        close(0, Gamma.of(1, 1).mode());
        close(0, Gamma.of(0.5, 5).mode());
    }

    @Test
    void logNormalModeAndMedian() {
        close(0.3678794411714423, LogNormal.of(0, 1).mode());
        close(2.553589458062927, LogNormal.of(1, 0.25).mode());
        close(Math.E, LogNormal.of(1, 0.25).median());
    }

    @Test
    void hypergeometricSupportAndCdf() {
        // m=10 white, n=2 black, k=5 draws: support is [3, 5]
        Hypergeometric h = Hypergeometric.of(10, 2, 5);
        assertEquals(5, h.maxValue(), 0);
        assertEquals(0, h.cdf(2.5), 0);
        assertEquals(1, h.cdf(5), 1e-12);
        assertEquals(5, h.quantile(1.0), 0);
        assertEquals(4, h.quantile(0.5), 0);

        Hypergeometric h2 = Hypergeometric.of(20, 20, 30);
        assertEquals(20, h2.maxValue(), 0);
        assertEquals(15, h2.quantile(0.5), 0);
        assertEquals(20, h2.quantile(1.0), 0);
        assertEquals(1, h2.cdf(20), 1e-12);
        assertTrue(h2.cdf(19) < 1);
    }

    private static void close(double expected, double actual) {
        if (Double.isNaN(expected)) {
            assertTrue(Double.isNaN(actual), "expected NaN, got " + actual);
            return;
        }
        if (Double.isInfinite(expected)) {
            assertEquals(expected, actual, 0);
            return;
        }
        assertEquals(expected, actual, Math.max(1e-12, Math.abs(expected) * REL));
    }
}
