package rapaio.ml.eval;

import lombok.AllArgsConstructor;
import lombok.Getter;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.datasets.Datasets;
import rapaio.experiment.ml.classifier.tree.CTree;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.eval.metric.Accuracy;
import rapaio.ml.eval.metric.ClassifierMetric;
import rapaio.ml.eval.split.KFold;
import rapaio.ml.eval.split.Split;
import rapaio.ml.eval.split.SplitStrategy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Classifier evaluation tool.
 *
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/26/20.
 */
@Getter
public class ClassifierEvaluation {

    public static ClassifierEvaluation eval(Frame df, String targetName, ClassifierModel model, ClassifierMetric... metrics) {
        ClassifierEvaluation eval = new ClassifierEvaluation()
                .withData(df)
                .withTarget(targetName)
                .withModel(model);
        for (ClassifierMetric metric : metrics) {
            eval.withMetric(metric);
        }
        return eval;
    }

    public static ClassifierEvaluation cv(Frame df, String targetName, ClassifierModel model, int folds, ClassifierMetric... metrics) {
        ClassifierEvaluation eval = new ClassifierEvaluation()
                .withData(df)
                .withTarget(targetName)
                .withModel(model)
                .withSplit(new KFold(folds));
        for (ClassifierMetric metric : metrics) {
            eval.withMetric(metric);
        }
        return eval;
    }

    private ClassifierModel model;
    private Frame data;
    private Var weights;
    private String targetName;
    private SplitStrategy splitStrategy;
    private int threads = 1;
    private List<ClassifierMetric> metrics = new ArrayList<>();

    public ClassifierEvaluation withData(Frame data) {
        this.data = data;
        return this;
    }

    public ClassifierEvaluation withTarget(String targetName) {
        this.targetName = targetName;
        return this;
    }

    public ClassifierEvaluation withModel(ClassifierModel model) {
        this.model = model;
        return this;
    }

    public ClassifierEvaluation withSplit(SplitStrategy splitStrategy) {
        this.splitStrategy = splitStrategy;
        return this;
    }

    public ClassifierEvaluation withThreads(int threads) {
        this.threads = threads;
        return this;
    }

    public ClassifierEvaluation withMetric(ClassifierMetric metric) {
        this.metrics.add(metric);
        return this;
    }

    public ClassifierEvaluationResult run() {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        List<Future<Run>> futures = new LinkedList<>();

        // create features for parallel execution

        List<Split> splits = splitStrategy.generateSplits(data, weights);

        for (Split split : splits) {
            Future<Run> futureRun = executorService.submit(() -> {
                var m = model.newInstance();
                m.fit(split.getTrainDf(), targetName);
                var trainResult = m.predict(split.getTrainDf(), true, true);
                var testResult = m.predict(split.getTestDf(), true, true);
                return new Run(split, trainResult, testResult);
            });
            futures.add(futureRun);
        }

        ClassifierEvaluationResult result = new ClassifierEvaluationResult(this);

        // collect results
        while (!futures.isEmpty()) {
            Iterator<Future<Run>> iterator = futures.iterator();
            while (iterator.hasNext()) {
                Future<Run> future = iterator.next();
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

    @AllArgsConstructor
    @Getter
    private static class Run {

        private final Split split;
        private final ClassifierResult trainResult;
        private final ClassifierResult testResult;
    }

    public static void main(String[] args) {
        Frame df = Datasets.loadIrisDataset();
        CTree tree = CTree.newC45().withMaxDepth(3);
        var result = ClassifierEvaluation.cv(df, "class", tree, 10, Accuracy.newMetric(true)).run();

        result.printFullContent();
    }
}
