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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.imageio.ImageIO;
import javax.net.ServerSocketFactory;
import javax.swing.SwingUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.IOColorPrint;
import org.openide.windows.WindowManager;
import rapaio.printer.Printer;
import rapaio.server.CommandBytes;
import rapaio.server.ClassMarshaller;
import rapaio.studio.printer.AgregatePrinter;
import rapaio.studio.printer.GraphicalIOPrinterTopComponent;
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
                            CommandBytes cb = new ClassMarshaller().unmarshall(s.getInputStream());

                            switch (cb.getType()) {

                                case CONFIG:
                                    cb = doConfig(cb);
                                    new ClassMarshaller().marshallConfig(s.getOutputStream(), cb);
                                    s.getOutputStream().flush();
                                    doDraw(new ClassMarshaller().unmarshall(s.getInputStream()));
                                    break;

                                case REMOTE:
                                    doRemote(cb);
                                    break;

                                case PRINT:
                                    doPrint(cb);
                                    break;

                                case DRAW:
                                    doDraw(cb);
                                    break;
                            }

                        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
                            Workspace.getPrinter().error("Error running remote command", ex);
                        }
                    }
                } catch (IOException ex) {
                    Workspace.getPrinter().error("Error running remote command", ex);
                }
            }

            private void doPrint(CommandBytes cb) throws IOException {
                switch(cb.getName()) {
                    case "print":
                        Workspace.print(cb.getValue());
                        break;
                    case "println":
                        Workspace.println();
                        break;
                    case "code":
                        Workspace.code(cb.getValue());
                        break;
                    case "error":
                        Workspace.error(cb.getValue(), null);
                        break;
                    case "p":
                        Workspace.p(cb.getValue());
                        break;
                    case "eqn":
                        Workspace.eqn(cb.getValue());
                        break;
                }
                if(cb.getName().startsWith("heading")) {
                    int h = Integer.parseInt(cb.getName().substring("heading".length()));
                    Workspace.heading(h, cb.getValue());
                }
            }

            private void doDraw(CommandBytes cb) throws IOException {
                InputStream in = new ByteArrayInputStream(cb.getBytes());
                final BufferedImage image = ImageIO.read(in);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        GraphicalIOPrinterTopComponent tc = (GraphicalIOPrinterTopComponent) WindowManager.getDefault().findTopComponent("GraphicalIOPrinterTopComponent");
                        tc.setImage(image);
                        tc.revalidate();
                        tc.repaint();
                    }
                });
            }

            private void doRemote(CommandBytes cb) throws IOException, InstantiationException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
                IOColorPrint.print(StandardIOPrinter.getIO(), "execute " + cb.getName() + " ..\n", new Color(0, 100, 0));
                CmdClassLoader cmdClassLoader = new CmdClassLoader(cb, Thread.currentThread().getContextClassLoader());
                Class<?> clazz = cmdClassLoader.findClass(cb.getName());
                Object object = clazz.newInstance();
                clazz.getMethod("run").invoke(object);
            }

            private CommandBytes doConfig(CommandBytes cb) throws IOException {
                cb.setTextWidth(Workspace.getPrinter().getTextWidth());
                cb.setGraphicalWidth(Workspace.getPrinter().getGraphicWidth());
                cb.setGraphicalHeight(Workspace.getPrinter().getGraphicHeight());
                return cb;
            }
        });
        listenerThread.start();

        for (WorkspaceListener listener : Lookup.getDefault().lookupAll(WorkspaceListener.class)) {
            Workspace.addListener(listener);
        }
        wireUpDataListeners();
        wireUpPrinter();
    }

    @SuppressWarnings("CallToThreadStopSuspendOrResumeManager")
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
