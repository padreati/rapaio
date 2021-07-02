/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.data.filter;

import org.junit.jupiter.api.Test;
import rapaio.core.RandomSource;
import rapaio.data.Var;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 9/28/18.
 */
public class VShuffleTest {

    @Test
    void testShuffle() {
        RandomSource.setSeed(1);
        double N = 1000.0;
        Var x = VarDouble.seq(0, N, 1);
        Var first = VarDouble.empty();
        for (int i = 0; i < 100; i++) {
            Var y = x.fapply(VShuffle.filter());
            double t = y.stream().mapToDouble().sum();
            assertEquals(N * (N + 1) / 2, t, 1e-30);
            first.addDouble(y.getDouble(0));
        }
    }
}
