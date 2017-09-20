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

import rapaio.core.CoreTools;
import rapaio.core.RandomSource;
import rapaio.core.SamplingTools;
import rapaio.data.*;
import rapaio.data.filter.frame.FFShuffle;
import rapaio.ml.classifier.CFit;
import rapaio.ml.classifier.Classifier;
import rapaio.ml.eval.Confusion;
import rapaio.ml.eval.ROC;
import rapaio.printer.IdeaPrinter;
import rapaio.sys.WS;
import rapaio.util.Pin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static rapaio.graphics.Plotter.*;
import static rapaio.sys.WS.print;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a>
 */
@Deprecated
public class CEvaluation {

    public static double cv(Frame df, String classColName, Classifier c, int folds) {
        print("\nCrossValidation with " + folds + " folds\n");
//        print("Model: \n");
//        c.newInstance().printSummary();

        List<List<Integer>> strata = buildStrata(df, folds, classColName);


        NumericVar acc = NumericVar.empty();

        for (int i = 0; i < folds; i++) {
            Mapping trainMapping = Mapping.empty();
            Mapping testMapping = Mapping.empty();
            for (int j = 0; j < folds; j++) {
                if (j == i) {
                    testMapping.addAll(strata.get(j));
                } else {
                    trainMapping.addAll(strata.get(j));
                }
            }
            Frame train = MappedFrame.byRow(df, trainMapping);
            Frame test = MappedFrame.byRow(df, testMapping);

            Classifier cc = c.newInstance();
            cc.train(train, classColName);
            CFit cp = cc.fit(test);

            Confusion conf = new Confusion(test.var(classColName), cp.firstClasses());
            acc.addValue(conf.accuracy());
            print(String.format("CV %2d:  acc=%.6f, mean=%.6f, se=%.6f\n", i + 1,
                    conf.accuracy(),
                    CoreTools.mean(acc).value(),
                    CoreTools.variance(acc).sdValue()));
        }

        double correct = CoreTools.mean(acc).value();
        print("==============\n");
        print(String.format("Mean accuracy:%.6f\n", correct));
        print(String.format("SE: %.6f     (Standard error)\n", CoreTools.variance(acc).sdValue()));
        return correct;
    }

