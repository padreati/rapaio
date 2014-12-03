/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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
 */

package rapaio;

import rapaio.graphics.base.Figure;
import rapaio.io.WorkspaceDataPersistence;
import rapaio.printer.Printer;
import rapaio.printer.StandardPrinter;
import rapaio.ws.WorkspaceData;
import rapaio.ws.WorkspaceListener;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * @author tutuianu
 */
public class WS {

    private static WorkspaceData session = new WorkspaceData();
    private static final List<WorkspaceListener> listeners = new LinkedList<>();
    private static Printer printer = new StandardPrinter();

    public static void setPrinter(Printer printer) {
        WS.printer = printer;
    }

    public static Printer getPrinter() {
        return printer;
    }

    public static void newWorkspace() {
        session = new WorkspaceData();
    }

    /**
     * Save the current workspace to a file. This method saves all workspace
     * objects and does not clear the current loaded workspace.
     *
     * @param fileName file getName, which will be suffixed with .EData if not
     */
    public static void saveWorkspace(String fileName) {
        print("saving workspace to file " + fileName + " ...");
        if (fileName == null || fileName.isEmpty()) {
            print("Can't save workspace");
        }

        WorkspaceDataPersistence persistence = new WorkspaceDataPersistence();
        try {
            persistence.storeToFile(session, fileName);
            print("workspace saved.");
        } catch (IOException ex) {
            error("Can't save workspace to file", ex);
        }
    }

    public static void loadWorkspace(String fileName) {
        print("loading workspace from file " + fileName + " ...");
        if (fileName == null || fileName.isEmpty()) {
            print("Can't load workspace from file.");
        }
        WorkspaceDataPersistence persistence = new WorkspaceDataPersistence();
        try {
            session = persistence.restoreFromFile(fileName);
            print("workspace loaded.");
        } catch (IOException | ClassNotFoundException ex) {
            error("Can't load workspace from file.", ex);
        }
    }

    public static void addListener(WorkspaceListener listener) {
        listeners.add(listener);
    }

    public static void clearListeners() {
        listeners.clear();
    }

    public Collection<WorkspaceListener> getListeners() {
        return listeners;
    }

    public static WorkspaceData getData() {
        return session;
    }

    public static void preparePrinter() {
        printer.preparePrinter();
    }

    public static void closePrinter() {
        printer.closePrinter();
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

    public static void printfln(String message, Object... args) {
        printer.print(String.format(message, args));
        printer.println();
    }

    public static void heading(int h, String lines) {
        printer.heading(h, lines);
    }

    public static void error(String message, Throwable ex) {
        printer.error(message, ex);
    }

    public static void code(String lines) {
        printer.code(lines);
    }

    public static void p(String lines) {
        printer.p(lines);
    }

    public static void eqn(String equation) {
        printer.eqn(equation);
    }

    public static void draw(Figure figure, int width, int height) {
        printer.draw(figure, width, height);
    }

    public static void draw(Figure figure) {
        printer.draw(figure);
    }
}
