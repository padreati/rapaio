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

import rapaio.core.stat.Mean;
import rapaio.core.stat.Quantiles;
import rapaio.core.stat.Variance;
import rapaio.correlation.PearsonRCorrelation;
import rapaio.data.Frame;
import rapaio.data.OneIndexVector;
import rapaio.data.Vector;
import rapaio.datasets.Datasets;
import rapaio.distributions.Distribution;
import rapaio.distributions.Normal;
import rapaio.workspace.Summary;
import static rapaio.workspace.Summary.summary;
import static rapaio.core.BaseMath.*;
import static rapaio.workspace.Workspace.*;

import rapaio.graphics.Histogram;
import rapaio.graphics.Plot;
import rapaio.graphics.QQPlot;
import rapaio.graphics.plot.ABLine;
import rapaio.graphics.plot.FunctionLine;
import rapaio.graphics.plot.Points;

import java.io.IOException;
import java.net.URISyntaxException;

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
        p("The studied data set contains " + df.getRowCount() + " observations and has " + df.getColCount() + " columns.");

        Summary.summary(df);

        p("First we take a look at the histograms for the two dimensions");

        for (int i = 0; i < df.getColCount(); i++) {
            Histogram hist = new Histogram(df.getCol(i), 23, true, 57, 80);
            hist.setBottomLabel(df.getColNames()[i]);
            hist.opt().setXRange(57, 80);
            hist.opt().setYRange(0, 0.20);

            Normal normal = new Normal(new Mean(df.getCol(i)).getValue(), sqrt(new Variance(df.getCol(i)).getValue()));
            FunctionLine nline = new FunctionLine(hist, normal.getPdfFunction());
            nline.opt().setColorIndex(new OneIndexVector(2));

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
            final Vector col = df.getCol(i);
            final int colIndex = i;
            draw(new QQPlot() {{
                double mu = new Mean(col).getValue();
                Distribution normal = new Normal();
                this.add(col, normal);
                this.setLeftLabel(df.getColNames()[colIndex]);

                new ABLine(this, mu, true);
                new ABLine(this, 0, false);
            }}, 500, 300);
        }

        summary(new Mean(df.getCol("Father")));
        summary(new Variance(df.getCol("Father")));

        summary(new Mean(df.getCol("Son")));
        summary(new Variance(df.getCol("Son")));

        summary(new PearsonRCorrelation(df.getCol("Father"), df.getCol("Son")));

        double[] perc = new double[11];
        for (int i = 0; i < perc.length; i++) {
            perc[i] = i / (10.);
        }
        final Quantiles fatherQuantiles = new Quantiles(df.getCol("Father"), perc);
        final Quantiles sonQuantiles = new Quantiles(df.getCol("Son"), perc);
        summary(fatherQuantiles);
        summary(sonQuantiles);

        draw(new Plot() {{
            for (int i = 0; i < fatherQuantiles.getValues().length; i++) {
                ABLine line = new ABLine(this, fatherQuantiles.getValues()[i], false);
                line.opt().setColorIndex(30);
            }
            for (int i = 0; i < sonQuantiles.getValues().length; i++) {
                ABLine line = new ABLine(this, sonQuantiles.getValues()[i], true);
                line.opt().setColorIndex(30);
            }
            new Points(this, df.getCol("Father"), df.getCol("Son"));
            opt().setXRange(55, 80);
            opt().setYRange(55, 80);
        }}, 600, 600);

        p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");
    }
}
