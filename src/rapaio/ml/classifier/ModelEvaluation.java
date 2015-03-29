/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
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

package rapaio.ml.classifier;

import rapaio.core.RandomSource;
import rapaio.core.sample.SamplingTool;
import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.filter.frame.FFShuffle;
import rapaio.ml.eval.ConfusionMatrix;

import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static rapaio.WS.print;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
public class ModelEvaluation {

    public double cv(Frame df, String classColName, Classifier c, int folds) {
        print("\nCrossValidation with " + folds + " folds\n");
        print("Model: " + c.fullName() + "\n");

        List<List<Integer>> strata = buildStrata(df, folds, classColName);

        double correct = 0;

        for (int i = 0; i < folds; i++) {
            Mapping trainMapping = Mapping.newEmpty();
            Mapping testMapping = Mapping.newEmpty();
            for (int j = 0; j < folds; j++) {
                if (j == i) {
                    testMapping.addAll(strata.get(j));
                } else {
                    trainMapping.addAll(strata.get(j));
                }
            }
            Frame train = MappedFrame.newByRow(df, trainMapping);
            Frame test = MappedFrame.newByRow(df, testMapping);

            c.learn(train, classColName);
            ClassifierFit cp = c.predict(test);
            double fcorrect = 0;
            for (int j = 0; j < test.rowCount(); j++) {
                if (test.getVar(classColName).index(j) == cp.firstClasses().index(j)) {
                    fcorrect++;
                }
            }
            print(String.format("CV %d, accuracy:%.6f\n", i + 1, fcorrect / (1. * test.rowCount())));
            correct += fcorrect;
        }
        correct /= (1. * df.rowCount());
        print(String.format("Mean accuracy:%.6f\n", correct));
        return correct;
    }

    private List<List<Integer>> buildStrata(Frame df, int folds, String classColName) {
        String[] dict = df.getVar(classColName).dictionary();
        List<List<Integer>> rows = IntStream.range(0, dict.length).boxed().map(ArrayList<Integer>::new).collect(toList());
        for (int i = 0; i < df.rowCount(); i++) {
            rows.get(df.index(i, df.varIndex(classColName))).add(i);
        }
        List<Integer> shuffle = new ArrayList<>();
        for (int i = 0; i < dict.length; i++) {
            Collections.shuffle(rows.get(i), RandomSource.getRandom());
            shuffle.addAll(rows.get(i));
        }
        List<List<Integer>> strata = new ArrayList<>();
        for (int i = 0; i < folds; i++) {
            strata.add(new ArrayList<>());
        }
        int fold = 0;
        for (int next : shuffle) {
            strata.get(fold).add(next);
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
        df = new FFShuffle().fitApply(df);
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
                ClassifierFit cp = c.predict(test);
                double acc = 0;
                for (int j = 0; j < cp.firstClasses().rowCount(); j++) {
                    if (cp.firstClasses().index(j) == test.getVar(classColName).index(j)) {
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
            int[] rows = SamplingTool.sampleWR(((int) (df.rowCount() * p)), df.rowCount());
            Frame train = MappedFrame.newByRow(df, rows);
            Mapping others = Mapping.newEmpty();
            Set<Integer> set = Arrays.stream(rows).boxed().collect(toSet());
            for (int j = 0; j < df.rowCount(); j++) {
                if (!set.contains(j)) others.add(j);
            }
            Frame test = MappedFrame.newByRow(df, others);

            c.learn(train, classColName);
            c.predict(test);
            Var classes = c.predict(test).firstClasses();
            double acc = new ConfusionMatrix(test.getVar(classColName), classes).accuracy();
            System.out.println(String.format("bootstrap(%d) : %.6f", i + 1, acc));
            total += acc;
            count++;
        }
        System.out.println(String.format("Average accuracy: %.6f", total / count));
    }
}
