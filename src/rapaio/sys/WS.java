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

package rapaio.sys;

import rapaio.graphics.base.Figure;
import rapaio.graphics.base.ImageUtility;
import rapaio.printer.Printer;
import rapaio.printer.standard.StandardPrinter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.LogManager;

/**
 * WS tool.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class WS {

    private static final LogManager logManager = LogManager.getLogManager();
    private static DecimalFormat formatDecShort = new DecimalFormat();
    private static DecimalFormat formatDecMedium = new DecimalFormat();
    private static DecimalFormat formatDecLong = new DecimalFormat();
    private static DecimalFormat formatDecFlex = new DecimalFormat();
    private static DecimalFormat formatDecFlexShort = new DecimalFormat();
    private static Printer printer = new StandardPrinter();

    static {
        formatDecShort.setMinimumIntegerDigits(1);
        formatDecShort.setMinimumFractionDigits(3);
        formatDecShort.setMaximumFractionDigits(3);
        formatDecMedium.setMinimumIntegerDigits(1);
        formatDecMedium.setMinimumFractionDigits(6);
        formatDecMedium.setMaximumFractionDigits(6);
        formatDecLong.setMinimumFractionDigits(30);
        formatDecLong.setMaximumFractionDigits(30);
        formatDecLong.setMinimumIntegerDigits(1);
        formatDecFlex.setMinimumFractionDigits(0);
        formatDecFlex.setMaximumFractionDigits(7);
        formatDecFlex.setMinimumIntegerDigits(1);
        formatDecFlexShort.setMinimumFractionDigits(0);
        formatDecFlexShort.setMaximumFractionDigits(3);
        formatDecFlexShort.setMinimumIntegerDigits(1);
    }

    static {
        try {
            logManager.readConfiguration(new ByteArrayInputStream(("\n" +
                    "handlers = java.util.logging.ConsoleHandler\n" +
                    "config   =\n" +
                    "\n" +
                    "\"logger\".handlers           =\n" +
                    "\"logger\".useParentHandlers  =\n" +
                    ".level              = ALL\n" +
                    "\n" +
                    "java.util.logging.ConsoleHandler.level     = CONFIG\n" +
                    "java.util.logging.ConsoleHandler.filter    =\n" +
                    "java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter\n" +
                    "java.util.logging.ConsoleHandler.encoding  =").getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Printer getPrinter() {
        return printer;
    }

    public static void setPrinter(Printer printer) {
        WS.printer = printer;
    }

    public static String formatFlex(double value) {
        if (Double.isNaN(value))
            return "?";
        if (Double.isInfinite(value))
            return Double.toString(value);
        return formatDecFlex.format(value);
    }

    public static String formatFlexShort(double value) {
        if (Double.isNaN(value))
            return "?";
        if (Double.isInfinite(value))
            return Double.toString(value);
        return formatDecFlexShort.format(value);
    }

    public static String formatShort(double value) {
        return formatDecShort.format(value);
    }

    public static String formatMedium(double value) {
        return formatDecMedium.format(value);
    }

    public static String formatLong(double value) {
        return formatDecLong.format(value);
    }

    public static String formatPValue(double pvalue) {
        if (pvalue <= 1e-16) {
            return "<2e-16";
        }
        if (pvalue >= 1e-6) {
            return formatMedium(pvalue);
        }
        return String.format("%10.2e", pvalue);
    }

    public static String getPValueStars(double pValue) {
        if (pValue > 0.1) return "";
        if (pValue > 0.05) return ".";
        if (pValue > 0.01) return "*";
        if (pValue > 0.001) return "**";
        return "***";
    }

    public static void print(String message) {
        printer.print(message);
    }

    public static void println() {
        printer.println();
    }

    public static void println(String message) {
        printer.print(message);
        printer.println();
    }

    public static void printf(String message, Object... args) {
        printer.print(String.format(message, args));
    }

    public static void draw(Figure figure) {
        printer.draw(figure);
    }

    public static void saveImage(Figure figure, int width, int height, String fileName) {
        try {
            ImageUtility.saveImage(figure, width, height, fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
