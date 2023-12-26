/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.preprocessing;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Gamma;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/2/18.
 */
public class VarStandardScalerTest {

    @Test
    void testDouble() {

        Distribution d = Gamma.of(0.5, 2);
        Random random = new Random(42);
        VarDouble x = VarDouble.from(1000, () -> d.sampleNext(random));

        double mean = Mean.of(x).value();
        double sd = Variance.of(x).sdValue();

        Var m1 = x.copy().fapply(VarStandardScaler.filter());
        Var m2 = x.copy().fapply(VarStandardScaler.filter(mean));
        Var m3 = x.copy().fapply(VarStandardScaler.filter(mean, sd));

        assertTrue(m1.deepEquals(m2));
        assertTrue(m2.deepEquals(m3));
    }

    @Test
    void testConstant() {
        VarDouble x = VarDouble.fill(100, 10);
        Var sd = x.copy().fapply(VarStandardScaler.filter());
        assertTrue(x.deepEquals(sd));
    }

    @Test
    void testNonNumeric() {
        VarNominal x = VarNominal.copy("a", "b");
        Var sd = x.fapply(VarStandardScaler.filter());
        assertTrue(x.deepEquals(sd));
    }
}
