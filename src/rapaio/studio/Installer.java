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
package rapaio.studio;

import java.io.IOException;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import rapaio.studio.server.RapaioNBServer;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        System.out.println("studio restored");
        super.restored();
        try {
            RapaioNBServer.getInstance().shutdown();
            RapaioNBServer.getInstance().start();
        } catch (IOException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void close() {
        System.out.println("studio close");
        super.close();
        try {
            RapaioNBServer.getInstance().shutdown();
        } catch (IOException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
