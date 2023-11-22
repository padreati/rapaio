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

import java.io.Serial;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.core.param.ListParam;
import rapaio.core.param.ParamSet;
import rapaio.core.param.ValueParam;
import rapaio.ml.eval.metric.Accuracy;
import rapaio.ml.eval.metric.ClassifierMetric;
import rapaio.ml.eval.split.KFold;
import rapaio.ml.eval.split.Split;
import rapaio.ml.eval.split.SplitStrategy;
import rapaio.ml.eval.split.StratifiedKFold;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;
import rapaio.util.parralel.ParallelStreamCollector;

/**
 * Classifier evaluation tool.
 *
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/26/20.
 */
public class ClassifierEvaluation extends ParamSet<ClassifierEvaluation> {

    @Serial
    private static final long serialVersionUID = 8533424311527093792L;

    public static ClassifierEvaluation eval(Frame df, String targetName, ClassifierModel<?, ?, ?> model, ClassifierMetric... metrics) {
        return new ClassifierEvaluation()
                .data.set(df)
                .targetName.set(targetName)
                .model.set(model)
                .metrics.set(metrics);
    }

    public static ClassifierEvaluation cv(Frame df, String targetName, ClassifierModel<?, ?, ?> model, int folds,
            ClassifierMetric... metrics) {
        return new ClassifierEvaluation()
                .data.set(df)
                .targetName.set(targetName)
                .model.set(model)
                .splitStrategy.set(new StratifiedKFold(folds, targetName))
                .metrics.set(metrics);
    }

    /**
     * Classification model.
     */
    public final ValueParam<ClassifierModel<?, ?, ?>, ClassifierEvaluation> model = new ValueParam<>(this, null, "model");

    /**
     * Data frame
     */
    public final ValueParam<Frame, ClassifierEvaluation> data = new ValueParam<>(this, null, "df");

    /**
     * Instance weights
     */
    public final ValueParam<Var, ClassifierEvaluation> weights = new ValueParam<>(this, null, "weights");

    /**
     * Target variable name
     */
    public final ValueParam<String, ClassifierEvaluation> targetName = new ValueParam<>(this, null, "target");

    /**
     * Split strategy used to obtain train and validation data sets.
     */
    public final ValueParam<SplitStrategy, ClassifierEvaluation> splitStrategy = new ValueParam<>(this, new KFold(10), "splitStrategy");

    /**
     * Number of threads used for evaluation.
     */
    public final ValueParam<Integer, ClassifierEvaluation> threads = new ValueParam<>(this, 1, "threads");

    public final ValueParam<Long, ClassifierEvaluation> seed = new ValueParam<>(this, 0L, "random");

    /**
     * Metrics used at evaluation time.
     */
    public final ListParam<ClassifierMetric, ClassifierEvaluation> metrics =
            new ListParam<>(this, List.of(Accuracy.newMetric()), "metrics", (in, out) -> true);

    public ClassifierEvaluationResult run() {

        int th = Math.max(1, threads.get() < 0 ? Runtime.getRuntime().availableProcessors() - 1 : threads.get());
        ExecutorService executorService = Executors.newWorkStealingPool(th);

        List<Split> splits = splitStrategy.get().generateSplits(data.get(), weights.get(), getRandom());
        ClassifierEvaluationResult result = new ClassifierEvaluationResult(this);

        splits.stream().collect(ParallelStreamCollector.streamingOrdered(split -> {
                    var m = model.get().newInstance();
                    m.fit(split.trainDf(), targetName.get());
                    var trainResult = m.predict(split.trainDf(), true, true);
                    var testResult = m.predict(split.testDf(), true, true);
                    return new Run(split, trainResult, testResult);
                }, executorService, th))
                .forEach(run -> result.appendRun(run.split, run.trainResult, run.testResult));
        // shut down executor
        executorService.shutdownNow();
        return result;
    }

    private record Run(Split split, ClassifierResult trainResult, ClassifierResult testResult) {
    }

    protected Random getRandom() {
        return (seed.get() == 0) ? new Random() : new Random(seed.get());
    }
}
