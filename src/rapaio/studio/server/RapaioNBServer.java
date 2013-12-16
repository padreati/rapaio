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

package rapaio.studio.server;

import java.awt.Color;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import javax.net.ServerSocketFactory;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOColorPrint;
import rapaio.printer.Printer;
import rapaio.server.ClassBytes;
import rapaio.server.ClassMarshaller;
import rapaio.studio.printer.AgregatePrinter;
import rapaio.studio.printer.StandardIOPrinter;
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

    private Thread listenerThread;

    public void start() throws IOException {
        listenerThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try (ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(DEFAULT_PORT)) {
                    while (true) {
                        try (Socket s = serverSocket.accept()) {
                            if (serverSocket.isClosed()) {
                                return;
                            }
                            ClassBytes cb = new ClassMarshaller().unmarshall(s.getInputStream());
                            IOColorPrint.print(StandardIOPrinter.getIO(), "execute " + cb.getName() + " ..\n", new Color(0, 100, 0));

                            CmdClassLoader cmdClassLoader = new CmdClassLoader(cb, Thread.currentThread().getContextClassLoader());
                            Class<?> clazz = cmdClassLoader.findClass(cb.getName());
                            Object object = clazz.newInstance();
                            clazz.getMethod("run").invoke(object);
                        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
                            Workspace.getPrinter().error("Error running remote command", ex);
                        }
                    }
                } catch (IOException ex) {
                    Workspace.getPrinter().error("Error running remote command", ex);
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

    public void shutdown() throws IOException, InterruptedException {
        if (listenerThread != null) {
            listenerThread.interrupt();
            listenerThread.stop();
            listenerThread.join(0);
        }
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
