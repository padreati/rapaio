/*
 * Copyright 2013 Aurelian Tutuianu
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

    private ServerSocket server;
    private WaitingThread wait;

    public void start() throws IOException {
        server = new ServerSocket(2341);
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
                     DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

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