/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.core.stat;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.VarType;
import rapaio.io.Csv;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static rapaio.core.stat.BaseStat.mean;
import static rapaio.core.stat.BaseStat.var;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CoreStatTest {

    private final Frame df;

    public CoreStatTest() throws IOException {
        this.df = new Csv().withHeader(true).withDefaultTypes(VarType.NUMERIC).read(getClass(), "core_stat.csv");
    }

    @Test
    public void testRReference() throws IOException {
        mean(df.var(0)).summary();
        var(df.var(0)).summary();
        assertEquals(Double.valueOf("999.98132402093892779"), mean(df.var(0)).value(), 1e-12);
        assertEquals(Double.valueOf("1.0012615815492349469"), Math.sqrt(new Variance(df.var(0)).value()), 1e-12);
        assertEquals(996.343866540788, new Minimum(df.var(0)).value(), 1e-12);
        assertEquals(1004.24956126934, new Maximum(df.var(0)).value(), 1e-12);
    }

    @Test
    public void testEmptyMean() {
        Numeric num1 = Numeric.newCopyOf(Double.NaN, Double.NaN, Double.NaN);
        double mean = mean(num1).value();
        assertTrue(Double.isNaN(mean));

        Numeric num2 = Numeric.newWrapOf(1, 2, 3, 4);
        StringBuilder sb = new StringBuilder();
        mean(num2).buildSummary(sb);

        assertEquals("> mean['null']\n" +
                "total rows: 4\n" +
                "complete: 4, missing: 0\n" +
                "mean: 2.5\n", sb.toString());
        sb = new StringBuilder();
        var(num2).buildSummary(sb);
        assertEquals("> variance['null']\n" +
                        "total rows: 4\n" +
                        "complete: 4, missing: 0\n" +
                        "variance: 1.6666666666666667\n" +
                        "sd: 1.2909944487358056\n",
                sb.toString());

        mean(num2).summary();
        var(num2).summary();
    }
}
