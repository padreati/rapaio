package rapaio.nbexplorer;

import rapaio.printer.impl.RemotePrinterServer;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author tutuianu
 */
public class NBOutputServer extends RemotePrinterServer {

    @Override
    public void captureImage(final BufferedImage image) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TopComponent tc = WindowManager.getDefault().findTopComponent("RapaioGraphicTopComponent");
                RapaioGraphicTopComponent out = (RapaioGraphicTopComponent) tc;
                out.setCurrentImage(image);
            }
        });
    }

    @Override
    public void captureText(final String string) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TopComponent tc = WindowManager.getDefault().findTopComponent("RapaioOutputTopComponent");
                RapaioOutputTopComponent out = (RapaioOutputTopComponent) tc;
                out.appendString(string);
                tc.open();
                tc.requestActive();
            }
        });
    }

    @Override
    public int getTextWidth() {
        return NbPreferences.forModule(NBOutputServer.class).getInt("textWidth", 120);
    }

    @Override
    public int getGraphicWidth() {
        return NbPreferences.forModule(NBOutputServer.class).getInt("graphicWidth", 800);
    }

    @Override
    public int getGraphicHeight() {
        return NbPreferences.forModule(NBOutputServer.class).getInt("graphicHeight", 600);
    }
}
