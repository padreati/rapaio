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

import rapaio.graphics.base.Figure;
import rapaio.printer.opt.POption;
import rapaio.printer.opt.POptionTextWidth;
import rapaio.printer.opt.POpts;

/**
 * Interface for the printing system. The printing system is the device responsible with
 * printing text and graphics to console or other devices for information purposes.
 * <p>
 * The text printing facility uses a text width property for wrapping text output.
 * <p>
 * The graphical printing facility uses graphic shape (width and height) as default values
 * for graphical output.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface Printer {

    static POption<Integer> textWidth(int textWidth) {
        return new POptionTextWidth(textWidth);
    }

    /**
     * Configures default graphic image shape (width and height)
     * @param width image width
     * @param height image height
     * @return printer instance
     */
    Printer withGraphicShape(int width, int height);

    /**
     * @return configured value for graphical image width
     */
    int graphicWidth();

    /**
     * @return configured value for graphical image height
     */
    int graphicHeight();


    /**
     * Creates a new instance of printing options collection.
     * Any modification on this instance will not affect what
     * is stored as configuration on the printer itself.
     *
     * If one wants to modify the printing options from printer instance
     * she must use {@link #withOptions(POption[])} method.
     *
     * @return new copy of printing options collection
     */
    POpts getOptions();

    /**
     * Changes the options for this printer. Any change will affect
     * new instances created with {@link #getOptions()}.
     *
     * @param options options to be applied on this printer instance
     * @return instance of the printer
     */
    Printer withOptions(POption...options);

    /**
     * Print a message to text output
     *
     * @param message message to be printed
     */
    void print(String message);

    /**
     * Print a new line.
     */
    void println();

    /**
     * Print a message and append a new line after the message.
     * @param message message to be printed
     */
    default void println(String message) {
        print(message);
        println();
    }


    default void println(double value) {
        print(String.valueOf(value));
        println();
    }

    void draw(Figure figure, int width, int height);

    default void draw(Figure figure) {
        draw(figure, graphicWidth(), graphicHeight());
    }

    default void printString(Printable printable) {
        println(printable.toString());
    }

    default void printSummary(Printable printable, POption... options) {
        println(printable.toSummary(this, options));
    }

    default void printContent(Printable printable, POption... options) {
        println(printable.toContent(this, options));
    }

    default void printFullContent(Printable printable, POption... options) {
        println(printable.toFullContent(this, options));
    }
}
