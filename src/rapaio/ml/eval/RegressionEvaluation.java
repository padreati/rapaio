/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2021 Aurelian Tutuianu
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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.eval.metric.RegressionMetric;
import rapaio.ml.eval.split.Split;
import rapaio.ml.eval.split.SplitStrategy;
import rapaio.ml.regression.RegressionModel;
import rapaio.ml.regression.RegressionResult;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/6/19.
 */
@Builder
@Getter
public class RegressionEvaluation {

    @NonNull
    private final RegressionModel model;
    @NonNull
    private final Frame df;

    private final Var weights;
    @NonNull
    private final String targetName;
    @NonNull
    private final SplitStrategy splitStrategy;

    @Builder.Default
    private final int threads = Runtime.getRuntime().availableProcessors() - 1;

    @Singular
    private final List<RegressionMetric> metrics;

    @Builder.Default
    private final boolean debug = false;

    public RegressionEvaluationResult run() {
        List<Task> tasks = new LinkedList<>();

        // create features for parallel execution

        List<Split> splits = splitStrategy.generateSplits(df, weights);

        for (Split split : splits) {
            tasks.add(new Task(model, targetName, split));
        }

        RegressionEvaluationResult result = new RegressionEvaluationResult(this);


        ExecutorService pool = Executors.newFixedThreadPool(threads);
        List<Future<Run>> futures = null;
        try {
            futures = pool.invokeAll(tasks);

            // collect results

            while (!futures.isEmpty()) {
                Iterator<Future<RegressionEvaluation.Run>> iterator = futures.iterator();
                while (iterator.hasNext()) {
                    Future<RegressionEvaluation.Run> future = iterator.next();
                    if (future.isDone()) {
                        try {
                            var run = future.get();
                            result.appendRun(run.getSplit(), run.getTrainResult(), run.getTestResult());
                            iterator.remove();
                        } catch (InterruptedException | ExecutionException e) {
                            // do nothing
                            iterator.remove();
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            shutdownAndAwaitTermination(pool);
        }

        shutdownAndAwaitTermination(pool);

        return result;
    }

    void shutdownAndAwaitTermination(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    @AllArgsConstructor
    @Getter
    private static class Run {

        private final Split split;
        private final RegressionResult trainResult;
        private final RegressionResult testResult;
    }

    @AllArgsConstructor
    private static class Task implements Callable<RegressionEvaluation.Run> {

        private final RegressionModel model;
        private final String targetName;
        private final Split split;

        @Override
        public Run call() throws Exception {
            var m = model.newInstance();
            m.fit(split.getTrainDf(), targetName);
            var trainResult = m.predict(split.getTrainDf());
            var testResult = m.predict(split.getTestDf());
            return new RegressionEvaluation.Run(split, trainResult, testResult);
        }
    }
}
