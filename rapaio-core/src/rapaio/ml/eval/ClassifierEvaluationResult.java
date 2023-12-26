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
 *    Copyright 2020 Aurelian Tutuianu
 *    Copyright 2021 Aurelian Tutuianu
 *    Copyright 2022 Aurelian Tutuianu
 *    Copyright 2023 Aurelian Tutuianu
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

package rapaio.ml.eval;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Group;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;
import rapaio.data.group.GroupFun;
import rapaio.data.preprocessing.RefSort;
import rapaio.ml.eval.metric.ClassifierMetric;
import rapaio.ml.eval.split.Split;
import rapaio.ml.model.ClassifierResult;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POpt;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/20.
 */
public class ClassifierEvaluationResult implements Printable {

    private static final String FIELD_DATASET = "dataset";
    private static final String FIELD_ROUND = "round";
    private static final String FIELD_FOLD = "fold";

    private final ClassifierEvaluation eval;
    private Frame trainScores;
    private Frame testScores;

    private final ReentrantLock scoresLock = new ReentrantLock();

    public ClassifierEvaluationResult(ClassifierEvaluation eval) {
        this.eval = eval;

        List<Var> vars = new ArrayList<>();
        vars.add(VarNominal.empty().name(FIELD_DATASET));
        vars.add(VarInt.empty().name(FIELD_ROUND));
        vars.add(VarInt.empty().name(FIELD_FOLD));
        for (ClassifierMetric metric : eval.metrics.get()) {
            vars.add(VarDouble.empty().name(metric.getName()));
        }
        trainScores = SolidFrame.byVars(vars).copy();
        testScores = trainScores.copy();
    }

    public ClassifierEvaluation getEval() {
        return eval;
    }

    public Frame getTestScores() {
        return testScores;
    }

    public Frame getTrainScores() {
        return trainScores;
    }

    public double getMeanTrainScore(String metric) {
        return Mean.of(trainScores.rvar(metric)).value();
    }

    public double getMeanTestScore(String metric) {
        return Mean.of(testScores.rvar(metric)).value();
    }

    void appendRun(Split split, ClassifierResult trainResult, ClassifierResult testResult) {

        scoresLock.lock();
        try {
            int lastRow = trainScores.rowCount();

            trainScores.addRows(1);
            trainScores.setLabel(lastRow, FIELD_DATASET, "train");
            trainScores.setInt(lastRow, FIELD_ROUND, split.round());
            trainScores.setInt(lastRow, FIELD_FOLD, split.fold());
            for (ClassifierMetric metric : eval.metrics.get()) {
                trainScores.setDouble(lastRow, metric.getName(),
                        metric.compute(split.trainDf().rvar(eval.targetName.get()), trainResult).getScore().value());
            }
            trainScores = trainScores.fapply(RefSort.by(
                    trainScores.rvar(FIELD_ROUND).refComparator(),
                    trainScores.rvar(FIELD_FOLD).refComparator()
            )).copy();

            testScores.addRows(1);
            testScores.setLabel(lastRow, FIELD_DATASET, "test");
            testScores.setInt(lastRow, FIELD_ROUND, split.round());
            testScores.setInt(lastRow, FIELD_FOLD, split.fold());
            for (ClassifierMetric metric : eval.metrics.get()) {
                testScores.setDouble(lastRow,
                        metric.getName(), metric.compute(split.testDf().rvar(eval.targetName.get()), testResult).getScore().value());
            }

            testScores = testScores.fapply(RefSort.by(
                    testScores.rvar(FIELD_ROUND).refComparator(),
                    testScores.rvar(FIELD_FOLD).refComparator()
            )).copy();
        } finally {
            scoresLock.unlock();
        }
    }

    private String toContentName(Printer printer, POpt<?>... options) {
        return "Model:\n"
                + eval.model.get().fullName() + "\n";
    }

    private String toContentCVScore(Printer printer, POpt<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("CV score in training data\n");
        sb.append("=========================\n");
        Var datasetVar = VarNominal.empty().name("dataset");
        Var metricVar = VarNominal.empty().name("metric");
        Var meanVar = VarDouble.empty().name("mean");
        Var stdVar = VarDouble.empty().name("std");
        Frame global = SolidFrame.byVars(datasetVar, metricVar, meanVar, stdVar);

        for (ClassifierMetric metric : eval.metrics.get()) {
            global.addRows(1);
            global.setLabel(global.rowCount() - 1, "dataset", "test");
            global.setLabel(global.rowCount() - 1, "metric", metric.getName());
            global.setDouble(global.rowCount() - 1, "mean", Mean.of(testScores.rvar(metric.getName())).value());
            global.setDouble(global.rowCount() - 1, "std", Variance.of(testScores.rvar(metric.getName())).sdValue());
        }
        for (ClassifierMetric metric : eval.metrics.get()) {
            global.addRows(1);
            global.setLabel(global.rowCount() - 1, "dataset", "train");
            global.setLabel(global.rowCount() - 1, "metric", metric.getName());
            global.setDouble(global.rowCount() - 1, "mean", Mean.of(trainScores.rvar(metric.getName())).value());
            global.setDouble(global.rowCount() - 1, "std", Variance.of(trainScores.rvar(metric.getName())).sdValue());
        }
        sb.append(global.toFullContent(printer, options));
        sb.append("\n");

        return sb.toString();
    }

    @Override
    public String toContent(Printer printer, POpt<?>... options) {
        return toContentName(printer, options) + toContentCVScore(printer, options);
    }

    @Override
    public String toFullContent(Printer printer, POpt<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("Model:\n");
        sb.append(eval.model.get().fullName()).append("\n");

        sb.append("Raw scores:\n");
        sb.append("===========\n");
        sb.append(testScores.bindRows(trainScores).toFullContent(printer, options));
        sb.append("\n");

        sb.append("Round scores:\n");
        sb.append("=============\n");
        List<GroupFun> groupFuns = new ArrayList<>();
        for (ClassifierMetric metric : eval.metrics.get()) {
            groupFuns.add(Group.mean(metric.getName()));
            groupFuns.add(Group.std(metric.getName()));
        }
        sb.append(Group.from(testScores.bindRows(trainScores), "dataset", "round").aggregate(groupFuns.toArray(GroupFun[]::new))
                .toFrame()
                .toFullContent(printer, options));
        sb.append("\n");

        sb.append(toContentCVScore(printer, options));

        return sb.toString();
    }
}