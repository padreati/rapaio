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

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Maximum;
import rapaio.core.stat.Minimum;
import rapaio.data.Numeric;
import rapaio.data.Var;
import rapaio.graphics.plot.Plot;
import rapaio.math.linear.RV;
import rapaio.math.linear.dense.SolidRV;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import static rapaio.graphics.Plotter.points;

public class Sandbox {

    public static void main(String[] args) {
//        WS.setPrinter(new IdeaPrinter());

        Normal normal = new Normal();
        RV x = SolidRV.from(100, i -> normal.sampleNext());
        Var xv = Numeric.empty(100);
        for (int i = 0; i < xv.rowCount(); i++) {
            xv.setValue(i, x.get(i));
        }


        x.printSummary();
        xv.printLines();

        WS.draw(points(xv));
    }
}
