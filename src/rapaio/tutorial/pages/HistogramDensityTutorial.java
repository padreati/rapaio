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
import rapaio.core.distributions.empirical.KDE;
import rapaio.core.distributions.empirical.KFunc;
import rapaio.data.Frame;
import rapaio.data.Index;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.graphics.plot.Histogram;
import rapaio.graphics.plot.Plot;
import rapaio.ws.Summary;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.sys.WS.*;
import static rapaio.graphics.Plotter.hist;
import static rapaio.graphics.Plotter.plot;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class HistogramDensityTutorial implements TutorialPage {

    @Override
    public String getPageName() {
        return "HistogramDensityTutorial";
    }

    @Override
    public String getPageTitle() {
        return "Histogram and Kernel Density Estimation Tutorial";
    }

    @Override
    public void render() throws IOException, URISyntaxException {
        heading(1, "Histogram and Kernel Density Estimation Tutorial");

        heading(3, "Data set used");

        p("First we need to load a frame with data. For convenience we don't use " +
                "input/output facilities. Instead, we load a set of data already " +
                "built-in the library (the classical Pearson father-son data set).");

        final Frame df = Datasets.loadPearsonHeightDataset();

        Summary.printSummary(df);

        p("We have two continuous random variables which observes heights of pairs " +
                "of father and sons. ");

        p("The 5-number summaries gives us some hints on how the densities " +
                "of the values for the two random variables looks like. " +
                "We note that the mean and median are very close which " +
                "leads us to think that the densities of the values " +
                "are somehow symmetric. ");

        heading(3, "Histograms");

        p("One of the most important and usual tools to obtain information " +
                "about the densities of a random variable is the histogram. ");

        p("In statistics, a histogram is a graphical representation of the densities of data. " +
                "It is an estimate of the probability densities of a continuous variable " +
                "and was first introduced by Karl Pearson. " +
                "A histogram is a representation of tabulated frequencies, shown as adjacent " +
                "rectangles, erected over discrete intervals (bins), with an area equal to " +
                "the frequency of the observations in the interval. ");

        p("We draw histograms with rapaio toolbox in the following way:");

        WS.draw(new Plot().add(new Histogram(df.var("Father"))));

        code("        WS.draw(new Plot().add(new Histogram(df.var(\"Father\"))));\n");

        p("The height of a rectangle is also equal to the frequency density of " +
                "the interval, i.e., the frequency divided by the width of the interval. " +
                "The total area of the histogram is equal to the number of data. ");

        p("The previous code uses the simplest form of building a histogram. " +
                "There are some parameters which can help tuning the output. ");

        p("By default the number of bins is computed using the Freedman-Diaconis estimator. " +
                "However, one can specify directly the number of bins. ");

        draw(hist(df.var("Father"), Plotter.bins(100)));

        code("        draw(hist(df.var(\"Father\"), bins(100)));\n");

        p("Note that on the vertical axis we can see frequency values which are " +
                "counters. This means that on y axis we have the number of elements found in bins. " +
                "This is useful when we want to have an absolute value image of the " +
                "histogram. However, if we are interested to see the proportions " +
                "of values in bins on y axis, we can change the default value for prob parameter. " +
                "This computes on y axis the the proportion, and makes the whole " +
                "graph of the histogram to have a total area of 1, which is " +
                "a desirable property if the histograms are compared to other " +
                "similar tools like densities. ");

        draw(hist(df.var("Father"), Plotter.prob(true)));

        code("        draw(hist(df.var(\"Father\"), prob(true)));\n");

        p("An alternative to the histogram is kernel density estimation, " +
                "which uses a kernel function to smooth samples. This will " +
                "construct a smooth probability density function estimation.");

        p("One can draw also the kernel density approximation, over a histogram or as a separate plot.");

        final Var col = df.var("Father");
        draw(plot().hist(col, Plotter.prob(true), Plotter.color(Index.newSeq(1, 255))).densityLine(col));

        code("        final Var col = df.var(\"Father\");\n" +
                "        draw(new Plot().hist(col, prob(true), color(Index.newSeq(1, 255))).densityLine(col));\n");

        p("In statistics, kernel density estimation (KDE) is a non-parametric way to " +
                "estimate the probability density function of a random variable. " +
                "Kernel density estimation is a fundamental data smoothing problem " +
                "where inferences about the population are made, based on a finite data " +
                "sample. In some fields such as signal processing and econometrics " +
                "it is also termed the Parzen–Rosenblatt window method, " +
                "after Emanuel Parzen and Murray Rosenblatt, who are usually credited " +
                "with independently creating it in its current form.");

        p("In it's default implementation, used without parameters, " +
                "the Rapaio toolbox learn a kernel density estimation with GaussianPdf " +
                "kernels and with bandwidth approximated by Silverman's rule of thumb.");

        p("However one can use a different value for bandwidth in order to obtain a " +
                "smoother or less smoother approximation of the density function.");

        draw(plot()
                        .hist(col, Plotter.prob(true))
                        .funLine(new KDE(col, 0.1)::pdf, Plotter.color(1))
                        .funLine(new KDE(col, 0.5)::pdf, Plotter.color(2))
                        .funLine(new KDE(col, 2)::pdf, Plotter.color(3))
                        .yLim(0, 0.18),
                600, 300
        );

        p("Another thing one can try with kernel density estimator is to " +
                "change the kernel function, which is the function used to " +
                "disperse in a small neighborhood around a point the " +
                "probability mass initially assigned to the points of " +
                "the discrete sample. The kernel choices are: uniform, triangular, " +
                "Epanechnikov, biweight, triweight, tricube, GaussianPdf and cosine. " +
                "Of course, it is easy to implement your own smoothing method " +
                "once you implement a custom kernel function. ");

        draw(plot()
                .funLine(new KDE(col)::pdf, Plotter.color(1))
                .densityLine(col, new KFunc() {
                    @Override
                    public String summary() {
                        return "custom kernel";
                    }

                    private static final long serialVersionUID = 2176030821754943447L;

                    @Override
                    public double pdf(double x, double x0, double bandwidth) {
                        return (Math.abs(x - x0) / bandwidth >= 0.5) ? 0 : 1.;
                    }

                    @Override
                    public double minValue(double x0, double bandwidth) {
                        return x0 + bandwidth;
                    }

                    @Override
                    public double getMaxValue(double x0, double bandwidth) {
                        return x0 - bandwidth;
                    }

                }, 0.5, Plotter.points(256))
                .yLim(0, 0.18)
                .xLim(55, 80));

        p("We could agree that my implementation of kernel function is ugly " +
                "and maybe no so useful, however you have to note that " +
                "the purpose of Rapaio toolbox is to give you sharp and " +
                "precise standard tools and, in the same time, the opportunity " +
                "to experiment with your owns.");

        p("One final graph will show the kernel approximation " +
                "of density functions for both numerical variables " +
                "that we have: father's heights and son's heights.");

        p("Blue line represents density approximation of father's heights, " +
                "red line represents density approximation of son's heights.");

        draw(plot().densityLine(df.var("Father"), Plotter.color(6))
                .densityLine(df.var("Son"), Plotter.color(9))
                .yLim(0, 0.18)
                .xLim(55, 80));

        p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");
    }
}
