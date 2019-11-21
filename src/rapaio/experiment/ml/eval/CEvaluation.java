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
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.MappedFrame;
import rapaio.data.Mapping;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.filter.FShuffle;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.eval.metric.Confusion;
import rapaio.printer.Printer;
import rapaio.printer.idea.IdeaPrinter;
import rapaio.sys.WS;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static rapaio.graphics.Plotter.*;
import static rapaio.sys.WS.print;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class CEvaluation {

    public static double cv(Frame df, String classColName, ClassifierModel c, int folds) {
        print("\nCrossValidation with " + folds + " folds\n");

        List<Mapping> strata = buildStrata(df, folds, classColName);
        VarDouble acc = VarDouble.empty();

        for (int i = 0; i < folds; i++) {
            Mapping trainMapping = Mapping.empty();
            Mapping testMapping = Mapping.empty();
            for (int j = 0; j < folds; j++) {
                if (j == i) {
                    testMapping.addAll(strata.get(j).iterator());
                } else {
                    trainMapping.addAll(strata.get(j).iterator());
                }
            }
            Frame train = MappedFrame.byRow(df, trainMapping);
            Frame test = MappedFrame.byRow(df, testMapping);

            ClassifierModel cc = c.newInstance();
            cc.fit(train, classColName);
            ClassifierResult cp = cc.predict(test);

            Confusion conf = Confusion.from(test.rvar(classColName), cp.firstClasses());
            acc.addDouble(conf.accuracy());
            print(String.format("CV %2d:  acc=%.6f, mean=%.6f, se=%.6f\n", i + 1,
                    conf.accuracy(),
                    Mean.of(acc).value(),
                    Variance.of(acc).sdValue()));
        }

        double correct = Mean.of(acc).value();
        print("==============\n");
        print(String.format("Mean accuracy:%.6f\n", correct));
        print(String.format("SE: %.6f     (Standard error)\n", Variance.of(acc).sdValue()));
        return correct;
    }

    private static List<Mapping> buildStrata(Frame df, int folds, String classColName) {
        List<String> dict = df.rvar(classColName).levels();
        List<Mapping> rows = new ArrayList<>();
        for (int i = 0; i < dict.size(); i++) {
            rows.add(Mapping.empty());
        }
        for (int i = 0; i < df.rowCount(); i++) {
            rows.get(df.getInt(i, classColName)).add(i);
        }
        Mapping shuffle = Mapping.empty();
        for (int i = 0; i < dict.size(); i++) {
            rows.get(i).shuffle();
            shuffle.addAll(rows.get(i).iterator());
        }
        List<Mapping> strata = new ArrayList<>();
        for (int i = 0; i < folds; i++) {
            strata.add(Mapping.empty());
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

    public static <M extends ClassifierModel<M, R>, R extends ClassifierResult<M>>
    PlotRunResult plotRunsAcc(Frame train, Frame test, String targetVar, ClassifierModel<M, R> c, int runs, int step) {

        BiConsumer<M, Integer> oldHook = c.runningHook();
        VarInt r = VarInt.empty().withName("runs");
        VarDouble testAcc = VarDouble.empty().withName("test");
        VarDouble trainAcc = VarDouble.empty().withName("predict");
        c.withRunningHook((cs, run) -> {
            if (run % step != 0) {
                return;
            }
            r.addInt(run);
            testAcc.addDouble(Confusion.from(test.rvar(targetVar), c.predict(test).firstClasses()).accuracy());
            trainAcc.addDouble(Confusion.from(train.rvar(targetVar), c.predict(train).firstClasses()).accuracy());

            WS.setPrinter(new IdeaPrinter());
            WS.draw(plot()
                    .lines(r, testAcc, color(1))
                    .lines(r, trainAcc, color(2))
            );
        });
        c.withRuns(runs);
        c.fit(train, targetVar);

        WS.println("Confusion matrix on training data set: ");
        Confusion trainConfusion = Confusion.from(train.rvar(targetVar), c.predict(train).firstClasses());
        trainConfusion.printSummary();
        WS.println();
        WS.println("Confusion matrix on test data set: ");
        Confusion testConfusion = Confusion.from(test.rvar(targetVar), c.predict(test).firstClasses());
        testConfusion.printSummary();

        return new PlotRunResult(r, trainAcc, testAcc, testConfusion, trainConfusion);
    }

    public static class PlotRunResult {
        public final Var runs;
        public final Var trainAcc;
        public final Var testAcc;
        public final Confusion testConfusion;
        public final Confusion trainConfusion;

        public PlotRunResult(Var runs, Var trainAcc, Var testAcc, Confusion testConfusion, Confusion trainConfusion) {
            this.runs = runs;
            this.trainAcc = trainAcc;
            this.testAcc = testAcc;
            this.testConfusion = testConfusion;
            this.trainConfusion = trainConfusion;
        }
    }
}
