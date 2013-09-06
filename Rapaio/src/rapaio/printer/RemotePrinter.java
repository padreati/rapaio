package rapaio.printer;

import rapaio.graphics.base.Figure;
import rapaio.graphics.base.ImageUtility;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author tutuianu
 */
public class RemotePrinter extends AbstractPrinter {

    private final int port;

    public RemotePrinter() {
        this(2341);
    }

    public RemotePrinter(int port) {
        this.port = port;
    }

    @Override
    public int getTextWidth() {
        try (Socket clientSocket = new Socket("localhost", port);
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
             DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {
            out.writeUTF("info");

            int textWidth = in.readInt();
            int graphicWidth = in.readInt();
            int graphicHeight = in.readInt();

            return textWidth;
        } catch (UnknownHostException ex) {
            Logger.getLogger(RemotePrinter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RemotePrinter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 120;
    }

    @Override
    public int getGraphicWidth() {
        try (Socket clientSocket = new Socket("localhost", port);
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
             DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {
            out.writeUTF("info");

            int textWidth = in.readInt();
            int graphicWidth = in.readInt();
            int graphicHeight = in.readInt();

            return graphicWidth;
        } catch (UnknownHostException ex) {
            Logger.getLogger(RemotePrinter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RemotePrinter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 120;
    }

    @Override
    public int getGraphicHeight() {
        try (Socket clientSocket = new Socket("localhost", port);
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
             DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {
            out.writeUTF("info");

            int textWidth = in.readInt();
            int graphicWidth = in.readInt();
            int graphicHeight = in.readInt();

            return graphicHeight;
        } catch (UnknownHostException ex) {
            Logger.getLogger(RemotePrinter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RemotePrinter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 120;
    }

    @Override
    public void setTextWidth(int chars) {
    }

    @Override
    public void setGraphicWidth(int width) {
    }

    @Override
    public void setGraphicHeight(int height) {
    }

    @Override
    public void print(String message) {
        try {
            sendText(message);
        } catch (IOException ex) {
            Logger.getLogger(RemotePrinter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void error(String message, Throwable throwable) {
        try {
            if (message != null) {
                sendText(message);
            }
            sendText(throwable.toString());
        } catch (IOException ex) {
            Logger.getLogger(RemotePrinter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void draw(Figure figure, int width, int height) {

        BufferedImage newImage = ImageUtility.buildImage(figure, width, height, BufferedImage.TYPE_3BYTE_BGR);
        try (Socket clientSocket = new Socket("localhost", port);
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());) {
            out.writeUTF("image");
            ImageIO.write(newImage, "JPG", out);
            out.flush();
        } catch (UnknownHostException ex) {
            Logger.getLogger(RemotePrinter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RemotePrinter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendText(String message) throws IOException {
        try (Socket clientSocket = new Socket("localhost", port);
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {
            out.writeUTF("text");
            out.writeUTF(message);
            out.flush();
        } catch (UnknownHostException ex) {
            Logger.getLogger(RemotePrinter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RemotePrinter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
