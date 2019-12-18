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

package rapaio.ml.classifier.bayes;

import rapaio.core.tools.DensityVector;
import rapaio.data.Frame;
import rapaio.data.Var;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by <a href="mailto:padreati@yahoo.com">Aurelian Tutuianu</a> on 8/5/19.
 */
public interface PriorSupplier extends Serializable {

    String name();

    Map<String, Double> learnPriors(Frame df, Var weights, String targetVar);

    PriorSupplier PRIOR_MLE = new PriorSupplier() {
        private static final long serialVersionUID = 2590209274166763951L;

        @Override
        public String name() {
            return "PRIOR_MLE";
        }

        @Override
        public Map<String, Double> learnPriors(Frame df, Var weights, String targetVar) {
            Map<String, Double> priors = new HashMap<>();
            DensityVector dv = DensityVector.fromWeights(false, df.rvar(targetVar), weights);
            dv.normalize();
            for (int i = 1; i < df.levels(targetVar).size(); i++) {
                priors.put(df.levels(targetVar).get(i), dv.get(i));
            }
            return priors;
        }
    };

    PriorSupplier PRIOR_UNIFORM = new PriorSupplier() {
        private static final long serialVersionUID = 4346918440758937122L;

        @Override
        public String name() {
            return "PRIOR_UNIFORM";
        }

        @Override
        public Map<String, Double> learnPriors(Frame df, Var weights, String targetVar) {
            Map<String, Double> priors = new HashMap<>();
            double degrees = df.levels(targetVar).size() - 1;
            double p = 1.0 / degrees;
            for (int i = 1; i < df.levels(targetVar).size(); i++) {
                priors.put(df.levels(targetVar).get(i), p);
            }
            return priors;
        }
    };
}
