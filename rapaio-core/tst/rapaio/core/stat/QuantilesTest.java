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

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Normal;
import rapaio.data.VarDouble;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/10/18.
 */
public class QuantilesTest {

    private static final double TOL = 1e-12;

    private Random random;

    @BeforeEach
    void setUp() {
        random = new Random(123);
    }

    @Test
    void testDoubleR7() {

        Normal normal = Normal.std();
        VarDouble x = VarDouble.from(1_000_000, () -> normal.sampleNext(random));

        Quantiles q = Quantiles.of(x, 0, 0.025, 0.5, 0.975, 1);
        double[] qq = q.values();

        assertEquals(Minimum.of(x).value(), qq[0], TOL);
        assertEquals(Maximum.of(x).value(), qq[4], TOL);

        // aprox -1.96
        assertEquals(-1.9562361490537725, qq[1], TOL);

        // aprox 0
        assertEquals(0.0011999109845885958, qq[2], TOL);

        // aprox 1.96
        assertEquals(1.9615631844255106, qq[3], TOL);
    }

    @Test
    void testDoubleR8() {

        Normal normal = Normal.std();
        VarDouble x = VarDouble.from(1_000_000, () -> normal.sampleNext(random));

        Quantiles q = Quantiles.of(x, Quantiles.Type.R8, 0, 0.025, 0.5, 0.975, 1);
        double[] qq = q.values();

        assertEquals(Minimum.of(x).value(), qq[0], TOL);
        assertEquals(Maximum.of(x).value(), qq[4], TOL);

        // aprox -1.96
        assertEquals(-1.9562507056994485, qq[1], TOL);

        // aprox 0
        assertEquals(0.0011999109845885958, qq[2], TOL);

        // aprox 1.96
        assertEquals(1.9615708871881077, qq[3], TOL);
    }
}
