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

import rapaio.graphics.base.*;

/**
 * Interface for the printing system.
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface Printer {

    Printer withTextWidth(int chars);

    int textWidth();

    Printer withGraphicShape(int width, int height);

    int graphicWidth();

    int graphicHeight();

    void print(String message);

    void println();

    default void println(String message) {
        print(message);
        println();
    }

    void draw(Figure figure, int width, int height);

    default void draw(Figure figure) {
        draw(figure, graphicWidth(), graphicHeight());
    }

    default void printString(Printable printable) {
        println(printable.toString());
    }

    default void printSummary(Printable printable) {
        println(printable.summary());
    }

    default void printContent(Printable printable) {
        println(printable.content());
    }

    default void printFullContent(Printable printable) {
        println(printable.fullContent());
    }
}
