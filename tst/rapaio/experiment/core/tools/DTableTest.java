/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.experiment.core.tools;


import org.junit.jupiter.api.Test;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DTableTest {

    @Test
    void testPlayNoMissing() {

        Frame df = Datasets.loadPlay();

        DTable id = DTable.fromCounts(df.rvar("outlook"), df.rvar("class"), false);
        assertEquals(0.694, id.splitByRowAverageEntropy(), 1e-3);
        assertEquals(0.246, id.splitByRowInfoGain(), 1e-3);

        assertEquals(0.156, id.splitByRowGainRatio(), 1e-3);

        id = DTable.fromCounts(df.rvar("windy"), df.rvar("class"), false);
        assertEquals(0.892, id.splitByRowAverageEntropy(), 1e-3);
        assertEquals(0.048, id.splitByRowInfoGain(), 1e-3);

        assertEquals(0.048, id.splitByRowGainRatio(), 1e-3);
    }

    @Test
    void testPlayWithMissing() {

        Frame df = Datasets.loadPlay();
        df.rvar("outlook").setMissing(5);

        DTable id = DTable.fromCounts(df.rvar("outlook"), df.rvar("class"), false);
        id.printSummary();

        assertEquals(0.747, id.splitByRowAverageEntropy(), 1e-3);
        assertEquals(0.214, id.splitByRowInfoGain(), 1e-3);

        assertEquals(0.138, id.splitByRowGainRatio(), 1e-3);
    }

    @Test
    void testEntropy() {
        DTable dt1 = DTable.empty(Arrays.asList("?", "a", "b"), Arrays.asList("?", "x", "y", "z"), false);

        dt1.update(0, 0, 1);
        dt1.update(1, 0, 1);
        dt1.update(2, 0, 1);

        dt1.update(0, 1, 1);
        dt1.update(0, 2, 1);
        dt1.update(0, 3, 1);

        dt1.update(1, 1, 10);
        dt1.update(1, 2, 7);
        dt1.update(1, 3, 6);

        dt1.update(2, 1, 8);
        dt1.update(2, 2, 19);
        dt1.update(2, 3, 12);

        dt1.printSummary();

        DTable dt2 = DTable.empty(Arrays.asList("a", "b"), Arrays.asList("x", "y", "z"), true);

        dt2.update(0, 0, 10);
        dt2.update(0, 1, 7);
        dt2.update(0, 2, 6);

        dt2.update(1, 0, 8);
        dt2.update(1, 1, 19);
        dt2.update(1, 2, 12);

        dt2.printSummary();

        assertEquals(dt1.splitByRowAverageEntropy(), dt2.splitByRowAverageEntropy(), 1e-30);
        assertEquals(dt1.splitByRowInfoGain(), dt2.splitByRowInfoGain(), 1e-30);
        assertEquals(dt1.splitByRowGainRatio(), dt2.splitByRowGainRatio(), 1e-30);
        assertEquals(dt1.splitByRowGiniGain(), dt2.splitByRowGiniGain(), 1e-30);
        assertEquals(dt1.splitByColGiniGain(), dt2.splitByColGiniGain(), 1e-30);

        dt1.withTotalSummary(true).printSummary();
        dt1.withTotalSummary(false).printSummary();
    }

    @Test
    void testNormalization() {
        DTable dt2 = DTable.empty(Arrays.asList("a", "b"), Arrays.asList("x", "y", "z"), true);

        dt2.update(0, 0, 10);
        dt2.update(0, 1, 7);
        dt2.update(0, 2, 6);

        dt2.update(1, 0, 8);
        dt2.update(1, 1, 19);
        dt2.update(1, 2, 12);

        assertEquals("              x         y         z     total \n" +
                "    a 0.5555556 0.2692308 0.3333333 1.1581197 \n" +
                "    b 0.4444444 0.7307692 0.6666667 1.8418803 \n" +
                "total 1         1         1         3         \n", dt2.normalizeOnCols().toSummary());

        dt2.withTotalSummary(false);

        assertEquals("          x         y         z \n" +
                "a 0.1612903 0.1129032 0.0967742 \n" +
                "b 0.1290323 0.3064516 0.1935484 \n", dt2.normalizeOverall().toSummary());
        assertEquals("          x         y         z \n" +
                "a 0.4347826 0.3043478 0.2608696 \n" +
                "b 0.2051282 0.4871795 0.3076923 \n", dt2.normalizeOnRows().toSummary());
        assertEquals("          x         y         z \n" +
                "a 0.5555556 0.2692308 0.3333333 \n" +
                "b 0.4444444 0.7307692 0.6666667 \n", dt2.normalizeOnCols().toSummary());
    }
}
