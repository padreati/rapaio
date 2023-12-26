/*
 *
 *  * Apache License
 *  * Version 2.0, January 2004
 *  * http://www.apache.org/licenses/
 *  *
 *  * Copyright 2013 - 2022 Aurelian Tutuianu
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *  http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package rapaio.core.distributions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.VarType;
import rapaio.io.Csv;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 4/28/15.
 */
public class StudentTTest {

    private static final double TOL = 1e-12;

    @Test
    void testPdf() throws IOException {
        Frame df = Csv.instance()
                .defaultTypes.set(VarType.DOUBLE)
                .quotes.set(false)
                .read(StudentTTest.class, "student-density.csv");

        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(df.getDouble(i, "y1"), StudentT.of(1).pdf(df.getDouble(i, "x")), 1e-15);
            assertEquals(df.getDouble(i, "y2"), StudentT.of(2).pdf(df.getDouble(i, "x")), 1e-15);
            assertEquals(df.getDouble(i, "y3"), StudentT.of(3).pdf(df.getDouble(i, "x")), 1e-15);
            assertEquals(df.getDouble(i, "y5"), StudentT.of(5).pdf(df.getDouble(i, "x")), 1e-15);
            assertEquals(df.getDouble(i, "y10"), StudentT.of(10).pdf(df.getDouble(i, "x")), 1e-15);
            assertEquals(df.getDouble(i, "y15"), StudentT.of(15).pdf(df.getDouble(i, "x")), 1e-14);
            assertEquals(df.getDouble(i, "y20"), StudentT.of(20).pdf(df.getDouble(i, "x")), 1e-14);
            assertEquals(df.getDouble(i, "y50"), StudentT.of(50).pdf(df.getDouble(i, "x")), 1e-14);
            assertEquals(df.getDouble(i, "y100"), StudentT.of(100).pdf(df.getDouble(i, "x")), 1e-13);
            assertEquals(df.getDouble(i, "y1000"), StudentT.of(1_000).pdf(df.getDouble(i, "x")), 1e-12);
            assertEquals(df.getDouble(i, "y10000"), StudentT.of(10_000).pdf(df.getDouble(i, "x")), 1e-11);
        }
    }

    @Test
    void testCdf() throws IOException {
        Frame df = Csv.instance()
                .defaultTypes.set(VarType.DOUBLE)
                .quotes.set(false)
                .read(StudentTTest.class, "student-distribution.csv");

        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(df.getDouble(i, "y1"), StudentT.of(1).cdf(df.getDouble(i, "x")), 1e-15);
            assertEquals(df.getDouble(i, "y2"), StudentT.of(2).cdf(df.getDouble(i, "x")), 1e-15);
            assertEquals(df.getDouble(i, "y3"), StudentT.of(3).cdf(df.getDouble(i, "x")), 1e-15);
            assertEquals(df.getDouble(i, "y5"), StudentT.of(5).cdf(df.getDouble(i, "x")), 1e-15);
            assertEquals(df.getDouble(i, "y10"), StudentT.of(10).cdf(df.getDouble(i, "x")), 1e-15);
            assertEquals(df.getDouble(i, "y15"), StudentT.of(15).cdf(df.getDouble(i, "x")), 1e-14);
            assertEquals(df.getDouble(i, "y20"), StudentT.of(20).cdf(df.getDouble(i, "x")), 1e-14);
            assertEquals(df.getDouble(i, "y50"), StudentT.of(50).cdf(df.getDouble(i, "x")), 1e-14);
            assertEquals(df.getDouble(i, "y100"), StudentT.of(100).cdf(df.getDouble(i, "x")), 1e-13);
            assertEquals(df.getDouble(i, "y1000"), StudentT.of(1_000).cdf(df.getDouble(i, "x")), 1e-12);
            assertEquals(df.getDouble(i, "y10000"), StudentT.of(10_000).cdf(df.getDouble(i, "x")), 1e-12);
        }
    }

    @Test
    void testQuantile() throws IOException {
        Frame df = Csv.instance()
                .defaultTypes.set(VarType.DOUBLE)
                .quotes.set(false)
                .read(StudentTTest.class, "student-quantile.csv");

        for (int i = 0; i < df.rowCount(); i++) {
            assertEquals(df.getDouble(i, "y1"), StudentT.of(1).quantile(df.getDouble(i, "x")), 1e-12);
            assertEquals(df.getDouble(i, "y2"), StudentT.of(2).quantile(df.getDouble(i, "x")), 1e-12);
            assertEquals(df.getDouble(i, "y3"), StudentT.of(3).quantile(df.getDouble(i, "x")), 1e-12);
            assertEquals(df.getDouble(i, "y5"), StudentT.of(5).quantile(df.getDouble(i, "x")), 1e-12);
            assertEquals(df.getDouble(i, "y10"), StudentT.of(10).quantile(df.getDouble(i, "x")), 1e-12);
            assertEquals(df.getDouble(i, "y15"), StudentT.of(15).quantile(df.getDouble(i, "x")), 1e-12);
            assertEquals(df.getDouble(i, "y20"), StudentT.of(20).quantile(df.getDouble(i, "x")), 1e-12);
            assertEquals(df.getDouble(i, "y50"), StudentT.of(50).quantile(df.getDouble(i, "x")), 1e-12);
            assertEquals(df.getDouble(i, "y100"), StudentT.of(100).quantile(df.getDouble(i, "x")), 1e-11);
            assertEquals(df.getDouble(i, "y1000"), StudentT.of(1_000).quantile(df.getDouble(i, "x")), 1e-10);
            assertEquals(df.getDouble(i, "y10000"), StudentT.of(10_000).quantile(df.getDouble(i, "x")), 1e-9);
        }
    }

    @Test
    void testWithR() throws IOException {
        Frame df = Csv.instance()
                .header.set(true)
                .separatorChar.set(',')
                .defaultTypes.set(VarType.DOUBLE)
                .naValues.set("?", "-Inf", "Inf", "NA")
                .read(this.getClass(), "student.csv");
        StudentT t1 = StudentT.of(1);
        StudentT t2 = StudentT.of(2);
        StudentT t5 = StudentT.of(5);
        StudentT t10 = StudentT.of(10);
        StudentT t100 = StudentT.of(100);

        assertEquals("StudentT(df=1, mu=0, sigma=1)", t1.name());
        assertFalse(t1.discrete());

        for (int i = 0; i < df.rowCount(); i++) {

            double x = df.getDouble(i, "x");

            assertEquals(df.getDouble(i, "pdf_1"), t1.pdf(x), TOL);
            assertEquals(df.getDouble(i, "cdf_1"), t1.cdf(x), TOL);
            if (x > 0 && x < 1) {
                if (!Double.isNaN(df.getDouble(i, "quantile_1")))
                    assertEquals(df.getDouble(i, "quantile_1"), t1.quantile(df.getDouble(i, "x")), TOL);
            }
            assertEquals(df.getDouble(i, "pdf_2"), t2.pdf(x), TOL);
            assertEquals(df.getDouble(i, "cdf_2"), t2.cdf(x), TOL);
            if (x > 0 && x < 1) {
                if (!Double.isNaN(df.getDouble(i, "quantile_2")))
                    assertEquals(df.getDouble(i, "quantile_2"), t2.quantile(df.getDouble(i, "x")), TOL);
            }
            assertEquals(df.getDouble(i, "pdf_5"), t5.pdf(x), TOL);
            assertEquals(df.getDouble(i, "cdf_5"), t5.cdf(x), TOL);
            if (x > 0 && x < 1) {
                if (!Double.isNaN(df.getDouble(i, "quantile_5")))
                    assertEquals(df.getDouble(i, "quantile_5"), t5.quantile(df.getDouble(i, "x")), TOL);
            }
            assertEquals(df.getDouble(i, "pdf_10"), t10.pdf(x), TOL);
            assertEquals(df.getDouble(i, "cdf_10"), t10.cdf(x), TOL);
            if (x > 0 && x < 1) {
                if (!Double.isNaN(df.getDouble(i, "quantile_10")))
                    assertEquals(df.getDouble(i, "quantile_10"), t10.quantile(df.getDouble(i, "x")), TOL);
            }
            assertEquals(df.getDouble(i, "pdf_100"), t100.pdf(x), TOL);
            assertEquals(df.getDouble(i, "cdf_100"), t100.cdf(x), TOL);
            if (x > 0 && x < 1) {
                if (!Double.isNaN(df.getDouble(i, "quantile_100")))
                    assertEquals(df.getDouble(i, "quantile_100"), t100.quantile(df.getDouble(i, "x")), TOL);
            }
        }
    }

    @Test
    void testOtherT() {

        StudentT t = StudentT.of(10, 2, 3);
        assertEquals(2, t.mean(), TOL);
        assertEquals(2, t.mode(), TOL);
        assertEquals(11.25, t.var(), TOL);
        assertEquals(Double.NEGATIVE_INFINITY, t.minValue(), TOL);
        assertEquals(Double.POSITIVE_INFINITY, t.maxValue(), TOL);
        assertEquals(0, t.skewness(), TOL);
        assertEquals(1, t.kurtosis(), TOL);

        assertEquals(Double.NaN, StudentT.of(2).skewness(), TOL);
        assertEquals(Double.NaN, StudentT.of(1).var(), TOL);
        assertEquals(Double.POSITIVE_INFINITY, StudentT.of(2).var(), TOL);

        assertEquals(Double.NaN, StudentT.of(2).kurtosis(), TOL);
        assertEquals(Double.POSITIVE_INFINITY, StudentT.of(4).kurtosis(), TOL);
    }

    @Test
    void testInvalidDf() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> StudentT.of(-1));
        assertEquals("degrees of freedom in student t distribution must have a value greater than 0.", ex.getMessage());
    }

    @Test
    void testInvalidNegativeProbabilityOnQuantile() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> StudentT.of(2).quantile(-1));
        assertEquals("Probability must be in the range [0,1]", ex.getMessage());
    }

    @Test
    void testInvalidPositiveProbabilityOnQuantile() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> StudentT.of(2).quantile(2));
        assertEquals("Probability must be in the range [0,1]", ex.getMessage());
    }

    @Test
    void testEntropy() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> StudentT.of(4).entropy());
        assertEquals("Not implemented.", ex.getMessage());
    }
}
