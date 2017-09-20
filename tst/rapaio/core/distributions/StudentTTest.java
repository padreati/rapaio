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

import static org.junit.Assert.assertEquals;

/**
 * Test for
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/28/15.
 */
public class StudentTTest {

    private static final double ERROR = 1e-12;

    @Test
    public void densityTest() throws IOException {
        Frame df = new Csv()
                .withDefaultTypes(VarType.NUMERIC)
                .withQuotes(false)
                .read(StudentTTest.class, "student-density.csv");
        df.printSummary();

        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(df.value(i, "y1"), new StudentT(1).pdf(df.value(i, "x")), 1e-15);
            assertEquals(df.value(i, "y2"), new StudentT(2).pdf(df.value(i, "x")), 1e-15);
            assertEquals(df.value(i, "y3"), new StudentT(3).pdf(df.value(i, "x")), 1e-15);
            assertEquals(df.value(i, "y5"), new StudentT(5).pdf(df.value(i, "x")), 1e-15);
            assertEquals(df.value(i, "y10"), new StudentT(10).pdf(df.value(i, "x")), 1e-15);
            assertEquals(df.value(i, "y15"), new StudentT(15).pdf(df.value(i, "x")), 1e-14);
            assertEquals(df.value(i, "y20"), new StudentT(20).pdf(df.value(i, "x")), 1e-14);
            assertEquals(df.value(i, "y50"), new StudentT(50).pdf(df.value(i, "x")), 1e-14);
            assertEquals(df.value(i, "y100"), new StudentT(100).pdf(df.value(i, "x")), 1e-13);
            assertEquals(df.value(i, "y1000"), new StudentT(1_000).pdf(df.value(i, "x")), 1e-12);
            assertEquals(df.value(i, "y10000"), new StudentT(10_000).pdf(df.value(i, "x")), 1e-11);
        }
    }

    @Test
    public void distributionTest() throws IOException {
        Frame df = new Csv()
                .withDefaultTypes(VarType.NUMERIC)
                .withQuotes(false)
                .read(StudentTTest.class, "student-distribution.csv");
        df.printSummary();

        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(df.value(i, "y1"), new StudentT(1).cdf(df.value(i, "x")), 1e-15);
            assertEquals(df.value(i, "y2"), new StudentT(2).cdf(df.value(i, "x")), 1e-15);
            assertEquals(df.value(i, "y3"), new StudentT(3).cdf(df.value(i, "x")), 1e-15);
            assertEquals(df.value(i, "y5"), new StudentT(5).cdf(df.value(i, "x")), 1e-15);
            assertEquals(df.value(i, "y10"), new StudentT(10).cdf(df.value(i, "x")), 1e-15);
            assertEquals(df.value(i, "y15"), new StudentT(15).cdf(df.value(i, "x")), 1e-14);
            assertEquals(df.value(i, "y20"), new StudentT(20).cdf(df.value(i, "x")), 1e-14);
            assertEquals(df.value(i, "y50"), new StudentT(50).cdf(df.value(i, "x")), 1e-14);
            assertEquals(df.value(i, "y100"), new StudentT(100).cdf(df.value(i, "x")), 1e-13);
            assertEquals(df.value(i, "y1000"), new StudentT(1_000).cdf(df.value(i, "x")), 1e-12);
            assertEquals(df.value(i, "y10000"), new StudentT(10_000).cdf(df.value(i, "x")), 1e-12);
        }
    }

    @Test
    public void quantileTest() throws IOException {
        Frame df = new Csv()
                .withDefaultTypes(VarType.NUMERIC)
                .withQuotes(false)
                .read(StudentTTest.class, "student-quantile.csv");
        df.printSummary();

        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(df.value(i, "y1"), new StudentT(1).quantile(df.value(i, "x")), 1e-12);
            assertEquals(df.value(i, "y2"), new StudentT(2).quantile(df.value(i, "x")), 1e-12);
            assertEquals(df.value(i, "y3"), new StudentT(3).quantile(df.value(i, "x")), 1e-12);
            assertEquals(df.value(i, "y5"), new StudentT(5).quantile(df.value(i, "x")), 1e-12);
            assertEquals(df.value(i, "y10"), new StudentT(10).quantile(df.value(i, "x")), 1e-12);
            assertEquals(df.value(i, "y15"), new StudentT(15).quantile(df.value(i, "x")), 1e-12);
            assertEquals(df.value(i, "y20"), new StudentT(20).quantile(df.value(i, "x")), 1e-12);
            assertEquals(df.value(i, "y50"), new StudentT(50).quantile(df.value(i, "x")), 1e-12);
            assertEquals(df.value(i, "y100"), new StudentT(100).quantile(df.value(i, "x")), 1e-11);
            assertEquals(df.value(i, "y1000"), new StudentT(1_000).quantile(df.value(i, "x")), 1e-10);
            assertEquals(df.value(i, "y10000"), new StudentT(10_000).quantile(df.value(i, "x")), 1e-9);
        }
    }

    @Test
    public void testWithR() throws IOException {
        Frame df = new Csv()
                .withHeader(true)
                .withSeparatorChar(',')
                .withDefaultTypes(VarType.NUMERIC)
                .withNAValues("?", "-Inf", "Inf", "NA")
                .read(this.getClass(), "student.csv");
        StudentT t1 = new StudentT(1);
        StudentT t2 = new StudentT(2);
        StudentT t5 = new StudentT(5);
        StudentT t10 = new StudentT(10);
        StudentT t100 = new StudentT(100);

        Assert.assertEquals("StudentT(df=1, mu=0, sigma=1)", t1.name());
        Assert.assertEquals(false, t1.discrete());

        for (int i = 0; i < df.rowCount(); i++) {

            double x = df.value(i, "x");

            Assert.assertEquals(df.value(i, "pdf_1"), t1.pdf(x), ERROR);
            Assert.assertEquals(df.value(i, "cdf_1"), t1.cdf(x), ERROR);
            if (x > 0 && x < 1) {
                if (!Double.isNaN(df.value(i, "quantile_1")))
                    Assert.assertEquals(df.value(i, "quantile_1"), t1.quantile(df.value(i, "x")), ERROR);
            }
            Assert.assertEquals(df.value(i, "pdf_2"), t2.pdf(x), ERROR);
            Assert.assertEquals(df.value(i, "cdf_2"), t2.cdf(x), ERROR);
            if (x > 0 && x < 1 && !Double.isNaN(x)) {
                if (!Double.isNaN(df.value(i, "quantile_2")))
                    Assert.assertEquals(df.value(i, "quantile_2"), t2.quantile(df.value(i, "x")), ERROR);
            }
            Assert.assertEquals(df.value(i, "pdf_5"), t5.pdf(x), ERROR);
            Assert.assertEquals(df.value(i, "cdf_5"), t5.cdf(x), ERROR);
            if (x > 0 && x < 1 && !Double.isNaN(x)) {
                if (!Double.isNaN(df.value(i, "quantile_5")))
                    Assert.assertEquals(df.value(i, "quantile_5"), t5.quantile(df.value(i, "x")), ERROR);
            }
            Assert.assertEquals(df.value(i, "pdf_10"), t10.pdf(x), ERROR);
            Assert.assertEquals(df.value(i, "cdf_10"), t10.cdf(x), ERROR);
            if (x > 0 && x < 1 && !Double.isNaN(x)) {
                if (!Double.isNaN(df.value(i, "quantile_10")))
                    Assert.assertEquals(df.value(i, "quantile_10"), t10.quantile(df.value(i, "x")), ERROR);
            }
            Assert.assertEquals(df.value(i, "pdf_100"), t100.pdf(x), ERROR);
            Assert.assertEquals(df.value(i, "cdf_100"), t100.cdf(x), ERROR);
            if (x > 0 && x < 1 && !Double.isNaN(x)) {
                if (!Double.isNaN(df.value(i, "quantile_100")))
                    Assert.assertEquals(df.value(i, "quantile_100"), t100.quantile(df.value(i, "x")), ERROR);
            }
        }
    }

    @Test
    public void testOtherT() {

        StudentT t = new StudentT(10, 2, 3);
        Assert.assertEquals(2, t.mean(), ERROR);
        Assert.assertEquals(2, t.mode(), ERROR);
        Assert.assertEquals(11.25, t.var(), ERROR);
        Assert.assertEquals(Double.NEGATIVE_INFINITY, t.min(), ERROR);
        Assert.assertEquals(Double.POSITIVE_INFINITY, t.max(), ERROR);
        Assert.assertEquals(0, t.skewness(), ERROR);
        Assert.assertEquals(1, t.kurtosis(), ERROR);
    }
}
