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

import rapaio.sys.WS;

/**
 * Interface implemented by all objects which outputs information about themselves
 * for exploratory purposes or for other reasons.
 * <p>
 * Implementations of this interface works directly with default printer and
 * does not returns a string format description due to various ways the output
 * is rendered by different printer implementations.
 * <p>
 * See also {@link rapaio.printer.Printer}
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface Printable {

    String summary();

    String content();

    String fullContent();

    default void printString() {
        WS.getPrinter().printString(this);
    }

    default void printSummary() {
        WS.getPrinter().printSummary(this);
    }

    default void printContent() {
        WS.getPrinter().printContent(this);
    }

    default void printFullContent() {
        WS.getPrinter().printFullContent(this);
    }
}
