
package rapaio.studio.server;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import javax.net.ServerSocketFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import rapaio.printer.Printer;
import rapaio.server.ClassBytes;
import rapaio.server.ClassMarshaller;
import rapaio.studio.printer.AgregatePrinter;
import rapaio.workspace.Workspace;
import rapaio.workspace.WorkspaceDataListener;
import rapaio.workspace.WorkspaceListener;

/**
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@ServiceProvider(service = WorkspaceListener.class)
public class RapaioNBServer implements WorkspaceListener {

    public static final int DEFAULT_PORT = 56339;
    private static RapaioNBServer instance;

    public static RapaioNBServer getInstance() {
        if (instance == null) {
            instance = new RapaioNBServer();
        }
        return instance;
    }

    private ServerSocket serverSocket;
    private Thread listenerThread;

    public void start() throws IOException {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ioe) {
            }
        }
        serverSocket = ServerSocketFactory.getDefault().createServerSocket(DEFAULT_PORT);
        listenerThread = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    if (serverSocket.isClosed()) {
                        return;
                    }
                    try (Socket s = serverSocket.accept()) {
                        ClassBytes cb = new ClassMarshaller().unmarshall(s.getInputStream());
                        Workspace.print("received command: " + cb.getName() + " ..\n");

                        CmdClassLoader cmdClassLoader = new CmdClassLoader(cb, Thread.currentThread().getContextClassLoader());
                        Class<?> clazz = cmdClassLoader.findClass(cb.getName());
                        Object object = clazz.newInstance();
                        clazz.getMethod("run").invoke(object);

                    } catch (SocketException se) {
                        return;
                    } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
        listenerThread.start();

        for (WorkspaceListener listener : Lookup.getDefault().lookupAll(WorkspaceListener.class)) {
            Workspace.addListener(listener);
        }
        wireUpDataListeners();
        wireUpPrinter();
    }

    public void shutdown() throws IOException {
        serverSocket.close();
        listenerThread.interrupt();
    }

    @Override
    public void onNewWorkspaceData() {
        wireUpDataListeners();
    }

    @Override
    public void onLoadWorkspaceData(String string) {
        wireUpDataListeners();
    }

    @Override
    public void onWriteWorkspaceData(String string) {
    }

    private void wireUpDataListeners() {
        for (WorkspaceDataListener listener : Lookup.getDefault().lookupAll(WorkspaceDataListener.class)) {
            Workspace.getData().addListener(listener);
        }
    }

    private void wireUpPrinter() {
        AgregatePrinter mainPrinter = new AgregatePrinter();
        for (Printer printer : Lookup.getDefault().lookupAll(Printer.class)) {
            mainPrinter.addPrinter(printer);
        }
        Workspace.setPrinter(mainPrinter);
    }
}
