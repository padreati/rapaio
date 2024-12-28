/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

import rapaio.printer.opt.POpt;
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
     * @param printer printer instance
     * @param options printing options
     * @return summary statistics text representation
     */
    default String toSummary(Printer printer, POpt<?>... options) {
        return toString();
    }

    /**
     * Returns a text representation of summary statistics.
     *
     * @param options printing options
     * @return summary statistics text representation
     */
    default String toSummary(POpt<?>... options) {
        return toSummary(WS.getPrinter(), options);
    }

    /**
     * Eventually truncated text representation of the printable object content
     *
     * @param printer printer instance
     * @param options printing options
     * @return eventually truncated content's text representation
     */
    default String toContent(Printer printer, POpt<?>... options) {
        return toString();
    }

    /**
     * Eventually truncated text representation of the printable object content
     *
     * @param options printing options
     * @return eventually truncated content's text representation
     */
    default String toContent(POpt<?>... options) {
        return toContent(WS.getPrinter(), options);
    }

    /**
     * Full text representation of the printable object content
     *
     * @param printer printer instance
     * @param options printing options
     * @return full content's text representation
     */
    default String toFullContent(Printer printer, POpt<?>... options) {
        return toString();
    }

    /**
     * Full text representation of the printable object content
     *
     * @param options printing options
     * @return full content's text representation
     */
    default String toFullContent(POpt<?>... options) {
        return toFullContent(WS.getPrinter(), options);
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
     *
     * @param options printing options
     */
    default void printSummary(POpt<?>... options) {
        printSummary(WS.getPrinter(), options);
    }

    /**
     * Prints summary representation to the given printer
     *
     * @param printer printer instance
     * @param options printing options
     */
    default void printSummary(Printer printer, POpt<?>... options) {
        printer.printSummary(this, options);
    }

    /**
     * Prints reduced text representation of the printable object content to default printer
     *
     * @param options printing options
     */
    default void printContent(POpt<?>... options) {
        printContent(WS.getPrinter(), options);
    }

    /**
     * Prints reduced text representation of the printable content to the given printer instance
     *
     * @param printer printer instance
     * @param options printing options
     */
    default void printContent(Printer printer, POpt<?>... options) {
        printer.printContent(this, options);
    }

    /**
     * Prints full text representation of the printable object content to default printer
     *
     * @param options printing options
     */
    default void printFullContent(POpt<?>... options) {
        printFullContent(WS.getPrinter(), options);
    }

    /**
     * Prints full text representation of the printable content to the given printer instance
     *
     * @param printer printer instance
     * @param options printing options
     */
    default void printFullContent(Printer printer, POpt<?>... options) {
        printer.printFullContent(this, options);
    }
}
