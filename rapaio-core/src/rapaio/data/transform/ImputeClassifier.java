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

package rapaio.data.transform;

import java.io.Serial;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.ml.model.ClassifierModel;
import rapaio.ml.model.ClassifierResult;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/12/19.
 */
public class ImputeClassifier extends AbstractTransform {

    public static ImputeClassifier of(ClassifierModel<?, ?, ?> model, VarRange inputVars, String targetName) {
        return new ImputeClassifier(model, inputVars, targetName);
    }

    @Serial
    private static final long serialVersionUID = 7428989420235407246L;

    private final ClassifierModel<?, ?, ?> model;
    private final String targetName;

    private ImputeClassifier(ClassifierModel<?, ?, ?> model, VarRange inputVars, String targetName) {
        super(inputVars);
        this.model = model;
        this.targetName = targetName;
    }

    @Override
    protected void coreFit(Frame df) {
        var selection = df.mapVars(varNames).stream().filter(s -> !s.isMissing(targetName)).toMappedFrame().copy();
        model.fit(selection, targetName);
    }

    @Override
    public Frame coreApply(Frame df) {
        var toFill = df.stream().filter(s -> s.isMissing(targetName)).toMappedFrame();
        ClassifierResult result = model.predict(toFill, true, false);
        Var prediction = result.firstClasses();
        for (int i = 0; i < prediction.size(); i++) {
            toFill.setLabel(i, targetName, prediction.getLabel(i));
        }
        return df;
    }

    @Override
    public ImputeClassifier newInstance() {
        return new ImputeClassifier(model.newInstance(), varRange, targetName);
    }
}
