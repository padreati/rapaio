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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
    private final int threads = 1;

    @Singular
    private final List<RegressionMetric> metrics;

    @Builder.Default
    private final boolean debug = false;

    public RegressionEvaluationResult run() {
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        List<Future<RegressionEvaluation.Run>> futures = new LinkedList<>();

        // create features for parallel execution

        List<Split> splits = splitStrategy.generateSplits(df, weights);

        for (Split split : splits) {
            Future<RegressionEvaluation.Run> futureRun = executorService.submit(() -> {
                var m = model.newInstance();
                m.fit(split.getTrainDf(), targetName);
                var trainResult = m.predict(split.getTrainDf());
                var testResult = m.predict(split.getTestDf());
                return new RegressionEvaluation.Run(split, trainResult, testResult);
            });
            futures.add(futureRun);
        }

        RegressionEvaluationResult result = new RegressionEvaluationResult(this);

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
        private final RegressionResult trainResult;
        private final RegressionResult testResult;
    }
}
