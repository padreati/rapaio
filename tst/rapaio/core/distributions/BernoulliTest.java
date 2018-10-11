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

package rapaio.core.distributions;

import org.junit.Test;
import rapaio.core.RandomSource;
import rapaio.core.stat.Sum;
import rapaio.data.Var;

import static org.junit.Assert.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/17/16.
 */
public class BernoulliTest {

    private static final double TOL = 1e-12;

    @Test
    public void testBase() {

        RandomSource.setSeed(1234);

        Bernoulli b90 = Bernoulli.of(0.9);
        Bernoulli b80 = Bernoulli.of(0.8);
        Bernoulli b50 = Bernoulli.of(0.5);
        Bernoulli b10 = Bernoulli.of(0.1);

        int N = 10_000;
        Var x90 = b90.sample(N);
        Var x80 = b80.sample(N);
        Var x50 = b50.sample(N);
        Var x10 = b10.sample(N);

        System.out.println(Sum.of(x90).value()/N);
        System.out.println(Sum.of(x80).value()/N);
        System.out.println(Sum.of(x50).value()/N);
        System.out.println(Sum.of(x10).value()/N);

        assertEquals(9024, Sum.of(x90).value(), TOL);
        assertEquals(8075, Sum.of(x80).value(), TOL);
        assertEquals(5045, Sum.of(x50).value(), TOL);
        assertEquals(997, Sum.of(x10).value(), TOL);

        assertTrue(b90.discrete());
        assertEquals("Ber(p=0.9)", b90.name());

        assertEquals(0, b90.pdf(-1), TOL);
        assertEquals(0.1, b90.pdf(0), TOL);
        assertEquals(0, b90.pdf(0.5), TOL);
        assertEquals(0.9, b90.pdf(1), TOL);
        assertEquals(0, b90.pdf(1.1), TOL);

        assertEquals(0, b90.cdf(-1), TOL);
        assertEquals(0.1, b90.cdf(0), TOL);
        assertEquals(0.1, b90.cdf(0.5), TOL);
        assertEquals(1, b90.cdf(1), TOL);
        assertEquals(1, b90.cdf(1.1), TOL);

        assertEquals(0, b90.quantile(-1), TOL);
        assertEquals(0, b90.quantile(0), TOL);
        assertEquals(1, b90.quantile(0.5), TOL);
        assertEquals(1, b90.quantile(1), TOL);
        assertEquals(1, b90.quantile(1.1), TOL);

        assertEquals(0, b90.min(), TOL);
        assertEquals(1, b90.max(), TOL);
        assertEquals(0.9, b90.mean(), TOL);
        assertEquals(1, b90.mode(), TOL);

        assertEquals(0.08999999999999998, b90.var(), TOL);
        assertEquals(3.3333333333333335, b90.skewness(), TOL);
        assertEquals(5.1111111111111125, b90.kurtosis(), TOL);
        assertEquals(0.3250829733914482, b90.entropy(), TOL);

    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidProbabilityTest() {
        Bernoulli.of(12);
    }
}
