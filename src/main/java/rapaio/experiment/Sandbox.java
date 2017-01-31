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

import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.filter.Filters;
import rapaio.graphics.Plotter;
import rapaio.graphics.plot.Plot;
import rapaio.io.Csv;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static rapaio.graphics.Plotter.*;

public class Sandbox {

    public static void main(String[] args) {
        Frame df = new Csv()
                .withNAValues("n/a")
                .withQuotes(true)
                .read("/home/ati/data/rundoc-perf-all.csv");
        df.printSummary();

        df = Filters.refSort(df, df.var("asgarn").refComparator());

        df.var("fmid").printSummary();

        WS.setPrinter(new IdeaPrinter());

        Var x = Numeric.seq(0, 100, 1).withName("bin");
        Frame a = SolidFrame.byVars(x);


        Var asgLevel = df.stream().filter(s -> s.label("fmid").equals("SegmentedARwAsgClm(14,14)"))
                .toMappedFrame().var("avgcpuutil2days");
        Var hostLevel = df.stream().filter(s -> s.label("fmid").equals("SegmentedARwHostClm(14,7)"))
                .toMappedFrame().var("avgcpuutil2days");

        WS.draw(hist(asgLevel, 0, 20, bins(100), alpha(0.5f), color(1))
                .hist(hostLevel, 0, 20, bins(100), alpha(0.5f), color(2)));
        System.exit(0);

        List<Var> ys = new ArrayList<>();

        List<String> models = df.var("fmid").streamLevels().skip(1).collect(Collectors.toList());
        for (String model : models) {

            Numeric y = Numeric.fill(x.rowCount(), 0).withName(model);
            df.stream().filter(s -> s.label("fmid").equals(model)).toMappedFrame()
                    .var("avgcpuutil2days").stream()
                    .mapToDouble().forEach(v -> {
                int index = (int) Math.rint(v / 1);
                y.setValue(index, y.value(index) + 1);
            });
            ys.add(y);

            a = a.bindVars(y);
        }

        a = SolidFrame.byVars(
                a.var("bin"),
                a.var("Benchmark1Day"),
                a.var("HourlyARwClm(14,7)"),
                a.var("SegmentedARwHostClm(14,7)"),
                a.var("GlobalARwAsgClm(14,14)"),
                a.var("SegmentedARwAsgClm(14,14)"));

        a.printLines();

        Plot p = plot();
        for (int i = 0; i < models.size(); i++) {
            p.lines(x, ys.get(i), color(i+1));
        }
        WS.draw(p);
    }
}
