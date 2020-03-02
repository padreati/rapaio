package rapaio.ml.eval;

import rapaio.core.stat.Mean;
import rapaio.core.stat.Variance;
import rapaio.data.Frame;
import rapaio.data.Group;
import rapaio.data.Mapping;
import rapaio.data.SolidFrame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.data.VarNominal;
import rapaio.data.group.GroupFun;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.printer.Printable;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;
import rapaio.sys.WS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/26/20.
 */
public class CEval {

    public static <M extends ClassifierModel<M, R>, R extends ClassifierResult<M>> Result<M, R> cv(
            Frame df, String targetName, ClassifierModel<M, R> model, int folds, List<Metric> metrics) {
        return cv(df, targetName, model, folds, 1, metrics, false, 1);
    }

    public static <M extends ClassifierModel<M, R>, R extends ClassifierResult<M>> Result<M, R> cv(
            Frame df, String targetName, ClassifierModel<M, R> model, int folds, int rounds, List<Metric> metrics,
            boolean interactive, int threads) {

        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        List<Future<Run<M, R>>> futures = new LinkedList<>();

        // create features for parallel execution

        for (int round = 0; round < rounds; round++) {
            int const_round = round;
            List<Mapping> foldRows = buildStrata(df, folds, targetName);
            for (int fold = 0; fold < folds; fold++) {
                int const_fold = fold;
                Frame train = df.removeRows(foldRows.get(fold));
                Frame test = df.mapRows(foldRows.get(fold));

                Future<Run<M, R>> futureRun = executorService.submit(() -> {
                    var m = model.newInstance();
                    m.fit(train, targetName);
                    var prediction = m.predict(test, true, true);
                    return new Run<>(const_round, const_fold, prediction, test);
                });
                futures.add(futureRun);
            }

        }

        var result = new Result<>(model, df, targetName, folds, rounds, metrics);

        // collect results

        while (!futures.isEmpty()) {
            Iterator<Future<Run<M, R>>> iterator = futures.iterator();
            while (iterator.hasNext()) {
                Future<Run<M, R>> future = iterator.next();
                if (future.isDone()) {
                    try {
                        var run = future.get();
                        result.appendRun(run.round, run.fold, run.result, run.test.rvar(targetName));
                        if (interactive) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("round:").append(run.round);
                            sb.append(", fold:").append(run.fold).append(":\n");
                            for (Metric metric : metrics) {
                                sb.append("- ").append(metric.name()).append(": ");
                                sb.append(metric.compute(run.result, run.test.rvar(targetName)));
                            }
                            WS.println(sb.toString());
                        }
                        iterator.remove();
                    } catch (InterruptedException | ExecutionException e) {
                        // do nothing
                    }
                }
            }
        }

        // shut down executor

        executorService.shutdownNow();

