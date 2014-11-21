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

package rapaio.ml.classifier.tools;

import rapaio.core.eval.ConfusionMatrix;
import rapaio.core.sample.Sampling;
import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.ml.classifier.CPrediction;
import rapaio.ml.classifier.Classifier;

import java.util.*;

import static rapaio.WS.print;
import static rapaio.data.filters.BaseFilters.shuffle;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class ModelEvaluation {

    public double cv(Frame df, String classColName, Classifier c, int folds) {
        print("\n<pre><code>\n");
        print("CrossValidation with " + folds + " folds\n");

        List<Integer>[] strata = buildStrata(df, folds, classColName);

        double correct = 0;

        for (int i = 0; i < folds; i++) {
            Mapping trainMapping = Mapping.newEmpty();
            Mapping testMapping = Mapping.newEmpty();
            for (int j = 0; j < folds; j++) {
                if (j == i) {
                    testMapping.addAll(strata[j]);
                } else {
                    trainMapping.addAll(strata[j]);
                }
            }
            Frame train = MappedFrame.newByRow(df, trainMapping);
            Frame test = MappedFrame.newByRow(df, testMapping);

            c.learn(train, classColName);
            CPrediction cp = c.predict(test);
            double fcorrect = 0;
            for (int j = 0; j < test.rowCount(); j++) {
                if (test.var(classColName).index(j) == cp.firstClasses().index(j)) {
                    fcorrect++;
                }
            }
            print(String.format("CV %d, accuracy:%.6f\n", i + 1, fcorrect / (1. * test.rowCount())));
            correct += fcorrect;
        }
        correct /= (1. * df.rowCount());
        print(String.format("Mean accuracy:%.6f\n", correct));

        print("</code></pre>\n");
        return correct;
    }

    private List<Integer>[] buildStrata(Frame df, int folds, String classColName) {
        String[] dict = df.var(classColName).dictionary();
        List<Integer>[] rows = new List[dict.length];
        for (int i = 0; i < dict.length; i++) {
            rows[i] = new ArrayList<>();
        }
        for (int i = 0; i < df.rowCount(); i++) {
            rows[df.index(i, df.varIndex(classColName))].add(i);
        }
        List<Integer> shuffle = new ArrayList<>();
        for (int i = 0; i < dict.length; i++) {
            Collections.shuffle(rows[i]);
            shuffle.addAll(rows[i]);
        }
        List<Integer>[] strata = new List[folds];
        for (int i = 0; i < strata.length; i++) {
            strata[i] = new ArrayList<>();
        }
        int fold = 0;
        for (int next : shuffle) {
            strata[fold].add(next);
            fold++;
            if (fold == folds) {
                fold = 0;
            }
        }
        return strata;
    }

    public void multiCv(Frame df, String classColName, List<Classifier> classifiers, int folds) {
        print("\n<pre><code>\n");
        print("CrossValidation with " + folds + " folds\n");
        df = shuffle(df);
        double[] tacc = new double[classifiers.size()];

        for (int i = 0; i < folds; i++) {
            Mapping trainMapping = Mapping.newEmpty();
            Mapping testMapping = Mapping.newEmpty();
            if (folds >= df.rowCount() - 1) {
                testMapping.add(i);
                for (int j = 0; j < df.rowCount(); j++) {
                    if (j != i) {
                        trainMapping.add(j);
                    }
                }

            } else {
                for (int j = 0; j < df.rowCount(); j++) {
                    if (j % folds == i) {
                        testMapping.add(j);
                    } else {
                        trainMapping.add(j);
                    }
                }
            }
            Frame train = MappedFrame.newByRow(df, trainMapping);
            Frame test = MappedFrame.newByRow(df, testMapping);

            for (int k = 0; k < classifiers.size(); k++) {
                Classifier c = classifiers.get(k);
//                c = c.newInstance();
                c.learn(train, classColName);
                CPrediction cp = c.predict(test);
                double acc = 0;
                for (int j = 0; j < cp.firstClasses().rowCount(); j++) {
                    if (cp.firstClasses().index(j) == test.var(classColName).index(j)) {
                        acc++;
                    }
                }
                acc /= (1. * cp.firstClasses().rowCount());
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

    public void bootstrapValidation(Frame df, String classColName, Classifier c, int bootstraps) {
        bootstrapValidation(df, classColName, c, bootstraps, 1.0);
    }

    public void bootstrapValidation(Frame df, String classColName, Classifier c, int bootstraps, double p) {
        print(bootstraps + " bootstrap evaluation\n");
        double total = 0;
        double count = 0;
        for (int i = 0; i < bootstraps; i++) {
            int[] rows = Sampling.sampleWR(((int) (df.rowCount() * p)), df.rowCount());
            Frame train = MappedFrame.newByRow(df, rows);
            Mapping others = Mapping.newEmpty();
            Set<Integer> set = new HashSet<>();
            for (int j = 0; j < rows.length; j++) {
                set.add(rows[i]);
            }
            for (int j = 0; j < df.rowCount(); j++) {
                if (!set.contains(j)) others.add(j);
            }
            Frame test = MappedFrame.newByRow(df, others);

            c.learn(train, classColName);
            c.predict(test);
            Var classes = c.predict(test).firstClasses();
            double acc = new ConfusionMatrix(test.var(classColName), classes).accuracy();
            System.out.println(String.format("bootstrap(%d) : %.6f", i + 1, acc));
            total += acc;
            count++;
        }
        System.out.println(String.format("Average accuracy: %.6f", total / count));
    }
}
