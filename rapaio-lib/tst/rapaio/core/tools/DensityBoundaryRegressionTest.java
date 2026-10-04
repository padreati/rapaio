/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.core.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import rapaio.data.VarDouble;
import rapaio.data.VarNominal;

/**
 * Regression tests for the counting tools: a missing column value must not be counted in some other cell,
 * and the cached total of a density vector must follow a normalization.
 */
public class DensityBoundaryRegressionTest {

    private static final double TOL = 1e-12;

    @Test
    void binaryLevelWeightsSkipAMissingColumnValueWhenMissingIsNotCounted() {
        // the missing column value has no index, -1 used to be written as a column and hit a neighbour cell
        var rows = VarNominal.copy("a", "b", "a", "b");
        var cols = VarNominal.copy("x", "?", "y", "x");
        var dt = DensityTable.fromBinaryLevelWeights(false, rows, cols, null, "a");

        assertEquals(1.0, dt.get("a", "x"), TOL);
        assertEquals(1.0, dt.get("a", "y"), TOL);
        assertEquals(1.0, dt.get("other", "x"), TOL);
        assertEquals(0.0, dt.get("other", "y"), TOL);
    }

    @Test
    void binaryLevelWeightsAgreeWithTheGenericBuilderOnMissingColumnValues() {
        var rows = VarNominal.copy("a", "b", "a", "b", "a");
        var cols = VarNominal.copy("x", "?", "y", "x", "?");
        var weights = VarDouble.wrap(1, 2, 3, 4, 5);
        var binary = DensityTable.fromBinaryLevelWeights(false, rows, cols, weights, "a");
        var generic = DensityTable.fromLabels(false, rows, cols, weights);

        for (String col : new String[] {"x", "y"}) {
            assertEquals(generic.get("a", col), binary.get("a", col), TOL, "a/" + col);
            assertEquals(generic.get("b", col), binary.get("other", col), TOL, "b/" + col);
        }
    }

    @Test
    void binaryLevelWeightsCountTheMissingColumnValueWhenMissingIsCounted() {
        var rows = VarNominal.copy("a", "b");
        var cols = VarNominal.copy("x", "?");
        var dt = DensityTable.fromBinaryLevelWeights(true, rows, cols, null, "a");

        assertEquals(1.0, dt.get("a", "x"), TOL);
        assertEquals(1.0, dt.get("other", "?"), TOL);
    }

    @Test
    void normalizedDensityVectorSumsToOne() {
        var dv = DensityVector.emptyByLabels(false, "a", "b");
        dv.increment(0, 3.0);
        dv.increment(1, 1.0);
        dv.normalize();

        assertEquals(0.75, dv.get(0), TOL);
        assertEquals(0.25, dv.get(1), TOL);
        // the cells were rescaled in place and the cached total stayed 4
        assertEquals(1.0, dv.sum(), TOL);
        assertEquals(0.25, dv.sumExcept(0), TOL);
    }

    @Test
    void normalizedDensityVectorKeepsTheTotalInSyncForOtherNorms() {
        var dv = DensityVector.emptyByLabels(false, "a", "b");
        dv.increment(0, 3.0);
        dv.increment(1, 4.0);
        dv.normalize(2);

        // L2 norm is 5
        assertEquals(0.6, dv.get(0), TOL);
        assertEquals(0.8, dv.get(1), TOL);
        assertEquals(dv.get(0) + dv.get(1), dv.sum(), TOL);
    }

    @Test
    void totalRemainsConsistentWhenUsedAfterNormalization() {
        var dv = DensityVector.emptyByLabels(false, "a", "b");
        dv.increment(0, 2.0);
        dv.increment(1, 2.0);
        dv.normalize();
        dv.increment(0, 1.0);

        assertEquals(2.0, dv.sum(), TOL);
        assertEquals(dv.get(0) + dv.get(1), dv.sum(), TOL);
    }

    @Test
    void normalizingAnAllZeroVectorChangesNothing() {
        var dv = DensityVector.emptyByLabels(false, "a", "b");
        dv.normalize();
        assertEquals(0.0, dv.sum(), 0.0);
        assertEquals(0.0, dv.get(0), 0.0);
    }
}
