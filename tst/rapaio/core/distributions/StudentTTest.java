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

package rapaio.core.distributions;

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
@Deprecated
public class StudentTTest {

    @Test
    public void densityTest() throws IOException {
        Frame df = new Csv()
                .withDefaultTypes(VarType.NUMERIC)
                .withQuotes(false)
                .read(StudentTTest.class, "student-density.csv");
        df.summary();

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
        df.summary();

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
        df.summary();

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
}
