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

package rapaio.printer;

import rapaio.graphics.base.Figure;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Created by <a href="mailto:tutuianu@amazon.com">Aurelian Tutuianu</a> on 9/29/15.
 */
public class LoggingPrinter implements Printer {

    private Printer copy = new StandardPrinter();
    private static final LogManager logManager = LogManager.getLogManager();
    private final Logger fLogger = Logger.getLogger(this.getClass().getPackage().getName());

    public LoggingPrinter() {
        try {
            logManager.readConfiguration(new ByteArrayInputStream(("\n" +
                    "handlers = java.util.logging.ConsoleHandler\n" +
                    "config   =\n" +
                    "\n" +
                    "\"logger\".handlers           =\n" +
                    "\"logger\".useParentHandlers  =\n" +
                    ".level              = ALL\n" +
                    "\n" +
//                    "java.util.logging.ConsoleHandler.level     = CONFIG\n" +
//                    "java.util.logging.ConsoleHandler.filter    =\n" +
//                    "java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter\n" +
//                    "java.util.logging.ConsoleHandler.encoding  =").getBytes()));
                    "# --- FileHandler ---\n" +
                    "# Override of global logging level\n" +
                    "java.util.logging.FileHandler.level=ALL\n" +
                    "\n" +
                    "# Naming style for the output file:\n" +
                    "# (The output file is placed in the directory\n" +
                    "# defined by the \"user.home\" System property.)\n" +
                    "java.util.logging.FileHandler.pattern=%h/java%u.log\n" +
                    "\n" +
                    "# Limiting size of output file in bytes:\n" +
                    "java.util.logging.FileHandler.limit=50000\n" +
                    "\n" +
                    "# Number of output files to cycle through, by appending an\n" +
                    "# integer to the base file name:\n" +
                    "java.util.logging.FileHandler.count=1\n" +
                    "\n" +
                    "# Style of output (Simple or XML):\n" +
                    "java.util.logging.FileHandler.formatter=java.util.logging.SimpleFormatter ").getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getTextWidth() {
        return copy.getTextWidth();
    }

    @Override
    public void setTextWidth(int textWidth) {
        copy.setTextWidth(textWidth);
    }

    @Override
    public int getGraphicWidth() {
        return copy.getGraphicWidth();
    }

    @Override
    public void setGraphicWidth(int width) {
        copy.setGraphicWidth(width);
    }

    @Override
    public int getGraphicHeight() {
        return copy.getGraphicHeight();
    }

    @Override
    public void setGraphicHeight(int height) {
        copy.setGraphicHeight(height);
    }

    @Override
    public void print(String message) {
        copy.print(message);
        fLogger.info(message);
    }

    @Override
    public void println() {
        copy.println();
        fLogger.info("\n");
    }

    @Override
    public void error(String message, Throwable throwable) {
        copy.error(message, throwable);
        fLogger.log(Level.SEVERE, message, throwable);
    }

    @Override
    public void preparePrinter() {
        copy.preparePrinter();
    }

    @Override
    public void closePrinter() {
        copy.closePrinter();
    }

    @Override
    public void heading(int h, String lines) {
        copy.heading(h, lines);
        fLogger.info(lines);
    }

    @Override
    public void code(String lines) {
        copy.code(lines);
        fLogger.info(lines);
    }

    @Override
    public void p(String lines) {
        copy.p(lines);
        fLogger.info(lines);
    }

    @Override
    public void eqn(String equation) {
        copy.eqn(equation);
        fLogger.info(equation);
    }

    @Override
    public void draw(Figure figure, int width, int height) {
        copy.draw(figure, width, height);
        fLogger.warning("figure drawing is not supported");
    }

    @Override
    public void draw(Figure figure) {
        copy.draw(figure);
        fLogger.warning("figure drawing is not supported");
    }
}
