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

package rapaio.ml.classifier.bayes.nb;

import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.printer.Format;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/4/20.
 */
public class PriorMLE implements Prior {

    @Serial
    private static final long serialVersionUID = 2590209274166763951L;

    private Map<String, Double> priors = new HashMap<>();

    @Override
    public Prior newInstance() {
        return new PriorMLE();
    }

    @Override
    public String name() {
        return "MLE";
    }

    @Override
    public String fittedName() {
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append("{");
        sb.append(priors.entrySet().stream().map(e -> e.getKey() + ":" + Format.floatFlex(e.getValue())).collect(Collectors.joining(",")));
        sb.append("}");
        return sb.toString();
    }

    @Override
    public void fitPriors(Frame df, Var weights, String targetVar) {
        priors = new HashMap<>();
        var target = df.rvar(targetVar);
        var dv = DensityVector.fromLevelWeights(false, target, weights);
        dv.normalize();
        for (String targetLevel : dv.index().getValues()) {
            priors.put(targetLevel, dv.get(targetLevel));
        }
    }

    @Override
    public double computePrior(String category) {
        return priors.getOrDefault(category, Double.NaN);
    }
}
