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

package rapaio.printer;

import java.io.IOException;
import java.net.Socket;
import org.openide.util.Exceptions;
import rapaio.graphics.base.Figure;
import static rapaio.server.AbstractCmd.DEFAULT_PORT;
import rapaio.server.ClassMarshaller;
import rapaio.server.CommandBytes;

/**
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class LocalPrinter implements Printer {

    @Override
    public int getTextWidth() {
        return 120;
    }

    @Override
    public void setTextWidth(int chars) {
    }

    @Override
    public int getGraphicWidth() {
        return 600;
    }

    @Override
    public void setGraphicWidth(int width) {
    }

    @Override
    public int getGraphicHeight() {
        return 400;
    }

    @Override
    public void setGraphicHeight(int height) {
    }

    @Override
    public void print(String message) {
        try (Socket s = new Socket("localhost", DEFAULT_PORT)) {
            new ClassMarshaller().marshallPrint(s.getOutputStream(), "print", message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void println() {
        try (Socket s = new Socket("localhost", DEFAULT_PORT)) {
            new ClassMarshaller().marshallPrint(s.getOutputStream(), "print", "\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void error(String message, Throwable throwable) {
        try (Socket s = new Socket("localhost", DEFAULT_PORT)) {
            new ClassMarshaller().marshallPrint(s.getOutputStream(), "error", message);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void preparePrinter() {
    }

    @Override
    public void closePrinter() {
    }

    @Override
    public void heading(int h, String lines) {
        try (Socket s = new Socket("localhost", DEFAULT_PORT)) {
            new ClassMarshaller().marshallPrint(s.getOutputStream(), "heading" + h, lines);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void code(String lines) {
        try (Socket s = new Socket("localhost", DEFAULT_PORT)) {
            new ClassMarshaller().marshallPrint(s.getOutputStream(), "code", lines);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void p(String lines) {
        try (Socket s = new Socket("localhost", DEFAULT_PORT)) {
            new ClassMarshaller().marshallPrint(s.getOutputStream(), "p", lines);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void eqn(String equation) {
        try (Socket s = new Socket("localhost", DEFAULT_PORT)) {
            new ClassMarshaller().marshallPrint(s.getOutputStream(), "eqn", equation);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void draw(Figure figure, int width, int height) {
        draw(figure);
    }

    @Override
    public void draw(Figure figure) {
        try (Socket s = new Socket("localhost", DEFAULT_PORT)) {
            new ClassMarshaller().marshallConfig(s.getOutputStream());
            CommandBytes cb = new ClassMarshaller().unmarshall(s.getInputStream());
            new ClassMarshaller().marshallDraw(s.getOutputStream(), figure, cb.getGraphicalWidth(), cb.getGraphicalHeight());
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}
