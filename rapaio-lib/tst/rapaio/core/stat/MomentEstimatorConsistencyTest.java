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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Exponential;
import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;

/**
 * {@link Skewness#value()} and {@link Kurtosis#value()} are the plug-in (method of moments)
 * estimators {@code g1}/{@code g2}, the same quantities {@link OnlineStat#skewness()} and
 * {@link OnlineStat#kurtosis()} return and the sample counterparts of
 * {@code Distribution.skewness()}/{@code kurtosis()}. Until 2026-09-28 they returned the
 * e1071 type 3 forms {@code b1}/{@code b2}, which no other estimator in the package uses.
 */
public class MomentEstimatorConsistencyTest {

    @Test
    void defaultsAgreeWithOnlineStatAndScipy() {
        VarDouble x = VarDouble.wrap(1, 2, 45, 109, 200);
        OnlineStat os = OnlineStat.empty();
        x.stream().forEach(s -> os.update(s.getDouble()));

        // scipy.stats.skew / kurtosis with the default bias=True
        assertEquals(0.6899293135253384, Skewness.of(x).value(), 1e-14);
        assertEquals(-0.9960162072435548, Kurtosis.of(x).value(), 1e-14);
        assertEquals(os.skewness(), Skewness.of(x).value(), 1e-12);
        assertEquals(os.kurtosis(), Kurtosis.of(x).value(), 1e-12);

        // the other two forms are still available under their own names
        assertEquals(1.0284858964749477, Skewness.of(x).bigG1(), 1e-14);
        assertEquals(0.493673230307975, Skewness.of(x).b1(), 1e-14);
        assertEquals(0.015935171025782235, Kurtosis.of(x).bigG2(), 1e-12);
        assertEquals(-1.7174503726358747, Kurtosis.of(x).b2(), 1e-14);
    }

    @Test
    void defaultsEstimateTheDistributionMoments() {
        Random random = new Random(7);
        int n = 200_000;
        VarDouble normal = Normal.of(3, 2).sample(random, n);
        VarDouble exponential = Exponential.of(1.5).sample(random, n);

        // Normal: skewness 0, excess kurtosis 0
        assertEquals(0, Skewness.of(normal).value(), 0.03);
        assertEquals(0, Kurtosis.of(normal).value(), 0.06);
        // Exponential: skewness 2, excess kurtosis 6 (heavy tail: wider sampling error)
        assertEquals(2, Skewness.of(exponential).value(), 0.1);
        assertEquals(6, Kurtosis.of(exponential).value(), 1.0);

        // the three forms coincide for large n
        Skewness sk = Skewness.of(exponential);
        assertTrue(Math.abs(sk.g1() - sk.bigG1()) < 1e-3 && Math.abs(sk.g1() - sk.b1()) < 1e-3);
    }
}
