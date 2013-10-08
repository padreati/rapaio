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

import rapaio.core.BaseMath;
import rapaio.correlation.PearsonRCorrelation;
import rapaio.data.*;
import rapaio.datasets.Datasets;
import rapaio.explore.Summary;
import static rapaio.explore.Workspace.*;
import rapaio.filters.ColFilters;
import static rapaio.filters.NumericFilters.*;
import rapaio.graphics.Histogram;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Lines;
import rapaio.graphics.plot.Points;
import rapaio.ml.supervised.ClassifierModel;
import rapaio.ml.supervised.CrossValidation;
import rapaio.ml.supervised.tree.ID3;
import rapaio.ml.supervised.tree.RandomForest;
import rapaio.sample.Sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ClassificationWithRF implements TutorialPage {
    @Override
    public String getPageName() {
        return "ClassificationWithRandomForest";
    }

    @Override
    public String getPageTitle() {
        return "Classifcation with Random Forests";
    }

    @Override
    public void render() throws IOException {
        preparePrinter();

        heading(1, "Classification with Random Forests");


        p("Random Forests ");

        Frame all = Datasets.loadSpamBase();
        all = ColFilters.retainCols(all, "1-20,spam");


        Summary.names(all);

//        for (int i = 0; i < all.getColCount(); i++) {
//            int index = all.getColIndex("spam");
//            if(index==i) continue;
//            new PearsonRCorrelation(all.getCol("spam"), all.getCol(i)).summary();
//        }

        draw(new Histogram(all.getCol(1)));

        List<Frame> frames = Sample.randomSample(all, new int[]{all.getRowCount() * 15 / 100});

        Frame train = frames.get(0);
        Frame test = frames.get(1);

        Plot p = new Plot();

        int pos = 0;
        Vector index = new IndexVector("number of trees", 1000);
        Vector accuracy = new NumericVector("test error", 1000);
        Vector oob = new NumericVector("oob error", 1000);
        for (int mtree = 1; mtree < 60; mtree += 3) {

            RandomForest rf = new RandomForest(mtree, 3, true);
            rf.learn(train, "spam");
            ClassifierModel model = rf.predict(test);

            index.setIndex(pos, mtree);
            accuracy.setValue(pos, 1 - computeAccuracy(model, test));
            oob.setValue(pos, rf.getOobError());

            pos++;
        }
        Lines lines = new Lines(p, index, accuracy);
        lines.opt().setColorIndex(new OneIndexVector(2));
        p.add(lines);
        Points pts = new Points(p, index, accuracy);
        pts.opt().setColorIndex(new OneIndexVector(2));
        p.add(pts);
        p.add(new Lines(p, index, oob));
        p.add(new Points(p, index, oob));
        draw(p, 600, 300);


        pos = 0;
        index = new IndexVector("mtree", 1000);
        accuracy = new NumericVector("test error", 1000);
        oob = new NumericVector("oob error", 1000);
        for (int mtree = 1; mtree < 20; mtree += 1) {

            RandomForest rf = new RandomForest(10, mtree, true);
            rf.learn(train, "spam");
            ClassifierModel model = rf.predict(test);

            index.setIndex(pos, mtree);
            accuracy.setValue(pos, 1 - computeAccuracy(model, test));
            oob.setValue(pos, rf.getOobError());

            pos++;
        }
        p = new Plot();
        lines = new Lines(p, index, accuracy);
        lines.opt().setColorIndex(new OneIndexVector(2));
        p.add(lines);
        pts = new Points(p, index, accuracy);
        pts.opt().setColorIndex(new OneIndexVector(2));
        p.add(pts);
        p.add(new Lines(p, index, oob));
        p.add(new Points(p, index, oob));
        draw(p, 600, 300);

        closePrinter();
    }

    private double computeAccuracy(ClassifierModel model, Frame test) {
        Vector predict = model.getClassification();
        double accuracy = 0;
        double total = predict.getRowCount();
        for (int i = 0; i < predict.getRowCount(); i++) {
            if (test.getCol("spam").getIndex(i) == predict.getIndex(i)) {
                accuracy += 1.;
            }
        }
        return accuracy / total;
    }
}
