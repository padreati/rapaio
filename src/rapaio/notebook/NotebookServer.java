/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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
 *
 */

package rapaio.notebook;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import rapaio.notebook.state.NotebookConfig;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 7/13/15.
 */
public class NotebookServer {

    private static NotebookServer instance;

    private Server server;
    private NotebookConfig config;

    public static NotebookServer getInstance() {
        return instance;
    }

    public static void main(String[] args) throws Exception {
        instance = new NotebookServer();
        instance.run();
    }

    public void run() throws Exception {

        server = new Server(9908);
        config = new NotebookConfig();
        WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
//        webapp.setWar(NotebookServer.class.getClassLoader().getResource("rapaio/notebook/web").toExternalForm());
        webapp.setResourceBase(NotebookServer.class.getClassLoader().getResource("rapaio/notebook/web").toExternalForm());
        webapp.setDescriptor(NotebookServer.class.getClassLoader().getResource("rapaio/notebook/web/WEB-INF/web.xml").toExternalForm());

        server.setHandler(webapp);
        server.start();
        server.join();
    }

    public Server getServer() {
        return server;
    }

    public NotebookConfig getConfig() {
        return config;
    }
}
