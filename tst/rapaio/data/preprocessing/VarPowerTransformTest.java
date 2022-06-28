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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.RandomSource;
import rapaio.core.correlation.CorrPearson;
import rapaio.core.correlation.CorrSpearman;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Variance;
import rapaio.data.Var;
import rapaio.data.VarDouble;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/28/18.
 */
public class VarPowerTransformTest {

    @BeforeEach
    void beforeEach() {
        RandomSource.setSeed(1);
    }

    @Test
    void testTransformPositiveLambda() {
        Var x = Normal.std().sample(1000).stream().mapToDouble(s -> Math.pow(s.getDouble(), 2)).boxed().collect(VarDouble.collector());
        Var y = x.copy().fapply(VarPowerTransform.with(0.2));

        assertEquals(1.459663, Variance.of(x).sdValue(), 1e-6);
        assertEquals(0.5788231, Variance.of(y).sdValue(), 1e-6);

        assertEquals(0.8001133350403581, CorrPearson.of(x, y).matrix().get(0, 1), 1e-6);
        assertEquals(1, CorrSpearman.of(x, y).matrix().get(0, 1), 1e-6);
    }

    @Test
    void testTransformZeroLambda() {
        Var x = Normal.std().sample(1000).stream().mapToDouble(s -> Math.pow(s.getDouble(), 2)).boxed().collect(VarDouble.collector());
        Var y = x.copy().fapply(VarPowerTransform.with(0));

        assertEquals(1.459663, Variance.of(x).sdValue(), 1e-6);
        assertEquals(0.6713084463366682, Variance.of(y).sdValue(), 1e-6);

        assertEquals(0.6406002413733152, CorrPearson.of(x, y).matrix().get(0, 1), 1e-6);
        assertEquals(1, CorrSpearman.of(x, y).matrix().get(0, 1), 1e-6);
    }

    @Test
    void testNegativeValues() {
        var ex = assertThrows(IllegalArgumentException.class,
                () -> VarDouble.from(100, row -> Normal.std().sampleNext()).fapply(VarPowerTransform.with(10)).printContent());
        assertEquals("The source variable ? contains negative values, geometric mean cannot be computed", ex.getMessage());
    }

}
