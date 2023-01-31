/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/27/18.
 */
public class Format {

    public static DecimalFormat floatShort() {
        DecimalFormat format = new DecimalFormat();
        format.setMinimumIntegerDigits(1);
        format.setMinimumFractionDigits(3);
        format.setMaximumFractionDigits(3);
        return format;
    }

    public static String floatShort(double value) {
        return floatShort().format(value);
    }

    public static DecimalFormat floatMedium() {
        DecimalFormat format = new DecimalFormat();
        format.setMinimumIntegerDigits(1);
        format.setMinimumFractionDigits(7);
        format.setMaximumFractionDigits(7);
        return format;
    }

    public static String floatMedium(double value) {
        return floatMedium().format(value);
    }

    public static DecimalFormat floatLong() {
        DecimalFormat format = new DecimalFormat();
        format.setMinimumIntegerDigits(1);
        format.setMinimumFractionDigits(30);
        format.setMaximumFractionDigits(30);
        return format;
    }

    public static String floatLong(double value) {
        return floatLong().format(value);
    }

    public static DecimalFormat floatFlex() {
        DecimalFormat format = new DecimalFormat();
        format.setMinimumIntegerDigits(1);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(7);
        return format;
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
        DecimalFormat format = new DecimalFormat();
        format.setMinimumIntegerDigits(1);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(3);
        return format;
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
        DecimalFormat format = new DecimalFormat();
        format.setMinimumIntegerDigits(1);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(30);
        return format;
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
        return String.format("%10.2e", pvalue);
    }

    private Format() {
    }
}
