/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio.core.stat;

import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.data.filters.BaseFilters;
import rapaio.datasets.Datasets;
import rapaio.io.Csv;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CoreStatTest {

    @Test
    public void testRReferenceMean() throws IOException {
        Frame df = BaseFilters.toNumeric(new Csv().withHeader(false).read(getClass(), "core_stat.csv"));
        assertEquals(Double.valueOf("999.98132402093892779"), new Mean(df.var(0)).value(), 1e-12);
    }

    @Test
    public void tesMeantWithEmptyValues() {
        Var num = Numeric.newEmpty(10);
        num.setValue(0, 1);
        assertEquals(1.0, new Mean(num).value(), 10e-10);

        num.setValue(7, 5);
        assertEquals(3.0, new Mean(num).value(), 10e-10);
    }

    @Test
    public void testRReferenceVariance() throws IOException {
        Frame df = BaseFilters.toNumeric(new Csv().withHeader(false).read(getClass(), "core_stat.csv"));
        assertEquals(Double.valueOf("1.0012615815492349469"), Math.sqrt(new Variance(df.var(0)).getValue()), 1e-12);
    }

    @Test
    public void testPearsonDSVariance() throws IOException, URISyntaxException {
        Frame df = Datasets.loadPearsonHeightDataset();
        assertEquals(Double.valueOf("7.93094884953222"), new Variance(df.var("Son")).getValue(), 1e-12);
    }

    @Test
    public void testMinMax() {
        Var v = Numeric.newWrapOf(1, 2, 3, 4, 5, 6);

        assertEquals(6.0, new Maximum(v).value(), 1e-12);
        assertEquals(1.0, new Minimum(v).value(), 1e-12);

        v.addMissing();
        v.addValue(12);
        v.addValue(-1);

        assertEquals(12, new Maximum(v).value(), 1e-12);
        assertEquals(-1, new Minimum(v).value(), 1e-12);
    }
}
