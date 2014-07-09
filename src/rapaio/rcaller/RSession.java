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

package rapaio.rcaller;

import rcaller.Globals;
import rcaller.RCaller;
import rcaller.RCode;
import rcaller.ROutputParser;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public class RSession {
    private final RCode code;
    private final RCaller caller;

    public RSession() {
        code = new RCode();
        caller = new RCaller();

        caller.setRCode(code);
        caller.setRExecutable(Globals.R_Linux);
        caller.setRscriptExecutable(Globals.RScript_Linux);
    }

    public void clear() {
        code.clearOnline();
    }

    public RCode code() {
        return code;
    }

    public RCaller caller() {
        return caller;
    }

    public File startPlot() throws IOException {
        return code.startPlot();
    }

    public void endPlot() {
        code.endPlot();
    }

    public void run(String ret) {
        caller.runAndReturnResultOnline(ret);
    }

    public void stop() {
        caller.deleteTempFiles();
        caller.stopStreamConsumers();
        caller.StopRCallerOnline();
    }

    public void runOnly() {
        caller.runOnly();
    }

    public void showPlot(File file) {
        code.showPlot(file);
    }

    public ROutputParser parser() {
        return caller.getParser();
    }
}
