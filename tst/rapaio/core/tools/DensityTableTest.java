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

package rapaio.core.tools;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.datasets.Datasets;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DensityTableTest {

    @Test
    void testBuilders() {
        var dt = DensityTable.emptyByLabel(true, Arrays.asList("a", "b"), Arrays.asList("c", "d", "e"));
        assertEquals(2, dt.rowCount());
        assertEquals(Arrays.asList("a", "b"), dt.rowIndex().getValues());
        assertEquals(3, dt.colCount());
        assertEquals(Arrays.asList("c", "d", "e"), dt.colIndex().getValues());

        dt = DensityTable.emptyByLabel(false, Arrays.asList("a", "b"), Arrays.asList("c", "d", "e"));
        assertEquals(Collections.singletonList("b"), dt.rowIndex().getValues());
        assertEquals(Arrays.asList("d", "e"), dt.colIndex().getValues());

        Var x = VarNominal.copy("a", "b", "a", "c").name("x");
        Var y = VarNominal.copy("d", "e", "d", "e").name("y");

        Var w = VarDouble.copy(1.0, 2.0, 3.0, 4.0).name("w");

        Frame df = SolidFrame.byVars(x, y);

        dt = DensityTable.fromLevelCounts(false, x, y);
        assertEquals(Arrays.asList("a", "b", "c"), dt.rowIndex().getValues());
        assertEquals(Arrays.asList("d", "e"), dt.colIndex().getValues());
        assertEquals(0, dt.get("a", "e"));
        assertEquals(2, dt.get("a", "d"));
        assertEquals(1, dt.get("b", "e"));
        assertEquals(1, dt.get("c", "e"));

        dt = DensityTable.fromLevelCounts(false, df, "x", "y");
        assertEquals(Arrays.asList("a", "b", "c"), dt.rowIndex().getValues());
        assertEquals(Arrays.asList("d", "e"), dt.colIndex().getValues());
        assertEquals(0, dt.get("a", "e"));
        assertEquals(2, dt.get("a", "d"));
        assertEquals(1, dt.get("b", "e"));
        assertEquals(1, dt.get("c", "e"));

        dt = DensityTable.fromLevelWeights(false, x, y, w);
        assertEquals(Arrays.asList("a", "b", "c"), dt.rowIndex().getValues());
        assertEquals(Arrays.asList("d", "e"), dt.colIndex().getValues());
        assertEquals(0, dt.get("a", "e"));
        assertEquals(4, dt.get("a", "d"));
        assertEquals(2, dt.get("b", "e"));
        assertEquals(4, dt.get("c", "e"));

        dt = DensityTable.fromLevelWeights(false, df, "x", "y", w);
        assertEquals(Arrays.asList("a", "b", "c"), dt.rowIndex().getValues());
        assertEquals(Arrays.asList("d", "e"), dt.colIndex().getValues());
        assertEquals(0, dt.get("a", "e"));
        assertEquals(4, dt.get("a", "d"));
        assertEquals(2, dt.get("b", "e"));
        assertEquals(4, dt.get("c", "e"));

        dt = DensityTable.fromBinaryLevelWeights(true, df, "x", "y", w, "a");
        assertEquals(Arrays.asList("?", "a", "other"), dt.rowIndex().getValues());
        assertEquals(Arrays.asList("?", "d", "e"), dt.colIndex().getValues());
        assertEquals(0, dt.get(0, 0));
        assertEquals(4, dt.get("a", "d"));
        assertEquals(6, dt.get("other", "e"));

        assertEquals(dt.toSummary(), DensityTable.fromBinaryLevelWeights(true, x, y, w, "a").toSummary());

        dt = DensityTable.fromBinaryLevelWeights(false, df, "x", "y", w, "a");
        assertEquals(Arrays.asList("a", "other"), dt.rowIndex().getValues());
        assertEquals(Arrays.asList("d", "e"), dt.colIndex().getValues());
        assertEquals(4, dt.get("a", "d"));
        assertEquals(6, dt.get("other", "e"));

        assertEquals(dt.toSummary(), DensityTable.fromBinaryLevelWeights(false, x, y, w, "a").toSummary());
    }


    @Test
    void testPlayNoMissing() {

        Frame df = Datasets.loadPlay();

        var id = DensityTable.fromLevelCounts(false, df.rvar("outlook"), df.rvar("class"));
        assertEquals(0.694, id.splitByRowAverageEntropy(), 1e-3);
        assertEquals(0.246, id.splitByRowInfoGain(), 1e-3);
        assertEquals(0.156, id.splitByRowGainRatio(), 1e-3);

        id = DensityTable.fromLevelCounts(false, df.rvar("windy"), df.rvar("class"));
        assertEquals(0.892, id.splitByRowAverageEntropy(), 1e-3);
        assertEquals(0.048, id.splitByRowInfoGain(), 1e-3);
        assertEquals(0.048, id.splitByRowGainRatio(), 1e-3);
    }

    @Test
    void testPlayWithMissing() {

        Frame df = Datasets.loadPlay();
        df.rvar("outlook").setMissing(5);

        var id = DensityTable.fromLevelCounts(false, df.rvar("outlook"), df.rvar("class"));

        assertEquals(0.747, id.splitByRowAverageEntropy(), 1e-3);
        assertEquals(0.214, id.splitByRowInfoGain(), 1e-3);

        assertEquals(0.138, id.splitByRowGainRatio(), 1e-3);
    }

    @Test
    void testEntropy() {
        var dt1 = DensityTable.emptyByLabel(false, Arrays.asList("?", "a", "b"), Arrays.asList("?", "x", "y", "z"));

        dt1.increment(0, 0, 10);
        dt1.increment(0, 1, 7);
        dt1.increment(0, 2, 6);

        dt1.increment(1, 0, 8);
        dt1.increment(1, 1, 19);
        dt1.increment(1, 2, 12);

        var dt2 = DensityTable.emptyByLabel(true, Arrays.asList("a", "b"), Arrays.asList("x", "y", "z"));

        dt2.increment(0, 0, 10);
        dt2.increment(0, 1, 7);
        dt2.increment(0, 2, 6);

        dt2.increment(1, 0, 8);
        dt2.increment(1, 1, 19);
        dt2.increment(1, 2, 12);

        assertEquals(dt1.splitByRowAverageEntropy(), dt2.splitByRowAverageEntropy(), 1e-30);
        assertEquals(dt1.splitByRowInfoGain(), dt2.splitByRowInfoGain(), 1e-30);
        assertEquals(dt1.splitByRowGainRatio(), dt2.splitByRowGainRatio(), 1e-30);
        assertEquals(dt1.splitByRowGiniGain(), dt2.splitByRowGiniGain(), 1e-30);
        assertEquals(dt1.splitByColGiniGain(), dt2.splitByColGiniGain(), 1e-30);
    }

    @Test
    void testNormalization() {
        var dt2 = DensityTable.emptyByLabel(true, Arrays.asList("a", "b"), Arrays.asList("x", "y", "z"));

        dt2.increment(0, 0, 10);
        dt2.increment(0, 1, 7);
        dt2.increment(0, 2, 6);

        dt2.increment("b", "x", 8);
        dt2.increment(1, 1, 19);
        dt2.increment(1, 2, 12);

        assertEquals("              x         y         z     total \n" +
                "    a 0.5555556 0.2692308 0.3333333 1.1581197 \n" +
                "    b 0.4444444 0.7307692 0.6666667 1.8418803 \n" +
                "total 1         1         1         3         \n", dt2.normalizeOnCols().toSummary());

        assertEquals("              x         y         z     total \n" +
                "    a 0.1612903 0.1129032 0.0967742 0.3709677 \n" +
                "    b 0.1290323 0.3064516 0.1935484 0.6290323 \n" +
                "total 0.2903226 0.4193548 0.2903226 1         \n", dt2.normalizeOverall().toSummary());
        assertEquals("              x         y         z total \n" +
                "    a 0.4347826 0.3043478 0.2608696   1   \n" +
                "    b 0.2051282 0.4871795 0.3076923   1   \n" +
                "total 0.6399108 0.7915273 0.5685619   2   \n", dt2.normalizeOnRows().toSummary());
        assertEquals("              x         y         z     total \n" +
                "    a 0.5555556 0.2692308 0.3333333 1.1581197 \n" +
                "    b 0.4444444 0.7307692 0.6666667 1.8418803 \n" +
                "total 1         1         1         3         \n", dt2.normalizeOnCols().toSummary());
    }
}
