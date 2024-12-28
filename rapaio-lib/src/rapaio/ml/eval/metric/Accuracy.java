/*
 * Apache License
 * Version 2.0, January 2004
 * http://www.apache.org/licenses/
 *
 *    Copyright 2013 - 2025 Aurelian Tutuianu
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

package rapaio.ml.eval.metric;

import java.io.Serial;

import rapaio.data.Var;
import rapaio.ml.model.ClassifierResult;

/**
 * @author <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 3/3/20.
 */
public class Accuracy extends AbstractClassifierMetric implements ClassifierMetric {
    public static final String ID = "Accuracy";

    public static Accuracy newMetric(boolean normalize) {
        return new Accuracy(normalize);
    }

    public static Accuracy newMetric() {
        return newMetric(true);
    }

    @Serial
    private static final long serialVersionUID = -3526955062164344415L;

    private final boolean normalize;

    private Accuracy(boolean normalize) {
        super(ID);
        this.normalize = normalize;
    }

    @Override
    public Accuracy compute(Var actual, ClassifierResult prediction) {
        double match = 0;
        for (int i = 0; i < prediction.firstClasses().size(); i++) {
            if (prediction.firstClasses().getLabel(i).equals(actual.getLabel(i))) {
                match++;
            }
        }
        score = new ClassifierScore(normalize ? match / prediction.firstClasses().size() : match);
        return this;
    }
}

