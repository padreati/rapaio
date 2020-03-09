package rapaio.ml.eval;

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
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.eval.cmetric.CMetric;
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
public class CEvalResult<M extends ClassifierModel<M, R>, R extends ClassifierResult<M>> implements Printable {

    private static final String FIELD_ROUND = "round";
    private static final String FIELD_FOLD = "fold";

    private final CEval<M, R> ceval;
    private ReentrantLock scoresLock = new ReentrantLock();
    private Frame trainScores;
    private Frame testScores;

    public CEvalResult(CEval<M, R> ceval) {
        this.ceval = ceval;

        List<Var> vars = new ArrayList<>();
        vars.add(VarInt.empty().withName(FIELD_ROUND));
        vars.add(VarInt.empty().withName(FIELD_FOLD));
        for (CMetric metric : ceval.getMetrics()) {
            vars.add(VarDouble.empty().withName(metric.name()));
        }
        trainScores = SolidFrame.byVars(vars).copy();
        testScores = trainScores.copy();
    }

    public ClassifierModel<M, R> getModel() {
        return ceval.getModel();
    }

    public Frame getFrame() {
        return ceval.getDf();
    }

    public Var getWeights() {
        return ceval.getWeights();
    }

    public String getTargetName() {
        return ceval.getTargetName();
    }

    public Frame getTrainScores() {
        return trainScores;
    }

    public Frame getTestScores() {
        return testScores;
    }

    public double getMeanTrainScore(String metric) {
        return Mean.of(trainScores.rvar(metric)).value();
    }

    public double getMeanTestScore(String metric) {
        return Mean.of(testScores.rvar(metric)).value();
    }

    void appendRun(Split split, ClassifierResult<M> trainResult, ClassifierResult<M> testResult) {

        scoresLock.lock();
        try {
            int lastRow = trainScores.rowCount();

            trainScores.addRows(1);
            trainScores.setInt(lastRow, FIELD_ROUND, split.getRound());
            trainScores.setInt(lastRow, FIELD_FOLD, split.getFold());
            for (CMetric metric : ceval.getMetrics()) {
                trainScores.setDouble(lastRow, metric.name(), metric.compute(trainResult, split.getTrainDf().rvar(split.getTargetName())));
            }
            trainScores = trainScores.fapply(FRefSort.by(
                    trainScores.rvar(FIELD_ROUND).refComparator(),
                    trainScores.rvar(FIELD_FOLD).refComparator()
            )).copy();

            testScores.addRows(1);
            testScores.setInt(lastRow, FIELD_ROUND, split.getRound());
            testScores.setInt(lastRow, FIELD_FOLD, split.getFold());
            for (CMetric metric : ceval.getMetrics()) {
                testScores.setDouble(lastRow, metric.name(), metric.compute(trainResult, split.getTrainDf().rvar(split.getTargetName())));
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
        sb.append(ceval.getModel().fullName()).append("\n");
        return sb.toString();
    }

    private String toContentCVScore(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("CV score\n");
        sb.append("=============\n");
        Var metricVar = VarNominal.empty().withName("metric");
        Var meanVar = VarDouble.empty().withName("mean");
        Var stdVar = VarDouble.empty().withName("std");
        Frame global = SolidFrame.byVars(metricVar, meanVar, stdVar);

        for (CMetric metric : ceval.getMetrics()) {
            global.addRows(1);
            global.setLabel(global.rowCount() - 1, "metric", metric.name());
            global.setDouble(global.rowCount() - 1, "mean", Mean.of(trainScores.rvar(metric.name())).value());
            global.setDouble(global.rowCount() - 1, "std", Variance.of(trainScores.rvar(metric.name())).sdValue());
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
        sb.append(ceval.getModel().fullName()).append("\n");

        sb.append("Raw scores:\n");
        sb.append("===========\n");
        sb.append(trainScores.toFullContent(printer, options));
        sb.append("\n");

        sb.append("Round scores:\n");
        sb.append("=============\n");
        List<GroupFun> groupFuns = new ArrayList<>();
        for (CMetric metric : ceval.getMetrics()) {
            groupFuns.add(Group.mean(metric.name()));
            groupFuns.add(Group.std(metric.name()));
        }
        sb.append(Group.from(trainScores, "round").aggregate(groupFuns.toArray(GroupFun[]::new))
                .toFrame()
                .toFullContent(printer, options));
        sb.append("\n");

        sb.append(toContentCVScore(printer, options));

        return sb.toString();
    }
}