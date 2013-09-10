/*
 * Copyright 2013 Aurelian Tutuianu <padreati@yahoo.com>
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

import rapaio.core.stat.Mean;
import rapaio.core.stat.Quantiles;
import rapaio.core.stat.Variance;
import rapaio.correlation.PearsonRhoCorrelation;
import rapaio.data.Frame;
import rapaio.data.OneIndexVector;
import rapaio.datasets.Datasets;
import rapaio.distributions.Distribution;
import rapaio.distributions.Normal;
import rapaio.explore.Summary;
import static rapaio.explore.Summary.summary;
import static rapaio.core.BaseMath.*;
import static rapaio.explore.Workspace.*;
import rapaio.graphics.Histogram;
import rapaio.graphics.Plot;
import rapaio.graphics.QQPlot;
import rapaio.graphics.plot.ABLine;
import rapaio.graphics.plot.FunctionLine;
import rapaio.graphics.plot.Points;
import rapaio.printer.HTMLPrinter;

import java.io.IOException;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class PearsonHeight implements TutorialPage {

    public static void main(String[] args) throws IOException {
        setPrinter(new HTMLPrinter("pearsonheight.html", "Pearson's Height Dataset Analysis"));
        preparePrinter();
        new PearsonHeight().render();
        closePrinter();
    }

    @Override
    public String getCategory() {
        return "ExplorationSample";
    }

    @Override
    public String getPageName() {
        return "ExplorePearsonFatherSon";
    }

    @Override
    public String getPageTitle() {
        return "Explore Pearson Father Son Data";
    }

    @Override
    public void render() throws IOException {
        heading(1, "Analysis of Pearson's Height dataset");

        Frame df = Datasets.loadPearsonHeightDataset();

        p("This exploratory analysis is provided as a sample of analysis produced with Rapaio system.");
        p("The studied data set contains " + df.getRowCount() + " observations and has " + df.getColCount() + " columns.");

        Summary.summary(df);

        p("First we take a look at the histograms for the two dimensions");

        for (int i = 0; i < df.getColCount(); i++) {
            Histogram hist = new Histogram(df.getCol(i), 23, true, 57, 80);
            hist.setBottomLabel(df.getColNames()[i]);
            hist.getOp().setXRange(57, 80);
            hist.getOp().setYRange(0, 0.20);

//            Normal normal = new Normal(new Mean(df.getCol(i)).getValue(), 1.3);
            Normal normal = new Normal(new Mean(df.getCol(i)).getValue(), sqrt(new Variance(df.getCol(i)).getValue()));
            FunctionLine nline = new FunctionLine(hist, normal.getPdfFunction());
            nline.opt().setColorIndex(new OneIndexVector(2));
            hist.add(nline);

            draw(hist, 700, 300);
        }

        heading(2, "About normality");

        p("Looking at both produced histograms we are interested to understand "
                + "if the data contained in both variables resemble a normal "
                + "curve. Basically we are interested if the the values of "
                + "those dimensions are normally distributed.");

        p("An ususal graphical tools which can give us insights about that fact "
                + "is the quantile-quantile plot. ");

        for (int i = 0; i < df.getColCount(); i++) {

            QQPlot qqplot = new QQPlot();

            double mu = new Mean(df.getCol(i)).getValue();

            Distribution normal = new Normal();
            qqplot.add(df.getCol(i), normal);
            qqplot.setLeftLabel(df.getColNames()[i]);

            qqplot.add(new ABLine(qqplot, mu, true));
            qqplot.add(new ABLine(qqplot, 0, false));

            draw(qqplot, 500, 300);
        }

        summary(new Mean(df.getCol("Father")));
        summary(new Variance(df.getCol("Father")));

        summary(new Mean(df.getCol("Son")));
        summary(new Variance(df.getCol("Son")));

        summary(new PearsonRhoCorrelation(df.getCol("Father"), df.getCol("Son")));

        double[] perc = new double[11];
        for (int i = 0; i < perc.length; i++) {
            perc[i] = i / (10.);
        }
        Quantiles fatherQuantiles = new Quantiles(df.getCol("Father"), perc);
        Quantiles sonQuantiles = new Quantiles(df.getCol("Son"), perc);
        summary(fatherQuantiles);
        summary(sonQuantiles);

        Plot plot = new Plot();
        plot.getOp().setXRange(55, 80);
        plot.getOp().setYRange(55, 80);

        for (int i = 0; i < fatherQuantiles.getValues().length; i++) {
            ABLine line = new ABLine(plot, fatherQuantiles.getValues()[i], false);
            line.opt().setColorIndex(new OneIndexVector(30));
            plot.add(line);
        }

        for (int i = 0; i < sonQuantiles.getValues().length; i++) {
            ABLine line = new ABLine(plot, sonQuantiles.getValues()[i], true);
            line.opt().setColorIndex(new OneIndexVector(30));
            plot.add(line);
        }
        plot.add(new Points(plot, df.getCol("Father"), df.getCol("Son")));
        draw(plot, 600, 600);
    }
}
