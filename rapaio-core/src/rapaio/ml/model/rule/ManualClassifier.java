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

package rapaio.ml.model.rule;

import java.util.ArrayList;
import java.util.List;

import rapaio.data.Frame;
import rapaio.data.Unique;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.data.stream.FSpot;
import rapaio.ml.common.Capabilities;
import rapaio.core.param.ListParam;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;
import rapaio.ml.model.RunInfo;
import rapaio.util.function.SFunction;

public class ManualClassifier extends ClassifierModel<ManualClassifier, ClassifierResult, RunInfo<ManualClassifier>> {

    public static ManualClassifier model() {
        return new ManualClassifier();
    }

    public final ListParam<SFunction<FSpot, String>, ManualClassifier> rules = new ListParam<>(this,
            List.of(), "rules", (list, list2) -> true);

    private String majorityLabel;

    private ManualClassifier() {
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities()
                .minInputCount(0).maxInputCount(Integer.MAX_VALUE)
                .allowMissingInputValues(true)
                .inputTypes(VarType.DOUBLE, VarType.INT, VarType.BINARY, VarType.LONG, VarType.NOMINAL)
                .minTargetCount(1).maxTargetCount(1)
                .allowMissingTargetValues(false)
                .targetTypes(VarType.NOMINAL, VarType.BINARY);
    }

    @Override
    public ClassifierModel<ManualClassifier, ClassifierResult, RunInfo<ManualClassifier>> newInstance() {
        return new ManualClassifier().rules.set(new ArrayList<>(rules.get()));
    }

    @Override
    public String name() {
        return "ManualClassifier";
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        var unique = Unique.ofLabel(df.rvar(firstTargetName()));
        var counts = unique.countSortedIds();
        majorityLabel = unique.uniqueValue(counts.getInt(counts.size() - 1));
        return true;
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        var result = ClassifierResult.build(this, df, withClasses, withDistributions);

        for (FSpot s : df.spotList()) {
            String label = majorityLabel;
            for (SFunction<FSpot, String> rule : rules.get()) {
                String l = rule.apply(s);
                if (l != null) {
                    label = l;
                    break;
                }
            }

            if (withClasses) {
                result.firstClasses().setLabel(s.row(), label);
            }
            if (withDistributions) {
                for (var v : result.firstDensity().varList()) {
                    v.setDouble(s.row(), v.name().equals(label) ? 1 : 0);
                }
            }
        }

        return result;
    }
}