    private static List<List<Integer>> buildStrata(Frame df, int folds, String classColName) {
        String[] dict = df.var(classColName).levels();
        List<List<Integer>> rows = IntStream.range(0, dict.length).boxed().map(ArrayList<Integer>::new).collect(toList());
        for (int i = 0; i < df.rowCount(); i++) {
            rows.get(df.index(i, classColName)).add(i);
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

    public static void multiCv(Frame df, String classColName, List<Classifier> classifiers, int folds) {
        print("CrossValidation with " + folds + " folds\n");
        df = new FFShuffle().fitApply(df);
        double[] tacc = new double[classifiers.size()];

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

            for (int k = 0; k < classifiers.size(); k++) {
                Classifier c = classifiers.get(k).newInstance();
                c.train(train, classColName);
                CFit cp = c.fit(test);
                Confusion cm = new Confusion(test.var(classColName), cp.firstClasses());
//                cm.printSummary();
                double acc = cm.accuracy();
                tacc[k] += acc;
                print(String.format("CV %d, accuracy:%.6f, classifier:%s\n", i + 1, acc, c.name()));
            }
            print("-----------\n");

        }

        for (int k = 0; k < classifiers.size(); k++) {
            tacc[k] /= (1. * folds);
            print(String.format("Mean accuracy %.6f, for classifier: %s\n", tacc[k], classifiers.get(k).name()));
        }
    }

    public static void bootstrapValidation(Frame df, String classColName, Classifier c, int bootstraps) {
        Var weights = NumericVar.fill(df.rowCount(), 1.0);
        bootstrapValidation(df, weights, classColName, c, bootstraps, 1.0);
    }

    public static void bootstrapValidation(Frame df, Var weights, String classColName, Classifier c, int bootstraps) {
        bootstrapValidation(df, weights, classColName, c, bootstraps, 1.0);
    }

    public static void bootstrapValidation(Frame df, String classColName, Classifier c, int bootstraps, double p) {
        Var weights = NumericVar.fill(df.rowCount(), 1.0d);
        bootstrapValidation(df, weights, classColName, c, bootstraps, p);
    }

    public static void bootstrapValidation(Frame df, Var weights, String classColName, Classifier c, int bootstraps, double p) {
        print(bootstraps + " bootstrap evaluation\n");
        double total = 0;
        double count = 0;
        for (int i = 0; i < bootstraps; i++) {
//            System.out.println("get sample...");
            int[] rows = SamplingTools.sampleWR(df.rowCount(), (int) (df.rowCount() * p));
//            System.out.println("build train set ...");
            Frame train = df.mapRows(rows);
//            System.out.println("build test set ...");
            Frame test = df.removeRows(rows);
//            System.out.println("learn train set ...");
            Classifier cc = c.newInstance();
            cc.train(train, weights.mapRows(rows), classColName);
//            System.out.println("fit test cases ...");
            Var classes = cc.fit(test).firstClasses();
//            System.out.println("build confusion matrix ...");
            Confusion cm = new Confusion(test.var(classColName), classes);
            cm.printSummary();
            double acc = cm.accuracy();
            System.out.println(String.format("bootstrap(%d) : %.6f", i + 1, acc));
            total += acc;
            count++;
            System.out.flush();
        }
        System.out.println(String.format("Average accuracy: %.6f", total / count));
    }

    public static PlotRunResult plotRunsAcc(Frame train, Frame test, String targetVar, Classifier c, int runs, int step) {

        BiConsumer<Classifier, Integer> oldHook = c.runningHook();
        IndexVar r = IndexVar.empty().withName("runs");
        NumericVar testAcc = NumericVar.empty().withName("test");
        NumericVar trainAcc = NumericVar.empty().withName("train");
        c.withRunningHook((cs, run) -> {

            if (run % step != 0) {
                return;
            }
            r.addIndex(run);
            testAcc.addValue(new Confusion(test.var(targetVar), c.fit(test).firstClasses()).accuracy());
            trainAcc.addValue(new Confusion(train.var(targetVar), c.fit(train).firstClasses()).accuracy());

            WS.setPrinter(new IdeaPrinter());
            WS.draw(plot()
                    .lines(r, testAcc, color(1))
                    .lines(r, trainAcc, color(2))
            );
        });
        c.withRuns(runs);
        c.train(train, targetVar);

        WS.println("Confusion matrix on training data set: ");
        Confusion trainConfusion = new Confusion(train.var(targetVar), c.fit(train).firstClasses());
        trainConfusion.printSummary();
        WS.println();
        WS.println("Confusion matrix on test data set: ");
        Confusion testConfusion = new Confusion(test.var(targetVar), c.fit(test).firstClasses());
        testConfusion.printSummary();

        return new PlotRunResult(r, trainAcc, testAcc, testConfusion, trainConfusion);
    }

    public static PlotRunResult plotRunsRoc(
            Frame train,
            Frame test,
            String targetVar,
            String label,
            Classifier cc,
            int runs,
            int step,
            boolean alterClassifier) {

        Classifier c = alterClassifier ? cc : cc.newInstance();
        BiConsumer<Classifier, Integer> oldHook = c.runningHook();
        IndexVar r = IndexVar.empty().withName("runs");
        NumericVar testAuc = NumericVar.empty().withName("test");
        NumericVar trainAuc = NumericVar.empty().withName("train");
        Pin<Double> prevAuc = new Pin<>(0.0);
        c.withRunningHook((cs, run) -> {

            if ((run % step != 0) && run != 1) {
                return;
            }
            r.addIndex(run);
            ROC roc = ROC.from(c.fit(test).firstDensity().var(label), test.var(targetVar), label);
            WS.draw(rocCurve(roc).title("testAuc: " + WS.formatFlex(roc.auc()) + ", run: " + run));
            testAuc.addValue(roc.auc());
            WS.println("testAuc: " + WS.formatLong(roc.auc()) + ", run: " + run + ", auc gain: " + WS.formatLong(roc.auc()-prevAuc.get()));
            prevAuc.set(roc.auc());
//            trainAuc.addValue(new ROC(c.fit(train).firstDensity().var(label), train.var(targetVar), label).auc());

//            WS.draw(plot()
//                            .lines(r, testAuc, color(1))
//                            .title("testAuc: " + WS.formatFlex(testAuc.value(testAuc.rowCount() - 1)))
//            );
        });
        c.withRuns(runs);
        c.train(train, targetVar);

//        WS.println("Confusion matrix on training data set: ");
        Confusion trainConfusion = new Confusion(train.var(targetVar), c.fit(train).firstClasses());
        trainConfusion.printSummary();
//        WS.println();
        WS.println("Confusion matrix on test data set: ");
        Confusion testConfusion = new Confusion(test.var(targetVar), c.fit(test).firstClasses());
        testConfusion.printSummary();


        return new PlotRunResult(r, trainAuc, testAuc, testConfusion, trainConfusion);
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
