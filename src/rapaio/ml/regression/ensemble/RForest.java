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

package rapaio.ml.regression.ensemble;

import lombok.AllArgsConstructor;
import rapaio.data.Frame;
import rapaio.data.VType;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.sample.Sample;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.Param;
import rapaio.ml.common.ValueParam;
import rapaio.ml.common.VarSelector;
import rapaio.ml.regression.AbstractRegressionModel;
import rapaio.ml.regression.RegressionModel;
import rapaio.ml.regression.RegressionResult;
import rapaio.ml.regression.tree.RTree;
import rapaio.ml.regression.tree.rtree.RTreeSplitter;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/15/15.
 */
public class RForest extends AbstractRegressionModel<RForest, RegressionResult> {

    public static RForest newBagging() {
        return new RForest()
                .model.set(RTree
                        .newCART()
                        .varSelector.set(VarSelector.all())
                        .splitter.set(RTreeSplitter.RANDOM)
                        .minCount.set(1)
                );
    }

    public static RForest newRF() {
        return new RForest()
                .model.set(RTree.newCART()
                        .varSelector.set(VarSelector.auto())
                        .splitter.set(RTreeSplitter.RANDOM)
                        .minCount.set(1)
                );
    }

    public static RForest newRF(RegressionModel model) {
        return new RForest().model.set(model);
    }

    private static final long serialVersionUID = -3926256335736143438L;

    public final Param<RegressionModel, RForest> model = new ValueParam<>(this,
            RTree.newCART()
                    .varSelector.set(VarSelector.auto())
                    .splitter.set(RTreeSplitter.RANDOM)
                    .minCount.set(1),
            "model",
            "Weak learner model",
            Objects::nonNull);

    private final List<RegressionModel> regressions = new ArrayList<>();

    @Override
    public RForest newInstance() {
        return new RForest().copyParameterValues(this);
    }

    @Override
    public String name() {
        return "RForest";
    }

    @Override
    public Capabilities capabilities() {
        return Capabilities.builder()
                .minInputCount(1).maxInputCount(1_000_000)
                .minTargetCount(1).maxTargetCount(1)
                .inputTypes(Arrays.asList(VType.BINARY, VType.INT, VType.DOUBLE, VType.NOMINAL))
                .targetType(VType.DOUBLE)
                .allowMissingInputValues(true)
                .allowMissingTargetValues(false)
                .build();
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        regressions.clear();
        int threads = poolSize.get() < 0 ? Runtime.getRuntime().availableProcessors() - 1 : poolSize.get();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        Queue<Future<rapaio.ml.regression.RegressionModel>> futures = new LinkedList<>();
        for (int i = 0; i < runs.get(); i++) {
            Sample sample = rowSampler.get().nextSample(df, weights);
            RegressionModel m = model.get().newInstance();
            Future<rapaio.ml.regression.RegressionModel> future = pool.submit(new FitTask(sample, m, targetNames));
            futures.add(future);
        }

        int run = 1;
        while (!futures.isEmpty()) {
            Iterator<Future<rapaio.ml.regression.RegressionModel>> it = futures.iterator();
            while (it.hasNext()) {
                Future<rapaio.ml.regression.RegressionModel> future = it.next();
                if (future.isDone()) {
                    try {
                        RegressionModel m = future.get();
                        regressions.add(m);
                        runningHook.get().accept(this, run++);
                        it.remove();
                    } catch (InterruptedException | ExecutionException ignored) {
                    }
                }
            }
        }
        return true;
    }

    public List<rapaio.ml.regression.RegressionModel> getFittedModels() {
        return regressions;
    }

    @Override
    protected RegressionResult corePredict(Frame df, boolean withResiduals) {
        RegressionResult fit = RegressionResult.build(this, df, withResiduals);
        List<VarDouble> results = regressions
                .parallelStream()
                .map(r -> r.predict(df, false).firstPrediction())
                .collect(Collectors.toList());
        fit.firstPrediction().op().fill(0);
        for (VarDouble result : results) {
            fit.firstPrediction().op().plus(result);
        }
        fit.firstPrediction().op().divide(regressions.size());
        if (withResiduals) {
            fit.buildComplete();
        }
        return fit;
    }

    @AllArgsConstructor
    private static class FitTask implements Callable<rapaio.ml.regression.RegressionModel>, Serializable {

        private static final long serialVersionUID = -5432992679557031337L;

        private final Sample sample;
        private final rapaio.ml.regression.RegressionModel model;
        private final String[] targetNames;

        @Override
        public rapaio.ml.regression.RegressionModel call() {
            return model.fit(sample.df, sample.weights, targetNames);
        }
    }

    @Override
    public String toString() {
        return fullName() + ", is fitted: " + isFitted();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        StringBuilder sb = new StringBuilder();
        sb.append("Model:\n");
        sb.append(fullName()).append("\n");
        sb.append("fitted: ").append(isFitted()).append("\n");
        return sb.toString();
    }

    @Override
    public String toContent(POption<?>... options) {
        return toSummary(options);
    }

    @Override
    public String toFullContent(POption<?>... options) {
        return toSummary(options);
    }
}
