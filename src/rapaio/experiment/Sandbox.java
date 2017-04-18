/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
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

package rapaio.experiment;

import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

public class Sandbox {

    public static void main(String[] args) {
        WS.setPrinter(new IdeaPrinter());

        double eps = 1 / 16.;
        for (int i = 1; i < 1000; i++) {
            double delta = Math.abs(f(i) - 3.);
            WS.printf("%d : delta: %.6f\n", i, delta);
            if (delta < eps) {
                break;
            }
        }

    }

    static double f(double n) {
        return (6 * n + 11) / (2 * n + 6.0);
    }
}
