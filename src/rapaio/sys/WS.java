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

import rapaio.graphics.base.*;
import rapaio.printer.*;
import rapaio.printer.standard.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.LogManager;

/**
 * WS tool.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class WS {

    private static final LogManager logManager = LogManager.getLogManager();
    private static Printer printer = new StandardPrinter();

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

    public static void print(double value) {
        printer.print(String.valueOf(value));
    }

    public static void print(String message) {
        printer.print(message);
    }

    public static void println() {
        printer.println();
    }

    public static void println(double value) {
        printer.println(value);
    }

    public static void println(String message) {
        printer.print(message);
        printer.println();
    }

    public static void printf(String message, Object... args) {
        printer.print(String.format(message, args));
    }

    public static void draw(Figure figure, int w, int h) {
        printer.draw(figure, w, h);
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

    public static BufferedImage image(Figure figure) {
        return image(figure, getPrinter().graphicWidth(), getPrinter().graphicHeight());
    }

    public static BufferedImage image(Figure figure, int w, int h) {
        return ImageUtility.buildImage(figure, w, h, BufferedImage.TYPE_4BYTE_ABGR_PRE);
    }
}
