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

package rapaio.sys;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;

import rapaio.math.tensor.TensorEngine;
import rapaio.math.tensor.engine.barray.BaseArrayTensorEngine;
import rapaio.printer.Figure;
import rapaio.printer.ImageTools;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;
import rapaio.printer.standard.StandardPrinter;

/**
 * This class emulates a global working session. A global session
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public final class WS {

    private static final LogManager logManager = LogManager.getLogManager();
    private static Printer printer = new StandardPrinter();
    private static TensorEngine tensorEngine = new BaseArrayTensorEngine();

    public static void initLog(Level level) {
        try {
            logManager.readConfiguration(new ByteArrayInputStream(("""
                    handlers = java.util.logging.ConsoleHandler
                    config   =

                    "logger".handlers           =
                    "logger".useParentHandlers  =
                    .level              = %s

                    java.util.logging.ConsoleHandler.level     = %s
                    java.util.logging.ConsoleHandler.filter    =
                    java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
                    java.util.logging.ConsoleHandler.encoding  =""".formatted(level.getName(), level.getName())).getBytes()));
        } catch (IOException ignored) {
            // ignored
        }
    }

    private WS() {
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
            ImageTools.saveFigureImage(figure, width, height, fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static BufferedImage image(Figure figure, POpt<?>...options) {
        var opts = printer.getOptions().bind(options);
        return image(figure, opts.getGraphicWidth(), opts.getGraphicHeight());
    }

    public static BufferedImage image(Figure figure, int w, int h) {
        return ImageTools.makeImage(figure, w, h);
    }

    public static TensorEngine tensorEngine() {
        return tensorEngine;
    }
}
