package rapaio.ml.eval;

import lombok.Getter;
import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Group;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;
import rapaio.data.filter.FRefSort;
import rapaio.data.group.GroupFun;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.eval.metric.ClassifierMetric;
import rapaio.ml.eval.split.Split;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/20.
 */
public class ClassifierEvaluationResult implements Printable {

    private static final String FIELD_DATASET = "dataset";
    private static final String FIELD_ROUND = "round";
    private static final String FIELD_FOLD = "fold";

    @Getter
    private final ClassifierEvaluation eval;
    @Getter
    private Frame trainScores;
    @Getter
    private Frame testScores;

    private final ReentrantLock scoresLock = new ReentrantLock();

    public ClassifierEvaluationResult(ClassifierEvaluation eval) {
        this.eval = eval;

        List<Var> vars = new ArrayList<>();
        vars.add(VarNominal.empty().name(FIELD_DATASET));
        vars.add(VarInt.empty().name(FIELD_ROUND));
        vars.add(VarInt.empty().name(FIELD_FOLD));
        for (ClassifierMetric metric : eval.getMetrics()) {
            vars.add(VarDouble.empty().name(metric.getName()));
        }
        trainScores = SolidFrame.byVars(vars).copy();
        testScores = trainScores.copy();
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
            trainScores.setInt(lastRow, FIELD_ROUND, split.getRound());
            trainScores.setInt(lastRow, FIELD_FOLD, split.getFold());
            for (ClassifierMetric metric : eval.getMetrics()) {
                trainScores.setDouble(lastRow, metric.getName(),
                        metric.compute(split.getTrainDf().rvar(eval.getTargetName()), trainResult).getScore().getValue());
            }
            trainScores = trainScores.fapply(FRefSort.by(
                    trainScores.rvar(FIELD_ROUND).refComparator(),
                    trainScores.rvar(FIELD_FOLD).refComparator()
            )).copy();

            testScores.addRows(1);
            testScores.setLabel(lastRow, FIELD_DATASET, "test");
            testScores.setInt(lastRow, FIELD_ROUND, split.getRound());
            testScores.setInt(lastRow, FIELD_FOLD, split.getFold());
            for (ClassifierMetric metric : eval.getMetrics()) {
                testScores.setDouble(lastRow,
                        metric.getName(), metric.compute(split.getTestDf().rvar(eval.getTargetName()), testResult).getScore().getValue());
            }

            testScores = testScores.fapply(FRefSort.by(
                    testScores.rvar(FIELD_ROUND).refComparator(),
                    testScores.rvar(FIELD_FOLD).refComparator()
            )).copy();
        } finally {
            scoresLock.unlock();
        }
    }

    private String toContentName(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("Model:\n");
        sb.append(eval.getModel().fullName()).append("\n");
        return sb.toString();
    }

    private String toContentCVScore(Printer printer, POption<?>... options) {
        printer.withOptions(options);
        StringBuilder sb = new StringBuilder();
        sb.append("CV score in training data\n");
        sb.append("=========================\n");
        Var datasetVar = VarNominal.empty().name("dataset");
        Var metricVar = VarNominal.empty().name("metric");
        Var meanVar = VarDouble.empty().name("mean");
        Var stdVar = VarDouble.empty().name("std");
        Frame global = SolidFrame.byVars(datasetVar, metricVar, meanVar, stdVar);

        for (ClassifierMetric metric : eval.getMetrics()) {
            global.addRows(1);
            global.setLabel(global.rowCount() - 1, "dataset", "test");
            global.setLabel(global.rowCount() - 1, "metric", metric.getName());
            global.setDouble(global.rowCount() - 1, "mean", Mean.of(testScores.rvar(metric.getName())).value());
            global.setDouble(global.rowCount() - 1, "std", Variance.of(testScores.rvar(metric.getName())).sdValue());
        }
        for (ClassifierMetric metric : eval.getMetrics()) {
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
    public String toContent(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();

        sb.append(toContentName(printer, options));
        sb.append(toContentCVScore(printer, options));

        return sb.toString();
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("Model:\n");
        sb.append(eval.getModel().fullName()).append("\n");

        sb.append("Raw scores:\n");
        sb.append("===========\n");
        sb.append(testScores.bindRows(trainScores).toFullContent(printer, options));
        sb.append("\n");

        sb.append("Round scores:\n");
        sb.append("=============\n");
        List<GroupFun> groupFuns = new ArrayList<>();
        for (ClassifierMetric metric : eval.getMetrics()) {
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