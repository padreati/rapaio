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
import rapaio.data.filters.BaseFilters;
import rapaio.io.Csv;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CoreStatTest {

    private final Frame df;

    public CoreStatTest() throws IOException {
        this.df = BaseFilters.toNumeric(new Csv().withHeader(false).read(getClass(), "core_stat.csv"));
    }

    @Test
    public void testRReference() throws IOException {
        assertEquals(Double.valueOf("999.98132402093892779"), new Mean(df.var(0)).value(), 1e-12);
        assertEquals(Double.valueOf("1.0012615815492349469"), Math.sqrt(new Variance(df.var(0)).getValue()), 1e-12);
        assertEquals(996.343866540788, new Minimum(df.var(0)).value(), 1e-12);
        assertEquals(1004.24956126934, new Maximum(df.var(0)).value(), 1e-12);
    }
}
