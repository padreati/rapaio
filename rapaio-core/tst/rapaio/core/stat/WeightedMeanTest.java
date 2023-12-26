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

package rapaio.core.stat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/25/14.
 */
public class WeightedMeanTest {

    private static final double TOL = 1e-12;

    @Test
    void testBasic() {

        VarDouble values = VarDouble.copy(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).name("x");

        VarDouble weights = VarDouble.fill(10, 1);

        assertEquals(5.5, WeightedMean.of(values, weights).value(), TOL);
        assertEquals(5.5, WeightedMean.of(SolidFrame.byVars(values), weights, "x").value(), TOL);

        weights = VarDouble.copy(1, 2, 1, 2, 1, 2, 1, 2, 1, 2).name("x");
        assertEquals(5.666666666666667, WeightedMean.of(values, weights).value(), 10e-12);
        assertEquals(5.666666666666667, WeightedMean.of(SolidFrame.byVars(values), weights, "x").value(), TOL);

        WeightedOnlineStat wos = WeightedOnlineStat.empty();
        for (int i = 0; i < values.size(); i++) {
            wos.update(values.getDouble(i), weights.getDouble(i));
        }
        assertEquals(WeightedMean.of(values, weights).value(), wos.mean(), TOL);

        assertEquals("""
                        > weightedMean[x]
                        total rows: 10 (complete: 10, missing: 0)
                        weightedMean: 5.6666667
                        """,
                WeightedMean.of(values, weights).toSummary());
    }

    @Test
    void testMissing() {

        VarDouble values = VarDouble.copy(Double.NaN, Double.NaN).name("x");
        VarDouble weights = VarDouble.copy(1, 1);

        assertTrue(Double.isNaN(WeightedMean.of(values, weights).value()));
        assertTrue(Double.isNaN(WeightedMean.of(SolidFrame.byVars(values), weights, "x").value()));

        values = VarDouble.copy(1, 2).name("x");
        weights = VarDouble.copy(Double.NaN, Double.NaN);
        assertTrue(Double.isNaN(WeightedMean.of(values, weights).value()));
        assertTrue(Double.isNaN(WeightedMean.of(SolidFrame.byVars(values), weights, "x").value()));

        values = VarDouble.copy(Double.NaN, 1, 2).name("x");
        weights = VarDouble.copy(1, 2, Double.NaN);
        assertEquals(1.0, WeightedMean.of(values, weights).value(), 10e-12);
        assertEquals(1.0, WeightedMean.of(SolidFrame.byVars(values), weights, "x").value(), 10e-12);
    }

}
