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

package rapaio.ml.supervised;

import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;
import static rapaio.explore.Workspace.code;
import static rapaio.explore.Workspace.print;
import rapaio.filters.NominalFilters;
import static rapaio.filters.RowFilters.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class CrossValidation {

    public void cv(Frame df, String classColName, Classifier c, int folds) {
        print("\n<pre><code>\n");
        print("CrossValidation with " + folds + " folds\n");
        df = shuffle(df);
        ClassifierModel[] results = new ClassifierModel[folds];

        double tacc = 0;

        for (int i = 0; i < folds; i++) {
            List<Integer> trainMapping = new ArrayList<>();
            List<Integer> testMapping = new ArrayList<>();
            if (folds >= df.getRowCount() - 1) {
                testMapping.add(i);
                for (int j = 0; j < df.getRowCount(); j++) {
                    if (j != i) {
                        trainMapping.add(df.getRowId(j));
                    }
                }

            } else {
                for (int j = 0; j < df.getRowCount(); j++) {
                    if (j % folds == i) {
                        testMapping.add(df.getRowId(j));
                    } else {
                        trainMapping.add(df.getRowId(j));
                    }
                }
            }
            Frame train = new MappedFrame(df.getSourceFrame(), new Mapping(trainMapping));
            Frame test = new MappedFrame(df.getSourceFrame(), new Mapping(testMapping));

            c.learn(train, classColName);
            results[i] = c.predict(test);
            ClassifierModel cr = results[i];
            double acc = 0;
            for (int j = 0; j < cr.getClassification().getRowCount(); j++) {
                if (cr.getClassification().getIndex(j) == cr.getTestFrame().getCol(classColName).getIndex(j)) {
                    acc++;
                }
            }
            acc /= (1. * cr.getClassification().getRowCount());
            tacc += acc;
            print(String.format("CV %d, accuracy:%.6f\n", i + 1, acc));

        }

        tacc /= (1. * folds);

        print(String.format("Mean accuracy:%.6f\n", tacc)

        );

        print("</code></pre>\n");
    }

    public void multiCv(Frame df, String classColName, List<Classifier> classifiers, int folds) {
        print("\n<pre><code>\n");
        print("CrossValidation with " + folds + " folds\n");
        df = shuffle(df);
        ClassifierModel[] results = new ClassifierModel[folds];

        double[] tacc = new double[classifiers.size()];

        for (int i = 0; i < folds; i++) {
            List<Integer> trainMapping = new ArrayList<>();
            List<Integer> testMapping = new ArrayList<>();
            if (folds >= df.getRowCount() - 1) {
                testMapping.add(i);
                for (int j = 0; j < df.getRowCount(); j++) {
                    if (j != i) {
                        trainMapping.add(df.getRowId(j));
                    }
                }

            } else {
                for (int j = 0; j < df.getRowCount(); j++) {
                    if (j % folds == i) {
                        testMapping.add(df.getRowId(j));
                    } else {
                        trainMapping.add(df.getRowId(j));
                    }
                }
            }
            Frame train = new MappedFrame(df.getSourceFrame(), new Mapping(trainMapping));
            Frame test = new MappedFrame(df.getSourceFrame(), new Mapping(testMapping));

            for (int k = 0; k < classifiers.size(); k++) {
                Classifier c = classifiers.get(k);
                c.learn(train, classColName);
                results[i] = c.predict(test);
                ClassifierModel cr = results[i];
                double acc = 0;
                for (int j = 0; j < cr.getClassification().getRowCount(); j++) {
                    if (cr.getClassification().getIndex(j) == cr.getTestFrame().getCol(classColName).getIndex(j)) {
                        acc++;
                    }
                }
                acc /= (1. * cr.getClassification().getRowCount());
                tacc[k] += acc;
                print(String.format("CV %d, classifier[%d] - accuracy:%.6f\n", i + 1, k + 1, acc));
            }
            print("-----------\n");

        }

        for (int k = 0; k < classifiers.size(); k++) {
            tacc[k] /= (1. * folds);
            print(String.format("Mean accuracy for classifier[%d] :%.6f\n", k + 1, tacc[k]));
        }


        print("</code></pre>\n");
    }
}
