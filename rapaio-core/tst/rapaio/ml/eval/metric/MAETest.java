/*
 *
 *  * Apache License
 *  * Version 2.0, January 2004
 *  * http://www.apache.org/licenses/
 *  *
 *  * Copyright 2013 - 2022 Aurelian Tutuianu
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package rapaio.ml.eval.metric;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 10/6/17.
 */
public class MAETest {

    private static final double TOL = 1e-20;

    @Test
    void smokeTest() {

        final int N = 100;
        Var x = VarDouble.fill(N, 0).name("x");
        Var y = VarDouble.seq(1, N).name("y");
        Var z = VarDouble.from(y, value -> -value).name("z");


        MAE mae1 = MAE.from(x, y);
        MAE mae2 = MAE.from(x, z);

        MAE mae3 = MAE.from(SolidFrame.byVars(x, x), SolidFrame.byVars(y, z));

        assertEquals((N + 1) / 2.0, mae1.mae(0), TOL);
        assertEquals(mae1.mae(0), mae1.totalMae(), TOL);

        assertEquals(mae1.mae(0), mae2.mae(0), TOL);

        double[] mae_values_3 = mae3.mae();
        assertEquals(mae1.mae(0), mae_values_3[0], TOL);
        assertEquals(mae2.mae(0), mae_values_3[1], TOL);

        assertEquals("""
                > MAE (Mean Absolute Error):

                names  mae\s
                x | y 50.5\s

                Total mae: 50.5

                """, mae1.toSummary());
    }
}
