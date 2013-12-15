
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

    @Override
    public void print(String string) {
        InputOutput io = IOProvider.getDefault().getIO("Rapaio output", false);
        io.getOut().append(string);
        io.select();
    }

    @Override
    public void println() {
    }

    @Override
    public void error(String string, Throwable thrwbl) {
        InputOutput io = IOProvider.getDefault().getIO("Rapaio output", false);
        io.getErr().append(string);
        io.select();
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
