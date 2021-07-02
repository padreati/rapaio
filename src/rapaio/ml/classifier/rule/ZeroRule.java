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

package rapaio.ml.classifier.rule;

import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.data.VarType;
import rapaio.ml.classifier.AbstractClassifierModel;
import rapaio.ml.classifier.ClassifierResult;
import rapaio.ml.common.Capabilities;
import rapaio.printer.Printer;
import rapaio.printer.opt.POption;

import java.io.Serial;
import java.util.List;

/**
 * ZeroR classification algorithm.
 * This basic classification algorithm does not use inputs, only target variable.
 * It predicts with majority class.
 * <p>
 * This classification algorithm it is not designed for production prediction,
 * but it it used as a baseline prediction.
 * <p>
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 1/24/20.
 */
public class ZeroRule extends AbstractClassifierModel<ZeroRule, ClassifierResult> {

    public static ZeroRule newModel() {
        return new ZeroRule();
    }

    @Serial
    private static final long serialVersionUID = 3972781939411346774L;
    private String prediction;

    private ZeroRule() {
    }

    @Override
    public ZeroRule newInstance() {
        return new ZeroRule();
    }

    @Override
    public String name() {
        return "ZeroRule";
    }

    @Override
    public Capabilities capabilities() {
        return new Capabilities(
                0, Integer.MAX_VALUE,
                List.of(VarType.DOUBLE, VarType.INT, VarType.NOMINAL, VarType.BINARY, VarType.LONG, VarType.INSTANT, VarType.STRING), true,
                1, 1, List.of(VarType.NOMINAL, VarType.BINARY), true);
    }

    @Override
    protected boolean coreFit(Frame df, Var weights) {
        Var target = df.rvar(firstTargetName());
        DensityVector<String> dv = DensityVector.fromLevelWeights(false, target, weights);
        prediction = dv.index().getValueString(dv.findBestIndex());
        return true;
    }

    @Override
    protected ClassifierResult corePredict(Frame df, boolean withClasses, boolean withDistributions) {
        if (!hasLearned()) {
            throw new IllegalStateException("Model was not trained/fitted on data.");
        }
        var result = ClassifierResult.build(this, df, withClasses, withDistributions);

        for (int i = 0; i < df.rowCount(); i++) {
            if (withClasses) {
                result.classes(firstTargetName()).setLabel(i, prediction);
            }
            if (withDistributions) {
                result.firstDensity().setDouble(i, prediction, 1);
            }
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(fullName()).append("; fitted=").append(hasLearned());
        if (hasLearned()) {
            sb.append(", fittedClass=").append(prediction);
        }
        return sb.toString();
    }

    @Override
    public String toSummary(Printer printer, POption<?>... options) {
        return toString();
    }

    @Override
    public String toContent(Printer printer, POption<?>... options) {
        return toString();
    }

    @Override
    public String toFullContent(Printer printer, POption<?>... options) {
        return toString();
    }
}
