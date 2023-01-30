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

package rapaio.ml.eval.metric;

import java.io.Serial;

import rapaio.data.Var;
import rapaio.ml.model.ClassifierResult;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/20.
 */
public class LogLoss extends AbstractClassifierMetric {

    public static LogLoss newMetric() {
        return new LogLoss(1e-15, true);
    }

    public static LogLoss newMetric(boolean normalize) {
        return new LogLoss(1e-15, normalize);
    }

    public static LogLoss newMetric(double eps, boolean normalize) {
        return new LogLoss(eps, normalize);
    }

    @Serial
    private static final long serialVersionUID = 8850076650664844719L;
    private static final String NAME = "LogLoss";

    private final boolean normalize;
    private final double eps;

    private LogLoss(double eps, boolean normalize) {
        super(NAME);
        this.eps = eps;
        this.normalize = normalize;
    }

    @Override
    public LogLoss compute(Var actual, ClassifierResult result) {
        double logloss = 0;
        for (int i = 0; i < actual.size(); i++) {
            logloss -= Math.log(Math.max(eps, Math.min(1 - eps, result.firstDensity().getDouble(i, actual.getLabel(i)))));
        }
        score = new ClassifierScore(normalize ? logloss / actual.size() : logloss);
        return this;
    }
}
