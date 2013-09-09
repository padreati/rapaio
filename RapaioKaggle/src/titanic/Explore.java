/*
 * Copyright 2013 Aurelian Tutuianu
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

package titanic;

import rapaio.core.RandomSource;
import rapaio.data.Frame;
import rapaio.explore.Summary;
import static rapaio.explore.Workspace.*;
import static rapaio.filters.NominalFilters.consolidate;
import static rapaio.filters.NumericFilters.jitter;
import rapaio.graphics.BarChart;
import rapaio.graphics.BoxPlot;
import rapaio.graphics.Histogram;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Points;
import rapaio.printer.HTMLPrinter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Explore {

    public static void main(String[] args) throws IOException {
        setPrinter(new HTMLPrinter("kaggle-titanic.html", "kaggle titanic"));

        RandomSource.setSeed(1);

        preparePrinter();

        heading(1, "Kaggle Titanic analysis");

        Frame train = Utils.read("train.csv");
        Frame test = Utils.read("test.csv");
        List<Frame> frames = consolidate(Arrays.asList(train, test));
        train = frames.get(0);
        test = frames.get(1);

        Summary.summary(train);
        Summary.summary(test);

        heading(3, "Graphical description");

        draw(new Histogram(train.getCol("Age"), 30, false, 0, 80));
        draw(new Histogram(train.getCol("LogFare"), 40, false));

        draw(new BarChart(train.getCol("Sex")));
        draw(new BarChart(train.getCol("Pclass")));
        draw(new BarChart(train.getCol("Title")));

        draw(new BoxPlot(train.getCol("Age"), train.getCol("Pclass")));

        p("The upper figure is a sad one, and it seems to appear here, also. " +
                "Somehow describes or give a hint about how wealth is " +
                "distributed across age groups. " +
                "The younger ones are poor, and buy 3rd class tickets. " +
                "The older ones are richer, and buy 1st class tickets.");

        draw(new BoxPlot(train.getCol("LogFare"), train.getCol("Pclass")), 400, 400);

        Plot plot = new Plot();
        Points points = new Points(plot, jitter(train.getCol("LogFare")), jitter(train.getCol("SibSp")));
        points.opt().setColorIndex(train.getCol("Survived"));
        plot.add(points);
        draw(plot);

        plot = new Plot();
        points = new Points(plot, jitter(train.getCol("LogFare")), jitter(train.getCol("Parch")));
        points.opt().setColorIndex(train.getCol("Survived"));
        plot.add(points);
        draw(plot);


        closePrinter();
    }

}
