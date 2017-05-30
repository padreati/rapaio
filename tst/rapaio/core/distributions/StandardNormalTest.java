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
import org.junit.Test;
import rapaio.data.Frame;
import rapaio.data.VarType;
import rapaio.io.Csv;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class StandardNormalTest {

    private static final double ERROR = 1e-12;
    private Frame df;

    public StandardNormalTest() throws IOException, URISyntaxException {
        df = new Csv()
                .withHeader(true)
                .withSeparatorChar(',')
                .withDefaultTypes(VarType.NUMERIC)
                .withNAValues("?", "-Inf", "Inf", "NA")
                .read(this.getClass(), "standard_normal.csv");
    }

    @Test
    public void testStandardQuantile() {
        Normal d = new Normal(0, 1);
        for (int i = 0; i < df.getRowCount(); i++) {
            if (df.getValue(i, "x") > 0 && df.getValue(i, "x") < 1) {
                Assert.assertEquals(df.getValue(i, "quantile"), d.quantile(df.getValue(i, "x")), ERROR);
            }
        }
    }

    @Test
    public void testStandardPdf() {
        Normal d = new Normal(0, 1);
        for (int i = 0; i < df.getRowCount(); i++) {
            Assert.assertEquals(df.getValue(i, "pdf"), d.pdf(df.getValue(i, "x")), ERROR);
        }
    }

    @Test
    public void testStandardCdf() {
        Normal d = new Normal(0, 1);
        for (int i = 0; i < df.getRowCount(); i++) {
            Assert.assertEquals(df.getValue(i, "cdf"), d.cdf(df.getValue(i, "x")), ERROR);
        }
    }

    @Test
    public void testOtherAspects() {
        Normal normal = new Normal();
        Assert.assertEquals(Double.NEGATIVE_INFINITY, normal.min(), ERROR);
        Assert.assertEquals(Double.POSITIVE_INFINITY, normal.max(), ERROR);
        Assert.assertEquals(0, normal.mean(), ERROR);
        Assert.assertEquals(0, normal.mode(), ERROR);
        Assert.assertEquals(1, normal.var(), ERROR);
        Assert.assertEquals(0, normal.skewness(), ERROR);
        Assert.assertEquals(0, normal.kurtosis(), ERROR);
        Assert.assertEquals(2.8378770664093453, normal.entropy(), ERROR);
    }
}