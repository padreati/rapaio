/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 * Copyright 2013 - 2021 Aurelian Tutuianu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package rapaio.ml.model.ensemble;

import java.io.Serial;
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
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarDouble;
import rapaio.data.VarType;
import rapaio.data.sample.RowSampler;
import rapaio.ml.common.Capabilities;
import rapaio.ml.common.Param;
import rapaio.ml.common.ValueParam;
import rapaio.ml.common.VarSelector;
import rapaio.ml.model.RegressionModel;
import rapaio.ml.model.RegressionResult;
import rapaio.ml.model.RunInfo;
import rapaio.ml.model.tree.RTree;
import rapaio.ml.model.tree.rtree.Splitter;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> at 1/15/15.
 */
public class RForest extends RegressionModel<RForest, RegressionResult, RunInfo<RForest>> {

    public static RForest newBagging() {
        return new RForest()
                .model.set(RTree
                        .newCART()
                        .varSelector.set(VarSelector.all())
                        .splitter.set(Splitter.Random)
                        .minCount.set(1)
                );
    }

    public static RForest newRF() {
        return new RForest()
                .model.set(RTree.newCART()
                        .varSelector.set(VarSelector.auto())
                        .splitter.set(Splitter.Random)
                        .minCount.set(1)
                );
    }

    public static RForest newRF(RegressionModel<?, ?, ?> model) {
        return new RForest().model.set(model);
    }

    @Serial
    private static final long serialVersionUID = -3926256335736143438L;

    /**
     * Weak learner model
     */
    public final Param<RegressionModel<?, ?, ?>, RForest> model = new ValueParam<>(this,
            RTree.newCART()
                    .varSelector.set(VarSelector.auto())
                    .splitter.set(Splitter.Random)
                    .minCount.set(1),
            "model", Objects::nonNull);

    private final List<RegressionModel<?, ?, ?>> regressions = new ArrayList<>();

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
        return new Capabilities()
                .inputs(1, 1_000_000, true, VarType.BINARY, VarType.INT, VarType.DOUBLE, VarType.NOMINAL)
                .targets(1, 1, false, VarType.DOUBLE);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        regressions.clear();
        Queue<Future<RegressionModel<?, ?, ?>>> futures = new LinkedList<>();
        for (int i = 0; i < runs.get(); i++) {
            RowSampler.Sample sample = rowSampler.get().nextSample(df, weights);
            RegressionModel<?, ?, ?> m = model.get().newInstance();
            Future<RegressionModel<?, ?, ?>> future = ForkJoinPool.commonPool().submit(new FitTask(sample, m, targetNames));
            futures.add(future);
        }

        int run = 1;
        while (!futures.isEmpty()) {
            Iterator<Future<RegressionModel<?, ?, ?>>> it = futures.iterator();
            while (it.hasNext()) {
                Future<RegressionModel<?, ?, ?>> future = it.next();
                if (future.isDone()) {
                    try {
                        RegressionModel<?, ?, ?> m = future.get();
                        regressions.add(m);
                        runningHook.get().accept(RunInfo.forRegression(this, run++));
                        it.remove();
                    } catch (InterruptedException | ExecutionException ignored) {
                    }
                }
            }
        }
        return true;
    }

    public List<RegressionModel<?, ?, ?>> getFittedModels() {
        return regressions;
    }

    @Override
    protected RegressionResult corePredict(Frame df, boolean withResiduals, final double[] quantiles) {
        RegressionResult fit = RegressionResult.build(this, df, withResiduals, quantiles);
        List<VarDouble> results = regressions
                .parallelStream()
                .map(r -> r.predict(df, false).firstPrediction()).toList();
        var pred = fit.firstPrediction().dv();
        pred.fill(0);
        for (VarDouble result : results) {
            pred.add(result.dv());
        }
        pred.div(regressions.size());
        if (withResiduals) {
            fit.buildComplete();
        }
        return fit;
    }

    private record FitTask(RowSampler.Sample sample, RegressionModel<?, ?, ?> model, String[] targetNames)
            implements Callable<RegressionModel<?, ?, ?>>, Serializable {

        @Serial
        private static final long serialVersionUID = -5432992679557031337L;

        @Override
        public RegressionModel<?, ?, ?> call() {
            return model.fit(sample.df(), sample.weights(), targetNames);
        }
    }

    @Override
    public String toString() {
        return fullName() + ", is fitted: " + isFitted();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        return "Model:\n"
                + fullName() + "\n"
                + "fitted: " + isFitted() + "\n";
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
