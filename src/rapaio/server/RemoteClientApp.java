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

import java.io.IOException;
import java.net.Socket;
import rapaio.data.Frame;
import rapaio.workspace.Workspace;

/**
 *
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */


public class RemoteClientApp implements RapaioCmd{

    public static void main(String[] args) throws IOException {
        
    }
    
    public void runRemote() throws IOException {
        try (Socket s = new Socket("localhost", RemoteServerApp.DEFAULT_PORT)) {
            new ClassMarshaller().marshall(this.getClass(), s.getOutputStream());
        }
    }

    @Override
    public void run() {
//        Workspace.getData().putFrame("a", new SolidFrame(0, new Vector[0], new String[0]));
        Frame df = Workspace.getData().getFrame("a");
        System.out.println(df.getRowCount());
    }
}
