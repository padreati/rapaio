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
package rapaio.studio.printer;

import javax.swing.SwingUtilities;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.WindowManager;
import rapaio.graphics.base.Figure;
import rapaio.printer.AbstractPrinter;
import rapaio.printer.Printer;

/**
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@ServiceProvider(service = Printer.class)
public class StandardIOPrinter extends AbstractPrinter {
    
    public static InputOutput getIO() {
        return IOProvider.getDefault().getIO("Rapaio output", false);
    }

    @Override
    public void print(String string) {
        getIO().getOut().append(string);
        getIO().select();
    }

    @Override
    public void println() {
    }

    @Override
    public void error(String string, Throwable thrwbl) {
        getIO().getErr().append(string);
        getIO().getErr().append("\n");
        thrwbl.printStackTrace(getIO().getErr());
        getIO().select();
    }

    @Override
    public void draw(final Figure figure, int width, int height) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                GraphicalIOPrinterTopComponent tc = (GraphicalIOPrinterTopComponent)WindowManager.getDefault().findTopComponent("GraphicalIOPrinterTopComponent");
                tc.setFigure(figure);
                tc.revalidate();
                tc.repaint();
            }
        });
    }
}
