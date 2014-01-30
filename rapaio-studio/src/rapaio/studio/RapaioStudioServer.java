package rapaio.studio;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ApplicationComponent;
import org.jetbrains.annotations.NotNull;
import rapaio.server.ClassMarshaller;
import rapaio.server.CommandBytes;
import rapaio.studio.server.CmdClassLoader;
import rapaio.workspace.Workspace;

import javax.imageio.ImageIO;
import javax.net.ServerSocketFactory;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * User: Aurelian Tutuianu <paderati@yahoo.com>
 */
public class RapaioStudioServer implements ApplicationComponent {

	public static final int DEFAULT_PORT = 56339;
	public static final String RAPAIO_GROUP_ID_INFO = "RapaioInfo";
	private static RapaioStudioServer instance;

	private ExtendedPrinter graphicPrinter;
	private ExtendedPrinter textPrinter;

	public static RapaioStudioServer getInstance() {
		if (instance == null) {
			instance = new RapaioStudioServer();
		}
		return instance;
	}

	private ExtendedPrinter printer;
	private ServerSocket serverSocket;

	private RapaioStudioServer() {
	}

	public void initComponent() {
		try {
			getInstance().shutdown();
			getInstance().start();
		} catch (Exception ex) {
			Notifications.Bus.notify(
					new Notification(RAPAIO_GROUP_ID_INFO, "Error", ex.getMessage(), NotificationType.ERROR));
		}
	}

	public void disposeComponent() {
		try {
			getInstance().shutdown();
		} catch (Exception ex) {
			Notifications.Bus.notify(
					new Notification(RAPAIO_GROUP_ID_INFO, "Error", ex.getMessage(), NotificationType.ERROR));
		}
	}

	public void setExtendedPrinter(ExtendedPrinter printer) {
		this.printer = printer;
		Workspace.setPrinter(printer);
	}

	@NotNull
	public String getComponentName() {
		return "rapaio.studio.RapaioStudioServer";
	}

	private Thread listenerThread;

	public void start() throws IOException {
		listenerThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					serverSocket = ServerSocketFactory.getDefault().createServerSocket(DEFAULT_PORT);
					while (true) {
						try {
							Socket s = serverSocket.accept();
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

						} catch (Exception ex) {
							Workspace.getPrinter().error("Error running remote command", ex);
						}
					}
				} catch (IOException ex) {
					Workspace.getPrinter().error("Error running remote command", ex);
				}
			}

			private void doPrint(CommandBytes cb) throws IOException {
				if ("print".equals(cb.getName())) {
					Workspace.print(cb.getValue());
				}
				if ("println".equals(cb.getName())) {
					Workspace.println();
				}
				if ("code".equals(cb.getName())) {
					Workspace.code(cb.getValue());
				}
				if ("error".equals(cb.getName())) {
					Workspace.error(cb.getValue(), null);
				}
				if ("p".equals(cb.getName())) {
					Workspace.p(cb.getValue());
				}
				if ("eqn".equals(cb.getName())) {
					Workspace.eqn(cb.getValue());
				}

				if (cb.getName().startsWith("heading")) {
					int h = Integer.parseInt(cb.getName().substring("heading".length()));
					Workspace.heading(h, cb.getValue());
				}
			}

			private void doDraw(CommandBytes cb) throws IOException {
				InputStream in = new ByteArrayInputStream(cb.getBytes());
				final BufferedImage image = ImageIO.read(in);
				if (printer != null)
					printer.setImage(image);
			}

			private void doRemote(CommandBytes cb) throws IOException, InstantiationException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
				CmdClassLoader cmdClassLoader = new CmdClassLoader(cb, Thread.currentThread().getContextClassLoader());
				Class<?> clazz = cmdClassLoader.findClass(cb.getName());
				Object object = clazz.newInstance();
				clazz.getMethod("run").invoke(object);
			}

			private CommandBytes doConfig(CommandBytes cb) throws IOException {
				if (printer != null) {
					cb.setTextWidth(printer.getTextWidth());
					cb.setGraphicalWidth(printer.getGraphicWidth());
					cb.setGraphicalHeight(printer.getGraphicHeight());
				} else {
					cb.setTextWidth(80);
					cb.setGraphicalWidth(200);
					cb.setGraphicalHeight(200);
				}
				return cb;
			}
		});
		listenerThread.start();
	}

	public void shutdown() throws IOException, InterruptedException {
		try {
			serverSocket.close();
		} catch (Throwable ex) {
		}
		if (listenerThread != null) {
			listenerThread.interrupt();
			listenerThread.join(0);
		}
	}
}
