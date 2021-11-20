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

package rapaio.ml.supervised.linear;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import rapaio.core.stat.Variance;
import rapaio.data.VarDouble;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/27/20.
 */
public class ScalingTest {

    @Test
    void scaleTest() {
        assertEquals(Scaling.NONE.compute(VarDouble.seq(10)), 1.0);
        assertEquals(Scaling.NONE.compute(VarDouble.seq(100)), 1.0);

        assertEquals(Scaling.SD.compute(VarDouble.seq(10)), Variance.of(VarDouble.seq(10)).biasedSdValue());
        assertEquals(Scaling.SD.compute(VarDouble.seq(100)), Variance.of(VarDouble.seq(100)).biasedSdValue());

        assertEquals(Scaling.NORM.compute(VarDouble.seq(10)), Math.sqrt(VarDouble.seq(10).dVec().apply(x -> x * x).nansum()));
        assertEquals(Scaling.NORM.compute(VarDouble.seq(100)), Math.sqrt(VarDouble.seq(100).dVec().apply(x -> x * x).nansum()));
    }
}
