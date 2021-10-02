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

package rapaio.data.filter;

import java.io.Serial;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarRange;
import rapaio.ml.classifier.ClassifierModel;
import rapaio.ml.classifier.ClassifierResult;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 11/12/19.
 */
public class FImputeClassifier extends AbstractFFilter {

    public static FImputeClassifier of(ClassifierModel model, VarRange inputVars, String targetName) {
        return new FImputeClassifier(model, inputVars, targetName);
    }

    @Serial
    private static final long serialVersionUID = 7428989420235407246L;

    private final ClassifierModel model;
    private final String targetName;

    private FImputeClassifier(ClassifierModel model, VarRange inputVars, String targetName) {
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
    public Frame apply(Frame df) {
        var toFill = df.stream().filter(s -> s.isMissing(targetName)).toMappedFrame();
        ClassifierResult result = model.predict(toFill, true, false);
        Var prediction = result.firstClasses();
        for (int i = 0; i < prediction.size(); i++) {
            toFill.setLabel(i, targetName, prediction.getLabel(i));
        }
        return df;
    }

    @Override
    public FImputeClassifier newInstance() {
        return new FImputeClassifier(model.newInstance(), varRange, targetName);
    }
}
