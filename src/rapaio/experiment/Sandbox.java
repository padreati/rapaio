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

import rapaio.core.correlation.CorrPearson;
import rapaio.core.stat.Mean;
import rapaio.data.*;
import rapaio.data.filter.frame.FFAddIntercept;
import rapaio.graphics.Plotter;
import rapaio.graphics.plot.plotcomp.CorrGram;
import rapaio.ml.regression.Regression;
import rapaio.ml.regression.linear.LinearRegression;
import rapaio.ml.regression.linear.LinearRFit;
import rapaio.io.Csv;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static rapaio.graphics.Plotter.*;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 6/23/17.
 */
public class Sandbox {

    public static void main(String[] args) {
        WS.setPrinter(new IdeaPrinter());

        Frame df = new Csv().read("/home/ati/data/forecast/data.csv");
//        df.printSummary();

        df = df.mapVars(VRange.of(IntStream.range(0, 20).toArray()));


        CorrPearson.from(df).printSummary();

        WS.draw(plot().add(new CorrGram(df)));

        System.exit(0);



        WS.draw(lines(df.getVar(0)));

        System.exit(0);

        List<Var> variables = new ArrayList<>();
        for (Var var : df.varList()) {
            NumericVar mean = NumericVar.empty().withName(var.getName());
            for (int i = 0; i < var.getRowCount(); i+=12) {
                mean.addValue(Mean.from(var.mapRows(Mapping.range(i, i+12))).getValue());
            }
            variables.add(mean);
        }
        Frame result = SolidFrame.byVars(variables);

        new Csv().write(result, "/home/ati/data/forecast/data1h.csv");
    }
}
