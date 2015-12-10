/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.experiment.sandbox;

import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.experiment.grid.MeshGrid1D;
import rapaio.graphics.Plotter;
import rapaio.graphics.opt.ColorGradient;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.plotcomp.MeshContour;
import rapaio.graphics.plot.plotcomp.Points;
import rapaio.printer.IdeaPrinter;

import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static rapaio.graphics.Plotter.lwd;
import static rapaio.sys.WS.draw;
import static rapaio.sys.WS.setPrinter;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/21/15.
 */
@Deprecated
public class ContourSample {

    public static void main(String[] args) {

        setPrinter(new IdeaPrinter(true));

        Frame xy = SolidFrame.wrapOf(
                Numeric.wrap(3, -1, -2).withName("x"),
                Numeric.wrap(3, -1, 6).withName("y")
        );
        xy.printSummary();
        Normal d = new Normal(0, 2);

        BiFunction<Double, Double, Double> bi = (x, y) ->
                IntStream.range(0, 3).mapToDouble(
                        row -> d.pdf(Math.sqrt(Math.pow(x - xy.value(row, "x"), 2) + Math.pow(y - xy.value(row, "y"), 2)))
                ).sum();

        Numeric x = Numeric.seq(-3, 10, 0.05);
        Numeric y = Numeric.seq(-3, 10, 0.05);

        MeshGrid1D mg = new MeshGrid1D(x, y);
        mg.fillWithFunction(bi);

        Plot p = new Plot();
        Var q = Numeric.seq(0, 1, 0.05);
        double[] qq = mg.quantiles(q.stream().mapToDouble().toArray());
        qq[qq.length - 1] = 1;
//        ColorGradient gradient = ColorGradient.newBiColorGradient(
//                new Color(0, 0, 255), new Color(0, 128, 0), q.stream().mapToDouble().toArray());
//
        ColorGradient gradient = ColorGradient.newHueGradient(q.stream().mapToDouble().toArray());

        for (int i = 0; i < q.rowCount() - 1; i++) {
            p.add(new MeshContour(mg.compute(qq[i], qq[i + 1]), true, true,
                    Plotter.color(gradient.getColor(i)), lwd(0.2f)));
        }
        p.add(new Points(xy.var("x"), xy.var("y")));
        draw(p);
    }
}
