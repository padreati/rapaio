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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Number formatting used by all printers and text outputs of the library.
 * <p>
 * Every format produced here is locale independent: '.' is the decimal separator and digits are never
 * grouped, whatever the JVM default locale. This keeps table columns aligned and lets written numbers
 * (for example in CSV files) be parsed back with {@code Double.parseDouble}.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/27/18.
 */
public class Format {

    /**
     * Symbols shared by every formatter: decimal point, no grouping, ASCII digits.
     */
    public static final DecimalFormatSymbols SYMBOLS = DecimalFormatSymbols.getInstance(Locale.ROOT);

    /**
     * Creates a locale independent decimal format with the given number of fraction digits.
     *
     * @param minFraction minimum number of fraction digits
     * @param maxFraction maximum number of fraction digits
     * @return decimal format
     */
    public static DecimalFormat decimal(int minFraction, int maxFraction) {
        DecimalFormat format = new DecimalFormat("0", SYMBOLS);
        format.setGroupingUsed(false);
        format.setMinimumIntegerDigits(1);
        format.setMinimumFractionDigits(minFraction);
        format.setMaximumFractionDigits(maxFraction);
        return format;
    }

    public static DecimalFormat floatShort() {
        return decimal(3, 3);
    }

    public static String floatShort(double value) {
        return floatShort().format(value);
    }

    public static DecimalFormat floatMedium() {
        return decimal(7, 7);
    }

    public static String floatMedium(double value) {
        return floatMedium().format(value);
    }

    public static DecimalFormat floatLong() {
        return decimal(30, 30);
    }

    public static String floatLong(double value) {
        return floatLong().format(value);
    }

    public static DecimalFormat floatFlex() {
        return decimal(0, 7);
    }

    public static String floatFlex(double value) {
        if (Double.isNaN(value)) {
            return "?";
        }
        if (Double.isInfinite(value)) {
            return Double.toString(value);
        }
        return floatFlex().format(value);
    }

    public static DecimalFormat floatFlexShort() {
        return decimal(0, 3);
    }

    public static String floatFlexShort(double value) {
        if (Double.isNaN(value)) {
            return "?";
        }
        if (Double.isInfinite(value)) {
            return Double.toString(value);
        }
        return floatFlexShort().format(value);
    }

    public static DecimalFormat floatFlexLong() {
        return decimal(0, 30);
    }

    public static String floatFlexLong(double value) {
        if (Double.isNaN(value)) {
            return "?";
        }
        if (Double.isInfinite(value)) {
            return Double.toString(value);
        }
        return floatFlexLong().format(value);
    }

    public static String pValueStars(double pValue) {
        if (pValue > 0.1) {
            return "";
        }
        if (pValue > 0.05) {
            return ".";
        }
        if (pValue > 0.01) {
            return "*";
        }
        if (pValue > 0.001) {
            return "**";
        }
        return "***";
    }

    public static String pValue(double pvalue) {
        if (pvalue <= 1e-16) {
            return "<2e-16";
        }
        if (pvalue >= 1e-6) {
            return floatMedium(pvalue);
        }
        return String.format(Locale.ROOT, "%10.2e", pvalue);
    }

    private Format() {
    }
}
