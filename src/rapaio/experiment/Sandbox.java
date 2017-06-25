/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
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

import rapaio.data.Frame;
import rapaio.data.NumericVar;
import rapaio.data.SolidFrame;
import rapaio.data.filter.frame.FFAddIntercept;
import rapaio.ml.regression.linear.LinearRegression;
import rapaio.ml.regression.linear.LinearRFit;
import rapaio.io.Csv;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import static rapaio.graphics.Plotter.color;
import static rapaio.graphics.Plotter.points;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/23/17.
 */
public class Sandbox {

    public static void main(String[] args) {

        Frame dftr = new Csv().read("/home/ati/data/estimator/download-transmit.csv");
        Frame dfcpu = new Csv().read("/home/ati/data/estimator/download-cpu.csv");

        Frame df = SolidFrame.byVars(dftr.getVar(1).withName("tb"), dfcpu.getVar(1).withName("cpu"));
        df.printSummary();

        WS.setPrinter(new IdeaPrinter());


        LinearRegression lm = (LinearRegression) new LinearRegression().withInputFilters(FFAddIntercept.filter());
        lm.train(df, "tb");
        LinearRFit fit = (LinearRFit)lm.fit(df, true);

        NumericVar cpux = NumericVar.empty();
        NumericVar tbx = NumericVar.empty();

        cpux.addValue(80);
        tbx.addValue(lm.firstCoeff().get(0) + lm.firstCoeff().get(1)*80);

        WS.draw(points(df.getVar(1), df.getVar(0))
                .lines(df.getVar(1), fit.firstFit(), color(1))
//                .points(cpux, tbx)
        );

        SolidFrame.byVars(cpux, tbx).printLines();
        lm.firstCoeff().printSummary();
    }
}
