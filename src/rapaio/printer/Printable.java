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
 * There are four text output a printable object can expose. Below there are
 * the types of text outputs and their meaning:
 * <ul>
 *     <li>printString - it is the string representation of the object exposed by toString method;
 *     usually this text output type contains the structural information of the printable instance</li>
 *     <li>summary - contains a compact summary of the printable instance holding statistics and
 *     other summary information</li>
 *     <li>content - text representation of the printable object content, truncated if the output is too big</li>
 *     <li>full content - full text representation of the printable object content, no truncation</li>
 * </ul>
 * <p>
 * There are three ways one can use that types of information: getting it directly as text, printing on default printer
 * or printing on a specific printer given as parameter
 * <p>
 * See also {@link rapaio.printer.Printer}
 * <p>
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public interface Printable {

    /**
     * String representation of a printable object. If the
     * printable object does not implement to string, the implementation
     * is inherited from {@link Object#toString()}
     *
     * @return string representation of the object
     */
    @Override
    String toString();

    /**
     * Returns a text representation of summary statistics.
     *
     * @return summary statistics text representation
     */
    default String toSummary() {
        return toString();
    }

    /**
     * Eventually truncated text representation of the printable object content
     *
     * @return eventually truncated content's text representation
     */
    default String toContent() {
        return toString();
    }

    /**
     * Full text representation of the printable object content
     *
     * @return full content's text representation
     */
    default String toFullContent() {
        return toString();
    }


    /**
     * Prints string representation to default printer
     */
    default void printString() {
        WS.getPrinter().printString(this);
    }

    /**
     * Prints string representation to the given printer
     *
     * @param printer printer instance
     */
    default void printString(Printer printer) {
        printer.printString(this);
    }

    /**
     * Prints summary representation to default printer
     */
    default void printSummary() {
        WS.getPrinter().printSummary(this);
    }

    /**
     * Prints summary representation to the given printer
     *
     * @param printer printer instance
     */
    default void printSummary(Printer printer) {
        printer.printSummary(this);
    }

    /**
     * Prints reduced text representation of the printable object content to default printer
     */
    default void printContent() {
        WS.getPrinter().printContent(this);
    }

    /**
     * Prints reduced text representation of the printable content to the given printer instance
     *
     * @param printer printer instance
     */
    default void printContent(Printer printer) {
        printer.printContent(this);
    }

    /**
     * Prints full text representation of the printable object content to default printer
     */
    default void printFullContent() {
        WS.getPrinter().printFullContent(this);
    }

    /**
     * Prints full text representation of the printable content to the given printer instance
     *
     * @param printer printer instance
     */
    default void printFullContent(Printer printer) {
        printer.printFullContent(this);
    }
}
