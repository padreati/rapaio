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

package rapaio.ml.model.bayes.nb;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import rapaio.data.Frame;
import rapaio.data.Var;
import rapaio.printer.Format;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 2/4/20.
 */
public class PriorUniform implements Prior {

    @Serial
    private static final long serialVersionUID = 4346918440758937122L;

    private final Set<String> levels = new HashSet<>();
    private double uniformPrior = Double.NaN;

    @Override
    public Prior newInstance() {
        return new PriorUniform();
    }

    @Override
    public String name() {
        return "Uniform";
    }

    @Override
    public String fittedName() {
        return name() + "{value=" + Format.floatFlex(uniformPrior) + ","
                + "targetLevels=[" + String.join(",", levels) + "]"
                + "}";
    }

    @Override
    public void fitPriors(Frame df, Var weights, String targetVar) {
        List<String> targetLevels = new ArrayList<>(df.levels(targetVar));
        targetLevels = targetLevels.subList(1, targetLevels.size());
        if (!df.levels(targetVar).isEmpty()) {
            double degrees = df.levels(targetVar).size() - 1;
            uniformPrior = 1.0 / degrees;
            levels.addAll(targetLevels);
        }
    }

    @Override
    public double computePrior(String category) {
        if (levels.contains(category)) {
            return uniformPrior;
        }
        return Double.NaN;
    }
}
