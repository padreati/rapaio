/*
 Apache License
 Version 2.0, January 2004
 http://www.apache.org/licenses/

 Copyright 2013 Aurelian Tutuianu

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package rapaio.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import javax.net.ServerSocketFactory;

/**
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class RemoteServerApp {

    public static final int DEFAULT_PORT = 56339;
    private int port = DEFAULT_PORT;

    public static void main(String[] args) throws Exception {

        RemoteServerApp server = new RemoteServerApp();
        server.run();
    }

    private void run() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(port);
        while (true) {
            try (Socket s = serverSocket.accept()) {
                Class<?> cmdClass = new ClassMarshaller().unmarshall(s.getInputStream());
                RapaioCmd cmd = (RapaioCmd)cmdClass.newInstance();
                cmd.run();
            }
        }
    }
}
