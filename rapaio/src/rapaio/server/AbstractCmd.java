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
package rapaio.server;

import rapaio.printer.LocalPrinter;
import rapaio.printer.StandardPrinter;
import rapaio.workspace.Workspace;

import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public abstract class AbstractCmd implements RapaioCmd{
    
    public static final int DEFAULT_PORT = 56339;

    @Override
    public void runRemote() {
        try (Socket s = new Socket("localhost", DEFAULT_PORT)) {
            new ClassMarshaller().marshallRemote(s.getOutputStream(), this.getClass());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void runConsole() {
        Workspace.setPrinter(new StandardPrinter());
        run();
    }

    @Override
    public void runLocal() {
        Workspace.setPrinter(new LocalPrinter());
        run();
    }
}
