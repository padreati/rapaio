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
import rapaio.data.VType;
import rapaio.io.Csv;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class OtherNormalTest {

    private static final double ERROR = 1e-12;
    private Frame df;

    public OtherNormalTest() throws IOException, URISyntaxException {
        df = new Csv()
                .withHeader(true)
                .withSeparatorChar(',')
                .withDefaultTypes(VType.DOUBLE)
                .withNAValues("?", "-Inf", "Inf", "NA")
                .read(this.getClass(), "other_normal.csv");
    }

    @Test
    public void testStandardQuantile() {
        Normal d = Normal.from(10, 2);
        for (int i = 0; i < df.rowCount(); i++) {
            if (df.getDouble(i, "x") > 0 && df.getDouble(i, "x") < 1) {
                Assert.assertEquals(df.getDouble(i, "quantile"), d.quantile(df.getDouble(i, "x")), ERROR);
            }
        }
    }

    @Test
    public void testStandardPdf() {
        Normal d = Normal.from(10, 2);
        for (int i = 0; i < df.rowCount(); i++) {
            Assert.assertEquals(df.getDouble(i, "pdf"), d.pdf(df.getDouble(i, "x")), ERROR);
        }
    }

    @Test
    public void testStandardCdf() {
        Normal d = Normal.from(10, 2);
        for (int i = 0; i < df.rowCount(); i++) {
            Assert.assertEquals(df.getDouble(i, "cdf"), d.cdf(df.getDouble(i, "x")), ERROR);
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