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


import rcaller.RCaller;
import rcaller.RCode;

import java.io.IOException;

/**
 * @author <a href="mailto:padreati@yahoo.com>Aurelian Tutuianu</a>
 */
@Deprecated
public class RJavaTest {

    public static void main(String[] args) throws IOException {

        RCaller caller = new RCaller();
        RCode code = new RCode();
        double[] xvector = new double[]{1, 3, 5, 3, 2, 4};
        double[] yvector = new double[]{6, 7, 5, 6, 5, 6};

        caller.setRscriptExecutable("/usr/bin/Rscript");

        code.addDoubleArray("X", xvector);
        code.addDoubleArray("Y", yvector);
        code.addRCode("ols <- lm ( Y ~ X )");

        caller.setRCode(code);

        caller.runAndReturnResult("ols");

        double[] residuals = caller.getParser().getAsDoubleArray("residuals");
        for (int i = 0; i < residuals.length; i++) {
            System.out.print(residuals[i] + " ");
        }
    }
}
