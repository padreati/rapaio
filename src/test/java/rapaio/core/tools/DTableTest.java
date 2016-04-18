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

package rapaio.core.tools;

import junit.framework.Assert;
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.sys.WS;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class DTableTest {

    @Test
    public void testPlayNoMissing() throws IOException {

        Frame df = Datasets.loadPlay();

        DTable id = DTable.fromCounts(df.var("outlook"), df.var("class"), false);
        assertEquals(0.940, id.totalColEntropy(), 1e-3);
        assertEquals(0.694, id.splitByRowAverageEntropy(), 1e-3);
        assertEquals(0.246, id.splitByRowInfoGain(), 1e-3);

        assertEquals(1.577, id.splitByRowIntrinsicInfo(), 1e-3);
        assertEquals(0.156, id.splitByRowGainRatio(), 1e-3);

        id = DTable.fromCounts(df.var("windy"), df.var("class"), false);
        assertEquals(0.940, id.totalColEntropy(), 1e-3);
        assertEquals(0.892, id.splitByRowAverageEntropy(), 1e-3);
        assertEquals(0.048, id.splitByRowInfoGain(), 1e-3);

        assertEquals(0.985, id.splitByRowIntrinsicInfo(), 1e-3);
        assertEquals(0.048, id.splitByRowGainRatio(), 1e-3);
    }

    @Test
    public void testPlayWithMissing() throws IOException {

        Frame df = Datasets.loadPlay();
        df.var("outlook").setMissing(5);

        DTable id = DTable.fromCounts(df.var("outlook"), df.var("class"), false);
        id.printSummary();

        assertEquals(0.961, id.totalColEntropy(), 1e-3);
        assertEquals(0.747, id.splitByRowAverageEntropy(), 1e-3);
        assertEquals(0.214, id.splitByRowInfoGain(), 1e-3);

        assertEquals(1.549, id.splitByRowIntrinsicInfo(), 1e-3);
        assertEquals(0.138, id.splitByRowGainRatio(), 1e-3);
    }

    @Test
    public void testEntropy() {
        DTable dt1 = DTable.newEmpty(new String[]{"?", "a", "b"}, new String[]{"?", "x", "y", "z"}, false);

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

        DTable dt2 = DTable.newEmpty(new String[]{"a", "b"}, new String[]{"x", "y", "z"}, true);

        dt2.update(0, 0, 10);
        dt2.update(0, 1, 7);
        dt2.update(0, 2, 6);

        dt2.update(1, 0, 8);
        dt2.update(1, 1, 19);
        dt2.update(1, 2, 12);

        dt2.printSummary();

        WS.println("totalColEntropy: " + WS.formatFlex(dt1.totalColEntropy()));
        Assert.assertEquals(dt1.totalColEntropy(), dt2.totalColEntropy(), 1e-30);

        WS.println("totalRowEntropy: " + WS.formatFlex(dt1.totalRowEntropy()));
        Assert.assertEquals(dt1.totalRowEntropy(), dt2.totalRowEntropy(), 1e-30);

        WS.println("splitByRowAverageEntropy: " + WS.formatFlex(dt1.splitByRowAverageEntropy()));
        Assert.assertEquals(dt1.splitByRowAverageEntropy(), dt2.splitByRowAverageEntropy(), 1e-30);

        WS.println("splitByColAverageEntropy: " + WS.formatFlex(dt1.splitByColAverageEntropy()));
        Assert.assertEquals(dt1.splitByColAverageEntropy(), dt2.splitByColAverageEntropy(), 1e-30);

        WS.println("splitByRowInfoGain: " + WS.formatFlex(dt1.splitByRowInfoGain()));
        Assert.assertEquals(dt1.splitByRowInfoGain(), dt2.splitByRowInfoGain(), 1e-30);

        WS.println("splitByColInfoGain: " + WS.formatFlex(dt1.splitByColInfoGain()));
        Assert.assertEquals(dt1.splitByColInfoGain(), dt2.splitByColInfoGain(), 1e-30);

        WS.println("splitByRowGainRatio: " + WS.formatFlex(dt1.splitByRowGainRatio()));
        Assert.assertEquals(dt1.splitByRowGainRatio(), dt2.splitByRowGainRatio(), 1e-30);

        WS.println("splitByColGainRatio: " + WS.formatFlex(dt1.splitByColGainRatio()));
        Assert.assertEquals(dt1.splitByColGainRatio(), dt2.splitByColGainRatio(), 1e-30);

        WS.println("splitByRowGiniGain: " + WS.formatFlex(dt1.splitByRowGiniGain()));
        Assert.assertEquals(dt1.splitByRowGiniGain(), dt2.splitByRowGiniGain(), 1e-30);

        WS.println("splitByColGiniGain: " + WS.formatFlex(dt1.splitByColGiniGain()));
        Assert.assertEquals(dt1.splitByColGiniGain(), dt2.splitByColGiniGain(), 1e-30);

        dt1.withTotalSummary(true).printSummary();
        dt1.withTotalSummary(false).printSummary();
    }

    @Test
    public void testNormalization() {
        DTable dt2 = DTable.newEmpty(new String[]{"a", "b"}, new String[]{"x", "y", "z"}, true);

        dt2.update(0, 0, 10);
        dt2.update(0, 1, 7);
        dt2.update(0, 2, 6);

        dt2.update(1, 0, 8);
        dt2.update(1, 1, 19);
        dt2.update(1, 2, 12);

        Assert.assertEquals("" +
                "           x     y     z total\n" +
                "     a 0.161 0.113 0.097 0.371\n" +
                "     b 0.129 0.306 0.194 0.629\n" +
                " total 0.290 0.419 0.290 1.000\n" +
                "\n", dt2.normalizeOverall().summary());
        Assert.assertEquals("" +
                "           x     y     z total\n" +
                "     a 0.435 0.304 0.261 1.000\n" +
                "     b 0.205 0.487 0.308 1.000\n" +
                " total 0.640 0.792 0.569 2.000\n" +
                "\n", dt2.normalizeOnRows().summary());
        Assert.assertEquals("" +
                "           x     y     z total\n" +
                "     a 0.556 0.269 0.333 1.158\n" +
                "     b 0.444 0.731 0.667 1.842\n" +
                " total 1.000 1.000 1.000 3.000\n" +
                "\n", dt2.normalizeOnCols().summary());

        dt2.withTotalSummary(false);

        Assert.assertEquals("" +
                "       x     y     z\n" +
                " a 0.161 0.113 0.097\n" +
                " b 0.129 0.306 0.194\n" +
                "\n", dt2.normalizeOverall().summary());
        Assert.assertEquals("" +
                "       x     y     z\n" +
                " a 0.435 0.304 0.261\n" +
                " b 0.205 0.487 0.308\n" +
                "\n", dt2.normalizeOnRows().summary());
        Assert.assertEquals("" +
                "       x     y     z\n" +
                " a 0.556 0.269 0.333\n" +
                " b 0.444 0.731 0.667\n" +
                "\n", dt2.normalizeOnCols().summary());
    }
}
