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
 */

package rapaio.tutorial.pages;

import rapaio.core.RandomSource;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.core.tests.KSTest;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.filter.var.VFJitter;
import rapaio.datasets.Datasets;
import rapaio.graphics.GridLayer;
import rapaio.graphics.Plot;
import rapaio.graphics.QQPlot;
import rapaio.graphics.plot.ABLine;
import rapaio.graphics.plot.DensityLine;
import rapaio.graphics.plot.Histogram;
import rapaio.graphics.plot.Points;
import rapaio.ws.Summary;

import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.WS.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class IrisExplorePage implements TutorialPage {

    @Override
    public String getPageName() {
        return "IrisExplorationPage";
    }

    @Override
    public String getPageTitle() {
        return "Iris Exploration";
    }

    @Override
    public void render() throws IOException, URISyntaxException {

        RandomSource.setSeed(1);

        heading(1, "Iris sample exploratory analysis");

        p("The aim of this tutorial is to show Rapaio capabilities for data exploration.");

        heading(3, "Data used");

        p("Data used in this exploratory study is a well-known data set made by " +
                "Sir Ronald Fisher containing measurements about three Iris species. " +
                "This data set is bundled in rapaio library and the most simple " +
                "way to obtain the data set is through Datasets utility class. ");

        code("        Frame df = Datasets.loadIrisDataset();\n");

        Frame df = Datasets.loadIrisDataset();

        p("The names of the measurements included in this data set are:");

        code("        Summary.names(df);\n");

        Summary.names(df);

        p("Measurements include length and width of sepal and petal species. " +
                "Below is a summary of the data set in question.");

        code("        Summary.summary(df);\n");

        Summary.summary(df);

        heading(3, "Distributions with histograms and density lines");

        p("Now we will take a look over the distribution of first numeric measurements. " +
                "We will draw a grid plot with all histograms for all the four " +
                "variables. ");

        code("        Grid grid = new Grid(2, 2);\n" +
                "        for (int i = 0; i < 2; i++) {\n" +
                "            for (int j = 0; j < 2; j++) {\n" +
                "                grid.add(i + 1, j + 1, new Plot()\n" +
                "                        .add(new Histogram(df.var(i * 2 + j)).bins(10)));\n" +
                "            }\n" +
                "        }\n" +
                "        draw(grid, 600, 400);\n");

        GridLayer grid = new GridLayer(2, 2);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                grid.add(i + 1, j + 1, new Plot()
                        .add(new Histogram(df.var(i * 2 + j)).bins(10)));
            }
        }
        draw(grid, 600, 400);

        p("An alternative graph which can describe the distribution of the values " +
                "for each numeric measurement is the density graph. We will draw a grid " +
                "with kernel density estimation lines, one for each numerical variable. ");

        code("        grid = new Grid(2, 2);\n" +
                "        for (int i = 0; i < 2; i++) {\n" +
                "            for (int j = 0; j < 2; j++) {\n" +
                "                grid.add(i + 1, j + 1, new Plot()\n" +
                "                        .add(new DensityLine(df.var(i * 2 + j))));\n" +
                "            }\n" +
                "        }\n" +
                "        draw(grid, 600, 400);\n");

        grid = new GridLayer(2, 2);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                grid.add(i + 1, j + 1, new Plot()
                        .add(new DensityLine(df.var(i * 2 + j))));
            }
        }
        draw(grid, 600, 400);

        heading(3, "Distribution of each class on each numeric dimension");

        p("We can see that for petal-length and petal-width there are two well-separated modes. " +
                "We can hope that these grouping is useful also to separate classes, so " +
                "we can draw one density line for each class, on each numerical dimension. " +
                "Let's see how these things looks like. ");

        code("        grid = new Grid(2, 2);\n" +
                "        for (int i = 0; i < 2; i++) {\n" +
                "            for (int j = 0; j < 2; j++) {\n" +
                "                Plot p = new Plot();\n" +
                "                for (int k = 0; k < 3; k++) {\n" +
                "                    int kk = k;\n" +
                "                    p.add(new DensityLine(df.stream().filter(s -> s.index(\"class\") == kk + 1).toMappedFrame().var(i * 2 + j)).color(kk + 1));\n" +
                "                }\n" +
                "                grid.add(i + 1, j + 1, p);\n" +
                "            }\n" +
                "        }\n" +
                "        draw(grid, 600, 400);\n");

        grid = new GridLayer(2, 2);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                Plot p = new Plot();
                for (int k = 0; k < 3; k++) {
                    int kk = k;
                    p.add(new DensityLine(df.stream().filter(s -> s.index("class") == kk + 1).toMappedFrame().var(i * 2 + j)).color(kk + 1))
                            .xLab(df.varNames()[i * 2 + j]);
                }
                grid.add(i + 1, j + 1, p);
            }
        }
        draw(grid, 600, 400);

        p("Indeed we can see from the figure above that both petal variables " +
                "are very good discriminant between iris species. At least, it is obvious that " +
                "one of the species is very well separated. In order to clarify this we can " +
                "go further and see a plot with these two features, where color is used to signal " +
                "the iris class. ");

        draw(new Plot()
                        .add(new Points(
                                new VFJitter(0.1).fitApply(df.var(2)),
                                new VFJitter(0.1).fitApply(df.var(3)))
                                .color(df.var("class"))
                                .pch(1))
                        .title("Iris data points colored by species"),
                600, 350
        );
        p("" +
                "We can notice the red points clustered towards the bottom-left corner " +
                "of the plot. The other types of points (the blue and the green ones) are " +
                "separated also, but there is no clear border between them. " +
                "The blue and the green groups does not looks like a " +
                "linear separable groups, on the two dimensions used: petal-length " +
                "and petal-width.");

        heading(3, "Some notes on sepal-width feature");

        p("Sepal width looks like it is symmetrically distributed. " +
                "We will draw a qq-plot to see how those values are distributed on a normal curve. ");
        p("We estimate first the mean and the variance sepal-width sample values. ");

        code("        Var sw = df.var(\"sepal-width\");\n" +
                "        new Mean(sw).summary();\n" +
                "        new Variance(sw).summary();\n" +
                "\n" +
                "        draw(new QQPlot()\n" +
                "                        .add(df.var(\"sepal-width\"), new Normal(new Mean(sw).value(), Math.sqrt(new Variance(sw).value())))\n" +
                "                        .add(new ABLine(1, 0).color(Color.GRAY))\n" +
                "        );\n");

        Var sw = df.var("sepal-width");
        new Mean(sw).summary();
        new Variance(sw).summary();

        draw(new QQPlot()
                        .add(df.var("sepal-width"), new Normal(new Mean(sw).value(), Math.sqrt(new Variance(sw).value())))
                        .add(new ABLine(1, 0).color(Color.GRAY))
        );

        p("From the figure above we probably should not consider that values from the " +
                "sepal-width variables comes from a normal distribution. In order " +
                "to check further this assumption, we can use Kolmogorov-Smirnof " +
                "one-sample test. ");

        code("        new KSTest(\"normality test\", sw, new Normal(new Mean(sw).value(), Math.sqrt(new Variance(sw).value()))).summary();\n");

        new KSTest("normality test", sw, new Normal(new Mean(sw).value(), Math.sqrt(new Variance(sw).value()))).summary();

        p("The KS Test gives us a p-value of 0.078 which is more than the usual 0.05 threshold value. " +
                "Note however that there is some departure from normality caused by the fact that " +
                "the measurements from the data set are truncated at the first decimal place. " +
                "This causes the agglomeration on truncated values of many points, which deteriorates " +
                "the results obtained by KS test. On the other hand, if we would not know about the " +
                "truncation process, one can think of another discrete positive valued distribution, but " +
                "here is not the case: a width on a flower is naturally a continuous measure. ");

        p("If we apply a jitter filter on these values, to emulate the true continuous measures, " +
                "the situation does not improve. ");

        code("        sw = new VFJitter(0.05).fitApply(df.var(\"sepal-width\"));\n" +
                "        draw(new QQPlot()\n" +
                "                        .add(df.var(\"sepal-width\"), new Normal(new Mean(sw).value(), Math.sqrt(new Variance(sw).value())))\n" +
                "                        .add(new ABLine(1, 0).color(Color.GRAY))\n" +
                "        );\n" +
                "        new KSTest(\"normality test\", sw, new Normal(new Mean(sw).value(), Math.sqrt(new Variance(sw).value()))).summary();\n");

        sw = new VFJitter(0.05).fitApply(df.var("sepal-width"));
        draw(new QQPlot()
                        .add(df.var("sepal-width"), new Normal(new Mean(sw).value(), Math.sqrt(new Variance(sw).value())))
                        .add(new ABLine(1, 0).color(Color.GRAY))
        );
        new KSTest("normality test", sw, new Normal(new Mean(sw).value(), Math.sqrt(new Variance(sw).value()))).summary();


        p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");
    }
}
