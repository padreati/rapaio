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

package rapaio.tutorial.pages;

import rapaio.graphics.Plotter;
import rapaio.sys.WS;
import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Maximum;
import rapaio.core.stat.Minimum;
import rapaio.data.*;
import rapaio.data.Frame;
import rapaio.experiment.grid.MeshGrid1D;
import rapaio.datasets.Datasets;
import rapaio.graphics.opt.ColorGradient;
import rapaio.graphics.plot.Plot;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.classifier.CFit;
import rapaio.experiment.classifier.svm.BinarySMO;
import rapaio.experiment.classifier.svm.kernel.PolyKernel;
import rapaio.experiment.classifier.svm.kernel.WaveletKernel;
import rapaio.ml.eval.ConfusionMatrix;
import rapaio.ws.Summary;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.function.BiFunction;
import java.util.stream.IntStream;

import static rapaio.sys.WS.*;
import static rapaio.graphics.Plotter.plot;
import static rapaio.graphics.Plotter.points;
import static rapaio.graphics.Plotter.color;
import static rapaio.graphics.Plotter.lwd;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/28/15.
 */
@Deprecated
public class MeshGridTutorialPage implements TutorialPage {

    @Override
    public String getPageName() {
        return "MeshGridTutorialPage";
    }

    @Override
    public String getPageTitle() {
        return "Mesh Grid Tutorial Page";
    }

