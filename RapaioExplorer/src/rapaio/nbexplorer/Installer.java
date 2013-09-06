package rapaio.nbexplorer;

import java.io.IOException;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;

public class Installer extends ModuleInstall {
    
    private NBOutputServer server;

    @Override
    public void restored() {
        try {
            if(server!=null) {
                server.stop();
            }
            server = new NBOutputServer();
            server.start();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
