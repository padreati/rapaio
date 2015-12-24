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

package rapaio.manual.graphics;

import rapaio.core.distributions.Normal;
import rapaio.data.Frame;
import rapaio.data.Numeric;
import rapaio.datasets.Datasets;
import rapaio.graphics.base.Figure;
import rapaio.graphics.base.ImageUtility;
import rapaio.graphics.plot.Plot;
import rapaio.graphics.plot.plotcomp.DensityLine;
import rapaio.graphics.plot.plotcomp.Histogram;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.stream.DoubleStream;

import static rapaio.graphics.Plotter.*;

public class Graphics {

    public static final String root = "/home/ati/work/rapaio/manual-src/images/";

    public static Frame iris;

    public static void main(String[] args) throws IOException, URISyntaxException {

        WS.setPrinter(new IdeaPrinter().withTextWidth(90));

        iris = Datasets.loadIrisDataset();
        iris.printSummary();

//        overviewCode();
//        boxPlotCode();
//        pointsCode();
//        histogramCode();
        densityLineCode();
    }

    private static void pointsCode() throws IOException {
        Figure fig = points(iris.var("petal-length"), iris.var("petal-width"));
        WS.draw(fig);
        ImageUtility.saveImage(fig, 600, 400, root + "graphics-points-iris-1.png");


        fig = points(iris.var("petal-length"), iris.var("petal-width"), color(iris.var("class")), pch(2))
                .legend(1.5, 2.2, labels("setosa", "versicolor", "virginica"));
        WS.draw(fig);
        ImageUtility.saveImage(fig, 600, 400, root + "graphics-points-iris-2.png");
    }

    private static void boxPlotCode() throws IOException {
        Figure fig = boxPlot(iris.mapVars("0~3"), color(1, 2, 3, 4), alpha(0.5f));
        WS.draw(fig);
        ImageUtility.saveImage(fig, 600, 400, root + "graphics-boxplot-iris-frame.png");

        fig = boxPlot(iris.var("sepal-length"), iris.var("class")).title("sepal-length separation");
        WS.draw(fig);
        ImageUtility.saveImage(fig, 600, 400, root + "graphics-boxplot-iris-sepal-length.png");
    }

    private static void overviewCode() {
        Plot plot = new Plot();
        plot.add(new Histogram(iris.var("sepal-length"), 0, 10, bins(40), color(10), prob(true)));
        plot.add(new DensityLine(iris.var("sepal-length"), lwd(2), color(2)));
        WS.draw(plot);

        WS.draw(hist(iris.var("sepal-length"), 0, 10, bins(40), color(10), prob(true))
                .densityLine(iris.var("sepal-length"), lwd(2), color(2)));
    }

    private static void histogramCode() throws IOException {

        Figure fig = hist(iris.var("sepal-length"));
        WS.draw(fig);
        ImageUtility.saveImage(fig, 600, 400, root + "graphics-hist-iris-1.png");

        fig =
                plot(alpha(0.3f))
                        .hist(iris.var("sepal-length"), 0, 10, bins(40), color(1))
                        .hist(iris.var("petal-length"), 0, 10, bins(40), color(2))
                        .legend(7, 20, labels("sepal-length", "petal-length"), color(1, 2))
                        .xLab("variable");
        WS.draw(plot(alpha(0.3f))
                .hist(iris.var("sepal-length"), 0, 10, bins(40), color(1))
                .hist(iris.var("petal-length"), 0, 10, bins(40), color(2))
                .legend(7, 20, labels("sepal-length", "petal-length"))
                .xLab("variable"));

        ImageUtility.saveImage(fig, 600, 400, root + "graphics-hist-iris-2.png");

    }

    private static void densityLineCode() throws IOException {

        // this is our sample
        Numeric x = Numeric.wrap(-2.1, -1.3, -0.4, 1.9, 5.1, 6.2);

        // declare a bandwidth for smoothing
        double bw = 1.25;

        // build a density line
        Plot p = densityLine(x, bw);

        // for each point draw a normal distribution
        x.stream().forEach(s -> p.funLine(xi -> new Normal(s.value(), bw).pdf(xi) / x.rowCount(), color(1)));
//        WS.draw(p);

        WS.saveImage(p, 600, 400, root + "graphics-kde-1.png");

//        WS.draw(hist(iris.var("sepal-length"), prob(true)).densityLine(iris.var("sepal-length")));
        WS.saveImage(hist(iris.var("sepal-length"), prob(true)).densityLine(iris.var("sepal-length")),
                600, 400, root + "graphics-kde-2.png");

        Plot p2 = plot();
        DoubleStream.iterate(0.05, xi -> xi + 0.02).limit(20)
                .forEach(v -> p2.densityLine(iris.var("sepal-length"), v));
        WS.draw(p2);
        WS.saveImage(p2, 600, 400, root + "graphics-kde-3.png");
    }


}
