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

package rapaio.ml.regression.linear;

import org.junit.jupiter.api.Test;
import rapaio.core.stat.Mean;
import rapaio.data.VarDouble;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/27/20.
 */
public class CenteringTest {

    @Test
    void centeringTest() {
        assertEquals(Centering.NONE.compute(null), 0.0);
        assertEquals(Centering.NONE.compute(VarDouble.seq(10)), 0.0);

        assertEquals(Centering.MEAN.compute(VarDouble.seq(10)), Mean.of(VarDouble.seq(10)).value());
        assertEquals(Centering.MEAN.compute(VarDouble.seq(100)), Mean.of(VarDouble.seq(100)).value());
    }
}
