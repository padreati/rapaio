/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
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

import rapaio.core.MathBase;
import rapaio.core.distributions.empirical.KernelDensityEstimator;
import rapaio.core.distributions.empirical.KernelFunction;
import rapaio.data.Frame;
import rapaio.data.Vector;
import rapaio.data.Vectors;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.DensityLine;
import rapaio.graphics.plot.FunctionLine;
import rapaio.graphics.plot.Histogram;
import rapaio.workspace.Summary;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.workspace.Workspace.*;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
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

        p("First we need to load a frame with data. For convenience we don't use "
                + "input/output facilities. Instead, we load a set of data already "
                + "built-in the library.");

        final Frame df = Datasets.loadPearsonHeightDataset();

        p("We will use the classical Pearson father-son data set. ");

        Summary.summary(df);

        p("We have two continuous random variables which observes height of "
                + "pairs of father and sons. ");

        p("The 5-number summaries gives us some hints on how the distribution "
                + "of the values for the 2 random variables looks like. "
                + "We note that the mean and median are very close which "
                + "leads us to think that the distribution of the values "
                + "are somehow symmetric. ");

        heading(3, "Histograms");

        p("One of the most important and usual tools to obtain information "
                + "about the distribution of a random variable is histogram. ");

        p("In statistics, a histogram is a graphical representation of the "
                + "distribution of data. "
                + "It is an estimate of the probability distribution of a "
                + "continuous variable and was first introduced by Karl Pearson. "
                + "A histogram is a representation of tabulated frequencies, "
                + "shown as adjacent rectangles, erected over discrete intervals (bins), "
                + "with an area equal to the frequency of the observations "
                + "in the interval. ");

        p("We draw histograms with Rapaio toolbox in the following way:");

        code("        draw(new Histogram(df.getCol(\"Father\")));\n");

        draw(new Plot().add(new Histogram(df.col("Father"))));

        p("The height of a rectangle is also equal to the frequency density of "
                + "the interval, i.e., the frequency divided by the width of the interval. "
                + "The total area of the histogram is equal to the number of data. ");

        p("The previous code uses the simplest form of "
                + "building a histogram. There are some parameters "
                + "which can help somebody to tune the output. ");

        p("One can change the number of bins. ");

        code("        draw(new Plot().add(new Histogram(df.getCol(\"Father\"), 100, false)));\n");
        draw(new Plot().add(new Histogram(df.col("Father"), 100, false)));

        p("Note that on the vertical axis we found the count of the elements which "
                + "are held by the bins that are displayed. We can "
                + "change how the heights of the bins are computed into "
                + "densities which makes the total area under curve to be 1. "
                + "That feature is a key property of a probability density function, also.");

        p("        draw(new Plot().add(new Histogram(df.getCol(\"Father\"), 30, true)));\n");
        draw(new Plot().add(new Histogram(df.col("Father"), 30, true)));

        p("The histogram is useful but have a weak point. Its weak point lies "
                + "into it's flexibility given by the number of bins. "
                + "There is no \"best\" number of bins, and different bin sizes "
                + "can reveal different features of the data. ");
        p("Some theoreticians have attempted to determine an optimal number "
                + "of bins, but these methods generally make strong assumptions "
                + "about the shape of the distribution. Depending on the actual "
                + "data distribution and the goals of the analysis, different bin "
                + "widths may be appropriate, so experimentation is usually needed "
                + "to determine an appropriate width. ");

        p("An alternative to the histogram is kernel density estimation, "
                + "which uses a kernel to smooth samples. This will construct a "
                + "smooth probability density function, which will in general more "
                + "accurately reflect the underlying variable.");

        p("One can draw also the kernel density approximation, over "
                + "a histogram or as a separate plot.");

        code("        final Vector getCol = df.getCol(\"Father\");\n"
                + "        draw(new Plot()\n"
                + "                .add(new HistogramBars(getCol).setColorIndex(new IndexVector(1, 255, 1)))\n"
                + "                .add(new DensityLine(getCol)));\n"
                + "");

        final Vector col = df.col("Father");
        draw(new Plot()
                .add(new Histogram(col).setColorIndex(Vectors.newSeq(1, 255, 1)))
                .add(new DensityLine(col)));

        p("In statistics, kernel density estimation (KDE) is a non-parametric way to "
                + "estimate the probability density function of a random variable. "
                + "Kernel density estimation is a fundamental data smoothing problem "
                + "where inferences about the population are made, based on a finite data "
                + "sample. In some fields such as signal processing and econometrics "
                + "it is also termed the Parzen–Rosenblatt window method, "
                + "after Emanuel Parzen and Murray Rosenblatt, who are usually credited "
                + "with independently creating it in its current form.");

        p("In it's default implementation, used without parameters, "
                + "the Rapaio toolbox learn a kernel density estimation with Gaussian "
                + "kernels and with bandwidth approximated by Silverman's rule "
                + "of thumb.");

        p("However one can use a different getValue for bandwidth in order to obtain "
                + "a smooth or less smooth approximation of the density function.");

        draw(new Plot()
                .add(new Histogram(col))
                .add(new FunctionLine(new KernelDensityEstimator(col, 0.1).getPdfFunction()).setColorIndex(1))
                .add(new FunctionLine(new KernelDensityEstimator(col, 0.5).getPdfFunction()).setColorIndex(2))
                .add(new FunctionLine(new KernelDensityEstimator(col, 2).getPdfFunction()).setColorIndex(3))
                .setYRange(0, 0.18),
                600, 300);

        p("Another thing one can try with kernel density estimator is to "
                + "change the kernel function, which is the function used to "
                + "disperse in a small neighborhood around a point the "
                + "probability mass initially assigned to the points of "
                + "the discrete sample. The kernel choices are: uniform, triangular, "
                + "Epanechnikov, biweight, triweight, tricube, Gaussian and cosine. "
                + "Of course, it is easy to implement your own smoothing method "
                + "once you implement a custom kernel function. ");

        draw(new Plot()
                .add(new FunctionLine(new KernelDensityEstimator(col).getPdfFunction()).setColorIndex(1))
                .add(new DensityLine(col, new KernelFunction() {
                    @Override
                    public double pdf(double x, double x0, double bandwidth) {
                        return (MathBase.abs(x - x0) / bandwidth >= 0.5) ? 0 : 1.;
                    }

                    @Override
                    public double getMinValue(double x0, double bandwidth) {
                        return x0 + bandwidth;
                    }

                    @Override
                    public double getMaxValue(double x0, double bandwidth) {
                        return x0 - bandwidth;
                    }
                }, 0.5, 256))
                .setYRange(0, 0.18)
                .setXRange(55, 80));

        p("We could agree that my implementation of kernel function is ugly "
                + "and maybe no so useful, however you have to know that "
                + "the purpose of Rapaio toolbox is to give you sharp and "
                + "precise standard tools and, in the same time, the opportunity "
                + "to experiment with your owns.");

        p("One final graph will show the kernel approximation "
                + "of density functions for both numerical variables "
                + "that we have: father's heights and son's heights.");

        p("Blue line represents density approximation of father's heights, "
                + "red line represents density approximation of son's heights.");

        draw(new Plot().add(new DensityLine(df.col("Father")).setColorIndex(6))
                .add(new DensityLine(df.col("Son")).setColorIndex(9))
                .setYRange(0, 0.18)
                .setXRange(55, 80));

        p("Note: the sole purpose of this tutorial is to show what and how it can "
                + "be done with Rapaio toolbox library. ");

        p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");
    }
}
