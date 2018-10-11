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

package rapaio.core.distributions;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.io.Csv;

import java.io.IOException;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class StandardNormalTest {

    private static final double ERROR = 1e-12;
    private Frame df;

    @Before
    public void setUp() throws IOException {
        df = new Csv()
                .withHeader(true)
                .withSeparatorChar(',')
                .withDefaultTypes(VType.DOUBLE)
                .withNAValues("?", "-Inf", "Inf", "NA")
                .read(this.getClass(), "standard_normal.csv");
    }

    @Test
    public void testStandardQuantile() {
        Normal d = Normal.std();
        for (int i = 0; i < df.rowCount(); i++) {
            if (df.getDouble(i, "x") > 0 && df.getDouble(i, "x") < 1) {
                Assert.assertEquals(df.getDouble(i, "quantile"), d.quantile(df.getDouble(i, "x")), ERROR);
            }
        }
    }

    @Test
    public void testStandardPdf() {
        Normal d = Normal.std();
        for (int i = 0; i < df.rowCount(); i++) {
            Assert.assertEquals(df.getDouble(i, "pdf"), d.pdf(df.getDouble(i, "x")), ERROR);
        }
    }

    @Test
    public void testStandardCdf() {
        Normal d = Normal.std();
        for (int i = 0; i < df.rowCount(); i++) {
            Assert.assertEquals(df.getDouble(i, "cdf"), d.cdf(df.getDouble(i, "x")), ERROR);
        }
    }
}