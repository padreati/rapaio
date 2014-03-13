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

import rapaio.core.correlation.PearsonRCorrelation;
import rapaio.core.distributions.Distribution;
import rapaio.core.distributions.Normal;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Quantiles;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Vector;
import rapaio.datasets.Datasets;
import rapaio.graphics.Plot;
import rapaio.graphics.QQPlot;
import rapaio.graphics.plot.ABLine;
import rapaio.graphics.plot.FunctionLine;
import rapaio.graphics.plot.Histogram;
import rapaio.graphics.plot.Points;
import rapaio.workspace.Summary;

import java.io.IOException;
import java.net.URISyntaxException;

import static rapaio.core.MathBase.sqrt;
import static rapaio.workspace.Summary.summary;
import static rapaio.workspace.Workspace.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class PearsonHeight implements TutorialPage {

    @Override
    public String getPageName() {
        return "ExplorePearsonFatherSon";
    }

    @Override
    public String getPageTitle() {
        return "Explore Pearson Father Son Data";
    }

    @Override
    public void render() throws IOException, URISyntaxException {
        heading(1, "Analysis of Pearson's Height dataset");

        final Frame df = Datasets.loadPearsonHeightDataset();

        p("This exploratory analysis is provided as a sample of analysis produced with Rapaio system.");
        p("The studied data set contains " + df.rowCount() + " observations and has " + df.colCount() + " columns.");

        Summary.summary(df);

        p("First we take a look at the histograms for the two dimensions");

        for (int i = 0; i < df.colCount(); i++) {
            Normal normal = new Normal(new Mean(df.col(i)).getValue(), sqrt(new Variance(df.col(i)).getValue()));
            draw(new Plot()
                    .add(new Histogram(df.col(i), 23, true, 57, 80))
                    .add(new FunctionLine(normal.getPdfFunction())
                            .setColorIndex(2))
                    .setBottomLabel(df.colNames()[i])
                    .setXRange(57, 80)
                    .setYRange(0, 0.20),
                    700, 300);
        }

        heading(2, "About normality");

        p("Looking at both produced histograms we are interested to understand "
                + "if the data contained in both variables resemble a normal "
                + "curve. Basically we are interested if the the values of "
                + "those dimensions are normally distributed.");

        p("An ususal graphical tools which can give us insights about that fact "
                + "is the quantile-quantile plot. ");

        for (int i = 0; i < df.colCount(); i++) {
            final Vector col = df.col(i);
            final int colIndex = i;
            double mu = new Mean(col).getValue();
            Distribution normal = new Normal();
            draw(new QQPlot()
                    .add(col, normal)
                    .setLeftLabel(df.colNames()[colIndex]),
                    500, 300);
        }

        summary(new Mean(df.col("Father")));
        summary(new Variance(df.col("Father")));

        summary(new Mean(df.col("Son")));
        summary(new Variance(df.col("Son")));

        summary(new PearsonRCorrelation(df.col("Father"), df.col("Son")));

        double[] perc = new double[11];
        for (int i = 0; i < perc.length; i++) {
            perc[i] = i / (10.);
        }
        final Quantiles fatherQuantiles = new Quantiles(df.col("Father"), perc);
        final Quantiles sonQuantiles = new Quantiles(df.col("Son"), perc);
        summary(fatherQuantiles);
        summary(sonQuantiles);

        Plot plot = new Plot()
                .setXRange(55, 80)
                .setYRange(55, 80);
        for (int i = 0; i < fatherQuantiles.getValues().length; i++) {
            plot.add(new ABLine(fatherQuantiles.getValues()[i], false)
                    .setColorIndex(30));
        }
        for (int i = 0; i < sonQuantiles.getValues().length; i++) {
            plot.add(new ABLine(sonQuantiles.getValues()[i], true).setColorIndex(30));
        }
        plot.add(new Points(df.col("Father"), df.col("Son")));
        draw(plot, 600, 600);

        p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");
    }
}
