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

    public static DecimalFormat formatDecShort = new DecimalFormat();
    public static DecimalFormat formatDecMedium = new DecimalFormat();
    public static DecimalFormat formatDecLong = new DecimalFormat();
    public static DecimalFormat formatDecFlex = new DecimalFormat();
    public static DecimalFormat formatDecFlexShort = new DecimalFormat();
    public static DecimalFormat formatDecFlexLong = new DecimalFormat();

    static {
        formatDecShort.setMinimumIntegerDigits(1);
        formatDecShort.setMinimumFractionDigits(3);
        formatDecShort.setMaximumFractionDigits(3);
        formatDecMedium.setMinimumIntegerDigits(1);
        formatDecMedium.setMinimumFractionDigits(7);
        formatDecMedium.setMaximumFractionDigits(7);
        formatDecLong.setMinimumIntegerDigits(1);
        formatDecLong.setMinimumFractionDigits(30);
        formatDecLong.setMaximumFractionDigits(30);
        formatDecFlexShort.setMinimumIntegerDigits(1);
        formatDecFlexShort.setMinimumFractionDigits(0);
        formatDecFlexShort.setMaximumFractionDigits(3);
        formatDecFlex.setMinimumIntegerDigits(1);
        formatDecFlex.setMinimumFractionDigits(0);
        formatDecFlex.setMaximumFractionDigits(7);
        formatDecFlexLong.setMinimumIntegerDigits(1);
        formatDecFlexLong.setMinimumFractionDigits(0);
        formatDecFlexLong.setMaximumFractionDigits(30);
    }

    private Format() {
    }

    public static String floatShort(double value) {
        return formatDecShort.format(value);
    }

    public static String floatMedium(double value) {
        return formatDecMedium.format(value);
    }

    public static String floatLong(double value) {
        return formatDecLong.format(value);
    }

    public static String floatFlex(double value) {
        if (Double.isNaN(value))
            return "?";
        if (Double.isInfinite(value))
            return Double.toString(value);
        return formatDecFlex.format(value);
    }

    public static String floatFlexShort(double value) {
        if (Double.isNaN(value))
            return "?";
        if (Double.isInfinite(value))
            return Double.toString(value);
        return formatDecFlexShort.format(value);
    }
    public static String floatFlexLong(double value) {
        if (Double.isNaN(value))
            return "?";
        if (Double.isInfinite(value))
            return Double.toString(value);
        return formatDecFlexLong.format(value);
    }

    public static String pValueStars(double pValue) {
        if (pValue > 0.1) return "";
        if (pValue > 0.05) return ".";
        if (pValue > 0.01) return "*";
        if (pValue > 0.001) return "**";
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
}
