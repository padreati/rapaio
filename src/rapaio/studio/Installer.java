
package rapaio.studio;

import java.io.IOException;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import rapaio.studio.server.RapaioNBServer;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        try {
            RapaioNBServer.getInstance().start();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void close() {
        try {
            RapaioNBServer.getInstance().shutdown();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public boolean closing() {
        try {
            RapaioNBServer.getInstance().shutdown();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return true;
    }
    
    
}
