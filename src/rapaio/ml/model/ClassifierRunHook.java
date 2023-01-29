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

package rapaio.ml.model;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.util.param.ListParam;
import rapaio.util.param.ParamSet;
import rapaio.util.param.ValueParam;
import rapaio.ml.eval.metric.ClassifierMetric;
import rapaio.printer.Format;
import rapaio.util.function.SConsumer;

/**
 * Utility class for following progress while fitting a classifier during runs.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/9/20.
 */
public class ClassifierRunHook<M extends ClassifierModel<M, ?, ?>> extends ParamSet<ClassifierRunHook<M>>
        implements SConsumer<RunInfo<M>> {

    @Serial
    private static final long serialVersionUID = 7165160125378670196L;

    private static final List<ClassifierMetric> DEFAULT_METRICS = List.of();

    /**
     * Skip step.
     */
    public final ValueParam<Integer, ClassifierRunHook<M>> skipStep = new ValueParam<>(this, 5, "skipStep", Objects::nonNull);

    /**
     * Train data set.
     */
    public final ValueParam<Frame, ClassifierRunHook<M>> train = new ValueParam<>(this, null, "train", x -> true);

    /**
     * Test data set.
     */
    public final ValueParam<Frame, ClassifierRunHook<M>> test = new ValueParam<>(this, null, "test", x -> true);

    /**
     * Metrics used to measure performance.
     */
    public final ListParam<ClassifierMetric, ClassifierRunHook<M>> metrics =
            new ListParam<>(this, DEFAULT_METRICS, "metrics", (m1, m2) -> true);

    private final VarInt runs = VarInt.empty().name("runs");
    private final LinkedHashMap<String, VarDouble> trainScores = new LinkedHashMap<>();
    private final LinkedHashMap<String, VarDouble> testScores = new LinkedHashMap<>();

    public VarInt getRuns() {
        return runs;
    }

    public LinkedHashMap<String, VarDouble> getTrainScores() {
        return trainScores;
    }

    public LinkedHashMap<String, VarDouble> getTestScores() {
        return testScores;
    }

    @Override
    public void accept(RunInfo<M> info) {
        if (info.run() == 0 || (info.run() % skipStep.get() == 0)) {

            runs.addInt(info.run());

            StringBuilder sb = new StringBuilder();
            sb.append("run ").append(info.run()).append("\n");
            for (var metric : metrics.get()) {
                sb.append(metric.getName()).append(" ");
                if (train.get() != null) {
                    var result = info.model().predict(train.get(), true, true);
                    double score = metric.compute(train.get().rvar(info.model().firstTargetName()), result).getScore().value();
                    trainScores.computeIfAbsent(metric.getName(), m -> VarDouble.empty()).addDouble(score);
                    sb.append("train:").append(Format.floatFlex(score));
                }

                if (test.get() != null) {
                    var result = info.model().predict(test.get(), true, true);
                    double score = metric.compute(test.get().rvar(info.model().firstTargetName()), result).getScore().value();
                    testScores.computeIfAbsent(metric.getName(), m -> VarDouble.empty()).addDouble(score);
                    sb.append(", test:").append(Format.floatFlex(score));
                }
            }
            System.out.println(sb);
        }
    }
}