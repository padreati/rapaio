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

package rapaio.ml.model.tree.ctree;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.core.tests.ChiSqIndependence;
import rapaio.core.tools.DensityTable;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/13/20.
 */
public class PurityTest {

    private DensityTable<String, String> dt;

    @BeforeEach
    void beforeEach() {
        dt = DensityTable.emptyByLabel(true, List.of("a", "b"), List.of("x", "y"));

        // 3,6
        // 3,2
        dt.increment(0, 0, 3);
        dt.increment(0, 1, 6);
        dt.increment(1, 0, 3);
        dt.increment(1, 1, 2);
    }

    @Test
    void purityTest() {

        assertEquals(dt.splitByRowInfoGain(), Purity.InfoGain.compute(dt));
        assertEquals(dt.splitByRowGainRatio(), Purity.GainRatio.compute(dt), 1e-10);
        assertEquals(dt.splitByRowGiniGain(), Purity.GiniGain.compute(dt));
        assertEquals(1 - ChiSqIndependence.from(dt, false).pValue(), Purity.ChiSquare.compute(dt));
    }
}
