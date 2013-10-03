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
import rapaio.data.SolidFrame;
import rapaio.data.Vector;
import rapaio.distributions.empirical.*;
import rapaio.explore.Summary;
import static rapaio.explore.Workspace.*;
import static rapaio.filters.BaseFilters.renameVector;
import rapaio.filters.ColFilters;
import static rapaio.filters.ColFilters.removeCols;
import static rapaio.filters.NominalFilters.consolidate;
import rapaio.graphics.Plot;
import rapaio.graphics.plot.FunctionLine;
import rapaio.graphics.plot.HistogramBars;
import rapaio.io.CsvPersistence;
import rapaio.ml.supervised.Classifier;
import rapaio.ml.supervised.ClassifierModel;
import rapaio.ml.supervised.ClassifierProvider;
import rapaio.ml.supervised.CrossValidation;
import rapaio.ml.supervised.meta.Bagging;
import rapaio.ml.supervised.tree.ID3;
import rapaio.ml.supervised.tree.RandomForest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * User: <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ConsoleExplore {

    public static void main(String[] args) throws IOException {
        Frame train = Utils.read("train.csv");
        Frame test = Utils.read("test.csv");
        List<Frame> frames = consolidate(Arrays.asList(train, test));
        train = frames.get(0);
        test = frames.get(1);

        Frame tr = train;

        tr = removeCols(tr, "PassengerId");
        tr = removeCols(tr, "Name");
        tr = removeCols(tr, "Ticket");
        tr = removeCols(tr, "Cabin");
        tr = removeCols(tr, "Fare");
//        tr = removeCols(tr, "SibSp");
//        tr = removeCols(tr, "Parch");
//        tr = removeCols(tr, "Pclass");
//        tr = removeCols(tr, "Title");
//        tr = removeCols(tr, "Sex");
//        tr = removeCols(tr, "Embarked");
//        tr = removeCols(tr, "Family");

//        tr = ColFilters.retainNominal(tr);
        Summary.summary(tr);

        final int mtree = 800;
        final int mcols = 4;
        RandomForest rf = new RandomForest(mtree, mcols);
//        rf.setDebug(true);
        long start = System.currentTimeMillis();
        CrossValidation cv = new CrossValidation();
        cv.cv(tr, "Survived", rf, 10);
        long end = System.currentTimeMillis();
        System.out.println("CV took " + (end - start) + " millis");

        rf = new RandomForest(mtree, mcols);
        long start2 = System.currentTimeMillis();
        rf.learn(tr, "Survived");
        long end2 = System.currentTimeMillis();
        System.out.println("submit train took " + (end2 - start2) + " millis");
        System.out.flush();

//        Summary.summary(id3);
        ClassifierModel cr = rf.predict(test);


        Frame submit = new SolidFrame("submit", test.getRowCount(), new Vector[]{
                test.getCol("PassengerId"),
                renameVector(cr.getClassification(), "Survived")
        });
        CsvPersistence persist = new CsvPersistence();
        persist.setColSeparator(',');
        persist.setHasQuotas(true);
        persist.setHasHeader(true);
        persist.write(submit, "/home/ati/work/rapaio/RapaioKaggle/src/titanic/submit.csv");

        closePrinter();
    }
}