        return result;
    }

    private static List<Mapping> buildStrata(Frame df, int folds, String classColName) {
        List<String> dict = df.rvar(classColName).levels();
        List<Mapping> rows = dict.stream().map(name -> Mapping.empty()).collect(Collectors.toList());
        for (int i = 0; i < df.rowCount(); i++) {
            rows.get(df.getInt(i, classColName)).add(i);
        }
        Mapping shuffle = Mapping.empty();
        for (int i = 0; i < dict.size(); i++) {
            rows.get(i).shuffle();
            shuffle.addAll(rows.get(i).iterator());
        }
        List<Mapping> strata = IntStream.range(0, folds).mapToObj(i -> Mapping.empty()).collect(Collectors.toList());
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

    private static class Run<M extends ClassifierModel<M, R>, R extends ClassifierResult<M>> {
        private final int round;
        private final int fold;
        private ClassifierResult<M> result;
        private Frame test;

        public Run(final int round, final int fold, ClassifierResult<M> result, Frame test) {
            this.round = round;
            this.fold = fold;
            this.result = result;
            this.test = test;
        }
    }

    /**
     * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/26/20.
     */
    public static class Result<M extends ClassifierModel<M, R>, R extends ClassifierResult<M>> implements Printable {

        final ClassifierModel<M, R> model;
        final Frame df;
        final String targetName;

        final int folds;
        final int rounds;
        final List<Metric> metrics;

        private Frame scores;

        public Result(ClassifierModel<M, R> model, Frame df, String targetName, int folds, int rounds, List<Metric> metrics) {
            this.model = model;
            this.df = df;
            this.targetName = targetName;
            this.folds = folds;
            this.rounds = rounds;
            this.metrics = metrics;

            List<Var> vars = new ArrayList<>();
            vars.add(VarInt.empty().withName("round"));
            vars.add(VarInt.empty().withName("fold"));
            for (Metric metric : metrics) {
                vars.add(VarDouble.empty().withName(metric.name()));
            }
            scores = SolidFrame.byVars(vars);

            //  fill scores heading columns

            for (int i = 0; i < rounds; i++) {
                for (int j = 0; j < folds; j++) {
                    scores.addRows(1);

                    int lastRow = scores.rowCount() - 1;
                    scores.setInt(lastRow, "round", i);
                    scores.setInt(lastRow, "fold", j);
                }
            }
        }

        public ClassifierModel<M, R> getModel() {
            return model;
        }

        public Frame getFrame() {
            return df;
        }

        public int getRounds() {
            return rounds;
        }

        public int getFolds() {
            return folds;
        }

        public String getTargetName() {
            return targetName;
        }

        public Frame getRawScores() {
            return scores;
        }

        public double getMeanScore(String metric) {
            return Mean.of(scores.rvar(metric)).value();
        }

        void appendRun(int round, int fold, ClassifierResult<M> result, Var testVar) {
            int row = round * folds + fold;
            for (Metric metric : metrics) {
                scores.setDouble(row, metric.name(), metric.compute(result, testVar));
            }
        }

        private String toContentName(Printer printer, POption<?>... options) {
            StringBuilder sb = new StringBuilder();
            sb.append("Model:\n");
            sb.append(model.fullName()).append("\n");
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

            for (Metric metric : metrics) {
                global.addRows(1);
                global.setLabel(global.rowCount() - 1, "metric", metric.name());
                global.setDouble(global.rowCount() - 1, "mean", Mean.of(scores.rvar(metric.name())).value());
                global.setDouble(global.rowCount() - 1, "std", Variance.of(scores.rvar(metric.name())).sdValue());
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
            sb.append(model.fullName()).append("\n");

            sb.append("Raw scores:\n");
            sb.append("===========\n");
            sb.append(scores.toFullContent(printer, options));
            sb.append("\n");

            sb.append("Round scores:\n");
            sb.append("=============\n");
            List<GroupFun> groupFuns = new ArrayList<>();
            for (Metric metric : metrics) {
                groupFuns.add(Group.mean(metric.name()));
                groupFuns.add(Group.std(metric.name()));
            }
            sb.append(Group.from(scores, "round").aggregate(groupFuns.toArray(GroupFun[]::new))
                    .toFrame()
                    .toFullContent(printer, options));
            sb.append("\n");

            sb.append(toContentCVScore(printer, options));

            return sb.toString();
        }
    }

    public interface Metric extends Serializable {

        String name();

        <M extends ClassifierModel<M, R>, R extends ClassifierResult<M>> double compute(ClassifierResult<M> result, Var testVar);

        static Metric accuracy() {
            return new Accuracy(true);
        }

        static Metric accuracy(boolean normalize) {
            return new Accuracy(normalize);
        }

        static Metric logloss() {
            return new LogLoss(1e-15);
        }

        static Metric logloss(double eps) {
            return new LogLoss(eps);
        }
    }
}


class Accuracy implements CEval.Metric {

    private static final long serialVersionUID = -3526955062164344415L;

    private final boolean normalize;

    public Accuracy(boolean normalize) {
        this.normalize = normalize;
    }

    @Override
    public String name() {
        return "Accuracy";
    }

    @Override
    public <M extends ClassifierModel<M, R>, R extends ClassifierResult<M>> double compute(ClassifierResult<M> result, Var testVar) {
        Var predictionVar = result.firstClasses();
        double match = 0;
        for (int i = 0; i < testVar.rowCount(); i++) {
            if (predictionVar.getLabel(i).equals(testVar.getLabel(i))) {
                match++;
            }
        }
        return normalize ? match / testVar.rowCount() : match;
    }
}

class LogLoss implements CEval.Metric {

    private static final long serialVersionUID = 8850076650664844719L;

    private final double eps;

    public LogLoss(double eps) {
        this.eps = eps;
    }

    @Override
    public String name() {
        return "LogLoss";
    }

    @Override
    public <M extends ClassifierModel<M, R>, R extends ClassifierResult<M>> double compute(ClassifierResult<M> result, Var testVar) {
        var densities = result.firstDensity();
        double logloss = 0;
        for (int i = 0; i < testVar.rowCount(); i++) {
            logloss -= Math.log(Math.max(eps, Math.min(1 - eps, densities.getDouble(i, testVar.getLabel(i)))));
        }
        return logloss;
    }
}