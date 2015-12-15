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

import rapaio.data.Frame;
import rapaio.datasets.Datasets;
import rapaio.graphics.base.Figure;
import rapaio.graphics.base.ImageUtility;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.graphics.Plotter.*;

public class Graphics {

    public static final String root = "/home/ati/work/rapaio/manual/images/";

    public static void main(String[] args) throws IOException, URISyntaxException {

        WS.setPrinter(new IdeaPrinter().withTextWidth(90));

        Frame iris = Datasets.loadIrisDataset();
        iris.printSummary();

        Figure fig = null;

//        Figure fig = boxPlot(iris.mapVars("0~3"), color(1, 2, 3, 4), alpha(0.5f));
//        WS.draw(fig);
//        ImageUtility.saveImage(fig, 600, 400, root + "graphics-boxplot-iris-frame.png");

//        fig = boxPlot(iris.var("sepal-length"), iris.var("class")).title("sepal-length separation");
//        WS.draw(fig);
//        ImageUtility.saveImage(fig, 600, 400, root + "graphics-boxplot-iris-sepal-length.png");


//        fig = points(iris.var("petal-length"), iris.var("petal-width"));
//        WS.draw(fig);
//        ImageUtility.saveImage(fig, 600, 400, root + "graphics-points-iris-1.png");


//        fig = points(iris.var("petal-length"), iris.var("petal-width"), color(iris.var("class")), pch(2))
//                .legend(1.5, 2.2, labels("setosa", "versicolor", "virginica"));
//        WS.draw(fig);
//        ImageUtility.saveImage(fig, 600, 400, root + "graphics-points-iris-2.png");


        fig = hist(iris.var("sepal-length"));
        WS.draw(fig);
        ImageUtility.saveImage(fig, 600, 400, root + "graphics-hist-iris-1.png");

        fig =
                plot(alpha(0.3f))
                        .hist(iris.var("sepal-length"), 0, 10, bins(40), color(1))
                        .hist(iris.var("petal-length"), 0, 10, bins(40), color(2))
                        .legend(7, 20, labels("sepal-length", "petal-length"), color(1, 2));
        WS.draw(plot(alpha(0.3f))
                .hist(iris.var("sepal-length"), 0, 10, bins(40), color(1))
                .hist(iris.var("petal-length"), 0, 10, bins(40), color(2))
                .legend(7, 20, labels("sepal-length", "petal-length"), color(1, 2)));

        ImageUtility.saveImage(fig, 600, 400, root + "graphics-hist-iris-2.png");

    }
}
