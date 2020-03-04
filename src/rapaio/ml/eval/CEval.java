package rapaio.ml.eval;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.eval.cmetric.CMetric;
import rapaio.ml.eval.split.Split;
import rapaio.ml.eval.split.SplitStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/26/20.
 */
public class CEval<M extends ClassifierModel<M, R>, R extends ClassifierResult<M>> {

    public static <M extends ClassifierModel<M, R>, R extends ClassifierResult<M>> CEval<M, R>
    newInstance(Frame df, String targetName, ClassifierModel<M, R> model) {
        return new CEval<>(model, df, VarDouble.fill(df.rowCount(), 1), targetName);
    }

    public static <M extends ClassifierModel<M, R>, R extends ClassifierResult<M>> CEval<M, R>
    newInstance(Frame df, Var weights, String targetName, ClassifierModel<M, R> model) {
        return new CEval<>(model, df, weights, targetName);
    }

    private final ClassifierModel<M, R> model;
    private final Frame df;
    private final Var weights;
    private final String targetName;

    private SplitStrategy splitStrategy;
    private int threads = 1;
    private List<CMetric> metrics = new ArrayList<>();

    private CEval(ClassifierModel<M, R> model, Frame df, Var weights, String targetName) {
        this.model = model;
        this.df = df;
        this.weights = weights;
        this.targetName = targetName;
    }

    public ClassifierModel<M, R> getModel() {
        return model;
    }

    public Frame getDf() {
        return df;
    }

    public Var getWeights() {
        return weights;
    }

    public String getTargetName() {
        return targetName;
    }

    public SplitStrategy getSplitStrategy() {
        return splitStrategy;
    }

    public CEval<M, R> withSplitStrategy(SplitStrategy splitStrategy) {
        this.splitStrategy = splitStrategy;
        return this;
    }

    public int getThreads() {
        return threads;
    }

    public CEval<M, R> withThreads(int threads) {
        this.threads = threads;
        return this;
    }

    public List<CMetric> getMetrics() {
        return metrics;
    }

    public CEval<M, R> withMetrics(CMetric... metrics) {
        this.metrics.addAll(Arrays.asList(metrics));
        return this;
    }

    public CEvalResult<M, R> run() {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        List<Future<Run<M, R>>> futures = new LinkedList<>();

        // create features for parallel execution

        List<Split> splits = splitStrategy.generateSplits(df, weights, targetName);

        for (Split split : splits) {
            Future<Run<M, R>> futureRun = executorService.submit(() -> {
                var m = model.newInstance();
                m.fit(split.getTrainDf(), targetName);
                var trainResult = m.predict(split.getTrainDf(), true, true);
                var testResult = m.predict(split.getTestDf(), true, true);
                return new Run<>(split, trainResult, testResult);
            });
            futures.add(futureRun);
        }

        CEvalResult<M, R> result = new CEvalResult<>(this);

        // collect results

        while (!futures.isEmpty()) {
            Iterator<Future<Run<M, R>>> iterator = futures.iterator();
            while (iterator.hasNext()) {
                Future<Run<M, R>> future = iterator.next();
                if (future.isDone()) {
                    try {
                        var run = future.get();
                        result.appendRun(run.getSplit(), run.getTrainResult(), run.getTestResult());
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

    private static class Run<M extends ClassifierModel<M, R>, R extends ClassifierResult<M>> {

        private final Split split;
        private final ClassifierResult<M> trainResult;
        private final ClassifierResult<M> testResult;

        public Run(Split split, ClassifierResult<M> trainResult, ClassifierResult<M> testResult) {
            this.split = split;
            this.trainResult = trainResult;
            this.testResult = testResult;
        }

        public Split getSplit() {
            return split;
        }

        public ClassifierResult<M> getTrainResult() {
            return trainResult;
        }

        public ClassifierResult<M> getTestResult() {
            return testResult;
        }
    }
}
