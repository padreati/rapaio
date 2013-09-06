package rapaio.printer.impl;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author tutuianu
 */
public abstract class RemotePrinterServer {

    private int port = 2341;
    private ServerSocket server;
    private WaitingThread wait;

    public void start() throws IOException {
        server = new ServerSocket(port);
        wait = new WaitingThread(server, this);
        wait.start();
    }

    public void stop() {
        wait.stop();
    }

    public abstract void captureImage(BufferedImage image);

    public abstract void captureText(String text);

    public abstract int getTextWidth();

    public abstract int getGraphicWidth();

    public abstract int getGraphicHeight();
}

class WaitingThread extends Thread {

    private final ServerSocket server;
    private final RemotePrinterServer parent;

    public WaitingThread(ServerSocket server, RemotePrinterServer parent) {
        this.server = server;
        this.parent = parent;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = server.accept();
                try (DataInputStream in = new DataInputStream(socket.getInputStream());
                     DataOutputStream out = new DataOutputStream(socket.getOutputStream());) {

                    String cmd = in.readUTF();
                    if (cmd.equals("image")) {
                        processImage(in, out);
                    }
                    if (cmd.equals("text")) {
                        processText(in, out);
                    }
                    if (cmd.equals("info")) {
                        processInfo(in, out);
                    }
                }

            } catch (IOException ex) {
                Logger.getLogger(WaitingThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private void processImage(DataInputStream in, DataOutputStream out) throws IOException {
        parent.captureImage(ImageIO.read(in));
    }

    private void processText(DataInputStream in, DataOutputStream out) throws IOException {
        parent.captureText(in.readUTF());
    }

    private void processInfo(DataInputStream in, DataOutputStream out) {
        try {
            out.writeInt(parent.getTextWidth());
            out.writeInt(parent.getGraphicWidth());
            out.writeInt(parent.getGraphicHeight());
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(WaitingThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}