    @Override
    public void render() throws IOException, URISyntaxException {

        heading(3, "Introduction");

        p("In this tutorial page I will present a nice graphical tool which can be used to draw " +
                "2D surfaces in plane. ");

        p("For now is implemented only MeshContour, known also as contour curves. In cartography, a contour line " +
                "joins points of equal elevation (height) above a given level, such as mean sea level. A contour " +
                "map is a map illustrated with contour lines, for example a topographic map, which thus shows " +
                "valleys and hills, and the steepness of slopes. ");

        p("While I was working to find a way to plot contour lines and contour areas, I tried various thing and " +
                "through iterative approaches I found an algorithm which I hoped that I discovered. In the end, " +
                "after additional research I found (it happen again and again) that there is already an algorithm " +
                "which does the same thing in a striking similar way. This algorithm is called Marching Squares. ");

        p("The idea behind of the algorithm is to build a rectangular grid area with values computed for each " +
                "point of the grid. I named this object a MeshGrid. A MeshGrid is used to describe the behaviour " +
                "of a function against some conditions which are used to define the contour lines. ");

        p("The simplest MeshGrid can be computed from a grid of function values (which are scalar values in this " +
                "case), and a threshold value used as the elevation of the contour plot. If the grid points are " +
                "very dense, then a linear interpolation found for each square of the grid will produce a smooth " +
                "looking curve. ");

        p("The idea behind a contour map can be extended to any condition applied to the function values. " +
                "We can think of a MeshGrid with multi-dimensional vectors and conditions could be functions " +
                "applied on multi-dimensional values. ");

        heading(3, "A simple example: simulation of a kernel density in 2 dimensions");

        p("Kernel density estimation is a procedure which enables one to estimate a probability density using only " +
                "information from a sample of values. Usually a continuous probability density is a smooth curve, " +
                "but if one has only a hand of sample values it is clear that it is difficult to estimate a " +
                "continuous density with few discrete values. What kernel density estimation does is to 'spread' " +
                "the probability mass from each sample value in its neighbourhood. In the end, if the local " +
                "influences are summed up, a smooth density function appears. ");

        p("For a uni-variate sample variable DensityLine utility can be used together with KDE objects to build " +
                "some graphs. The question is: how can this be done in 2 dimensions? ");

        p("Well, all the kernel densities I used have the nice property that they are symmetric. Thus been said, we " +
                "note that the 'spreading' of probability will be the same in any direction, for all the points at " +
                "the same euclidean distance. ");

        p("Suppose our sample has only 3 points. These 3 points are \\( A=(3,3), B=(-1,-1), C=(-2,6)\\). These points " +
                "were chosen at random, they does not have any special meaning. ");

        p("We will 'spread' probability mass around these points according with a normal distribution with standard " +
                "deviation equals 2. ");


        Frame xy = SolidFrame.newWrapOf(
                Numeric.newWrapOf(3, -1, -2).withName("x"),
                Numeric.newWrapOf(3, -1, 6).withName("y")
        );
        Normal d = new Normal(0, 2);

        code("        Frame xy = SolidFrame.newWrapOf(\n" +
                "                Numeric.newWrapOf(3, -1, -2).withName(\"x\"),\n" +
                "                Numeric.newWrapOf(3, -1, 6).withName(\"y\")\n" +
                "        );\n" +
                "        Normal d = new Normal(0, 2);\n");

        p("We build a mesh grid of 1-dimensional values. In order to do so, we first build two vectors with " +
                "coordinates of the mesh grid. Using those vectors, we later build a MeshGrid1D object, which " +
                "is filled with the values of a given spread function. The spread function sums the values " +
                "of each 3 normal densities (one from each sample point), density which is computed on " +
                "the euclidean distance from the current point and each sample point. ");

        Numeric x = Numeric.newSeq(-3, 10, 0.05);
        Numeric y = Numeric.newSeq(-3, 10, 0.05);
        MeshGrid1D mg = new MeshGrid1D(x, y);
        BiFunction<Double, Double, Double> bi = (xx, yy) ->
                IntStream.range(0, 3).mapToDouble(
                        row -> d.pdf(Math.sqrt(Math.pow(xx - xy.value(row, "x"), 2) + Math.pow(yy - xy.value(row, "y"), 2)))
                ).sum();

        mg.fillWithFunction(bi);

        code("        Numeric x = Numeric.newSeq(-3, 10, 0.05);\n" +
                "        Numeric y = Numeric.newSeq(-3, 10, 0.05);\n" +
                "        MeshGrid1D mg = new MeshGrid1D(x, y);\n" +
                "        BiFunction<Double, Double, Double> bi = (xx, yy) ->\n" +
                "                IntStream.range(0, 3).mapToDouble(\n" +
                "                        row -> d.pdf(Math.sqrt(Math.pow(xx - xy.value(row, \"x\"), 2) + " +
                "Math.pow(yy - xy.value(row, \"y\"), 2)))\n" +
                "                ).sum();\n" +
                "        mg.fillWithFunction(bi);\n");

        p("Now that we have a mash grid we draw a plot with one iso band for each quantile interval of with 0.1. ");

        Plot p = plot();
        Var q = Numeric.newSeq(0, 1, 0.1);
        double[] qq = mg.quantiles(q.stream().mapToDouble().toArray());
        qq[qq.length - 1] = Double.POSITIVE_INFINITY;
        ColorGradient gradient = ColorGradient.newBiColorGradient(new Color(0, 0, 200), new Color(255, 255, 255), q.stream().mapToDouble().toArray());

        for (int i = 0; i < q.rowCount() - 1; i++) {
            p.meshContour(mg.compute(qq[i], qq[i + 1]), true, true, Plotter.color(gradient.getColor(i)), lwd(0.2f));
        }
        p.points(xy.var("x"), xy.var("y"));
        draw(p, 600, 400);

        code("        Plot p = plot();\n" +
                "        Var q = Numeric.newSeq(0, 1, 0.1);\n" +
                "        double[] qq = mg.quantiles(q.stream().mapToDouble().toArray());\n" +
                "        qq[qq.length - 1] = Double.POSITIVE_INFINITY;\n" +
                "        ColorGradient gradient = ColorGradient.newBiColorGradient(new Color(0, 0, 200), new Color(255, 255, 255), q.stream().mapToDouble().toArray());\n" +
                "\n" +
                "        for (int i = 0; i < q.rowCount() - 1; i++) {\n" +
                "            p.meshContour(mg.compute(qq[i], qq[i + 1]), true, true, color(gradient.getColor(i)), lwd(0.2f));\n" +
                "        }\n" +
                "        p.add(new Points(xy.var(\"x\"), xy.var(\"y\")));\n" +
                "        draw(p, 600, 400);");

        heading(3, "A more complete example: iris classification");

        p("We will use again the classical UCI data set to build an example. " +
                "Because SVM algorithm that is implemented work only for " +
                "binary classification case, I will truncate the iris data set. ");

        p("Because petal variables separates very well all the case, I will use sepal " +
                "variables, to make a little harder the problem. I will also filter " +
                "our cases for the third class. ");

        Frame iris = Datasets.loadIrisDataset();
        iris = iris.mapVars("sepal-length,sepal-width,class");
        iris = iris.stream().filter(s -> s.index(2) != 3).toMappedFrame();

        Var trimmedClass = Nominal.newEmpty().withName("class");
        iris.var("class").stream().forEach(s -> trimmedClass.addLabel(s.label()));

        iris = BoundFrame.newByVars(iris.var(0), iris.var(1), trimmedClass).solidCopy();

        Summary.printSummary(iris);

        draw(points(iris.var(0), iris.var(1), Plotter.color(iris.var(2))));

        code("        Frame iris = Datasets.loadIrisDataset();\n" +
                "        iris = iris.mapVars(\"sepal-length,sepal-width,class\");\n" +
                "        iris = iris.stream().filter(s -> s.index(2) != 3).toMappedFrame();\n" +
                "\n" +
                "        Var trimmedClass = Nominal.newEmpty().withName(\"class\");\n" +
                "        iris.var(\"class\").stream().forEach(s -> trimmedClass.addLabel(s.label()));\n" +
                "\n" +
                "        iris = BoundFrame.newByVars(iris.var(0), iris.var(1), trimmedClass).copy();\n" +
                "\n" +
                "        Summary.printSummary(iris);\n");

        p("Now we will build a SMO classifier, with a poly kernel of degree 2, we learn the train set " +
                "and fit the trained SMO to the same train set to see how it works. ");

        BinarySMO smo = new BinarySMO().withKernel(new PolyKernel(2));
        smo.learn(iris, "class");
        CFit cr = smo.fit(iris);
        new ConfusionMatrix(iris.var("class"), cr.firstClasses()).printSummary();

        code("        BinarySMO smo = new BinarySMO().withKernel(new PolyKernel(2));\n" +
                "        smo.learn(iris, \"class\");\n" +
                "        CFit cr = smo.predict(iris);\n" +
                "        new ConfusionMatrix(iris.var(\"class\"), cr.firstClasses()).printSummary();\n");

        p("It looks like there is no error there. However it is legitimate to ask yourself " +
                "how it looks the decision function. One possibility is with iso lines. ");

        x = Numeric.newSeq(4, 7, 0.1).withName("sepal-length");
        y = Numeric.newSeq(2, 5, 0.1).withName("sepal-width");
        MeshGrid1D mg1 = new MeshGrid1D(x, y);

        // build a classification data sets with all required points

        Numeric sl = Numeric.newEmpty().withName("sepal-length");
        Numeric sw = Numeric.newEmpty().withName("sepal-width");
        for (int i = 0; i < x.rowCount(); i++) {
            for (int j = 0; j < y.rowCount(); j++) {
                sl.addValue(mg1.getX().value(i));
                sw.addValue(mg1.getY().value(j));
            }
        }
        CFit cr2 = smo.fit(SolidFrame.newWrapOf(sl, sw));
        int pos = 0;
        for (int i = 0; i < x.rowCount(); i++) {
            for (int j = 0; j < y.rowCount(); j++) {
                mg1.setValue(i, j, cr2.firstDensity().value(pos, 1) - cr2.firstDensity().value(pos, 2));
                pos++;
            }
        }

        p = new Plot();
        qq = mg1.quantiles(Numeric.newSeq(0, 1, 0.1).stream().mapToDouble().toArray());
        gradient = ColorGradient.newBiColorGradient(new Color(0, 0, 200), new Color(255, 255, 255), Numeric.newSeq(0, 1, 0.1).stream().mapToDouble().toArray());
        for (int i = 0; i < qq.length - 1; i++) {
            p.meshContour(mg1.compute(qq[i], qq[i + 1]), true, true, lwd(0.3f), Plotter.color(gradient.getColor(i)));
        }
        p.meshContour(mg1.compute(0, Double.POSITIVE_INFINITY), true, false, lwd(1.2f));
        p.points(iris.var(0), iris.var(1), Plotter.color(iris.var(2)));

        draw(p, 600, 400);

        p("The code to generate this is now a little bit tedious. Later on, after some good practice rules " +
                "emerge from usage scenarios, this kind of procedures will be encapsulated into " +
                "a more complex procedure with simple and fast syntax. ");

        heading(3, "Last case: an interesting decision surface");

        p("I generated a data set consisting on some binary labeled points placed " +
                "randomly on a chess board. The points are labels according with the " +
                "color of the square which contains it. I used this in order to plot " +
                "a nice example of drawing in interesting surface. ");

        // build the data set

        RandomSource.setSeed(1);

        Var v1 = Numeric.newEmpty().withName("x");
        Var v2 = Numeric.newEmpty().withName("y");
        Var v3 = Nominal.newEmpty().withName("class");

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 100; k++) {
                    v1.addValue(RandomSource.nextDouble() + i);
                    v2.addValue(RandomSource.nextDouble() + j);
                    v3.addLabel((i + j) % 2 == 0 ? "True" : "False");
                }
            }
        }

        Frame df = SolidFrame.newWrapOf(v1, v2, v3);

        // build classifier

        Classifier c = new BinarySMO().withKernel(new WaveletKernel(0.55)).withC(0.1);
        c.learn(df, "class");

        new ConfusionMatrix(df.var("class"), c.fit(df).firstClasses()).printSummary();

        // new build the mesh grid

        x = Numeric.newSeq(new Minimum(df.var(0)).value() - 1, new Maximum(df.var(0)).value() + 1, 0.05).withName("x");
        y = Numeric.newSeq(new Minimum(df.var(1)).value() - 1, new Maximum(df.var(1)).value() + 1, 0.05).withName("y");

        mg = new MeshGrid1D(x, y);

        Var x1 = Numeric.newEmpty().withName("x");
        Var y1 = Numeric.newEmpty().withName("y");

        for (int i = 0; i < x.rowCount(); i++) {
            for (int j = 0; j < y.rowCount(); j++) {
                x1.addValue(x.value(i));
                y1.addValue(y.value(j));
            }
        }

        // fit the mesh grid values

        Frame grid = SolidFrame.newWrapOf(x1, y1);
        cr = c.fit(grid, true, true);

        pos = 0;
        for (int i = 0; i < x.rowCount(); i++) {
            for (int j = 0; j < y.rowCount(); j++) {
                mg.setValue(i, j, cr.firstDensity().value(pos, 1) - cr.firstDensity().value(pos, 2));
                pos++;
            }
        }

        // and finally plot the results

        p = new Plot();
        double[] pp = new double[]{0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.8, 1};
        qq = mg.quantiles(pp);
        gradient = ColorGradient.newBiColorGradient(new Color(84, 112, 240), new Color(255, 255, 255), pp);
        for (int i = 0; i < qq.length - 1; i++) {
            p.meshContour(mg.compute(qq[i], qq[i + 1]), true, true, Plotter.color(gradient.getColor(i)), lwd(0.2f));
        }
        p.meshContour(mg.compute(0, Double.POSITIVE_INFINITY), true, false, Plotter.color(0));

        WS.draw(p.points(df.var(0), df.var(1), Plotter.color(df.var(2))), 800, 600);


        p("And the code which produced the last graph.");

        code("        // build the data set\n" +
                "        \n" +
                "        RandomSource.setSeed(1);\n" +
                "\n" +
                "        Var v1 = Numeric.newEmpty().withName(\"x\");\n" +
                "        Var v2 = Numeric.newEmpty().withName(\"y\");\n" +
                "        Var v3 = Nominal.newEmpty().withName(\"class\");\n" +
                "\n" +
                "        for (int i = 0; i < 4; i++) {\n" +
                "            for (int j = 0; j < 4; j++) {\n" +
                "                for (int k = 0; k < 100; k++) {\n" +
                "                    v1.addValue(RandomSource.nextDouble() + i);\n" +
                "                    v2.addValue(RandomSource.nextDouble() + j);\n" +
                "                    v3.addLabel((i + j) % 2 == 0 ? \"True\" : \"False\");\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "\n" +
                "        Frame df = SolidFrame.newWrapOf(v1, v2, v3);\n" +
                "        \n" +
                "        // build classifier \n" +
                "        \n" +
                "        Classifier c = new BinarySMO().withKernel(new WaveletKernel(0.55)).withC(0.1);\n" +
                "        c.learn(df, \"class\");\n" +
                "\n" +
                "        new ConfusionMatrix(df.var(\"class\"), c.predict(df).firstClasses()).printSummary();\n" +
                "\n" +
                "        // new build the mesh grid\n" +
                "        \n" +
                "        x = Numeric.newSeq(new Minimum(df.var(0)).value() - 1, new Maximum(df.var(0)).value() + 1, 0.05).withName(\"x\");\n" +
                "        y = Numeric.newSeq(new Minimum(df.var(1)).value() - 1, new Maximum(df.var(1)).value() + 1, 0.05).withName(\"y\");\n" +
                "\n" +
                "        mg = new MeshGrid1D(x, y);\n" +
                "\n" +
                "        Var x1 = Numeric.newEmpty().withName(\"x\");\n" +
                "        Var y1 = Numeric.newEmpty().withName(\"y\");\n" +
                "\n" +
                "        for (int i = 0; i < x.rowCount(); i++) {\n" +
                "            for (int j = 0; j < y.rowCount(); j++) {\n" +
                "                x1.addValue(x.value(i));\n" +
                "                y1.addValue(y.value(j));\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        // fit the mesh grid values\n" +
                "\n" +
                "        Frame grid = SolidFrame.newWrapOf(x1, y1);\n" +
                "        cr = c.predict(grid, true, true);\n" +
                "\n" +
                "        pos = 0;\n" +
                "        for (int i = 0; i < x.rowCount(); i++) {\n" +
                "            for (int j = 0; j < y.rowCount(); j++) {\n" +
                "                mg.setValue(i, j, cr.firstDensity().value(pos, 1) - cr.firstDensity().value(pos, 2));\n" +
                "                pos++;\n" +
                "            }\n" +
                "        }\n" +
                "        \n" +
                "        // and finally plot the results\n" +
                "\n" +
                "        p = new Plot();\n" +
                "        double[] pp = new double[]{0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.8, 1};\n" +
                "        qq = mg.quantiles(pp);\n" +
                "        gradient = ColorGradient.newBiColorGradient(new Color(84, 112, 240), new Color(255, 255, 255), pp);\n" +
                "        for (int i = 0; i < qq.length - 1; i++) {\n" +
                "            p.add(new MeshContour(mg.compute(qq[i], qq[i + 1]), true, true).color(gradient.getColor(i)).lwd(0.2f));\n" +
                "        }\n" +
                "        p.add(new MeshContour(mg.compute(0, Double.POSITIVE_INFINITY), true, false).color(0));\n" +
                "\n" +
                "        WS.draw(p.add(new Points(df.var(0), df.var(1)).color(df.var(2))), 800, 600);\n");


        p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");
    }
}
