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

import rapaio.data.Frame;
import rapaio.data.OneIndexVector;
import rapaio.datasets.Datasets;
import rapaio.explore.Summary;
import rapaio.graphics.Histogram;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Points;
import rapaio.printer.HTMLPrinter;

import java.io.IOException;

import static rapaio.explore.Workspace.*;
import rapaio.printer.Printer;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class IrisExplore implements TutorialPage {

    @Override
    public String getPageName() {
        return "IrisExploration";
    }

    @Override
    public String getPageTitle() {
        return "Iris Exploration StatSampling";
    }

    @Override
    public void render() throws IOException {
        Frame df = Datasets.loadIrisDataset();

        heading(1, "Iris sample exploratory analysis");
        p("This is a sample just to hold analysis of Iris dataset"
                + " and is used to demonstrate Rapaio capabilities.");

        heading(2, "Data used");
        p("Data used in this exploratory study is a well-known data set made by Sir Ronald Fisher "
                + "containing measurements about three Iris species. The names of the measuremens are:");

        Summary.names(df);

        p("Measurements include length and width of sepal and petal species. Below is a summary of the "
                + "dataset in question.");

        Summary.summary(df);

        heading(2, "Distribution of measurements");
        p("Now we will take a look over the distribuition of first two measurements.");

        for (int i = 0; i < df.getColCount() - 1; i++) {
            Histogram hist = new Histogram(df.getCol(i));
            hist.setBins(30);
            hist.setBottomLabel(df.getColNames()[i]);
            draw(hist, 500, 250);
        }

        p("We can see easily that for petal length and width there are two well-separated modes."
                + " This is probably a well separation of species, so a plot should clarify "
                + "that immediately.");

        Plot plot = new Plot();
        Points points = new Points(plot, df.getCol(2), df.getCol(3));
        points.opt().setColorIndex(df.getCol("class"));
        points.opt().setPchIndex(new OneIndexVector(1));
        plot.setBottomLabel(df.getColNames()[2]);
        plot.setLeftLabel(df.getColNames()[3]);
        plot.setTitle("Iris datapoints colored by species");
        plot.add(points);

        draw(plot, 600, 350);

        p(""
                + "Indeed, we can notice that the red points are clustered closer to the bottom-left corner "
                + "of the plot. The other type of points (the blue and the gree ones) are "
                + "separeded also, but there is no clear border between those. Its seem to"
                + " not be very linear separable, at least on the 2 dimensions used: petal-length "
                + " and petal-width.");

        p(""
                + "Sepal width looks like it is distrbuted as a symemtric distribution."
                + "We will plot now a qqplot to see how those values are distributed on a normal curve."
                + "We estimate first the mean and the variance sepal-width sample values:");

        p(">>>This tutorial is generated with Rapaio document printer facilities.<<<");

    }
}
