package rapaio.ml.eval;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.experiment.ml.classifier.ensemble.CForest;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.eval.metric.ClassifierMetric;
import rapaio.ml.eval.split.Split;
import rapaio.ml.eval.split.SplitStrategy;

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
@Builder
public class ClassifierEvaluation {

    @NonNull
    private final ClassifierModel model;

    @NonNull
    private final Frame df;

    private final Var weights;
    @NonNull
    private final String targetName;
    @NonNull
    private final SplitStrategy splitStrategy;

    @Builder.Default
    private final int threads = 1;

    @Singular
    private final List<ClassifierMetric> metrics;

    public ClassifierEvaluationResult run() {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        List<Future<Run>> futures = new LinkedList<>();

        // create features for parallel execution

        List<Split> splits = splitStrategy.generateSplits(df, weights);

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

        CForest cForest = CForest.newRF();
        ClassifierEvaluation.builder().model(cForest).build();
    }
}
