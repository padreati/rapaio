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

import rapaio.data.Frame;
import rapaio.data.NominalVector;
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.data.util.NominalConsolidator;
import rapaio.explore.Summary;
import rapaio.filters.FilterMissingNominal;
import rapaio.functions.UnivariateFunction;
import rapaio.graphics.BarChart;
import rapaio.graphics.BoxPlot;
import rapaio.graphics.Histogram;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Points;
import rapaio.io.CsvPersistence;
import rapaio.printer.HTMLPrinter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static rapaio.core.BaseFilters.*;
import static rapaio.core.BaseMath.*;
import static rapaio.explore.Workspace.*;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class Explore {

    public static void main(String[] args) throws IOException {
        setPrinter(new HTMLPrinter("kaggle-titanic.html", "kaggle titanic"));

        getRandomSource().setSeed(1);

        preparePrinter();

        heading(1, "Kaggle Titanic descriptive analysis");

        Frame train = read("train.csv");
        Frame test = read("test.csv");
        List<Frame> frames = NominalConsolidator.consolidate(Arrays.asList(new Frame[]{train, test}));
        train = frames.get(0);
        test = frames.get(1);

        Summary.summary(train);
//        Summary.summary(test);

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

    public static Frame read(String name) throws IOException {
        CsvPersistence csv = new CsvPersistence();
        csv.setHeader(true);
        csv.setColSeparator(',');
        Frame df = csv.read(name, Explore.class.getResourceAsStream(name));

        HashSet<String> numericColumns = new HashSet<>();
        numericColumns.add("Age");
        numericColumns.add("Fare");
        numericColumns.add("SibSp");
        numericColumns.add("Parch");
        Vector[] vectors = new Vector[df.getColCount() + 2];
        for (int i = 0; i < df.getColCount(); i++) {
            // default do nothing
            vectors[i] = df.getCol(i);
            // fill spaces as missing vaues
            if (vectors[i].isNominal()) {
                vectors[i] = new FilterMissingNominal().filter(vectors[i], new String[]{"", " "});
            }
            // transform to isNumeric on Age
            if (numericColumns.contains(df.getColNames()[i])) {
                vectors[i] = toValue(vectors[i]);
            }
        }
        vectors[vectors.length - 2] = applyFunction(toValue("LogFare", df.getCol("Fare")), new UnivariateFunction() {
            @Override
            public double eval(double value) {
                return log(E + value);
            }
        });
        HashMap<String, String> titleDict = new HashMap<>();
        titleDict.put("Mrs.", "Mrs");
        titleDict.put("Mr.", "Mr");
        titleDict.put("Master.", "Master");
        titleDict.put("Miss.", "Miss");
        titleDict.put("Ms.", "Miss");
        titleDict.put("Mlle.", "Miss");
        titleDict.put("Dr.", "Dr");
        titleDict.put("Rev.", "Rev");
        titleDict.put("Sir.", "Sir");
        titleDict.put("Major.", "Sir");
        titleDict.put("Don.", "Sir");
        titleDict.put("Mme.", "Mrs");
        titleDict.put("Col.", "Col");
        titleDict.put("Capt.", "Col");
        titleDict.put("Jonkheer.", "Col");
        titleDict.put("Countess.", "Lady");
        titleDict.put("Lady.", "Lady");
        titleDict.put("Dona.", "Lady");
        vectors[vectors.length - 1] = new NominalVector("Title", df.getRowCount(), titleDict.values());
        for (int i = 0; i < df.getRowCount(); i++) {
            for (String term : titleDict.keySet()) {
                if (df.getCol("Name").getLabel(i).contains(term)) {
                    vectors[vectors.length - 1].setLabel(i, titleDict.get(term));
                    break;
                }
            }
            if (vectors[vectors.length - 1].isMissing(i)) {
                System.out.println(df.getCol("Name").getLabel(i));
            }
        }
        df = new SolidFrame(df.getName(), df.getRowCount(), vectors);
        return df;
    }
}
