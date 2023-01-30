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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.VarType;
import rapaio.io.Csv;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class NormalTest {

    private static final double TOL = 1e-12;

    private Frame otherDf;
    private Frame stdDf;

    @BeforeEach
    void setUp() throws IOException {
        otherDf = Csv.instance()
                .header.set(true)
                .separatorChar.set(',')
                .defaultTypes.set(VarType.DOUBLE)
                .naValues.set("?", "-Inf", "Inf", "NA")
                .read(this.getClass(), "other_normal.csv");
        stdDf = Csv.instance()
                .header.set(true)
                .separatorChar.set(',')
                .defaultTypes.set(VarType.DOUBLE)
                .naValues.set("?", "-Inf", "Inf", "NA")
                .read(this.getClass(), "standard_normal.csv");
    }

    @Test
    void testOtherQuantile() {
        Normal d = Normal.of(10, 2);
        for (int i = 0; i < otherDf.rowCount(); i++) {
            if (otherDf.getDouble(i, "x") > 0 && otherDf.getDouble(i, "x") < 1) {
                assertEquals(otherDf.getDouble(i, "quantile"), d.quantile(otherDf.getDouble(i, "x")), TOL);
            }
        }
    }

    @Test
    void testOtherPdf() {
        Normal d = Normal.of(10, 2);
        for (int i = 0; i < otherDf.rowCount(); i++) {
            assertEquals(otherDf.getDouble(i, "pdf"), d.pdf(otherDf.getDouble(i, "x")), TOL);
        }
    }

    @Test
    void testOtherCdf() {
        Normal d = Normal.of(10, 2);
        for (int i = 0; i < otherDf.rowCount(); i++) {
            assertEquals(otherDf.getDouble(i, "cdf"), d.cdf(otherDf.getDouble(i, "x")), TOL);
        }
    }

    @Test
    void testOtherAspects() {
        Normal normal = Normal.std();
        assertEquals(Double.NEGATIVE_INFINITY, normal.minValue(), TOL);
        assertEquals(Double.POSITIVE_INFINITY, normal.maxValue(), TOL);
        assertEquals(0, normal.mean(), TOL);
        assertEquals(0, normal.mode(), TOL);
        assertEquals(1, normal.var(), TOL);
        assertEquals(0, normal.skewness(), TOL);
        assertEquals(0, normal.kurtosis(), TOL);
        assertEquals(2.8378770664093453, normal.entropy(), TOL);

        assertEquals(Double.NaN, Normal.std().cdf(Double.NaN), TOL);
        assertEquals(0, Normal.std().cdf(Double.NEGATIVE_INFINITY), TOL);
        assertEquals(0, Normal.std().cdf(Double.POSITIVE_INFINITY), TOL);

        assertEquals(Double.NEGATIVE_INFINITY, normal.quantile(0), TOL);
        assertEquals(Double.POSITIVE_INFINITY, normal.quantile(1), TOL);

        assertEquals(1, normal.sd(), TOL);
        assertEquals(0, normal.median(), TOL);
    }

    @Test
    void testInvalidQuantileSmallInput() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Normal.std().quantile(-1));
        assertEquals("Inverse of a probability requires a probablity in the range [0,1], not -1.0", ex.getMessage());
    }

    @Test
    void testInvalidQuantileBigInput() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Normal.std().quantile(2));
        assertEquals("Inverse of a probability requires a probablity in the range [0,1], not 2.0", ex.getMessage());
    }

    @Test
    void testName() {
        assertEquals("Normal(mu=10, sd=20)", Normal.of(10, 20).name());
        assertEquals("Normal(mu=10.3, sd=20.3)", Normal.of(10.3, 20.3).name());
    }

    @Test
    void testIsDiscrete() {
        assertFalse(Normal.std().discrete());
        assertFalse(Normal.of(0, 23).discrete());
    }


    @Test
    void testStandardQuantile() {
        Normal d = Normal.std();
        for (int i = 0; i < stdDf.rowCount(); i++) {
            if (stdDf.getDouble(i, "x") > 0 && stdDf.getDouble(i, "x") < 1) {
                assertEquals(stdDf.getDouble(i, "quantile"), d.quantile(stdDf.getDouble(i, "x")), TOL);
            }
        }
    }

    @Test
    void testStandardPdf() {
        Normal d = Normal.std();
        for (int i = 0; i < stdDf.rowCount(); i++) {
            assertEquals(stdDf.getDouble(i, "pdf"), d.pdf(stdDf.getDouble(i, "x")), TOL);
        }
    }

    @Test
    void testStandardCdf() {
        Normal d = Normal.std();
        for (int i = 0; i < stdDf.rowCount(); i++) {
            assertEquals(stdDf.getDouble(i, "cdf"), d.cdf(stdDf.getDouble(i, "x")), TOL);
        }
    }
}