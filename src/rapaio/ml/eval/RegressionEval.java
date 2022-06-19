/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2022 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.eval;

import java.io.Serial;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.ml.common.ListParam;
import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;
import rapaio.ml.eval.metric.RegressionMetric;
import rapaio.ml.eval.split.KFold;
import rapaio.ml.eval.split.Split;
import rapaio.ml.eval.split.SplitStrategy;
import rapaio.ml.model.RegressionModel;
import rapaio.ml.model.RegressionResult;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/6/19.
 */
public class RegressionEval extends ParamSet<RegressionEval> {

    public static RegressionEval newEval() {
        return new RegressionEval();
    }

    @Serial
    private static final long serialVersionUID = 8317463072268958015L;

    /**
     * Regression model.
     */
    public final ValueParam<RegressionModel<?, ?, ?>, RegressionEval> model = new ValueParam<>(this, null, "model");

    /**
     * Data set.
     */
    public final ValueParam<Frame, RegressionEval> df = new ValueParam<>(this, null, "df");

    /**
     * Instance weights
     */
    public final ValueParam<Var, RegressionEval> weights = new ValueParam<>(this, null, "weights");

    /**
     * Target variable name
     */
    public final ValueParam<String, RegressionEval> targetName = new ValueParam<>(this, null, "target");

    /**
     * Strategy used to build training and testing data sets.
     */
    public final ValueParam<SplitStrategy, RegressionEval> splitStrategy = new ValueParam<>(this, new KFold(10), "splitStrategy");

    /**
     * Number of threads used at evaluation
     */
    public final ValueParam<Integer, RegressionEval> threads = new ValueParam<>(this,
            Runtime.getRuntime().availableProcessors() - 1, "threads");

    /**
     * Metrics used at evaluation.
     */
    public final ListParam<RegressionMetric, RegressionEval> metrics = new ListParam<>(this, List.of(), "metrics", (in, out) -> true);

    /**
     * If evaluation is used in debug mode or not.
     */
    public final ValueParam<Boolean, RegressionEval> debug = new ValueParam<>(this, false, "debug");

    private RegressionEval() {
    }

    public RegressionEvaluationResult run() {
        List<Task> tasks = new LinkedList<>();

        // create features for parallel execution

        List<Split> splits = splitStrategy.get().generateSplits(df.get(), weights.get());

        for (Split split : splits) {
            tasks.add(new Task(model.get(), targetName.get(), split));
        }

        RegressionEvaluationResult result = new RegressionEvaluationResult(this);


        ExecutorService pool = Executors.newFixedThreadPool(threads.get());
        try {
            List<Future<Run>> futures = pool.invokeAll(tasks);

            // collect results

            while (!futures.isEmpty()) {
                Iterator<Future<RegressionEval.Run>> iterator = futures.iterator();
                while (iterator.hasNext()) {
                    Future<RegressionEval.Run> future = iterator.next();
                    if (future.isDone()) {
                        try {
                            var run = future.get();
                            result.appendRun(run.split(), run.trainResult(), run.testResult());
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

    private record Run(Split split, RegressionResult trainResult, RegressionResult testResult) {
    }

    private record Task(RegressionModel<?,?,?> model, String targetName, Split split) implements Callable<RegressionEval.Run> {

        @Override
        public Run call() {
            var m = model.newInstance();
            m.fit(split.trainDf(), targetName);
            var trainResult = m.predict(split.trainDf());
            var testResult = m.predict(split.testDf());
            return new RegressionEval.Run(split, trainResult, testResult);
        }
    }
}
