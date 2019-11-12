/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 Aurelian Tutuianu
 *    Copyright 2014 Aurelian Tutuianu
 *    Copyright 2015 Aurelian Tutuianu
 *    Copyright 2016 Aurelian Tutuianu
 *    Copyright 2017 Aurelian Tutuianu
 *    Copyright 2018 Aurelian Tutuianu
 *    Copyright 2019 Aurelian Tutuianu
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
 *
 */

package rapaio.experiment.ml.eval;

import rapaio.core.SamplingTools;
import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.filter.frame.FShuffle;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.eval.metric.Confusion;
import rapaio.printer.Printer;

import java.util.List;

import static rapaio.sys.WS.print;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class REvaluation {

    public static void multiCv(Frame df, String classColName, List<ClassifierModel> classifierModels, int folds) {
        print("CrossValidation with " + folds + " folds\n");
        df = df.fapply(FShuffle.filter());
        double[] tacc = new double[classifierModels.size()];

        for (int i = 0; i < folds; i++) {
            Mapping trainMapping = Mapping.empty();
            Mapping testMapping = Mapping.empty();
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
            Frame train = MappedFrame.byRow(df, trainMapping);
            Frame test = MappedFrame.byRow(df, testMapping);

            for (int k = 0; k < classifierModels.size(); k++) {
                ClassifierModel c = classifierModels.get(k).newInstance();
                c.fit(train, classColName);
                ClassifierResult cp = c.predict(test);
                Confusion cm = Confusion.from(test.rvar(classColName), cp.firstClasses());
                double acc = cm.accuracy();
                tacc[k] += acc;
                print(String.format("CV %d, accuracy:%.6f, classifier:%s\n", i + 1, acc, c.name()));
            }
            print("-----------\n");

        }

        for (int k = 0; k < classifierModels.size(); k++) {
            tacc[k] /= (1. * folds);
            print(String.format("Mean accuracy %.6f, for classifier: %s\n", tacc[k], classifierModels.get(k).name()));
        }
    }

    public static void bootstrapValidation(Printer printer, Frame df, String classColName, ClassifierModel c, int bootstraps) {
        Var weights = VarDouble.fill(df.rowCount(), 1.0);
        bootstrapValidation(printer, df, weights, classColName, c, bootstraps, 1.0);
    }

    public static void bootstrapValidation(Printer printer, Frame df, Var weights, String classColName, ClassifierModel c, int bootstraps) {
        bootstrapValidation(printer, df, weights, classColName, c, bootstraps, 1.0);
    }

    public static void bootstrapValidation(Printer printer, Frame df, String classColName, ClassifierModel c, int bootstraps, double p) {
        Var weights = VarDouble.fill(df.rowCount(), 1.0d);
        bootstrapValidation(printer, df, weights, classColName, c, bootstraps, p);
    }

    public static void bootstrapValidation(Printer printer, Frame df, Var weights, String classColName, ClassifierModel c, int bootstraps, double p) {
        print(bootstraps + " bootstrap evaluation\n");
        double total = 0;
        double count = 0;
        for (int i = 0; i < bootstraps; i++) {
//            System.out.println("get sample...");
            int[] rows = SamplingTools.sampleWR(df.rowCount(), (int) (df.rowCount() * p));
//            System.out.println("build predict set ...");
            Frame train = df.mapRows(rows);
//            System.out.println("build test set ...");
            Frame test = df.removeRows(rows);
//            System.out.println("learn predict set ...");
            ClassifierModel cc = c.newInstance();
            cc.fit(train, weights.mapRows(rows), classColName);
//            System.out.println("predict test cases ...");
            Var classes = cc.predict(test).firstClasses();
//            System.out.println("build confusion matrix ...");
            Confusion cm = Confusion.from(test.rvar(classColName), classes);
            printer.printSummary(cm);
            double acc = cm.accuracy();
            System.out.println(String.format("bootstrap(%d) : %.6f", i + 1, acc));
            total += acc;
            count++;
            System.out.flush();
        }
        System.out.println(String.format("Average accuracy: %.6f", total / count));
    }
}
