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

import rapaio.data.*;
import rapaio.datasets.Datasets;
import rapaio.explore.Summary;
import static rapaio.explore.Workspace.*;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.Lines;
import rapaio.graphics.plot.Points;
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


        p("Classification is a type of supervised learning which " +
                "involves the prediction of a nominal variable.");

        Frame all = Datasets.loadIrisDataset();

        List<Frame> frames = Sample.randomSample(all, new int[]{all.getRowCount() * 60 / 100});
        Frame train = frames.get(0);
        Frame test = frames.get(1);

        Summary.summary(all);

        Vector index = new IndexVector("N", 1, train.getRowCount(), 1);

        Vector onlineSample = new NumericVector("rf.oob", index.getRowCount());

        RandomForest rftest = new RandomForest(1000, 1);
        rftest.setDebug(true);
        rftest.learn(all, "class");

        onlineSample.setValue(0, 1);
        RandomForest rf = new RandomForest(1000, 1);
        List<Integer> mapping = new ArrayList<>();
        for (int i = 0; i < index.getRowCount(); i++) {
            mapping.add(index.getIndex(i));
            Frame sel = new MappedFrame(all.getSourceFrame(), new Mapping(mapping));
            rf.learn(sel, "class");
            onlineSample.setValue(i, 1 - rf.getOobError());
        }

        Plot p = new Plot();
        Lines lines = new Lines(p, index, onlineSample);
        p.add(lines);
        Points pts = new Points(p, index, onlineSample);
        p.add(pts);
        draw(p, 600, 500);


        closePrinter();
    }
}
