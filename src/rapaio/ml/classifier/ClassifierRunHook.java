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

package rapaio.ml.classifier;

import lombok.Getter;
import rapaio.data.Frame;
import rapaio.data.VarDouble;
import rapaio.data.VarInt;
import rapaio.ml.common.ListParam;
import rapaio.ml.common.ParamSet;
import rapaio.ml.common.ValueParam;
import rapaio.ml.eval.metric.ClassifierMetric;
import rapaio.printer.Format;
import rapaio.util.function.SBiConsumer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * Utility class for following progress while fitting a classifier during runs.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/9/20.
 */
public class ClassifierRunHook extends ParamSet<ClassifierRunHook> implements SBiConsumer<ClassifierModel, Integer> {

    private static final long serialVersionUID = 7165160125378670196L;

    private final static List<ClassifierMetric> DEFAULT_METRICS = List.of();

    public final ValueParam<Integer, ClassifierRunHook> skipStep = new ValueParam<>(this, 5,
            "skipStep",
            "Skip step",
            Objects::nonNull);

    public final ValueParam<Frame, ClassifierRunHook> train = new ValueParam<>(this, null,
            "train",
            "Train data set",
            x -> true);

    public final ValueParam<Frame, ClassifierRunHook> test = new ValueParam<>(this, null,
            "test",
            "Test data set",
            x -> true);

    public final ListParam<ClassifierMetric, ClassifierRunHook> metrics = new ListParam<>(this, List.of(),
            "metrics",
            "Metrics",
            (m1, m2) -> true);

    @Getter
    private final VarInt runs = VarInt.empty().name("runs");
    @Getter
    private final LinkedHashMap<String, VarDouble> trainScores = new LinkedHashMap<>();
    @Getter
    private final LinkedHashMap<String, VarDouble> testScores = new LinkedHashMap<>();

    @Override
    public void accept(ClassifierModel model, Integer run) {
        if (run == 0 || (run % skipStep.get() == 0)) {

            runs.addInt(run);

            StringBuilder sb = new StringBuilder();
            sb.append("run ").append(run).append("\n");
            for (var metric : metrics.get()) {
                sb.append(metric.getName()).append(" ");
                if (train.get() != null) {
                    var result = model.predict(train.get(), true, true);
                    double score = metric.compute(train.get().rvar(model.firstTargetName()), result).getScore().getValue();
                    trainScores.computeIfAbsent(metric.getName(), m -> VarDouble.empty()).addDouble(score);
                    sb.append("train:").append(Format.floatFlex(score));
                }

                if (test.get() != null) {
                    var result = model.predict(test.get(), true, true);
                    double score = metric.compute(test.get().rvar(model.firstTargetName()), result).getScore().getValue();
                    testScores.computeIfAbsent(metric.getName(), m -> VarDouble.empty()).addDouble(score);
                    sb.append(", test:").append(Format.floatFlex(score));
                }
            }
            System.out.println(sb.toString());
        }
    }
}