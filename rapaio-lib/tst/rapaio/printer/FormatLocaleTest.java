/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2026 Aurelian Tutuianu
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

package rapaio.printer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import rapaio.data.Frame;
import rapaio.data.SolidFrame;
import rapaio.data.VarDouble;
import rapaio.data.VarNominal;
import rapaio.io.Csv;
import rapaio.util.DoublePair;

/**
 * Number formatting must not depend on the JVM default locale. These tests run with a locale that uses
 * ',' as decimal separator and '.' for grouping (de_DE) and check that the output is still the ASCII
 * form that {@code Double.parseDouble} accepts.
 */
public class FormatLocaleTest {

    private Locale saved;

    @BeforeEach
    void beforeEach() {
        saved = Locale.getDefault();
        Locale.setDefault(Locale.GERMANY);
    }

    @AfterEach
    void afterEach() {
        Locale.setDefault(saved);
    }

    @Test
    void formatsUseDecimalPointAndNoGrouping() {
        assertEquals("1234567.891", Format.floatShort(1234567.8912));
        assertEquals("1234567.8912000", Format.floatMedium(1234567.8912));
        assertEquals("1234567.8912", Format.floatFlex(1234567.8912));
        assertEquals("1234567.891", Format.floatFlexShort(1234567.8912));
        assertEquals("-0.5", Format.floatFlex(-0.5));
        assertEquals("1000000", Format.floatFlex(1_000_000));
        assertEquals("?", Format.floatFlex(Double.NaN));
        assertEquals("Infinity", Format.floatFlex(Double.POSITIVE_INFINITY));
        assertEquals("  1.23e-08", Format.pValue(1.23e-8));
        assertEquals("Pair{ 1.500000, 2.250000 }", DoublePair.of(1.5, 2.25).toString());
    }

    @Test
    void formattedNumbersParseBack() {
        double[] values = {0.1, -3.75, 1234567.891, 1e-6, 9.99e9};
        for (double v : values) {
            assertEquals(v, Double.parseDouble(Format.floatFlexLong(v)), Math.abs(v) * 1e-12);
        }
    }

    @Test
    void csvRoundTripUnderGermanLocale() throws java.io.IOException {
        Frame df = SolidFrame.byVars(
                VarDouble.copy(1.5, -1234.25, 0.001, Double.NaN).name("x"),
                VarNominal.copy("a", "b", "c", "d").name("y"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Csv.instance().write(df, out);
        String text = out.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("-1234.25"), text);
        assertTrue(!text.contains("1234,25"), text);

        Frame back = Csv.instance().read(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
        assertEquals(df.rowCount(), back.rowCount());
        for (int i = 0; i < 3; i++) {
            assertEquals(df.getDouble(i, "x"), back.getDouble(i, "x"), 1e-12);
        }
        assertTrue(back.isMissing(3, "x"));
    }
}
